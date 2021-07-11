package com.mobit.mobit.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobit.mobit.db.MyDBHelper

class MyViewModel : ViewModel() {

    // FragmentCoinList에서 선택한 코인을 다른 Fragment에서 참고할 때 사용하는 변수
    val selectedCoin: MutableLiveData<String> = MutableLiveData()

    // 실시간으로 얻어온 코인 정보를 저장할 변수
    val coinInfo: MutableLiveData<ArrayList<CoinInfo>> = MutableLiveData()

    // 사용자가 즐겨찾기에 추가한 코인 정보를 저장할 변수
    val favoriteCoinInfo: MutableLiveData<ArrayList<CoinInfo>> = MutableLiveData()

    // 실시간으로 얻어온 호가 정보를 저장할 변수
    val orderBook: MutableLiveData<ArrayList<OrderBook>> = MutableLiveData()

    // 사용자가 보유중인 자산 정보를 저장할 변수
    val asset: MutableLiveData<Asset> = MutableLiveData()

    // 사용자가 매수 또는 매도를 진행할 때마다, 거래 내역을 저장할 변수
    val transaction: MutableLiveData<ArrayList<Transaction>> = MutableLiveData()

    // 차트에서 보여주는 메인 지표의 타입을 결정하는 변수
    val mainIndicatorType: MutableLiveData<Int> = MutableLiveData()

    // 차트에서 보여주는 메인 지표를 생성할 때, 사용되는 변수
    val mainIndicator: MutableLiveData<MainIndicator> = MutableLiveData()

    // DB에 데이터를 저장하기 위한 변수
    var myDBHelper: MyDBHelper? = null

    fun setSelectedCoin(selectedCoin: String) {
        this.selectedCoin.value = selectedCoin
    }

    fun setCoinInfo(coinInfo: ArrayList<CoinInfo>) {
        this.coinInfo.value = coinInfo
    }

    fun setFavoriteCoinInfo(favoriteCoinInfo: ArrayList<CoinInfo>) {
        this.favoriteCoinInfo.value = favoriteCoinInfo
    }

    fun setOrderBook(orderBook: ArrayList<OrderBook>) {
        this.orderBook.value = orderBook
    }

    fun setAsset(asset: Asset) {
        this.asset.value = asset
    }

    fun setTransaction(transaction: ArrayList<Transaction>) {
        this.transaction.value = transaction
    }

    fun setMainIndicatorType(mainIndicatorType: Int) {
        this.mainIndicatorType.value = mainIndicatorType
    }

    fun setMainIndicator(mainIndicator: MainIndicator) {
        this.mainIndicator.value = mainIndicator
    }

    fun addTransaction(transaction: Transaction) {
        val temp = ArrayList<Transaction>()
        temp.add(transaction)
        temp.addAll(this.transaction.value!!)
        this.transaction.value = temp
    }

    fun addFavoriteCoinInfo(coinInfo: CoinInfo): Boolean {
        if (favoriteCoinInfo.value == null)
            return false

        if (favoriteCoinInfo.value!!.contains(coinInfo))
            return false

        val temp = ArrayList<CoinInfo>()
        temp.addAll(favoriteCoinInfo.value!!)
        temp.add(coinInfo)
        this.favoriteCoinInfo.value = temp

        return true
    }

    fun removeFavoriteCoinInfo(coinInfo: CoinInfo): Boolean {
        if (favoriteCoinInfo.value == null)
            return false

        if (!favoriteCoinInfo.value!!.contains(coinInfo))
            return false

        val temp = ArrayList<CoinInfo>()
        temp.addAll(favoriteCoinInfo.value!!)
        temp.remove(coinInfo)
        favoriteCoinInfo.value = temp

        return true
    }

    fun bidCoin(code: String, name: String, price: Double, number: Double): Int {
        val temp = Asset(this.asset.value!!.krw, this.asset.value!!.coins)
        val ret = temp.bidCoin(code, name, price, number)
        Log.i("FragmentBuy before setValue", temp.coins[ret].number.toString())
        this.asset.value = temp
        Log.i("FragmentBuy after setValue", this.asset.value!!.coins[ret].number.toString())
        return ret
    }

    fun askCoin(code: String, price: Double, number: Double): CoinAsset? {
        val temp = Asset(this.asset.value!!.krw, this.asset.value!!.coins)
        val ret = temp.askCoin(code, price, number)
        this.asset.value = temp
        return ret
    }
}