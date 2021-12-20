package com.mobit.mobit

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.mobit.mobit.adapter.FragmentAssetAdapter
import com.mobit.mobit.adapter.FragmentAssetLegendAdapter
import com.mobit.mobit.data.Asset
import com.mobit.mobit.data.CoinAsset
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentAssetBinding
import java.text.DecimalFormat

/*
보유자산 기능이 구현될 Fragment 입니다.
*/
class FragmentAsset : Fragment() {

    // 보유자산 포트폴리오 PieChart에서 사용하는 색상들
    val portfolioChartColor: List<Int> = listOf(
        Color.rgb(148, 178, 74),
        Color.rgb(16, 129, 172),
        Color.rgb(107, 89, 148),
        Color.rgb(181, 105, 164),
        Color.rgb(247, 121, 41),
        Color.rgb(255, 178, 58),
        Color.rgb(198, 146, 107),
        Color.rgb(213, 178, 82),
        Color.rgb(99, 142, 173),
        Color.rgb(189, 190, 197)
    )

    // UI 변수 시작
    lateinit var binding: FragmentAssetBinding
    var portfolioLayoutVisible: Boolean = false
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()
    val retainedCoin: ArrayList<CoinAsset> = ArrayList<CoinAsset>()
    val legendEntries: ArrayList<LegendEntry> = ArrayList<LegendEntry>()

    lateinit var adapter: FragmentAssetAdapter
    lateinit var legendAdapter: FragmentAssetLegendAdapter

    val formatter = DecimalFormat("###,###")
    val changeFormatter = DecimalFormat("###,###.##")
    val percentFormatter = DecimalFormat("###,###.#")

    var listener: OnFragmentInteraction? = null

    interface OnFragmentInteraction {
        fun showTransaction()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssetBinding.inflate(layoutInflater)

        init()
        initChart()

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

            setPortfolioChartData(asset.coins, krw, total)

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
        adapter.listener = object : FragmentAssetAdapter.OnItemClickListener {
            override fun onItemClicked(code: String) {
                Log.i("FragmentAsset", "clicked code is $code")
                myViewModel.setSelectedCoin(code)
                listener?.showTransaction()
            }
        }
        legendAdapter = FragmentAssetLegendAdapter(legendEntries)
        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            legendRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            legendRecyclerView.adapter = legendAdapter

            portfolioTitle.setOnClickListener {
                if (portfolioLayoutVisible) {
                    portfolioLayoutVisible = false
                    portfolioTitle.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_keyboard_arrow_down_24,
                        0
                    )
                    portfolioLayout.visibility = View.GONE
                } else {
                    portfolioLayoutVisible = true
                    portfolioTitle.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_keyboard_arrow_up_24,
                        0
                    )
                    portfolioLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    fun initChart() {
        binding.apply {
            portfolioChart.apply {
                // Whether to show the middle hole
                isDrawHoleEnabled = true
                holeRadius = 40f
                setHoleColor(Color.TRANSPARENT)
                transparentCircleRadius = 0f

                // Whether to show text in the middle of the pie chart
                setDrawCenterText(true)
                centerText = getString(R.string.asset_percent)
                setCenterTextColor(Color.parseColor("#d3d4d6"))
                setCenterTextSize(14f)

                isRotationEnabled = false
                // Displayed as a percentage
                setUsePercentValues(true)
                description.isEnabled = false

                setBackgroundColor(Color.TRANSPARENT)
                legend.isEnabled = false

                setEntryLabelColor(Color.rgb(49, 48, 49))
                setEntryLabelTextSize(12f)
            }
        }
    }

    fun setPortfolioChartData(coins: ArrayList<CoinAsset>, krw: Double, total: Double) {
        val sortedCoin: ArrayList<CoinAsset> = ArrayList()
        sortedCoin.addAll(coins)
        sortedCoin.add(CoinAsset("KRW-KRW", "KRW", 1.0, 0.0, krw))
        sortedCoin.sortWith(object : Comparator<CoinAsset> {
            override fun compare(p0: CoinAsset?, p1: CoinAsset?): Int {
                val price1 = p0!!.averagePrice * p0.number
                val price2 = p1!!.averagePrice * p1.number
                return price2.compareTo(price1)
            }
        })
        val portfolioLegendEntries: ArrayList<LegendEntry> = ArrayList()
        val portfolioEntries: ArrayList<PieEntry> = ArrayList()
        var etcSum: Double = 0.0
        for (i in sortedCoin.indices) {
            val price: Double = (sortedCoin[i].averagePrice * sortedCoin[i].number)
            if (i < 9) {
                val percent: Double = (price / total) * 100
                if (percent >= 5.0) {
                    portfolioEntries.add(
                        PieEntry(
                            price.toFloat(),
                            percentFormatter.format(percent)
                        )
                    )
                } else {
                    portfolioEntries.add(PieEntry(price.toFloat(), ""))
                }
                val label: String =
                    "${sortedCoin[i].code.split('-')[1]}-${percentFormatter.format(percent)}%"
                portfolioLegendEntries.add(
                    LegendEntry(
                        label,
                        Legend.LegendForm.CIRCLE,
                        Float.NaN,
                        Float.NaN,
                        null,
                        portfolioChartColor[i]
                    )
                )
            } else {
                etcSum += price
            }
        }
        if (etcSum > 0.0) {
            val etcPercent: Double = (etcSum / total) * 100
            if (etcPercent >= 5.0) {
                portfolioEntries.add(
                    PieEntry(
                        etcSum.toFloat(),
                        percentFormatter.format((etcSum / total) * 100)
                    )
                )
            } else {
                portfolioEntries.add(PieEntry(etcSum.toFloat(), ""))
            }
            val label: String = "기타-${percentFormatter.format(etcPercent)}%"
            portfolioLegendEntries.add(
                LegendEntry(
                    label,
                    Legend.LegendForm.CIRCLE,
                    Float.NaN,
                    Float.NaN,
                    null,
                    portfolioChartColor[9]
                )
            )
        }

        legendEntries.clear()
        legendEntries.addAll(portfolioLegendEntries)
        legendAdapter.notifyDataSetChanged()

        val portfolioDataSet = PieDataSet(portfolioEntries, "").apply {
            setColors(portfolioChartColor)
            setDrawValues(false)
            // slice를 선택했을 때, slice가 움직이지 않도록 설정
            selectionShift = 0f
        }
        val portfolioData = PieData(portfolioDataSet)

        binding.portfolioChart.data = portfolioData
    }
}