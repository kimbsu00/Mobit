package com.mobit.mobit.network

import android.util.Log
import com.mobit.mobit.data.Candle
import com.mobit.mobit.data.OrderBook
import com.mobit.mobit.data.Price
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


/*
Upbit open API를 사용해서 데이터를 얻어오는 작업을 구현할 클래스입니다.
*/
class UpbitAPICaller {

    companion object {
        val TICKER_URL = "https://api.upbit.com/v1/ticker"
        val ORDERBOOK_URL = "https://api.upbit.com/v1/orderbook"
        val CANDLE_MINUTE_URL = "https://api.upbit.com/v1/candles/minutes"
        val CANDLE_DAY_URL = "https://api.upbit.com/v1/candles/days"
        val CANDLE_WEEK_URL = "https://api.upbit.com/v1/candles/weeks"
        val CANDLE_MONTH_URL = "https://api.upbit.com/v1/candles/months"
    }

    fun connect(connUrl: String): String {
        var ret = ""
        try {
            val url = URL(connUrl)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("Accept", "application/json")

            val responseCode = con.responseCode
            val br: BufferedReader
            if (responseCode == 200) {
                br = BufferedReader(InputStreamReader(con.inputStream))
            } else {
                br = BufferedReader(InputStreamReader(con.errorStream))
            }
            var inputLine: String?
            val response = StringBuffer()
            while (br.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            br.close()
            ret += response.toString()
        } catch (e: Exception) {
            Log.e("connect Error", e.toString())
            return ret
        }

        return ret
    }

    // code에 해당하는 코인 현재가 정보를 가져오는 함수
    fun getTicker(codes: ArrayList<String>): ArrayList<Price> {
        val ret = ArrayList<Price>()
        var markets = "?markets="
        for (i in codes.indices) {
            markets += when (i) {
                codes.size - 1 -> codes[i]
                else -> "${codes[i]}, "
            }
        }
        if (markets == "?markets=")
            return ret

        val url = TICKER_URL + markets
        val text = connect(url)
        if (text.isBlank())
            return ret

        val jsonArray: JSONArray = JSONArray(text)
        for (i in 0..jsonArray.length() - 1) {
            val jsonObject = jsonArray.getJSONObject(i)
            val openPrice = jsonObject.getDouble("opening_price")
            val highPrice = jsonObject.getDouble("high_price")
            val lowPrice = jsonObject.getDouble("low_price")
            val endPrice = jsonObject.getDouble("trade_price")
            val prevEndPrice = jsonObject.getDouble("prev_closing_price")
            val change = jsonObject.getString("change")
            val changePrice = jsonObject.getDouble("signed_change_price")
            val changeRate = jsonObject.getDouble("signed_change_rate")
            val totalTradeVolume = jsonObject.getDouble("acc_trade_volume")
            val totalTradePrice = jsonObject.getDouble("acc_trade_price")
            val totalTradePrice24 = jsonObject.getDouble("acc_trade_price_24h")
            val highestWeekPrice = jsonObject.getDouble("highest_52_week_price")
            val highestWeekDate = jsonObject.getString("highest_52_week_date")
            val lowestWeekPrice = jsonObject.getDouble("lowest_52_week_price")
            val lowestWeekDate = jsonObject.getString("lowest_52_week_date")
            val price = Price(
                endPrice,
                openPrice,
                highPrice,
                lowPrice,
                endPrice,
                prevEndPrice,
                change,
                changePrice,
                changeRate,
                totalTradeVolume,
                totalTradePrice,
                totalTradePrice24,
                highestWeekPrice,
                highestWeekDate,
                lowestWeekPrice,
                lowestWeekDate,
                0.0
            )
            ret.add(price)
        }

        return ret
    }

    // code에 해당하는 코인의 호가 정보를 가져오는 함수
    fun getOrderbook(code: String): ArrayList<OrderBook> {
        val ret = ArrayList<OrderBook>()

        val markets = "?markets=$code"
        val url = ORDERBOOK_URL + markets
        val text = connect(url)
        if (text.isBlank())
            return ret

        val orderbooks = JSONArray(text)
        val jsonObject = orderbooks!!.getJSONObject(0)
        val market = jsonObject.getString("market")
        val orderbook = jsonObject.getJSONArray("orderbook_units")
        val ask = ArrayList<OrderBook>()
        val bid = ArrayList<OrderBook>()
        // ask(매도)는 가격이 오름차순으로 정렬되어 있고,
        // bid(매수)는 가격이 내림차순으로 정렬되어 있다.
        for (j in 0..orderbook.length() - 1) {
            val temp = orderbook.getJSONObject(j)
            ask.add(OrderBook(temp.getDouble("ask_price"), temp.getDouble("ask_size")))
            bid.add(OrderBook(temp.getDouble("bid_price"), temp.getDouble("bid_size")))
        }
        ask.reverse()
        ret.addAll(ask)
        ret.addAll(bid)

        return ret
    }

    // code에 해당하는 코인의 unit분 단위의 캔들 차트 정보를 가져오는 함수
    fun getCandleMinute(code: String, unit: Int): ArrayList<Candle> {
        val ret = ArrayList<Candle>()

        val query = "/$unit?market=$code&count=200"
        val url = CANDLE_MINUTE_URL + query
        val text = connect(url)
        if (text.isBlank())
            return ret

        val candles = JSONArray(text)
        for (i in 1..candles.length()) {
            val candle = candles.getJSONObject(candles.length() - i)
            val createdAt = i.toLong()
            val open = candle.getDouble("opening_price").toFloat()
            val close = candle.getDouble("trade_price").toFloat()
            val shadowHigh = candle.getDouble("high_price").toFloat()
            val shadowLow = candle.getDouble("low_price").toFloat()
            val totalTradeVolume = candle.getDouble("candle_acc_trade_volume").toFloat()
            ret.add(Candle(createdAt, open, close, shadowHigh, shadowLow, totalTradeVolume))
        }

        return ret
    }

    // code에 해당하는 코인의 일 단위의 캔들 차트 정보를 가져오는 함수
    fun getCandleDay(code: String): ArrayList<Candle> {
        val ret = ArrayList<Candle>()

        val query = "?market=$code&count=200"
        val url = CANDLE_DAY_URL + query
        val text = connect(url)
        if (text.isBlank())
            return ret

        val candles = JSONArray(text)
        for (i in 1..candles.length()) {
            val candle = candles.getJSONObject(candles.length() - i)
            val createdAt = i.toLong()
            val open = candle.getDouble("opening_price").toFloat()
            val close = candle.getDouble("trade_price").toFloat()
            val shadowHigh = candle.getDouble("high_price").toFloat()
            val shadowLow = candle.getDouble("low_price").toFloat()
            val totalTradeVolume = candle.getDouble("candle_acc_trade_volume").toFloat()
            ret.add(Candle(createdAt, open, close, shadowHigh, shadowLow, totalTradeVolume))
        }

        return ret
    }

    // code에 해당하는 코인의 주 단위의 캔들 차트 정보를 가져오는 함수
    fun getCandleWeek(code: String): ArrayList<Candle> {
        val ret = ArrayList<Candle>()

        val query = "?market=$code&count=200"
        val url = CANDLE_WEEK_URL + query
        val text = connect(url)
        if (text.isBlank())
            return ret

        val candles = JSONArray(text)
        for (i in 1..candles.length()) {
            val candle = candles.getJSONObject(candles.length() - i)
            val createdAt = i.toLong()
            val open = candle.getDouble("opening_price").toFloat()
            val close = candle.getDouble("trade_price").toFloat()
            val shadowHigh = candle.getDouble("high_price").toFloat()
            val shadowLow = candle.getDouble("low_price").toFloat()
            val totalTradeVolume = candle.getDouble("candle_acc_trade_volume").toFloat()
            ret.add(Candle(createdAt, open, close, shadowHigh, shadowLow, totalTradeVolume))
        }

        return ret
    }

    // code에 해당하는 코인의 월 단위의 캔들 차트 정보를 가져오는 함수
    fun getCandleMonth(code: String): ArrayList<Candle> {
        val ret = ArrayList<Candle>()

        val query = "?market=$code&count=200"
        val url = CANDLE_MONTH_URL + query
        val text = connect(url)
        if (text.isBlank())
            return ret

        val candles = JSONArray(text)
        for (i in 1..candles.length()) {
            val candle = candles.getJSONObject(candles.length() - i)
            val createdAt = i.toLong()
            val open = candle.getDouble("opening_price").toFloat()
            val close = candle.getDouble("trade_price").toFloat()
            val shadowHigh = candle.getDouble("high_price").toFloat()
            val shadowLow = candle.getDouble("low_price").toFloat()
            val totalTradeVolume = candle.getDouble("candle_acc_trade_volume").toFloat()
            ret.add(Candle(createdAt, open, close, shadowHigh, shadowLow, totalTradeVolume))
        }

        return ret
    }

}