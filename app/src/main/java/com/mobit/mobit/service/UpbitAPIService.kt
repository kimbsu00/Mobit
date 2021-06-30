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

    lateinit var selectedCoin: String
    var favoriteCoinInfo: ArrayList<CoinInfo> = ArrayList()
    var coinInfo: ArrayList<CoinInfo> = ArrayList()

    val upbitAPICaller: UpbitAPICaller = UpbitAPICaller()

    // 코인 정보 가져오는 쓰레드
    lateinit var upbitAPIThread: UpbitAPIThread

    // 코인 호가 정보 가져오는 쓰레드
    lateinit var upbitAPIThread2: UpbitAPIThread

    val codes: ArrayList<String> = arrayListOf(
        CoinInfo.BTC_CODE,
        CoinInfo.ETH_CODE,
        CoinInfo.ADA_CODE,
        CoinInfo.DOGE_CODE,
        CoinInfo.XRP_CODE,
        CoinInfo.DOT_CODE,
        CoinInfo.BCH_CODE,
        CoinInfo.LTC_CODE,
        CoinInfo.LINK_CODE,
        CoinInfo.ETC_CODE,
        CoinInfo.THETA_CODE,
        CoinInfo.XLM_CODE,
        CoinInfo.VET_CODE,
        CoinInfo.EOS_CODE,
        CoinInfo.TRX_CODE,
        CoinInfo.NEO_CODE,
        CoinInfo.IOTA_CODE,
        CoinInfo.ATOM_CODE,
        CoinInfo.BSV_CODE,
        CoinInfo.BTT_CODE
    )

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

        upbitAPIThread = UpbitAPIThread(100, codes)
        upbitAPIThread2 = UpbitAPIThread(200, codes)
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
                    selectedCoin = intent.getStringExtra("selectedCoin")
                    favoriteCoinInfo.clear()
                    favoriteCoinInfo.addAll(intent.getSerializableExtra("favoriteCoinInfo") as ArrayList<CoinInfo>)
                }
                "SELECTED_COIN_SETTING" -> {
                    selectedCoin = intent.getStringExtra("selectedCoin")
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
                            upbitAPIThread = UpbitAPIThread(100, codes)
                            upbitAPIThread.start()
                            upbitAPIThread2 = UpbitAPIThread(200, codes)
                            upbitAPIThread2.start()
                        }
                    }
                    thread.start()
                }
                "START_THREAD1" -> {
                    val thread: Thread = object : Thread() {
                        override fun run() {
                            if (upbitAPIThread.isAlive) {
                                try {
                                    upbitAPIThread.join()
                                } catch (e: InterruptedException) {
                                    Log.e("OnRestart Error", e.toString())
                                }
                            }
                            upbitAPIThread = UpbitAPIThread(100, codes)
                            upbitAPIThread.start()
                        }
                    }
                    thread.start()
                }
                "START_THREAD2" -> {
                    val thread: Thread = object : Thread() {
                        override fun run() {
                            if (upbitAPIThread2.isAlive) {
                                try {
                                    upbitAPIThread2.join()
                                } catch (e: InterruptedException) {
                                    Log.e("OnRestart Error", e.toString())
                                }
                            }
                            upbitAPIThread2 = UpbitAPIThread(200, codes)
                            upbitAPIThread2.start()
                        }
                    }
                    thread.start()
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

    inner class UpbitAPIThread(var type: Int, val codes: ArrayList<String>) : Thread() {

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
                            _coinInfo.add(CoinInfo(codes[i], getCoinName(codes[i]), prices[i]))
                        }
                        intent.putExtra("coinInfo", _coinInfo)
                        intent.putExtra("isSuccess", true)

                        val favoriteCoinInfo2 = ArrayList<CoinInfo>()
                        for (favorite in favoriteCoinInfo) {
                            for (coin in _coinInfo) {
                                if (favorite.code == coin.code) {
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
                sleep(300)
            }
        }

        fun getCoinName(code: String): String {
            return when (code) {
                CoinInfo.BTC_CODE -> CoinInfo.BTC_NAME
                CoinInfo.ETH_CODE -> CoinInfo.ETH_NAME
                CoinInfo.ADA_CODE -> CoinInfo.ADA_NAME
                CoinInfo.DOGE_CODE -> CoinInfo.DOGE_NAME
                CoinInfo.XRP_CODE -> CoinInfo.XRP_NAME
                CoinInfo.DOT_CODE -> CoinInfo.DOT_NAME
                CoinInfo.BCH_CODE -> CoinInfo.BCH_NAME
                CoinInfo.LTC_CODE -> CoinInfo.LTC_NAME
                CoinInfo.LINK_CODE -> CoinInfo.LINK_NAME
                CoinInfo.ETC_CODE -> CoinInfo.ETC_NAME
                CoinInfo.THETA_CODE -> CoinInfo.THETA_NAME
                CoinInfo.XLM_CODE -> CoinInfo.XLM_NAME
                CoinInfo.VET_CODE -> CoinInfo.VET_NAME
                CoinInfo.EOS_CODE -> CoinInfo.EOS_NAME
                CoinInfo.TRX_CODE -> CoinInfo.TRX_NAME
                CoinInfo.NEO_CODE -> CoinInfo.NEO_NAME
                CoinInfo.IOTA_CODE -> CoinInfo.IOTA_NAME
                CoinInfo.ATOM_CODE -> CoinInfo.ATOM_NAME
                CoinInfo.BSV_CODE -> CoinInfo.BSV_NAME
                CoinInfo.BTT_CODE -> CoinInfo.BTT_NAME
                else -> CoinInfo.BTC_NAME
            }
        }

        fun threadStop(flag: Boolean) {
            this.stopFlag = flag
        }
    }
}