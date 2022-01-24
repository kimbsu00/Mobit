package com.mobit.mobit.renew.model.datamodel

data class OrderData(
    val dPrice: Double,         // 주문 금액
    val dSize: Double,          // 주문 잔량
    val orderType: Int          // 주문 타입 (0, 매도) (1, 매수)
) : Comparable<OrderData> {
    companion object {
        val ORDER_TYPE_ASK: Int = 0         // 매도
        val ORDER_TYPE_BID: Int = 1         // 매수
    }

    override fun compareTo(other: OrderData): Int {
        if (this.orderType == other.orderType)
            return this.dPrice.compareTo(other.dPrice)
        else if (this.orderType == ORDER_TYPE_ASK)
            return 1
        else
            return -1
    }

    override fun toString(): String {
        return "OrderData{" +
                "dPrice=$dPrice, " +
                "dSize=$dSize, " +
                "orderType=$orderType}"
    }
}