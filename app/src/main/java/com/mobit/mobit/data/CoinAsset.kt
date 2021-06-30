package com.mobit.mobit.data

import java.io.Serializable

data class CoinAsset(
    val code: String,           // 코인 코드
    val name: String,           // 코인 이름
    var number: Double,         // 코인 보유 개수
    var amount: Double,         // 코인 보유 금액
    var averagePrice: Double,   // 평균 단가
) : Serializable
