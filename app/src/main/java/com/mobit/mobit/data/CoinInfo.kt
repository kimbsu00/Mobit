package com.mobit.mobit.data

import java.io.Serializable

data class CoinInfo(
    val code: String,       // 코인 코드
    val name: String,       // 코인 이름
    val price: Price        // 코인 현재가 정보
) : Serializable {
    companion object {
        val BTC_CODE = "KRW-BTC"        // 비트코인
        val BTC_NAME = "비트코인"
        val ETH_CODE = "KRW-ETH"        // 이더리움
        val ETH_NAME = "이더리움"
        val ADA_CODE = "KRW-ADA"        // 에이다
        val ADA_NAME = "에이다"
        val DOGE_CODE = "KRW-DOGE"      // 도지코인
        val DOGE_NAME = "도지코인"
        val XRP_CODE = "KRW-XRP"        // 리플
        val XRP_NAME = "리플"
        val DOT_CODE = "KRW-DOT"        // 폴카닷
        val DOT_NAME = "폴카닷"
        val BCH_CODE = "KRW-BCH"        // 비트코인캐시
        val BCH_NAME = "비트코인캐시"
        val LTC_CODE = "KRW-LTC"        // 라이트코인
        val LTC_NAME = "라이트코인"
        val LINK_CODE = "KRW-LINK"      // 체인링크
        val LINK_NAME = "체인링크"
        val ETC_CODE = "KRW-ETC"        // 이더리움클래식
        val ETC_NAME = "이더리움클래식"
        val THETA_CODE = "KRW-THETA"    // 쎄타토큰
        val THETA_NAME = "쎄타토큰"
        val XLM_CODE = "KRW-XLM"        // 스텔라루멘
        val XLM_NAME = "스텔라루멘"
        val VET_CODE = "KRW-VET"        // 비체인
        val VET_NAME = "비체인"
        val EOS_CODE = "KRW-EOS"        // 이오스
        val EOS_NAME = "이오스"
        val TRX_CODE = "KRW-TRX"        // 트론
        val TRX_NAME = "트론"
        val NEO_CODE = "KRW-NEO"        // 네오
        val NEO_NAME = "네오"
        val IOTA_CODE = "KRW-IOTA"      // 아이오타
        val IOTA_NAME = "아이오타"
        val ATOM_CODE = "KRW-ATOM"      // 코스모스
        val ATOM_NAME = "코스모스"
        val BSV_CODE = "KRW-BSV"        // 비트코인에스브이
        val BSV_NAME = "비트코인에스브이"
        val BTT_CODE = "KRW-BTT"        // 비트토렌트
        val BTT_NAME = "비트토렌트"
        val QTUM_CODE = "KRW-QTUM"      // 퀀텀
        val QTUM_NAME = "퀀텀"
        val HBAR_CODE = "KRW-HBAR"      // 헤데라해시그래프
        val HBAR_NAME = "헤데라해시그래프"
        val CRO_CODE = "KRW-CRO"        // 크립토닷컴체인
        val CRO_NAME = "크립토닷컴체인"
        val XTZ_CODE = "KRW-XTZ"        // 테조스
        val XTZ_NAME = "테조스"
        val TFUEL_CODE = "KRW-TFUEL"    // 쎄타퓨엘
        val TFUEL_NAME = "쎄타퓨엘"
        val WAVES_CODE = "KRW-WAVES"    // 웨이브
        val WAVES_NAME = "웨이브"
        val CHZ_CODE = "KRW-CHZ"        // 칠리즈
        val CHZ_NAME = "칠리즈"
        val XEM_CODE = "KRW-XEM"        // 넴
        val XEM_NAME = "넴"
        val STX_CODE = "KRW-STX"        // 스택스
        val STX_NAME = "스택스"
        val ZIL_CODE = "KRW-ZIL"        // 질리카
        val ZIL_NAME = "질리카"
    }
}