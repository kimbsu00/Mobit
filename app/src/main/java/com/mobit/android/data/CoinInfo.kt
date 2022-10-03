package com.mobit.android.data

import java.io.Serializable

data class CoinInfo(
    val code: String,       // 코인 코드
    val name: String,       // 코인 이름
    var price: Price,       // 코인 현재가 정보
    val warning: String     // 투자 유의 유무, NONE=일반, CAUTION=투자유의
) : Serializable {
    companion object {
        val BTC_CODE = "KRW-BTC"        // 비트코인
        val ETH_CODE = "KRW-ETH"        // 이더리움
        val ADA_CODE = "KRW-ADA"        // 에이다
        val DOGE_CODE = "KRW-DOGE"      // 도지코인
        val XRP_CODE = "KRW-XRP"        // 리플
        val DOT_CODE = "KRW-DOT"        // 폴카닷
        val BCH_CODE = "KRW-BCH"        // 비트코인캐시
        val LTC_CODE = "KRW-LTC"        // 라이트코인
        val LINK_CODE = "KRW-LINK"      // 체인링크
        val ETC_CODE = "KRW-ETC"        // 이더리움클래식
        val THETA_CODE = "KRW-THETA"    // 쎄타토큰
        val XLM_CODE = "KRW-XLM"        // 스텔라루멘
        val VET_CODE = "KRW-VET"        // 비체인
        val EOS_CODE = "KRW-EOS"        // 이오스
        val TRX_CODE = "KRW-TRX"        // 트론
        val NEO_CODE = "KRW-NEO"        // 네오
        val IOTA_CODE = "KRW-IOTA"      // 아이오타
        val ATOM_CODE = "KRW-ATOM"      // 코스모스
        val BSV_CODE = "KRW-BSV"        // 비트코인에스브이
        val BTT_CODE = "KRW-BTT"        // 비트토렌트
        val QTUM_CODE = "KRW-QTUM"      // 퀀텀
        val HBAR_CODE = "KRW-HBAR"      // 헤데라해시그래프
        val CRO_CODE = "KRW-CRO"        // 크립토닷컴체인
        val XTZ_CODE = "KRW-XTZ"        // 테조스
        val TFUEL_CODE = "KRW-TFUEL"    // 쎄타퓨엘
        val WAVES_CODE = "KRW-WAVES"    // 웨이브
        val CHZ_CODE = "KRW-CHZ"        // 칠리즈
        val XEM_CODE = "KRW-XEM"        // 넴
        val STX_CODE = "KRW-STX"        // 스택스
        val ZIL_CODE = "KRW-ZIL"        // 질리카
    }
}