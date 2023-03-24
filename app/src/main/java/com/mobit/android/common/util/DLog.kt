package com.mobit.android.common.util

import android.util.Log
import com.mobit.android.BuildConfig

/**
 * Debug 모드로 빌드된 앱에서만 로그가 찍히도록 하기 위한 오브젝트
 *
 * @see Log
 */
object DLog {

    fun d(pTag: String, pMessage: String) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.d(pTag, pMessage)
        }
    }

    fun d(pTag: String, pMessage: String, pException: Exception) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.d(pTag, pMessage, pException)
        }
    }

    fun w(pTag: String, pMessage: String) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.w(pTag, pMessage)
        }
    }

    fun w(pTag: String, pMessage: String, pException: Exception) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.w(pTag, pMessage, pException)
        }
    }

    fun e(pTag: String, pMessage: String) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.e(pTag, pMessage)
        }
    }

    fun e(pTag: String, pMessage: String, pException: Exception) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.e(pTag, pMessage, pException)
        }
    }

    fun i(pTag: String, pMessage: String) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.i(pTag, pMessage)
        }
    }

    fun i(pTag: String, pMessage: String, pException: Exception) {
        if (BuildConfig.DEBUG && pMessage.isNotEmpty()) {
            Log.i(pTag, pMessage, pException)
        }
    }

}