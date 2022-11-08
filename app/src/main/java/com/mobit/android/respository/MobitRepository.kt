package com.mobit.android.respository

import android.app.Application
import com.mobit.android.common.util.JsonParserUtil
import com.mobit.android.data.MobitMarketData
import com.mobit.android.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

class MobitRepository(val application: Application) {

    val jsonParserUtil: JsonParserUtil = JsonParserUtil()

    suspend fun makeCoinListRequest(): NetworkResult<MobitMarketData> {
        return withContext(Dispatchers.IO) {
            val strUrl = "${UPBIT_API_HOST_URL}market/all"
            val hsParams = HashMap<String, String>().apply {
                put("isDeatils", "true")
            }
            val message = sendRequest(strUrl, hsParams, "GET")

            val result = if (message.isNotEmpty()) {
                val jsonRoot = try {
                    JSONArray(message)
                } catch (e: JSONException) {
                    JSONArray()
                }

                val data = jsonParserUtil.getMobitMarketData(jsonRoot)
                if (data.isValid) {
                    NetworkResult.Success(data)
                } else {
                    NetworkResult.Error(Exception("Response Data is Empty"))
                }
            } else {
                NetworkResult.Error(Exception("Can't Open Connection"))
            }

            result
        }
    }

    private fun sendRequest(
        strUrl: String,
        hsParams: HashMap<String, String>,
        urlState: String
    ): String {
        val connection: HttpURLConnection
        var message: String = ""
        try {
            val url: URL =
                if (urlState == "GET") URL("${strUrl}?${getParams(hsParams)}") else URL(strUrl)

            // Https Protocol Check
            connection = if (url.protocol == "https") {
                trustAllHosts()
                val https = url.openConnection() as HttpsURLConnection
                https.hostnameVerifier = DO_NOT_VERIFY
                https
            } else {
                url.openConnection() as HttpURLConnection
            }

            connection.apply {
                readTimeout = TIME_OUT
                connectTimeout = TIME_OUT
                requestMethod = urlState

                if (urlState == "PUT")
                    connection.setRequestProperty("Content-Type", "application/json")

                connection.doOutput = true
            }

            val os = connection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(getParams(hsParams))
            writer.flush()
            writer.close()

            os.flush()
            os.close()

            val bis = BufferedInputStream(connection.inputStream)
            message = getMessage(bis)

        } catch (e: Exception) {
            message = ""
        }

        return message
    }

    private fun getParams(hsParams: HashMap<String, String>): String {
        val sb = StringBuilder()

        val iterator = hsParams.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()

            if (sb.length > 0)
                sb.append("&")

            sb.append(URLEncoder.encode(entry.key, "UTF-8"))
            sb.append("=")
            sb.append(URLEncoder.encode(entry.value, "UTF-8"))
        }

        return sb.toString()
    }

    private fun getMessage(inputStream: InputStream): String {
        val sb = StringBuilder()

        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

            var line: String
            while ((reader.readLine().also { line = it }) != null) {
                sb.append(line)
            }
        } catch (e: UnsupportedEncodingException) {
        } catch (e: IOException) {
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
            }
        }

        return sb.toString()
    }

    companion object {
        private const val UPBIT_API_HOST_URL: String = "https://api.upbit.com/v1/"

        private val TIME_OUT = 10 * 1000

        private val DO_NOT_VERIFY: HostnameVerifier = object : HostnameVerifier {
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true
            }
        }

        private fun trustAllHosts() {
            val trustAllCerts = arrayOf(object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf<X509Certificate>()
                }
            })

            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(null, trustAllCerts, SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            } catch (e: Exception) {
            }
        }
    }

}