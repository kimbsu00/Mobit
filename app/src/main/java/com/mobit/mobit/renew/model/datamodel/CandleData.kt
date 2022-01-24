package com.mobit.mobit.renew.model.datamodel

data class CandleData(
    val strCode: String,                // 코인 코드
    val strUtcTime: String,             // 캔들 기준 시각(UTC 기준)
    val strKstTime: String,             // 캔들 기준 시각(KST 기준)
    val dOpenPrice: Double,             // 시가
    val dHighPrice: Double,             // 고가
    val dLowPrice: Double,              // 저가
    val dTradePrice: Double,            // 종가 == 현재가
    val lTimeStamp: Long,               // 타임스탬프
    val dAccTradePrice: Double,         // 누적 거래 금액
    val dAccTradeVolume: Double         // 누적 거래량
) {
    override fun toString(): String {
        return "CandleData{" +
                "strCode=$strCode, " +
                "strUtcTime=$strUtcTime, " +
                "strKstTime=$strKstTime, " +
                "dOpenPrice=$dOpenPrice, " +
                "dHighPrice=$dHighPrice, " +
                "dLowPrice=$dLowPrice, " +
                "dTradePrice=$dTradePrice, " +
                "lTimeStamp=$lTimeStamp, " +
                "dAccTradePrice=$dAccTradePrice, " +
                "dAccTradeVolume=$dAccTradeVolume}"
    }
}