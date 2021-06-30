package com.mobit.mobit.data

import java.io.Serializable

data class Candle(
    val createdAt: Long,
    val open: Float,
    val close: Float,
    val shadowHigh: Float,
    val shadowLow: Float,
    val totalTradeVolume: Float
) : Serializable