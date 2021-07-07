package com.mobit.mobit

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.mobit.mobit.data.Candle
import com.mobit.mobit.data.CoinInfo
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentChartBinding
import com.mobit.mobit.network.UpbitAPICaller
import java.text.DecimalFormat
import kotlin.math.abs

/*
코인 차트 기능이 구현될 Fragment 입니다.

차트 기능 구현할 때 사용할 라이브러리
->  https://github.com/PhilJay/MPAndroidChart
 */
class FragmentChart : Fragment() {

    companion object {
        const val UNIT_MIN_1: Int = 0
        const val UNIT_MIN_3: Int = 1
        const val UNIT_MIN_5: Int = 2
        const val UNIT_MIN_10: Int = 3
        const val UNIT_MIN_30: Int = 4
        const val UNIT_MIN_60: Int = 5
        const val UNIT_MIN_240: Int = 6
        const val UNIT_DAY: Int = 7
        const val UNIT_WEEK: Int = 8
        const val UNIT_MONTH: Int = 9
    }

    // UI 변수 시작
    lateinit var binding: FragmentChartBinding
    var minuteBtnFlag: Boolean = true
    // UI 변수 끝

    // 차트 관련 변수 시작
    var unitFlag: Int = UNIT_MIN_1
    var unitMinFlag: Int = UNIT_MIN_1
    // 차트 관련 변수 끝

    val upbitAPICaller: UpbitAPICaller = UpbitAPICaller()
    val upbitCandleHandler: UpbitCandleHandler = UpbitCandleHandler()
    lateinit var upbitCandleThread: UpbitCandleThread

    val myViewModel: MyViewModel by activityViewModels()

    var selectedCoin: CoinInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(layoutInflater)

        upbitCandleThread = UpbitCandleThread()
        upbitCandleThread.start()

