package com.mobit.mobit.renew.common.util

import android.util.Log
import com.mobit.mobit.renew.model.datamodel.*
import org.json.JSONArray
import org.json.JSONObject

object JSONParserUtil {

    val TAG: String = "JSONParserUtil"

    fun getString(jsonObj: JSONObject, key: String): String {
        try {
            if (jsonObj.has(key) && !jsonObj.isNull(key))
                return jsonObj.getString(key).trim()
            else
                return ""
        } catch (e: Exception) {
            return ""
        }
    }

    fun getString(jsonObj: JSONObject, key: String, strDefault: String): String {
        try {
            if (jsonObj.has(key) && !jsonObj.isNull(key))
                return jsonObj.getString(key).trim()
            else
                return strDefault
        } catch (e: Exception) {
            return strDefault
        }
    }

    fun getJsonObject(jsonObj: JSONObject, key: String): JSONObject? {
        try {
            if (jsonObj.has(key) && !jsonObj.isNull(key))
                return jsonObj.getJSONObject(key)
            else
                return null
        } catch (e: Exception) {
            return null
        }
    }

    fun getJsonObject(strJson: String): JSONObject? {
        try {
            val jsonObj = JSONObject(strJson)
            return jsonObj
        } catch (e: Exception) {
            return null
        }
    }

    fun getJsonArray(jsonObj: JSONObject, key: String): JSONArray? {
        try {
            if (jsonObj.has(key) && !jsonObj.isNull(key))
                return jsonObj.getJSONArray(key)
            else
                return null
        } catch (e: Exception) {
            return null
        }
    }

    fun getJsonArray(strJson: String): JSONArray? {
        try {
            val jsonObj = JSONArray(strJson)
            return jsonObj
        } catch (e: Exception) {
            return null
        }
    }

    fun getCode(strMarket: String): String {
        try {
            val tmp = strMarket.split('-')
            return if (tmp[0] == "KRW") tmp[1] else ""
        } catch (e: Exception) {
            Log.i("${TAG}_getCoinData", "market=$strMarket")
            return ""
        }
    }

    /*
     * 2022-01-24
     * 마켓 코드 조회 API의 결과 데이터를 파싱해서 ArrayList<CoinData> 형태로 반환하는 함수
     */
    fun getCoinData(jsonRoot: JSONArray): ArrayList<CoinData> {
        val ret = ArrayList<CoinData>()

        for (idx in 0..jsonRoot.length() - 1) {
            if (!jsonRoot.isNull(idx)) {
                val obj = jsonRoot.getJSONObject(idx)

                if (obj != null) {
                    var strCode = ""
                    var strKorName = ""
                    var strEngName = ""
                    var warning = false

                    if (!obj.isNull("market"))
                        strCode = getCode(getString(obj, "market"))
                    if (!obj.isNull("korean_name"))
                        strKorName = getString(obj, "korean_name")
                    if (!obj.isNull("english_name"))
                        strEngName = getString(obj, "english_name")
                    if (!obj.isNull("market_warning")) {
                        val tmp = getString(obj, "market_warning")
                        warning = (tmp == "CAUTION")
                    }

                    if (strCode.isNotEmpty())
                        ret.add(CoinData(strCode, strKorName, strEngName, warning))
                }
            }
        }

        return ret
    }

    /*
     * 2022-01-24
     * 현재가 정보 API의 결과 데이터를 파싱해서 
     * (key, value) = (코인 코드, PriceData) 인 HashMap을 반환하는 함수
     */
    fun getPriceData(jsonRoot: JSONArray): HashMap<String, PriceData> {
        val ret = HashMap<String, PriceData>()

        for (idx in 0..jsonRoot.length() - 1) {
            if (!jsonRoot.isNull(idx)) {
                val obj = jsonRoot.getJSONObject(idx)

                if (obj != null) {
                    var strCode = ""
                    var dOpenPrice = 0.0
                    var dHighPrice = 0.0
                    var dLowPrice = 0.0
                    var dTradePrice = 0.0
                    var dPrevClosingPrice = 0.0
                    var intChange = PriceData.PRICE_CHANGE_CODE_EVEN
                    var dChangePrice = 0.0
                    var dChangeRate = 0.0
                    var dAccTradePrice = 0.0
                    var dAccTradePrice24 = 0.0
                    var dAccTradeVolume = 0.0
                    var dAccTradeVolume24 = 0.0
                    var lTimeStamp = 0L

                    if (!obj.isNull("market"))
                        strCode = getCode(getString(obj, "market"))
                    if (!obj.isNull("opening_price"))
                        dOpenPrice = obj.getDouble("opening_price")
                    if (!obj.isNull("high_price"))
                        dHighPrice = obj.getDouble("high_price")
                    if (!obj.isNull("low_price"))
                        dLowPrice = obj.getDouble("low_price")
                    if (!obj.isNull("trade_price"))
                        dTradePrice = obj.getDouble("trade_price")
                    if (!obj.isNull("prev_closing_price"))
                        dPrevClosingPrice = obj.getDouble("prev_closing_price")
                    if (!obj.isNull("change")) {
                        val strChange = obj.getString("change")
                        intChange = when (strChange) {
                            "FALL" -> PriceData.PRICE_CHANGE_CODE_FALL
                            "EVEN" -> PriceData.PRICE_CHANGE_CODE_EVEN
                            "RISE" -> PriceData.PRICE_CHANGE_CODE_RISE
                            else -> PriceData.PRICE_CHANGE_CODE_EVEN
                        }
                    }
                    if (!obj.isNull("change_price"))
                        dChangePrice = obj.getDouble("change_price")
                    if (!obj.isNull("change_rate"))
                        dChangeRate = obj.getDouble("change_rate")
                    if (!obj.isNull("acc_trade_price"))
                        dAccTradePrice = obj.getDouble("acc_trade_price")
                    if (!obj.isNull("acc_trade_price_24h"))
                        dAccTradePrice24 = obj.getDouble("acc_trade_price_24h")
                    if (!obj.isNull("acc_trade_volume"))
                        dAccTradeVolume = obj.getDouble("acc_trade_volume")
                    if (!obj.isNull("acc_trade_volume_24h"))
                        dAccTradeVolume24 = obj.getDouble("acc_trade_volume_24h")
                    if (!obj.isNull("timestamp"))
                        lTimeStamp = obj.getLong("timestamp")

                    if (strCode.isNotEmpty())
                        ret.put(
                            strCode,
                            PriceData(
                                strCode,
                                dOpenPrice,
                                dHighPrice,
                                dLowPrice,
                                dTradePrice,
                                dPrevClosingPrice,
                                intChange,
                                dChangePrice,
                                dChangeRate,
                                dAccTradePrice,
                                dAccTradePrice24,
                                dAccTradeVolume,
                                dAccTradeVolume24,
                                lTimeStamp
                            )
                        )
                }
            }
        }

        return ret
    }

