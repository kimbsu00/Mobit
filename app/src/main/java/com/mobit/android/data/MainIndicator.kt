package com.mobit.android.data

import java.io.Serializable

class MainIndicator: Serializable {
    companion object IndicatorType {
        const val MOVING_AVERAGE: Int = 10
        const val BOLLINGER_BANDS: Int = 11
        const val DAILY_BALANCE_TABLE: Int = 12
        const val PIVOT: Int = 13
        const val ENVELOPES: Int = 14
        const val PRICE_CHANNELS: Int = 15
    }

    // 이동평균선에서 사용되는 변수
    var MA_N1: Int = 5
    var MA_N2: Int = 10
    var MA_N3: Int = 20
    var MA_N4: Int = 60
    var MA_N5: Int = 120

    // 볼린저밴드에서 사용되는 변수
    var BB_N: Int = 20
    var BB_K: Float = 2.0f

    // 일목균형표에서 사용되는 변수
    var DBT_1: Int = 9
    var DBT_2: Int = 26
    var DBT_3: Int = 26
    var DBT_4: Int = 26
    var DBT_5: Int = 52

    // Envelopes에서 사용되는 변수
    var ENV_N: Int = 20
    var ENV_K: Int = 6

    // PriceChannels에서 사용되는 변수
    var PC_N: Int = 5

    fun resetVariable() {
        MA_N1 = 5
        MA_N2 = 10
        MA_N3 = 20
        MA_N4 = 60
        MA_N5 = 120

        BB_N = 20
        BB_K = 2.0f

        DBT_1 = 9
        DBT_2 = 26
        DBT_3 = 26
        DBT_4 = 26
        DBT_5 = 52

        ENV_N = 20
        ENV_K = 6

        PC_N = 5
    }
}