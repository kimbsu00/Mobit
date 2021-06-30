package com.mobit.mobit

import android.os.Bundle
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
import com.mobit.mobit.adapter.FragmentCoinListAdapter
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentCoinListBinding

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

    val coinInfo: ArrayList<CoinInfo> = ArrayList()             // 전체 코인 리스트
    lateinit var adapter: FragmentCoinListAdapter               // 전체 코인 리스트 adapter

    val favoriteCoinInfo: ArrayList<CoinInfo> = ArrayList()     // 관심 코인 리스트
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
//            coinInfo.clear()
//            coinInfo.addAll(myViewModel.coinInfo.value!!)
            for (index in it.indices) {
                if (coinInfo.size > index)
                    coinInfo[index] = it[index]
                else
                    coinInfo.add(it[index])
            }
            adapter.notifyDataSetChanged()
        })

        myViewModel.favoriteCoinInfo.observe(viewLifecycleOwner, Observer {
            favoriteCoinInfo.clear()
            favoriteCoinInfo.addAll(myViewModel.favoriteCoinInfo.value!!)
            favoriteAdapter.notifyDataSetChanged()
        })

        // 일반 코인 목록을 보여주는 adapter
        adapter = FragmentCoinListAdapter(coinInfo, coinInfo)
        adapter.listener = object : FragmentCoinListAdapter.OnItemClickListener {
            override fun onItemClicked(view: View, coinInfo: CoinInfo) {
                myViewModel.setSelectedCoin(coinInfo.code)
                listener?.showTransaction()
                Log.i("Clicked Coin", coinInfo.code)
            }
        }
        // 관심 목록을 보여주는 adapter
        favoriteAdapter = FragmentCoinListAdapter(favoriteCoinInfo, favoriteCoinInfo)
        favoriteAdapter.listener = object : FragmentCoinListAdapter.OnItemClickListener {
            override fun onItemClicked(view: View, coinInfo: CoinInfo) {
                myViewModel.setSelectedCoin(coinInfo.code)
                listener?.showTransaction()
                Log.i("Clicked Coin", coinInfo.code)
            }
        }

        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

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

}