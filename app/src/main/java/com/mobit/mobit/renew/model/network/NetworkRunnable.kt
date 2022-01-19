package com.mobit.mobit.renew.model.network

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.mobit.mobit.renew.common.util.NetworkUtils
import com.mobit.mobit.renew.model.network.datamodel.NetworkData
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/*
 * urlState -> "GET" or "POST"
 */
class NetworkRunnable(val networkData: NetworkData, val context: Context, val urlState: String) :
    Runnable {

    companion object {
        val TAG: String = NetworkRunnable::class.java.simpleName
        val TIME_OUT: Int = 10 * 1000
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            val newtorkData: NetworkData = msg.obj as NetworkData
            Log.i("TAG_handler_dispatchMessage", "url=${networkData.url}")
            Log.i("TAG_handler_dispatchMessage", "params=${networkData.params}")
            Log.i("TAG_handler_dispatchMessage", "msg=${networkData.message}")

            if (networkData.responseCode != HttpURLConnection.HTTP_OK) {
                networkData.listener.onFailResult(networkData)
            } else {
                networkData.listener.onSuccessResult(networkData)
            }
        }
    }

    override fun run() {
        var connection: HttpURLConnection? = null

        try {
            val url = URL("${networkData.url}?${getParams(networkData.params)}")

            // https 프로토콜 통신
            if (url.protocol == "https") {
                trustAllHosts()
                val https: HttpsURLConnection = url.openConnection() as HttpsURLConnection
                https.setHostnameVerifier(DO_NOT_VERIFY)
                connection = https
            }
            // http 프로토콜 통신
            else {
                connection = url.openConnection() as HttpURLConnection
            }

            connection.readTimeout = TIME_OUT
            connection.connectTimeout = TIME_OUT

            connection.requestMethod = urlState

            // Header 설정
            connection.setRequestProperty("Accept", "application/json")

            val bis = BufferedInputStream(connection.inputStream)
            val msg = NetworkUtils.getMessage(bis)

            networkData.responseCode = HttpURLConnection.HTTP_OK
            networkData.message = msg
        } catch (e: Exception) {
            networkData.responseCode = -1
            e.message?.let {
                networkData.message = it
            }
        } finally {
            val message: Message = Message.obtain()
            message.obj = networkData
            handler.sendMessage(message)
        }
    }

    fun getParams(params: HashMap<String, String>): String {
        val buffer = StringBuilder()

        val iterator: Iterator<Map.Entry<String, String>> = params.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next() as Map.Entry<String, String>

            if (buffer.length > 0) {
                buffer.append("&")
            }

            buffer.append(URLEncoder.encode(entry.key, "UTF-8"))
            buffer.append("=")
            buffer.append(URLEncoder.encode(entry.value, "UTF-8"))
        }

        return buffer.toString()
    }

    var DO_NOT_VERIFY: HostnameVerifier = object : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }

    private fun trustAllHosts() {
        val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(object : X509TrustManager {

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<X509Certificate>()
            }

            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

        })

        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}