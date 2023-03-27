package com.mobit.android.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtil {

    /**
     * @return Cellular, WiFi, Ethernet 중에 하나라도 연결된 것이 있다면 true, 그렇지 않다면 false
     */
    fun checkNetworkEnable(pContext: Context): Boolean {
        val connectivityManager =
            pContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork?.let { network ->
                connectivityManager.getNetworkCapabilities(network)?.let { networkCapabilities ->
                    val isCellularConnected =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    val isWiFiConnected =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    val isEthernetConnected =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

                    isCellularConnected || isWiFiConnected || isEthernetConnected
                } ?: false
            } ?: false
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}