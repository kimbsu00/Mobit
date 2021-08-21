package com.mobit.mobit.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.network.UpbitAPICaller

class UpbitAPIService : Service() {

    var selectedCoin: String = CoinInfo.BTC_CODE
    var favoriteCoinInfo: ArrayList<CoinInfo> = ArrayList()
    var coinInfo: ArrayList<CoinInfo> = ArrayList()

    val upbitAPICaller: UpbitAPICaller = UpbitAPICaller()

    // 코인 정보 가져오는 쓰레드
    lateinit var upbitAPIThread: UpbitAPIThread

    // 코인 호가 정보 가져오는 쓰레드
    lateinit var upbitAPIThread2: UpbitAPIThread

    // 업비트에서 원화로 거래되는 가상화폐들의 코드, 한글이름, 투자유의여부
    val codes: ArrayList<String> = ArrayList()
    val names: HashMap<String, String> = HashMap()
    val warnings: HashMap<String, String> = HashMap()

    var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            APIControl(intent)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter("com.mobit.APICALL"))

        upbitAPIThread = UpbitAPIThread(100)
        upbitAPIThread2 = UpbitAPIThread(200)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun APIControl(intent: Intent?) {
        if (intent != null) {
            val mode = intent.getStringExtra("mode")
            when (mode) {
                "INITIAL_SETTING" -> {
                    selectedCoin = intent.getStringExtra("selectedCoin")!!
                    favoriteCoinInfo.clear()
                    favoriteCoinInfo.addAll(intent.getSerializableExtra("favoriteCoinInfo") as ArrayList<CoinInfo>)
                    codes.clear()
                    names.clear()
                    warnings.clear()
                    codes.addAll(intent.getSerializableExtra("codes") as ArrayList<String>)
                    names.putAll(intent.getSerializableExtra("names") as HashMap<String, String>)
                    warnings.putAll(intent.getSerializableExtra("warnings") as HashMap<String, String>)
                }
                "SELECTED_COIN_SETTING" -> {
                    selectedCoin = intent.getStringExtra("selectedCoin")!!
                }
                "FAVORITE_COININFO_SETTING" -> {
                    favoriteCoinInfo.clear()
                    favoriteCoinInfo.addAll(intent.getSerializableExtra("favoriteCoinInfo") as ArrayList<CoinInfo>)
                }
                "START" -> {
                    val thread: Thread = object : Thread() {
                        override fun run() {
                            if (upbitAPIThread.isAlive) {
                                try {
                                    upbitAPIThread.join()
                                    upbitAPIThread2.join()
                                } catch (e: InterruptedException) {
                                    Log.e("OnRestart Error", e.toString())
                                }
                            }
                            upbitAPIThread = UpbitAPIThread(100)
                            upbitAPIThread.start()
                            upbitAPIThread2 = UpbitAPIThread(200)
                            upbitAPIThread2.start()
                        }
                    }
                    thread.start()
                }
                "START_THREAD1" -> {
                    upbitAPIThread.threadStop(true)
                    if (upbitAPIThread.isAlive) {
                        upbitAPIThread.threadStop(true)
                        try {
                            upbitAPIThread.join()
                        } catch (e: InterruptedException) {
                            Log.e("OnRestart Error", e.toString())
                        }
                    }
                    upbitAPIThread = UpbitAPIThread(100)
                    upbitAPIThread.start()
                }
                "START_THREAD2" -> {
                    upbitAPIThread2.threadStop(true)
                    if (upbitAPIThread2.isAlive) {
                        try {
                            upbitAPIThread2.join()
                        } catch (e: InterruptedException) {
                            Log.e("OnRestart Error", e.toString())
                        }
                    }
                    upbitAPIThread2 = UpbitAPIThread(200)
                    upbitAPIThread2.start()
                }
                "STOP" -> {
                    upbitAPIThread.threadStop(true)
                    upbitAPIThread2.threadStop(true)
                }
                "STOP_THREAD1" -> {
                    upbitAPIThread.threadStop(true)
                }
                "STOP_THREAD2" -> {
                    upbitAPIThread2.threadStop(true)
                }
            }
        }
    }

    inner class UpbitAPIThread(var type: Int) : Thread() {

        var stopFlag: Boolean = false

        override fun run() {
            while (!stopFlag) {
                val intent = Intent("com.mobit.APIRECEIVE")
                // 코인 정보 받아오기
                if (type == 100) {
                    intent.putExtra("mode", "CoinInfo")
                    val prices = upbitAPICaller.getTicker(codes)
                    if (prices.isNotEmpty()) {
                        val _coinInfo = ArrayList<CoinInfo>()
                        for (i in prices.indices) {
                            if (coinInfo.isNotEmpty()) {
                                prices[i].realTimePriceDiff =
                                    prices[i].realTimePrice - coinInfo[i].price.realTimePrice
                            }
                            _coinInfo.add(
                                CoinInfo(
                                    codes[i],
                                    names.get(codes[i])!!,
                                    prices[i],
                                    warnings.get(codes[i])!!
                                )
                            )
                        }
                        intent.putExtra("coinInfo", _coinInfo)
                        intent.putExtra("isSuccess", true)

                        val favoriteCoinInfo2 = ArrayList<CoinInfo>()
                        for (i in 0 until favoriteCoinInfo.size) {
                            for (coin in _coinInfo) {
                                if (favoriteCoinInfo[i].code == coin.code) {
                                    favoriteCoinInfo2.add(coin)
                                    break
                                }
                            }
                        }

                        coinInfo.clear()
                        coinInfo.addAll(_coinInfo)
                        intent.putExtra("favoriteCoinInfo", favoriteCoinInfo2)
                    } else {
                        intent.putExtra("isSuccess", false)
                    }
                }
                // 호가 정보 받아오기
                else if (type == 200) {
                    intent.putExtra("mode", "orderBook")
                    val orderBook = upbitAPICaller.getOrderbook(selectedCoin)
                    if (orderBook.isNotEmpty()) {
                        intent.putExtra("orderBook", orderBook)
                        intent.putExtra("isSuccess", true)
                    } else {
                        intent.putExtra("isSuccess", false)
                    }
                }

                sendBroadcast(intent)
                sleep(200)
            }
        }

        fun threadStop(flag: Boolean) {
            this.stopFlag = flag
        }
    }
}