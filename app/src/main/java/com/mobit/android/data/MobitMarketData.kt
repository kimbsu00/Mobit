package com.mobit.android.data

data class MobitMarketData(
    val mobitCoinMap: HashMap<String, ArrayList<MobitCoinInfoData>> = hashMapOf()
) {

    val isValid get() = mobitCoinMap.size > 0

    /**
     * 모비트 코인 데이터를 추가하는 함수
     *
     * @param pCoinInfoData     추가할 코인 데이터
     */
    fun addCoin(pCoinInfoData: MobitCoinInfoData) {
        // 이전에 추가한 적이 있는 마켓의 코인인 경우
        if (mobitCoinMap.containsKey(pCoinInfoData.market)) {
            mobitCoinMap.get(pCoinInfoData.market)?.add(pCoinInfoData)
        }
        // 처음 추가하는 마켓의 코인인 경우
        else {
            mobitCoinMap.put(pCoinInfoData.market, arrayListOf(pCoinInfoData))
        }
    }

    override fun toString(): String {
        return "MobitMarketData{" +
                "mobitCoinMap=$mobitCoinMap}"
    }

}