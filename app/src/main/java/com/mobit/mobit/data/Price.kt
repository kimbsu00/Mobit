package com.mobit.mobit.data

import java.io.Serializable

/*
openPrice : 시가
highPrice : 고가
lowPrice : 저가
endPrice : 종가
prevEndPrice : 전일 종가
change : { ("EVEN", 보합), ("RISE", 상승), ("FALL", 하락) }
changePrice : 부호가 있는 변화액
changeRate : 부호가 있는 변화율
totalTradeVolume : 누적 거래량(UTC 0시 기준)
totalTradePrice : 누적 거래대금(UTC 0시 기준)
totalTradePrice24: 24시간 누적 거래대금
highestWeekPrice : 52주 신고가
highestWeekDate: 52주 신고가 달성일 "yyyy-MM-dd"
lowestWeekPrice : 52주 신저가
lowestWeekDate: 52주 신저가 달성일 "yyyy-MM-dd"
 */
data class Price(
    var realTimePrice: Double,
    var openPrice: Double,
    var highPrice: Double,
    var lowPrice: Double,
    var endPrice: Double,
    var prevEndPrice: Double,
    var change: String,
    var changePrice: Double,
    var changeRate: Double,
    var totalTradeVolume: Double,
    var totalTradePrice: Double,
    var totalTradePrice24: Double,
    var highestWeekPrice: Double,
    var highestWeekDate: String,
    var lowestWeekPrice: Double,
    var lowestWeekDate: String
) : Serializable