        initChart()
        init()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val thread = object : Thread() {
            override fun run() {
                if (!upbitCandleThread.isAlive) {
                    upbitCandleThread = UpbitCandleThread()
                    upbitCandleThread.start()
                }
            }
        }
        thread.start()
    }

    override fun onStop() {
        super.onStop()
        upbitCandleThread.threadStop(true)
    }

    fun initChart() {
        binding.apply {
            priceChart.description.isEnabled = false
            priceChart.setMaxVisibleValueCount(200)
            priceChart.setPinchZoom(false)
            priceChart.setDrawGridBackground(false)
            // x축 설정
            priceChart.xAxis.apply {
                textColor = Color.TRANSPARENT
                position = XAxis.XAxisPosition.BOTTOM
                // 세로선 표시 여부 설정
                this.setDrawGridLines(true)
                axisLineColor = Color.rgb(50, 59, 76)
                gridColor = Color.rgb(50, 59, 76)
            }
            // 왼쪽 y축 설정
            priceChart.axisLeft.apply {
                textColor = Color.WHITE
                isEnabled = false
            }
            // 오른쪽 y축 설정
            priceChart.axisRight.apply {
                setLabelCount(7, false)
                textColor = Color.WHITE
                // 가로선 표시 여부 설정
                setDrawGridLines(true)
                // 차트의 오른쪽 테두리 라인 설정
                setDrawAxisLine(true)
                axisLineColor = Color.rgb(50, 59, 76)
                gridColor = Color.rgb(50, 59, 76)
            }
            priceChart.legend.isEnabled = true
            val average5Legend = LegendEntry()
            average5Legend.label = "5"
            average5Legend.formColor = Color.rgb(219, 17, 179)
            val average10Legend = LegendEntry()
            average10Legend.label = "10"
            average10Legend.formColor = Color.rgb(11, 41, 175)
            val average20Legend = LegendEntry()
            average20Legend.label = "20"
            average20Legend.formColor = Color.rgb(234, 153, 1)
            val average60Legend = LegendEntry()
            average60Legend.label = "60"
            average60Legend.formColor = Color.rgb(253, 52, 0)
            val average120Legend = LegendEntry()
            average120Legend.label = "120"
            average120Legend.formColor = Color.rgb(170, 170, 170)
            priceChart.legend.apply {
                setCustom(
                    listOf(
                        average5Legend,
                        average10Legend,
                        average20Legend,
                        average60Legend,
                        average120Legend
                    )
                )
                textColor = Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(true)
            }

            transactionChart.description.isEnabled = false
            transactionChart.setMaxVisibleValueCount(200)
            transactionChart.setPinchZoom(false)
            transactionChart.setDrawGridBackground(false)
            transactionChart.xAxis.apply {
                textColor = Color.TRANSPARENT
                position = XAxis.XAxisPosition.BOTTOM
                this.setDrawGridLines(true)
                axisLineColor = Color.rgb(50, 59, 76)
                gridColor = Color.rgb(50, 59, 76)
            }
            transactionChart.axisLeft.apply {
                textColor = Color.WHITE
                isEnabled = false
            }
            transactionChart.axisRight.apply {
                setLabelCount(7, false)
                textColor = Color.WHITE
                setDrawGridLines(true)
                setDrawAxisLine(true)
                axisLineColor = Color.rgb(50, 59, 76)
                gridColor = Color.rgb(50, 59, 76)
            }
            transactionChart.legend.isEnabled = true
            transactionChart.legend.apply {
                setCustom(
                    listOf(
                        average5Legend,
                        average10Legend,
                        average20Legend
                    )
                )
                textColor = Color.WHITE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(true)
            }

            priceChart.onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartGestureEnd(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartLongPressed(me: MotionEvent?) {}

                override fun onChartDoubleTapped(me: MotionEvent?) {}

                override fun onChartSingleTapped(me: MotionEvent?) {}

                override fun onChartFling(
                    me1: MotionEvent?,
                    me2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ) {
                }

                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                    syncCharts(priceChart, transactionChart)
                }

                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    syncCharts(priceChart, transactionChart)
                }
            }
            transactionChart.onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureStart(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartGestureEnd(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartLongPressed(me: MotionEvent?) {}

                override fun onChartDoubleTapped(me: MotionEvent?) {}

                override fun onChartSingleTapped(me: MotionEvent?) {}

                override fun onChartFling(
                    me1: MotionEvent?,
                    me2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ) {
                }

                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                    syncCharts(transactionChart, priceChart)
                }

                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    syncCharts(transactionChart, priceChart)
                }
            }
        }
    }

    fun init() {
        myViewModel.coinInfo.observe(viewLifecycleOwner, Observer {
            for (coinInfo in myViewModel.coinInfo.value!!) {
                if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                    selectedCoin = coinInfo
                    break
                }
            }
            binding.apply {
                if (selectedCoin != null) {
                    val formatter = DecimalFormat("###,###")
                    val changeFormatter = DecimalFormat("###,###.##")
                    coinName.text = "${selectedCoin!!.name}(${selectedCoin!!.code.split('-')[1]})"
                    coinPrice.text =
                        if (selectedCoin!!.price.realTimePrice > 100.0)
                            formatter.format(selectedCoin!!.price.realTimePrice)
                        else
                            changeFormatter.format(selectedCoin!!.price.realTimePrice)
                    coinRate.text =
                        changeFormatter.format(selectedCoin!!.price.changeRate * 100) + "%"
                    coinDiff.text = when (selectedCoin!!.price.change) {
                        "EVEN" -> ""
                        "RISE" -> "▲ "
                        "FALL" -> "▼ "
                        else -> ""
                    } + changeFormatter.format(abs(selectedCoin!!.price.changePrice))

                    setTextViewColor(selectedCoin!!)
                }
            }
        })

        binding.apply {
            radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.minuteBtn -> {
                            unitFlag = unitMinFlag
                        }
                        R.id.dayBtn -> {
                            minuteBtnFlag = false
                            unitFlag = UNIT_DAY
                        }
                        R.id.weekBtn -> {
                            minuteBtnFlag = false
                            unitFlag = UNIT_WEEK
                        }
                        R.id.monthBtn -> {
                            minuteBtnFlag = false
                            unitFlag = UNIT_MONTH
                        }
                    }
                }
            })
            minuteBtn.setOnClickListener { view ->
                if (minuteBtnFlag && unitFlag in UNIT_MIN_1..UNIT_MIN_240) {
                    setUnitMinBtn(view)
                } else if (unitFlag in UNIT_MIN_1..UNIT_MIN_240) {
                    minuteBtnFlag = true
                }
            }

            for (coinInfo in myViewModel.coinInfo.value!!) {
                if (coinInfo.code == myViewModel.selectedCoin.value!!) {
                    selectedCoin = coinInfo
                    break
                }
            }
            // 코인이 관심목록에 등록되어 있는 경우에는 ImageButton을 채워진 별로 변경해야 한다.
            if (myViewModel.favoriteCoinInfo.value!!.contains(selectedCoin)) {
                favoriteBtn.setImageResource(R.drawable.ic_round_star_24)
            }
            favoriteBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    // 즐겨찾기에 이미 추가되어 있는 경우
                    if (myViewModel.favoriteCoinInfo.value!!.contains(selectedCoin)) {
                        if (myViewModel.removeFavoriteCoinInfo(selectedCoin!!)) {
                            val thread = object : Thread() {
                                override fun run() {
                                    val flag =
                                        myViewModel.myDBHelper!!.deleteFavorite(selectedCoin!!.code)
                                    Log.e("favorite delete", flag.toString())
                                }
                            }
                            thread.start()
                            favoriteBtn.setImageResource(R.drawable.ic_round_star_border_24)
                            Toast.makeText(context, "관심코인에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 즐겨찾기에 추가되어 있지 않은 경우
                    else {
                        if (myViewModel.addFavoriteCoinInfo(selectedCoin!!)) {
                            val thread = object : Thread() {
                                override fun run() {
                                    val flag =
                                        myViewModel.myDBHelper!!.insertFavoirte(selectedCoin!!.code)
                                    Log.e("favorite insert", flag.toString())
                                }
                            }
                            thread.start()
                            favoriteBtn.setImageResource(R.drawable.ic_round_star_24)
                            Toast.makeText(context, "관심코인으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    fun setUnitMinBtn(view: View) {
        val popupMenu: PopupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(
            R.menu.menu_chart_unit,
            popupMenu.menu
        )
        popupMenu.setOnMenuItemClickListener { item ->
            if (item != null) {
                when (item.itemId) {
                    R.id.minute_1 -> {
                        if (unitFlag != UNIT_MIN_1) {
                            unitFlag = UNIT_MIN_1
                            unitMinFlag = UNIT_MIN_1
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_1)
                        }
                    }
                    R.id.minute_3 -> {
                        if (unitFlag != UNIT_MIN_3) {
                            unitFlag = UNIT_MIN_3
                            unitMinFlag = UNIT_MIN_3
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_3)
                        }
                    }
                    R.id.minute_5 -> {
                        if (unitFlag != UNIT_MIN_5) {
                            unitFlag = UNIT_MIN_5
                            unitMinFlag = UNIT_MIN_5
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_5)
                        }
                    }
                    R.id.minute_10 -> {
                        if (unitFlag != UNIT_MIN_10) {
                            unitFlag = UNIT_MIN_10
                            unitMinFlag = UNIT_MIN_10
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_10)
                        }
                    }
                    R.id.minute_30 -> {
                        if (unitFlag != UNIT_MIN_30) {
                            unitFlag = UNIT_MIN_30
                            unitMinFlag = UNIT_MIN_30
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_30)
                        }
                    }
                    R.id.minute_60 -> {
                        if (unitFlag != UNIT_MIN_60) {
                            unitFlag = UNIT_MIN_60
                            unitMinFlag = UNIT_MIN_60
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_60)
                        }
                    }
                    R.id.minute_240 -> {
                        if (unitFlag != UNIT_MIN_240) {
                            unitFlag = UNIT_MIN_240
                            unitMinFlag = UNIT_MIN_240
                            binding.minuteBtn.text = getString(R.string.chart_unit_minute_240)
                        }
                    }
                }
            }
            true
        }
        popupMenu.show()
    }

    fun setTextViewColor(coinInfo: CoinInfo) {
        binding.apply {
            if (coinInfo.price.changeRate > 0) {
                coinPrice.setTextColor(Color.parseColor("#bd4e3a"))
                coinRate.setTextColor(Color.parseColor("#bd4e3a"))
                coinDiff.setTextColor(Color.parseColor("#bd4e3a"))
            } else if (coinInfo.price.changeRate < 0) {
                coinPrice.setTextColor(Color.parseColor("#135fc1"))
                coinRate.setTextColor(Color.parseColor("#135fc1"))
                coinDiff.setTextColor(Color.parseColor("#135fc1"))
            } else {
                coinPrice.setTextColor(Color.parseColor("#FFFFFF"))
                coinRate.setTextColor(Color.parseColor("#FFFFFF"))
                coinDiff.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }

    fun syncCharts(mainChart: CombinedChart, otherChart: CombinedChart) {
        val mainMatrix: Matrix
        val mainVals = FloatArray(9)
        val otherMatrix: Matrix
        val otherVals = FloatArray(9)
        mainMatrix = mainChart.viewPortHandler.matrixTouch
        mainMatrix.getValues(mainVals)

        otherMatrix = otherChart.viewPortHandler.matrixTouch
        otherMatrix.getValues(otherVals)
        otherVals[Matrix.MSCALE_X] = mainVals[Matrix.MSCALE_X]
        otherVals[Matrix.MTRANS_X] = mainVals[Matrix.MTRANS_X]
        otherVals[Matrix.MSKEW_X] = mainVals[Matrix.MSKEW_X]
        otherMatrix.setValues(otherVals)
        otherChart.viewPortHandler.refresh(otherMatrix, otherChart, true)
    }

    inner class UpbitCandleHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                val flag = bundle.getInt("unitFlag")
                val candles = bundle.getSerializable("candles") as ArrayList<Candle>
                val priceEntries = ArrayList<CandleEntry>()
                val transactionEntries = ArrayList<BarEntry>()
                val barColor = ArrayList<Int>()
                val average5Entries = ArrayList<Entry>()
                val average10Entries = ArrayList<Entry>()
                val average20Entries = ArrayList<Entry>()
                val average60Entries = ArrayList<Entry>()
                val average120Entries = ArrayList<Entry>()
                var count: Int = 0
                var average5: Float = 0.0f
                var average10: Float = 0.0f
                var average20: Float = 0.0f
                var average60: Float = 0.0f
                var average120: Float = 0.0f
                val tranAverage5Entries = ArrayList<Entry>()
                val tranAverage10Entries = ArrayList<Entry>()
                val tranAverage20Entries = ArrayList<Entry>()
                var tranAverage5: Float = 0.0f
                var tranAverage10: Float = 0.0f
                var tranAverage20: Float = 0.0f
                for (candle in candles) {
                    // 캔들 차트(가격 차트) entry 생성
                    priceEntries.add(
                        CandleEntry(
                            candle.createdAt.toFloat(),
                            candle.shadowHigh,
                            candle.shadowLow,
                            candle.open,
                            candle.close
                        )
                    )
                    // 막대 차트(거래량 차트) entry 생성
                    transactionEntries.add(
                        BarEntry(
                            candle.createdAt.toFloat(),
                            candle.totalTradeVolume
                        )
                    )
                    if (candle.close >= candle.open) {
                        barColor.add(Color.rgb(200, 74, 49))
                    } else {
                        barColor.add(Color.rgb(18, 98, 197))
                    }

                    count++
                    average5 += candle.close
                    average10 += candle.close
                    average20 += candle.close
                    average60 += candle.close
                    average120 += candle.close
                    tranAverage5 += candle.totalTradeVolume
                    tranAverage10 += candle.totalTradeVolume
                    tranAverage20 += candle.totalTradeVolume
                    val now = candles.indexOf(candle)
                    if (count >= 120) {
                        average5Entries.add(Entry(candle.createdAt.toFloat(), average5 / 5.0f))
                        average10Entries.add(Entry(candle.createdAt.toFloat(), average10 / 10.0f))
                        average20Entries.add(Entry(candle.createdAt.toFloat(), average20 / 20.0f))
                        average60Entries.add(Entry(candle.createdAt.toFloat(), average60 / 60.0f))
                        average120Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                average120 / 120.0f
                            )
                        )
                        average5 -= candles[now - 4].close
                        average10 -= candles[now - 9].close
                        average20 -= candles[now - 19].close
                        average60 -= candles[now - 59].close
                        average120 -= candles[now - 119].close

                        tranAverage5Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage5 / 5.0f
                            )
                        )
                        tranAverage10Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage10 / 10.0f
                            )
                        )
                        tranAverage20Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage20 / 20.0f
                            )
                        )
                        tranAverage5 -= candles[now - 4].totalTradeVolume
                        tranAverage10 -= candles[now - 9].totalTradeVolume
                        tranAverage20 -= candles[now - 19].totalTradeVolume
                    } else if (count >= 60) {
                        average5Entries.add(Entry(candle.createdAt.toFloat(), average5 / 5.0f))
                        average10Entries.add(Entry(candle.createdAt.toFloat(), average10 / 10.0f))
                        average20Entries.add(Entry(candle.createdAt.toFloat(), average20 / 20.0f))
                        average60Entries.add(Entry(candle.createdAt.toFloat(), average60 / 60.0f))
                        average5 -= candles[now - 4].close
                        average10 -= candles[now - 9].close
                        average20 -= candles[now - 19].close
                        average60 -= candles[now - 59].close

                        tranAverage5Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage5 / 5.0f
                            )
                        )
                        tranAverage10Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage10 / 10.0f
                            )
                        )
                        tranAverage20Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage20 / 20.0f
                            )
                        )
                        tranAverage5 -= candles[now - 4].totalTradeVolume
                        tranAverage10 -= candles[now - 9].totalTradeVolume
                        tranAverage20 -= candles[now - 19].totalTradeVolume
                    } else if (count >= 20) {
                        average5Entries.add(Entry(candle.createdAt.toFloat(), average5 / 5.0f))
                        average10Entries.add(Entry(candle.createdAt.toFloat(), average10 / 10.0f))
                        average20Entries.add(Entry(candle.createdAt.toFloat(), average20 / 20.0f))
                        average5 -= candles[now - 4].close
                        average10 -= candles[now - 9].close
                        average20 -= candles[now - 19].close

                        tranAverage5Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage5 / 5.0f
                            )
                        )
                        tranAverage10Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage10 / 10.0f
                            )
                        )
                        tranAverage20Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage20 / 20.0f
                            )
                        )
                        tranAverage5 -= candles[now - 4].totalTradeVolume
                        tranAverage10 -= candles[now - 9].totalTradeVolume
                        tranAverage20 -= candles[now - 19].totalTradeVolume
                    } else if (count >= 10) {
                        average5Entries.add(Entry(candle.createdAt.toFloat(), average5 / 5.0f))
                        average10Entries.add(Entry(candle.createdAt.toFloat(), average10 / 10.0f))
                        average5 -= candles[now - 4].close
                        average10 -= candles[now - 9].close

                        tranAverage5Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage5 / 5.0f
                            )
                        )
                        tranAverage10Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage10 / 10.0f
                            )
                        )
                        tranAverage5 -= candles[now - 4].totalTradeVolume
                        tranAverage10 -= candles[now - 9].totalTradeVolume
                    } else if (count >= 5) {
                        average5Entries.add(Entry(candle.createdAt.toFloat(), average5 / 5.0f))
                        average5 -= candles[now - 4].close

                        tranAverage5Entries.add(
                            Entry(
                                candle.createdAt.toFloat(),
                                tranAverage5 / 5.0f
                            )
                        )
                        tranAverage5 -= candles[now - 4].totalTradeVolume
                    }
                }

                val priceDataSet = CandleDataSet(priceEntries, "").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    // 심지 부분 설정
                    shadowColor = Color.LTGRAY
                    shadowWidth = 0.7F
                    // 음봉
                    decreasingColor = Color.rgb(18, 98, 197)
                    decreasingPaintStyle = Paint.Style.FILL
                    // 양봉
                    increasingColor = Color.rgb(200, 74, 49)
                    increasingPaintStyle = Paint.Style.FILL

                    neutralColor = Color.rgb(6, 18, 34)
                    setDrawValues(false)
                    // 터치시 노란 선 제거
                    highLightColor = Color.TRANSPARENT
                }
                val transactionDataSet = BarDataSet(transactionEntries, "").apply {
                    colors = barColor
                    setDrawValues(false)
                    highLightColor = Color.TRANSPARENT
                }
                val average5DataSet = LineDataSet(average5Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(219, 17, 179)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val average10DataSet = LineDataSet(average10Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(11, 41, 175)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val average20DataSet = LineDataSet(average20Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(234, 153, 1)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val average60DataSet = LineDataSet(average60Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(253, 52, 0)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val average120DataSet = LineDataSet(average120Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(170, 170, 170)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val tranAverage5DataSet = LineDataSet(tranAverage5Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(219, 17, 179)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val tranAverage10DataSet = LineDataSet(tranAverage10Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(11, 41, 175)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                val tranAverage20DataSet = LineDataSet(tranAverage20Entries, "").apply {
                    setDrawCircles(false)
                    color = Color.rgb(234, 153, 1)
                    highLightColor = Color.TRANSPARENT
                    valueTextSize = 0f
                    lineWidth = 1.0f
                }
                binding.priceChart.apply {
                    val combinedData = CombinedData()
                    combinedData.setData(CandleData(priceDataSet))
                    val lineData = LineData()
                    lineData.addDataSet(average5DataSet)
                    lineData.addDataSet(average10DataSet)
                    lineData.addDataSet(average20DataSet)
                    lineData.addDataSet(average60DataSet)
                    lineData.addDataSet(average120DataSet)
                    combinedData.setData(lineData)
                    this.data = combinedData
                    invalidate()
                }
                binding.transactionChart.apply {
                    val combinedData = CombinedData()
                    combinedData.setData(BarData(transactionDataSet))
                    val lineData = LineData()
                    lineData.addDataSet(tranAverage5DataSet)
                    lineData.addDataSet(tranAverage10DataSet)
                    lineData.addDataSet(tranAverage20DataSet)
                    combinedData.setData(lineData)
                    this.data = combinedData
                    invalidate()
                }
            }
        }
    }

    inner class UpbitCandleThread : Thread() {

        var stopFlag = false

        override fun run() {
            while (!stopFlag) {
                val message = upbitCandleHandler.obtainMessage()
                val bundle: Bundle = Bundle()

                when (unitFlag) {
                    UNIT_MIN_1 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 1)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_3 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 3)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_5 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 5)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_10 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 10)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_30 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 30)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_60 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 60)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MIN_240 -> {
                        val candles =
                            upbitAPICaller.getCandleMinute(myViewModel.selectedCoin.value!!, 240)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_DAY -> {
                        val candles =
                            upbitAPICaller.getCandleDay(myViewModel.selectedCoin.value!!)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_WEEK -> {
                        val candles =
                            upbitAPICaller.getCandleWeek(myViewModel.selectedCoin.value!!)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                    UNIT_MONTH -> {
                        val candles =
                            upbitAPICaller.getCandleMonth(myViewModel.selectedCoin.value!!)
                        bundle.putInt("unitFlag", unitFlag)
                        bundle.putSerializable("candles", candles)
                    }
                }

                message.data = bundle
                upbitCandleHandler.sendMessage(message)
                sleep(300)
            }
        }


        fun threadStop(flag: Boolean) {
            this.stopFlag = flag
        }
    }
}