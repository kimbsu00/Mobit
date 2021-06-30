package com.mobit.mobit.data

import java.io.Serializable

data class OrderBook(val price: Double, val size: Double) : Serializable, Comparable<OrderBook> {
    override fun compareTo(other: OrderBook): Int {
        return when (this.price > other.price) {
            true -> 1
            false -> -1
        }
    }
}
