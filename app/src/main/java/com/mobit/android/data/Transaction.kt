package com.mobit.android.data

data class Transaction(
    val code: String,           // 코인 코드
    val name: String,           // 코인 이름
    val time: String,           // 체결시간 "yyyy-MM-ddThh:mm:ss" 형태로 저장함 (https://developer.android.com/reference/java/time/format/DateTimeFormatter#ISO_LOCAL_DATE_TIME)
    val type: Int,              // 매수 매도 여부
    val quantity: Double,       // 거래 수량
    val unitPrice: Double,      // 거래 단가
    val tradePrice: Double,     // 거래 금액
    val fee: Double,            // 수수료
    val totalPrice: Double      // 정산 금액
) {
    companion object {
        const val BID = 100     // 매수
        const val ASK = 200     // 매도
    }
}