package com.mobit.mobit.renew.common

import android.content.Context
import com.mobit.mobit.BuildConfig
import com.mobit.mobit.renew.model.network.NetworkManager
import com.mobit.mobit.renew.model.network.datamodel.NetworkData
import com.mobit.mobit.renew.model.network.listener.NetworkResultListener

class CommonAPI(val listener: NetworkResultListener, val context: Context) : NetworkResultListener {

    fun getMarket(isDeatils: Boolean = true) {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()
            requestData.put("isDetails", isDeatils.toString())

            data.requestCode = CommonCodes.NETWORK_CODE_MARKET
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/market/all"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getTicker(markets: ArrayList<String>) {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("markets", getStrMarkets(markets))

            data.requestCode = CommonCodes.NETWORK_CODE_TICKER
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/ticker"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getOrderbook(markets: ArrayList<String>) {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("markets", getStrMarkets(markets))

            data.requestCode = CommonCodes.NETWORK_CODE_ORDERBOOK
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/orderbook"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getCandlesMin(unit: Int, market: String, count: Int = 200, to: String = "") {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("market", market)
            if (count < 1 || count > 200)
                requestData.put("count", "200")
            else
                requestData.put("count", count.toString())
            if (to.isNotEmpty())
                requestData.put("to", to)

            data.requestCode = CommonCodes.NETWORK_CODE_CANDLES_MIN
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/candles/minutes/$unit"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getCandlesDay(
        market: String,
        count: Int = 200,
        convertingPriceUnit: String = "",
        to: String = ""
    ) {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("market", market)
            if (count < 1 || count > 200)
                requestData.put("count", "200")
            else
                requestData.put("count", count.toString())
            if (convertingPriceUnit.isNotEmpty())
                requestData.put("convertingPriceUnit", convertingPriceUnit)
            if (to.isNotEmpty())
                requestData.put("to", to)

            data.requestCode = CommonCodes.NETWORK_CODE_CANDLES_DAY
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/candles/days"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getCandlesWeek(market: String, count: Int = 200, to: String = "") {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("market", market)
            if (count < 1 || count > 200)
                requestData.put("count", "200")
            else
                requestData.put("count", count.toString())
            if (to.isNotEmpty())
                requestData.put("to", to)

            data.requestCode = CommonCodes.NETWORK_CODE_CANDLES_WEEK
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/candles/weeks"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    fun getCandlesMonth(market: String, count: Int = 200, to: String = "") {
        try {
            val data = NetworkData()

            val requestData = HashMap<String, String>()

            requestData.put("market", market)
            if (count < 1 || count > 200)
                requestData.put("count", "200")
            else
                requestData.put("count", count.toString())
            if (to.isNotEmpty())
                requestData.put("to", to)

            data.requestCode = CommonCodes.NETWORK_CODE_CANDLES_MONTH
            data.params = requestData
            data.listener = this
            data.url = BuildConfig.UPBIT_API_HOST_URL + "v1/candles/months"

            NetworkManager.execute(data, context, "GET")
        } catch (e: Exception) {
        }
    }

    private fun getStrMarkets(markets: ArrayList<String>): String {
        var strMarkets = ""
        for (idx in markets.indices) {
            strMarkets +=
                if (idx == markets.size - 1)
                    markets[idx]
                else
                    "${markets[idx]}, "
        }
        return strMarkets
    }

    override fun onResult(networkData: NetworkData) {
        listener.onResult(networkData)
    }

    override fun onSuccessResult(networkData: NetworkData) {
        listener.onSuccessResult(networkData)
    }

    override fun onFailResult(networkData: NetworkData) {
        listener.onFailResult(networkData)
    }
}