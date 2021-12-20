package com.mobit.mobit.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mobit.mobit.R
import com.mobit.mobit.data.*
import com.mobit.mobit.databinding.ActivityMainBinding
import com.mobit.mobit.db.MyDBHelper
import com.mobit.mobit.network.NetworkManager
import com.mobit.mobit.network.UpbitAPICaller
import com.mobit.mobit.service.UpbitAPIService
import com.mobit.mobit.viewmodel.MyViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    // Fragment 변수 시작
    val fragmentCoinList: Fragment = FragmentCoinList()
    val fragmentChart: Fragment = FragmentChart()
    val fragmentTransaction: Fragment = FragmentTransaction()
    val fragmentInvestment: Fragment = FragmentInvestment()
    val fragmentSetting: Fragment = FragmentSetting()
    // Fragment 변수 끝

    // UI 변수 시작
    lateinit var binding: ActivityMainBinding
    val myProgressBar: MyProgressBar = MyProgressBar()
    var isDataLoaded: Boolean = false
    // UI 변수 끝

    val myViewModel: MyViewModel by viewModels<MyViewModel>()

    val codes: ArrayList<String> = ArrayList()
    val names: HashMap<String, String> = HashMap()
    val warnings: HashMap<String, String> = HashMap()
    var coinListThread: Thread? = null

    val dbHandler: DBHandler = DBHandler()
    lateinit var dbThread: DBThread

    // 뒤로가기 두번 누르면 앱 종료 관련 변수 시작
    val FINISH_INTERVAL_TIME: Long = 2000
    var backPressedTime: Long = 0
    // 뒤로가기 두번 누르면 앱 종료 관련 변수 끝

    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data != null) {
            val krw: Double = it.data!!.getDoubleExtra("krw", 10000000.0)
            myViewModel.asset.value!!.krw = krw
            val thread = object : Thread() {
                override fun run() {
                    myViewModel.myDBHelper!!.setFlag(true)
                    myViewModel.myDBHelper!!.setKRW(krw)
                }
            }
            thread.start()
        }
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val mode = intent.getStringExtra("mode")
                when (mode) {
                    "CoinInfo" -> {
                        val isSuccess = intent.getBooleanExtra("isSuccess", false)
                        if (isSuccess) {
                            val coinInfo =
                                intent.getSerializableExtra("coinInfo") as ArrayList<CoinInfo>
                            val favoriteCoinInfo =
                                intent.getSerializableExtra("favoriteCoinInfo") as ArrayList<CoinInfo>
                            if (coinInfo.isNotEmpty()) {
                                myViewModel.setCoinInfo(coinInfo)
                            } else {
                                Log.e("MainActivity", "coinInfo is empty")
                            }
                            if (favoriteCoinInfo.isNotEmpty()) {
                                myViewModel.setFavoriteCoinInfo(favoriteCoinInfo)
                            } else {
                                Log.i("MainActivity", "favoriteCoinInfo is empty")
                            }

                            if (!isDataLoaded) {
                                isDataLoaded = true
                                myProgressBar.progressOFF()
                            }
                        }
                    }
                    "orderBook" -> {
                        val isSuccess = intent.getBooleanExtra("isSuccess", false)
                        if (isSuccess) {
                            val orderBook =
                                intent.getSerializableExtra("orderBook") as ArrayList<OrderBook>
                            if (orderBook.isNotEmpty()) {
                                myViewModel.setOrderBook(orderBook)
                            } else {
                                Log.e("MainActivity", "orderBook is empty")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Mobit)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네트워크가 연결되어 있지 않은 경우
        if (!NetworkManager.checkNetworkState(this)) {
            Snackbar.make(
                binding.bottomNavBar,
                getString(R.string.network_connection_need),
                Snackbar.LENGTH_INDEFINITE
            ).setAction("확인", View.OnClickListener {
                moveTaskToBack(true)
                finishAndRemoveTask()
                android.os.Process.killProcess(android.os.Process.myPid())
            }).show()
        }
        // 네트워크가 연결되어 있는 경우
        else {
            myProgressBar.progressON(this, "Loading")

            // 업비트에서 원화로 거래되는 가상화폐 목록을 받아온다.
            // 받아온 데이터는 DBHandler.handleMessage()에서 Service와 동기화한다.
            coinListThread = object : Thread() {
                override fun run() {
                    val upbitAPICaller = UpbitAPICaller()
                    val coinList: ArrayList<Triple<String, String, String>> =
                        upbitAPICaller.getMarket()
                    if (coinList.isNotEmpty()) {
                        for (temp in coinList) {
                            codes.add(temp.first)
                            names.put(temp.first, temp.second)
                            warnings.put(temp.first, temp.third)
                        }
                    }
                }
            }
            coinListThread!!.start()

            initData()
            initService()
            initDB()
            init()
        }
    }

    override fun onBackPressed() {
        val tempTime: Long = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (0 <= intervalTime && intervalTime <= FINISH_INTERVAL_TIME) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            val msg: String = "뒤로 가기를 한 번 더 누르면 종료됩니다."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRestart() {
        super.onRestart()

        val serviceBRIntent = Intent(this, UpbitAPIService::class.java)
        serviceBRIntent.putExtra("mode", "START")
        sendBroadcast(serviceBRIntent)
    }

    override fun onStop() {
        super.onStop()

        val serviceBRIntent = Intent(this, UpbitAPIService::class.java)
        serviceBRIntent.putExtra("mode", "STOP")
        sendBroadcast(serviceBRIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun initDB() {
        myViewModel.myDBHelper = MyDBHelper(this)
        dbThread = DBThread()
        dbThread.start()
    }

    fun initData() {
        myViewModel.setSelectedCoin(CoinInfo.BTC_CODE)
        myViewModel.setCoinInfo(ArrayList<CoinInfo>())
        myViewModel.setFavoriteCoinInfo(ArrayList<CoinInfo>())
        myViewModel.setOrderBook(ArrayList<OrderBook>())
        myViewModel.setAsset(Asset(0.0, ArrayList<CoinAsset>()))
        myViewModel.setMainIndicatorType(MainIndicator.MOVING_AVERAGE)
        myViewModel.setMainIndicator(MainIndicator())
    }

    fun initService() {
        registerReceiver(receiver, IntentFilter("com.mobit.APIRECEIVE"))
        val intent = Intent(this, UpbitAPIService::class.java)
        startService(intent)
    }

    fun init() {
        myViewModel.selectedCoin.observe(this, androidx.lifecycle.Observer {
            val serviceBRIntent = Intent("com.mobit.APICALL")
            serviceBRIntent.putExtra("mode", "SELECTED_COIN_SETTING")
            serviceBRIntent.putExtra("selectedCoin", myViewModel.selectedCoin.value!!)
            sendBroadcast(serviceBRIntent)
        })
        myViewModel.favoriteCoinInfo.observe(this, androidx.lifecycle.Observer {
            val serviceBRIntent = Intent("com.mobit.APICALL")
            serviceBRIntent.putExtra("mode", "FAVORITE_COININFO_SETTING")
            serviceBRIntent.putExtra("favoriteCoinInfo", myViewModel.favoriteCoinInfo.value!!)
            sendBroadcast(serviceBRIntent)
        })

        replaceFragment(fragmentCoinList)
        binding.apply {
            bottomNavBar.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_coinlist -> {
                        replaceFragment(fragmentCoinList)
                        true
                    }
                    R.id.menu_chart -> {
                        replaceFragment(fragmentChart)
                        true
                    }
                    R.id.menu_transaction -> {
                        val serviceBTIntent = Intent("com.mobit.APICALL")
                        serviceBTIntent.putExtra("mode", "START_THREAD2")
                        sendBroadcast(serviceBTIntent)

                        replaceFragment(fragmentTransaction)
                        true
                    }
                    R.id.menu_investment -> {
                        replaceFragment(fragmentInvestment)
                        true
                    }
                    R.id.menu_setting -> {
                        replaceFragment(fragmentSetting)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

        (fragmentCoinList as FragmentCoinList).listener =
            object : FragmentCoinList.OnFragmentInteraction {
                override fun showTransaction() {
                    binding.bottomNavBar.selectedItemId = R.id.menu_transaction
                }
            }
        (fragmentTransaction as FragmentTransaction).listener =
            object : FragmentTransaction.OnFragmentInteraction {
                override fun orderBookThreadStop() {
                    val serviceBTIntent = Intent("com.mobit.APICALL")
                    serviceBTIntent.putExtra("mode", "STOP_THREAD2")
                    sendBroadcast(serviceBTIntent)
                }

                override fun orderBookThreadStart() {
                    val serviceBTIntent = Intent("com.mobit.APICALL")
                    serviceBTIntent.putExtra("mode", "START_THREAD2")
                    sendBroadcast(serviceBTIntent)
                }
            }
        (fragmentInvestment as FragmentInvestment).listener =
            object : FragmentInvestment.OnFragmentInteraction {
                override fun showTransaction() {
                    binding.bottomNavBar.selectedItemId = R.id.menu_transaction
                }
            }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: androidx.fragment.app.FragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

    inner class DBHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                val isFavorites = bundle.getBoolean("isFavorites")
                val isKrw = bundle.getBoolean("isKrw")
                val isCoinAssets = bundle.getBoolean("isCoinAssets")
                val isTransaction = bundle.getBoolean("isTransactions")

                if (isFavorites) {
                    val favorites = bundle.getSerializable("favorites") as ArrayList<String>
                    val list = ArrayList<CoinInfo>()
                    for (code in favorites) {
                        list.add(
                            CoinInfo(
                                code,
                                code,
                                Price(
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    "EVEN",
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0,
                                    "",
                                    0.0,
                                    "",
                                    0.0
                                ), "NONE"
                            )
                        )
                    }
                    myViewModel.setFavoriteCoinInfo(list)
                }
                if (isKrw) {
                    val krw = bundle.getDouble("krw")
                    if (isCoinAssets) {
                        val coinAssets =
                            bundle.getSerializable("coinAssets") as ArrayList<CoinAsset>
                        val asset = Asset(krw, coinAssets)
                        myViewModel.setAsset(asset)
                    } else {
                        val asset = Asset(krw, ArrayList<CoinAsset>())
                        myViewModel.setAsset(asset)
                    }
                    Log.i("isKrw True", krw.toString())
                } else {
                    val asset = Asset(10000000.0, ArrayList<CoinAsset>())
                    myViewModel.setAsset(asset)
                    Log.i("isKrw False", asset.krw.toString())
                }
                if (isTransaction) {
                    val transactions =
                        bundle.getSerializable("transactions") as ArrayList<Transaction>
                    myViewModel.setTransaction(transactions)
                } else {
                    val transactions = ArrayList<Transaction>()
                    myViewModel.setTransaction(transactions)
                }

                val mainIndicatorType = bundle.getInt("mainIndicatorType")
                if (mainIndicatorType != -1)
                    myViewModel.setMainIndicatorType(mainIndicatorType)

                val mainIndicator: MainIndicator =
                    bundle.getSerializable("mainIndicator") as MainIndicator
                myViewModel.setMainIndicator(mainIndicator)

                val flag = bundle.getBoolean("flag")
                window.statusBarColor = getColor(R.color.main_background)
                if (!flag) {
                    myProgressBar.progressOFF()
                    val intent = Intent(this@MainActivity, FirstSettingActivity::class.java)
                    getContent.launch(intent)
                }

                if (coinListThread!!.isAlive) {
                    try {
                        coinListThread!!.join()
                    } catch (e: InterruptedException) {
                        Log.e("MainActivity DBHandler", e.toString())
                    }
                }

                // 상장폐지된 화폐를 즐겨찾기 또는 자산으로 가지고 있는 경우, 문제가 발생할 수 있다.
                // 따라서 상장폐지된 화폐를 즐겨찾기와 자산에서 삭제한다.
                val delistingFavorites: ArrayList<CoinInfo> = ArrayList()
                for (favorite in myViewModel.favoriteCoinInfo.value!!) {
                    if (!codes.contains(favorite.code)) {
                        delistingFavorites.add(favorite)
                    }
                }
                val delistingCoinAsset: ArrayList<CoinAsset> = ArrayList()
                for (coinAsset in myViewModel.asset.value!!.coins) {
                    if (!codes.contains(coinAsset.code)) {
                        delistingCoinAsset.add(coinAsset)
                    }
                }
                Log.i(
                    "MainActivity",
                    "delistingFavorites size is ${delistingFavorites.size}"
                )
                Log.i(
                    "MainActivity",
                    "delistingCoinAsset size is ${delistingCoinAsset.size}"
                )
                // ViewModel에 저장되어 있는 변수 수정해주고
                myViewModel.favoriteCoinInfo.value!!.removeAll(delistingFavorites)
                myViewModel.asset.value!!.coins.removeAll(delistingCoinAsset)
                // DB에 저장되어 있는 값도 수정하면 된다.
                val removeDelistingThread: Thread = object : Thread() {
                    override fun run() {
                        for (favorite in delistingFavorites) {
                            myViewModel.myDBHelper!!.deleteFavorite(favorite.code)
                        }
                        for (coinAsset in delistingCoinAsset) {
                            myViewModel.myDBHelper!!.deleteCoinAsset(coinAsset)
                        }
                    }
                }
                removeDelistingThread.start()

                val serviceBRIntent = Intent("com.mobit.APICALL")
                serviceBRIntent.putExtra("mode", "INITIAL_SETTING")
                serviceBRIntent.putExtra("selectedCoin", myViewModel.selectedCoin.value!!)
                serviceBRIntent.putExtra("favoriteCoinInfo", myViewModel.favoriteCoinInfo.value!!)
                serviceBRIntent.putExtra("asset", myViewModel.asset.value!!)
                serviceBRIntent.putExtra("codes", codes)
                serviceBRIntent.putExtra("names", names)
                serviceBRIntent.putExtra("warnings", warnings)
                sendBroadcast(serviceBRIntent)

                val serviceBRIntent2 = Intent("com.mobit.APICALL")
                serviceBRIntent2.putExtra("mode", "START_THREAD1")
                sendBroadcast(serviceBRIntent2)
            }
        }
    }

    inner class DBThread : Thread() {
        override fun run() {
            val message: Message = dbHandler.obtainMessage()
            val bundle: Bundle = Bundle()

            // DB로부터  favorite 데이터 가져오기
            val favorites = myViewModel.myDBHelper!!.getFavorites()
            if (favorites.isNotEmpty()) {
                bundle.putBoolean("isFavorites", true)
                bundle.putSerializable("favorites", favorites)
            } else {
                bundle.putBoolean("isFavorites", false)
            }

            // DB로부터 KRW 데이터 가져오기
            val krw = myViewModel.myDBHelper!!.getKRW()
            if (krw != null) {
                bundle.putBoolean("isKrw", true)
                bundle.putDouble("krw", krw!!)
            } else {
                bundle.putBoolean("isKrw", false)
            }

            // DB로부터 CoinAsset 데이터 가져오기
            val coinAssets = myViewModel.myDBHelper!!.getCoinAssets()
            if (coinAssets.isNotEmpty()) {
                bundle.putBoolean("isCoinAssets", true)
                bundle.putSerializable("coinAssets", coinAssets)
            } else {
                bundle.putBoolean("isCoinAssets", false)
            }

            // DB로부터 Transaction 데이터 가져오기
            val transactions = myViewModel.myDBHelper!!.getTransactions()
            if (transactions.isNotEmpty()) {
                bundle.putBoolean("isTransactions", true)
                bundle.putSerializable("transactions", transactions)
            } else {
                bundle.putBoolean("isTransactions", false)
            }

            val flag = myViewModel.myDBHelper!!.getFlag()
            bundle.putBoolean("flag", flag)

            val mainIndicatorType = myViewModel.myDBHelper!!.getMainIndicatorType()
            bundle.putInt("mainIndicatorType", mainIndicatorType)

            val mainIndicator = myViewModel.myDBHelper!!.getMainIndicator()
            bundle.putSerializable("mainIndicator", mainIndicator)

            message.data = bundle
            dbHandler.sendMessage(message)
        }
    }

}