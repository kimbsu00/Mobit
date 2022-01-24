package com.mobit.mobit.renew.model.datamodel

data class PriceData(
    val strCode: String,                // 코인 코드
    val dOpenPrice: Double,             // 시가
    val dHighPrice: Double,             // 고가
    val dLowPrice: Double,              // 저가
    val dTradePrice: Double,            // 종가 == 현재가
    val dPrevClosingPrice: Double,      // 전일 종가
    val intChange: Int,                 // (-1, 하락) (0, 보합) (1, 상승)
    val dChangePrice: Double,           // 변화액의 절대값
    val dChangeRate: Double,            // 변화율의 절대값
    val dAccTradePrice: Double,         // UTC 0시 기준 누적 거래대금
    val dAccTradePrice24: Double,       // 24시간 누적 거래대금
    val dAccTradeVolume: Double,        // UTC 0시 기준 누적 거래량
    val dAccTradeVolume24: Double,      // 24시간 누적 거래량
    val lTimeStamp: Long                // 타임스탬프
) {
    companion object{
        val PRICE_CHANGE_CODE_FALL: Int = -1
        val PRICE_CHANGE_CODE_EVEN: Int = 0
        val PRICE_CHANGE_CODE_RISE: Int = 1
    }

    override fun toString(): String {
        return "PriceData{" +
                "dOpenPrice=$dOpenPrice, " +
                "dHighPrice=$dHighPrice, " +
                "dLowPrice=$dLowPrice, " +
                "dTradePrice=$dTradePrice, " +
                "dPrevClosingPrice=$dPrevClosingPrice, " +
                "intChange=$intChange, " +
                "dChangePrice=$dChangePrice, " +
                "dChangeRate=$dChangeRate, " +
                "dAccTradePrice=$dAccTradePrice, " +
                "dAccTradePrice24=$dAccTradePrice24, " +
                "dAccTradeVolume=$dAccTradeVolume, " +
                "dAccTradeVolume24=$dAccTradeVolume24, " +
                "lTimeStamp=$lTimeStamp}"
    }
}