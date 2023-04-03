package com.mobit.android.repository

import android.app.Application
import com.mobit.android.common.util.JsonParserUtil

class MobitRepository(
    val application: Application
) : BaseNetworkRepository(application, TAG) {

    private val jsonParserUtil: JsonParserUtil = JsonParserUtil()

    companion object {
        private const val TAG: String = "MobitRepository"

        private const val UPBIT_API_HOST_URL: String = "https://api.upbit.com/v1/"
    }

}