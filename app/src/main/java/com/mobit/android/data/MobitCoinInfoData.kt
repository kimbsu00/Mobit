package com.mobit.android.data

/**
 * @param market            업비트에서 제공중인 시장 정보
 * @param nameKor           거래 대상 암호화폐 한글명
 * @param nameEng           거래 대상 암호화폐 영문명
 * @param marketWarning     유의 종목 여부
 */
data class MobitCoinInfoData(
    val market: String,
    val nameKor: String,
    val nameEng: String,
    val marketWarning: Boolean = false
) {

    override fun toString(): String {
        return "MobitCoinInfoData{" +
                "market=$market, " +
                "nameKor=$nameKor, " +
                "nameEng=$nameEng, " +
                "marketWarning=$marketWarning}"
    }

}