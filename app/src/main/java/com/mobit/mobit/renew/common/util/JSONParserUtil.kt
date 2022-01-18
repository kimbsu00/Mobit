package com.mobit.mobit.renew.common.util

import org.json.JSONArray
import org.json.JSONObject

object JSONParserUtil {

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

}