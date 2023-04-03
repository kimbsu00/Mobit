package com.mobit.android.repository

import android.app.Application
import com.mobit.android.common.util.JsonParserUtil
import com.mobit.android.data.MobitMarketData
import com.mobit.android.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class IntroRepository(
    private val application: Application
) : BaseNetworkRepository(application, TAG) {

    private val jsonParserUtil: JsonParserUtil = JsonParserUtil()

    /**
     * 마켓 코드 조회 API를 호출하는 함수
     *
     * @return 마켓 코드 별, 거래 가능한 코인 리스트
     */
    suspend fun makeMarketCodeRequest(): NetworkResult<MobitMarketData> {
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
                    NetworkResult.Fail("Response Data is Empty")
                }
            } else {
                NetworkResult.Error(Exception("Can't Open Connection"))
            }

            result
        }
    }

    companion object {
        private const val TAG: String = "IntroRepository"
    }

}