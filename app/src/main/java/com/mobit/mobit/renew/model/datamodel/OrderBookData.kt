package com.mobit.mobit.renew.model.datamodel

data class OrderBookData(
    val strCode: String,                    // 코인 코드
    val lTimeStamp: Long,                   // 타임스탬프
    val dTotalAskSize: Double,              // 호가 매도 총 잔량
    val dTotalBidSize: Double,              // 호가 매수 총 잔량
    val orderList: ArrayList<OrderData>     // 호가 정보 리스트
) {
    override fun toString(): String {
        return "OrderBookData{" +
                "strCode=$strCode, " +
                "lTimeStamp=$lTimeStamp, " +
                "dTotalAskSize=$dTotalAskSize, " +
                "dTotalBidSize=$dTotalBidSize, " +
                "orderList=$orderList}"
    }
}