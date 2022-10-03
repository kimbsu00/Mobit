package com.mobit.android.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobit.android.R
import com.mobit.android.adapter.FragmentCoinListAdapter
import com.mobit.android.data.CoinInfo
import com.mobit.android.viewmodel.MyViewModel
import com.mobit.android.databinding.FragmentCoinListBinding

/*
가상화폐 목록과 정보 확인 기능이 구현될 Fragment 입니다.
*/
class FragmentCoinList : Fragment() {

    // UI 변수 시작
    lateinit var binding: FragmentCoinListBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    // true -> 전체 코인 리스트를 보여주고 있는 상황
    // false -> 관심 코인 리스트를 보여주고 있는 상황
    var adapterState: Boolean = true

    val coinInfoUpdateHandler: CoinInfoUpdateHandler = CoinInfoUpdateHandler()
    val coinInfo: ArrayList<CoinInfo> = ArrayList()             // 전체 코인 정보 리스트
    lateinit var adapter: FragmentCoinListAdapter               // 전체 코인 리스트 adapter

    val favoriteCoinInfo: ArrayList<CoinInfo> = ArrayList()     // 관심 코인 정보 리스트
    lateinit var favoriteAdapter: FragmentCoinListAdapter       // 관심 코인 리스트 adapter

    var listener: OnFragmentInteraction? = null     // MainActivity와 통신할 때 사용되는 interface

    interface OnFragmentInteraction {
        fun showTransaction()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoinListBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    fun init() {
        myViewModel.coinInfo.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val runnable: CoinInfoUpdateRunnable = CoinInfoUpdateRunnable()
            val thread: Thread = Thread(runnable)
            thread.start()
        })

        myViewModel.favoriteCoinInfo.observe(viewLifecycleOwner, Observer {
            favoriteCoinInfo.clear()
            favoriteCoinInfo.addAll(myViewModel.favoriteCoinInfo.value!!)
            favoriteAdapter.notifyItemRangeChanged(0, favoriteCoinInfo.size)
        })

        // 일반 코인 목록을 보여주는 adapter
        adapter = FragmentCoinListAdapter(coinInfo, coinInfo)
        adapter.listener = object : FragmentCoinListAdapter.OnItemClickListener {
            override fun onItemClicked(view: View, code: String) {
                myViewModel.setSelectedCoin(code)
                listener?.showTransaction()
                Log.i("Clicked Coin", code)
            }
        }
        // 관심 목록을 보여주는 adapter
        favoriteAdapter = FragmentCoinListAdapter(favoriteCoinInfo, favoriteCoinInfo)
        favoriteAdapter.listener = object : FragmentCoinListAdapter.OnItemClickListener {
            override fun onItemClicked(view: View, code: String) {
                myViewModel.setSelectedCoin(code)
                listener?.showTransaction()
                Log.i("Clicked Coin", code)
            }
        }

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = null

            radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.krwBtn -> {
                            adapterState = true
                            recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                        R.id.favoriteBtn -> {
                            adapterState = false
                            recyclerView.adapter = favoriteAdapter
                            favoriteAdapter.notifyDataSetChanged()
                        }
                        else -> {
                            Log.e("FragmentCoinList", "Radio Group Error")
                        }
                    }
                }
            })

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (adapterState) {
                        adapter.filter.filter(query)
                    } else {
                        favoriteAdapter.filter.filter(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (adapterState) {
                        adapter.filter.filter(newText)
                    } else {
                        favoriteAdapter.filter.filter(newText)
                    }
                    return true
                }

            })
        }
    }

    inner class CoinInfoUpdateHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                val notifyFlag: Boolean = bundle.getBoolean("notifyFlag")
                if (notifyFlag) {
                    adapter.notifyItemRangeChanged(0, coinInfo.size)
                }
            }
        }
    }

    inner class CoinInfoUpdateRunnable : Runnable {
        override fun run() {
            val message = coinInfoUpdateHandler.obtainMessage()
            val bundle: Bundle = Bundle()

            myViewModel.coinInfoLock.lock()
            try {
                val it = myViewModel.coinInfo.value!!
                for (index in it.indices) {
                    if (coinInfo.size > index) {
                        coinInfo[index].price.apply {
                            realTimePrice = it[index].price.realTimePrice
                            openPrice = it[index].price.openPrice
                            highPrice = it[index].price.highPrice
                            lowPrice = it[index].price.lowPrice
                            endPrice = it[index].price.endPrice
                            prevEndPrice = it[index].price.prevEndPrice
                            change = it[index].price.change
                            changePrice = it[index].price.changeRate
                            changeRate = it[index].price.changeRate
                            totalTradeVolume = it[index].price.totalTradeVolume
                            totalTradePrice = it[index].price.totalTradePrice
                            totalTradePrice24 = it[index].price.totalTradePrice24
                            highestWeekPrice = it[index].price.highestWeekPrice
                            highestWeekDate = it[index].price.highestWeekDate
                            lowestWeekPrice = it[index].price.lowestWeekPrice
                            lowestWeekDate = it[index].price.lowestWeekDate
                            realTimePriceDiff = it[index].price.realTimePriceDiff
                        }
                    } else {
                        coinInfo.add(it[index])
                    }
                    bundle.putBoolean("notifyFlag", true)
                }
            } finally {
                myViewModel.coinInfoLock.unlock()
            }

            message.data = bundle
            coinInfoUpdateHandler.sendMessage(message)
        }
    }
}