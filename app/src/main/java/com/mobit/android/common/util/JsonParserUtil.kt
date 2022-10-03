package com.mobit.android.common.util

import com.mobit.android.data.MobitCoinInfoData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonParserUtil {

    // region Base Function
    fun getString(jsonObj: JSONObject, key: String, strDefault: String = "") =
        if (jsonObj.has(key) && !jsonObj.isNull(key)) jsonObj.getString(key)
        else strDefault

    fun getBoolean(jsonObj: JSONObject, key: String, default: Boolean = false): Boolean {
        return if (jsonObj.has(key) && !jsonObj.isNull(key)) {
            val value = jsonObj.getString(key).trim()

            when (value.toLowerCase()) {
                "yes",
                "true",
                "y",
                "1" -> true
                else -> false
            }
        } else {
            default
        }
    }

    fun getInt(jsonObj: JSONObject, key: String, intDefault: Int = -1): Int {
        return if (jsonObj.has(key) && !jsonObj.isNull(key)) {
            val value = jsonObj.getString(key).trim()

            try {
                value.toInt()
            } catch (e: NumberFormatException) {
                intDefault
            }
        } else {
            intDefault
        }
    }

    fun getFloat(jsonObj: JSONObject, key: String, floatDefault: Float = -1f): Float {
        return if (jsonObj.has(key) && !jsonObj.isNull(key)) {
            val value = jsonObj.getString(key).trim()

            try {
                value.toFloat()
            } catch (e: NumberFormatException) {
                floatDefault
            }
        } else {
            floatDefault
        }
    }

    fun getDouble(jsonObj: JSONObject, key: String, doubleDefault: Double = -1.0): Double {
        return if (jsonObj.has(key) && !jsonObj.isNull(key)) {
            val value = jsonObj.getString(key).trim()

            try {
                value.toDouble()
            } catch (e: NumberFormatException) {
                doubleDefault
            }
        } else {
            doubleDefault
        }
    }

    fun getJsonObject(jsonObject: JSONObject, key: String): JSONObject? {
        return if (jsonObject.has(key) && !jsonObject.isNull(key)) {
            try {
                jsonObject.getJSONObject(key)
            } catch (e: JSONException) {
                null
            }
        } else {
            null
        }
    }

    fun getJSONArray(jsonObject: JSONObject, key: String): JSONArray? {
        return if (jsonObject.has(key) && !jsonObject.isNull(key)) {
            try {
                jsonObject.getJSONArray(key)
            } catch (e: JSONException) {
                null
            }
        } else {
            null
        }
    }
    // endregion Base Function

    fun getCoinInfoDataList(jsonArray: JSONArray): ArrayList<MobitCoinInfoData> {
        val ret = ArrayList<MobitCoinInfoData>()

        if (jsonArray.length() == 0)
            return ret

        for (idx in 0 until jsonArray.length()) {
            if (!jsonArray.isNull(idx)) {
                val obj = jsonArray.getJSONObject(idx)

                if (obj != null) {
                    val market = getString(obj, "market")
                    val nameKor = getString(obj, "korean_name")
                    val nameEng = getString(obj, "english_name")
                    val marketWarning = (getString(obj, "market_warning") == "CAUTION")

                    if (market.contains("KRW")) {
                        ret.add(MobitCoinInfoData(market, nameKor, nameEng, marketWarning))
                    }
                }
            }
        }

        return ret
    }

}