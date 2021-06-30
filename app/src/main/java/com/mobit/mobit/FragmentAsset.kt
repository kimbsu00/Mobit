package com.mobit.mobit

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobit.mobit.adapter.FragmentAssetAdapter
import com.mobit.mobit.data.Asset
import com.mobit.mobit.data.CoinAsset
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentAssetBinding
import java.text.DecimalFormat

/*
보유자산 기능이 구현될 Fragment 입니다.
*/
class FragmentAsset : Fragment() {

    // UI 변수 시작
    lateinit var binding: FragmentAssetBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()
    val retainedCoin: ArrayList<CoinAsset> = ArrayList<CoinAsset>()

    lateinit var adapter: FragmentAssetAdapter

    val formatter = DecimalFormat("###,###")
    val changeFormatter = DecimalFormat("###,###.##")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssetBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    fun init() {
        myViewModel.asset.observe(viewLifecycleOwner, Observer { asset ->
            val krw: Double = asset.krw             // 보유 KRW
            var total: Double = krw                 // 총 보유자산
            var buyPrice: Double = 0.0              // 총 매수
            var totalEvaluation: Double = 0.0       // 총 평가
            for (coin in asset.coins) {
                total += coin.amount
                buyPrice += coin.number * coin.averagePrice
                totalEvaluation += coin.amount
            }
            val gainAndLoss: Double = if (buyPrice > 0.0) totalEvaluation - buyPrice else 0.0
            val yieldValue: Double = if (buyPrice > 0.0) gainAndLoss / buyPrice * 100 else 0.0

            binding.apply {
                krwView.text = formatter.format(krw)
                totalView.text = formatter.format(total)
                totalBuyView.text = formatter.format(buyPrice)

                if (buyPrice > 0.0) {
                    recyclerView.visibility = View.VISIBLE
                    noAssetView.visibility = View.GONE

                    gainAndLossView.text = formatter.format(gainAndLoss)
                    totalEvaluationView.text = formatter.format(totalEvaluation)
                    yieldView.text = changeFormatter.format(yieldValue) + "%"

                    val rgb = if (buyPrice > totalEvaluation) Color.rgb(
                        25,
                        96,
                        186
                    ) else if (buyPrice < totalEvaluation) Color.rgb(
                        207,
                        80,
                        71
                    ) else Color.rgb(211, 212, 214)
                    gainAndLossView.setTextColor(rgb)
                    yieldView.setTextColor(rgb)
                } else {
                    recyclerView.visibility = View.GONE
                    noAssetView.visibility = View.VISIBLE

                    gainAndLossView.text = getString(R.string.asset_no_data)
                    totalEvaluationView.text = getString(R.string.asset_no_data)
                    yieldView.text = getString(R.string.asset_no_data)

                    val rgb = Color.rgb(211, 212, 214)
                    gainAndLossView.setTextColor(rgb)
                    totalEvaluationView.setTextColor(rgb)
                    yieldView.setTextColor(rgb)
                }
            }

            retainedCoin.clear()
            retainedCoin.addAll(myViewModel.asset.value!!.coins)
            adapter.notifyDataSetChanged()
        })
        myViewModel.coinInfo.observe(viewLifecycleOwner, Observer {
            val asset2 = Asset(myViewModel.asset.value!!.krw, ArrayList<CoinAsset>())
            for (i in myViewModel.asset.value!!.coins.indices) {
                for (coinInfo in it) {
                    if (myViewModel.asset.value!!.coins[i].code == coinInfo.code) {
                        myViewModel.asset.value!!.coins[i].amount =
                            myViewModel.asset.value!!.coins[i].number * coinInfo.price.realTimePrice
                        asset2.coins.add(myViewModel.asset.value!!.coins[i])
                        break
                    }
                }
            }
            myViewModel.setAsset(asset2)

            retainedCoin.clear()
            retainedCoin.addAll(myViewModel.asset.value!!.coins)
            adapter.notifyDataSetChanged()
        })

        adapter = FragmentAssetAdapter(retainedCoin)
        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
        }
    }
}