    /*
     * 2022-01-30
     * 호가 정보 조회 API의 결과 데이터를 파싱해서
     * (key, value) = (코인 코드, OrderBookData) 인 HashMap을 반환하는 함수
     */
    fun getOrderBookData(jsonRoot: JSONArray): HashMap<String, OrderBookData> {
        val ret = HashMap<String, OrderBookData>()

        for (idx in 0..jsonRoot.length() - 1) {
            if (!jsonRoot.isNull(idx)) {
                val obj = jsonRoot.getJSONObject(idx)

                if (obj != null) {
                    var strCode = ""
                    var lTimeStamp = 0L
                    var dTotalAskSize = 0.0
                    var dTotalBidSize = 0.0

                    if (!obj.isNull("market"))
                        strCode = getString(obj, "market")
                    if (!obj.isNull("timestamp"))
                        lTimeStamp = obj.getLong("timestamp")
                    if (!obj.isNull("total_ask_size"))
                        dTotalAskSize = obj.getDouble("total_ask_size")
                    if (!obj.isNull("total_bid_size"))
                        dTotalBidSize = obj.getDouble("total_bid_size")

                    if (!obj.isNull("orderbook_units")) {
                        val units = getJsonArray(obj, "orderbook_units")

                        // 매도 호가 리스트 -> idx와 주문금액이 서로 비례
                        val askOrderList = ArrayList<OrderData>()
                        // 매수 호가 리스트 -> idx와 주문금액이 서로 반비례
                        val bidOrderList = ArrayList<OrderData>()
                        if (units != null) {
                            for (idx2 in 0..units.length() - 1) {
                                if (!units.isNull(idx2)) {
                                    val unit = units.getJSONObject(idx2)

                                    var dAskPrice = 0.0
                                    var dBidPrice = 0.0
                                    var dAskSize = 0.0
                                    var dBidSize = 0.0

                                    if (!unit.isNull("ask_price"))
                                        dAskPrice = unit.getDouble("ask_price")
                                    if (!unit.isNull("bid_price"))
                                        dBidPrice = unit.getDouble("bid_price")
                                    if (!unit.isNull("ask_size"))
                                        dAskSize = unit.getDouble("ask_size")
                                    if (!unit.isNull("bid_size"))
                                        dBidSize = unit.getDouble("bid_size")

                                    askOrderList.add(
                                        OrderData(
                                            dAskPrice,
                                            dAskSize,
                                            OrderData.ORDER_TYPE_ASK
                                        )
                                    )
                                    bidOrderList.add(
                                        OrderData(
                                            dBidPrice,
                                            dBidSize,
                                            OrderData.ORDER_TYPE_BID
                                        )
                                    )
                                }
                            }
                        }
                        askOrderList.reverse()
                        val orderList = ArrayList<OrderData>().also {
                            it.addAll(askOrderList)
                            it.addAll(bidOrderList)
                        }

                        if (strCode.isNotEmpty())
                            ret.put(
                                strCode,
                                OrderBookData(
                                    strCode,
                                    lTimeStamp,
                                    dTotalAskSize,
                                    dTotalBidSize,
                                    orderList
                                )
                            )
                    }
                }
            }
        }

        return ret
    }

    fun getMinCandleData(jsonRoot: JSONArray): ArrayList<CandleData> {
        val ret = ArrayList<CandleData>()

        for (idx in 0..jsonRoot.length() - 1) {
            if (!jsonRoot.isNull(idx)) {
                val obj = jsonRoot.getJSONObject(idx)

                if (obj != null) {

                }
            }
        }

        return ret
    }

}