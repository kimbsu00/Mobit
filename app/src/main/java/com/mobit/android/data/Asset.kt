package com.mobit.android.data

import android.util.Log
import java.io.Serializable

class Asset : Serializable {

    constructor(krw: Double, coins: ArrayList<CoinAsset>) {
        this.krw = krw
        this.coins.addAll(coins)
    }

    var krw: Double = 0.0   // 보유 KRW 금액
    val coins: ArrayList<CoinAsset> = ArrayList()   // 보유 코인 자산

    fun canBidCoin(code: String, name: String, price: Double, number: Double): Boolean {
        val orderPrice = price * number * 1.0005

        if (krw < orderPrice)
            return false

        return true
    }

    // code에 해당하는 코인을 price 가격으로 number개 만큼 매수한다.
    // 매수한 코인의 인덱스를 리턴한다.
    fun bidCoin(code: String, name: String, price: Double, number: Double): Int {
        val orderPrice = price * number
        val fee = orderPrice * 0.0005
        if (krw < (orderPrice + fee))
            return -1
        krw -= (orderPrice + fee)

        var index: Int = -1
        for (i in coins.indices) {
            if (coins[i].code == code) {
                index = i
                break
            }
        }

        var ret: Int = 0
        if (index == -1) {
            val newCoin = CoinAsset(code, name, number, orderPrice, price)
            coins.add(newCoin)
            ret = coins.indexOf(newCoin)
        } else {
            coins[index].averagePrice =
                (coins[index].number * coins[index].averagePrice + orderPrice) / (coins[index].number + number)
            coins[index].number += number
            coins[index].amount += orderPrice
            ret = index
        }
        Log.i("bidCoin in Asset", coins[ret].number.toString())
        return ret
    }

    fun canAskCoin(code: String, price: Double, number: Double): Boolean {
        var index: Int = -1
        for (i in coins.indices) {
            if (coins[i].code == code) {
                index = i
                break
            }
        }
        if (index == -1)
            return false

        val coin = coins[index]
        if (coin!!.number < number)
            return false

        return true
    }

    // code에 해당하는 코인을 price 가격으로 number개 만큼 매도한다.
    // 매도한 코인의 coinAsset을 리턴한다.
    fun askCoin(code: String, price: Double, number: Double): CoinAsset? {
        var index: Int = -1
        for (i in coins.indices) {
            if (coins[i].code == code) {
                index = i
                break
            }
        }
        if (index == -1)
            return null

        val coin = coins[index]
        if (coin!!.number < number)
            return null

        val orderPrice = price * number
        val fee = orderPrice * 0.0005
        krw += (orderPrice - fee)

        var ret: CoinAsset? = null
        if (coin.number == number) {
            coins.remove(coin)
            coin.number = 0.0
            ret = coin
        } else {
            coins[index].number -= number
            coins[index].amount -= orderPrice
            ret = coins[index]
        }

        return ret
    }
}