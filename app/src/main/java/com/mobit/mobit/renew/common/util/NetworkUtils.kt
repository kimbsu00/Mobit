package com.mobit.mobit.renew.common.util

import java.io.*

object NetworkUtils {

    fun getMessage(inputStream: InputStream): String {
        val builder = StringBuilder()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(inputStream))
            var line: String = ""

            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
        } catch (e: UnsupportedEncodingException) {
        } catch (e: IOException) {
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                }
            }
        }
        return builder.toString()
    }

}