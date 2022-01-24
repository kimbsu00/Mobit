package com.mobit.mobit.renew.model.datamodel

data class CoinData(
    val strCode: String,            // 코인 코드
    val strKorName: String,         // 코인 한글명
    val strEngName: String,         // 코인 영문명
    val warning: Boolean            // 유의 종목 여부
): Comparable<CoinData> {

    override fun compareTo(other: CoinData): Int {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "CoinData{" +
                "strCode=$strCode, " +
                "strKorName=$strKorName, " +
                "strEngName=$strEngName, " +
                "warning=$warning}"
    }
}