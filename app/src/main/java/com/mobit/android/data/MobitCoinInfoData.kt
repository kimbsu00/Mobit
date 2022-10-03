package com.mobit.android.data

data class MobitCoinInfoData(
    val market: String,                     // 업비트에서 제공중인 시장 정보
    val nameKor: String,                    // 거래 대상 암호화폐 한글명
    val nameEng: String,                    // 거래 대상 암호화폐 영문명
    val marketWarning: Boolean = false      // 유의 종목 여부
) {

    override fun toString(): String {
        return "MobitCoinInfoData{" +
                "market=$market, " +
                "nameKor=$nameKor, " +
                "nameEng=$nameEng, " +
                "marketWarning=$marketWarning}"
    }

}