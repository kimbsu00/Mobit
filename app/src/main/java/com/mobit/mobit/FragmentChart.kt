package com.mobit.mobit

import android.app.Activity
import android.content.Intent
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.mobit.mobit.data.MainIndicator
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentChartBinding
import com.mobit.mobit.network.UpbitAPICaller
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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

    lateinit var getContent: ActivityResultLauncher<Intent>

    // UI 변수 시작
    lateinit var binding: FragmentChartBinding
    var minuteBtnFlag: Boolean = true
    // UI 변수 끝

    // 차트 관련 변수 시작
    var unitFlag: Int = UNIT_MIN_1
    var unitMinFlag: Int = UNIT_MIN_1
    val priceChartLegendList: ArrayList<LegendEntry> = ArrayList()
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

        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val mainIndicatorType: Int =
                        it.data!!.getIntExtra("mainIndicatorType", -1)
                    val mainIndicator: MainIndicator =
                        it.data!!.getSerializableExtra("mainIndicator") as MainIndicator
                    if (mainIndicatorType != -1) {
                        myViewModel.setMainIndicatorType(mainIndicatorType)
                        myViewModel.setMainIndicator(mainIndicator)

                        val thread: Thread = object : Thread() {
                            override fun run() {
                                myViewModel.myDBHelper!!.setMainIndicatorType(mainIndicatorType)
                                myViewModel.myDBHelper!!.setMainIndicator(mainIndicator)
                            }
                        }
                        thread.start()
                    }
                }
            }
        }

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
            transactionChart.legend.isEnabled = true
            setChartLegend(myViewModel.mainIndicatorType.value!!)

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
            val average5Legend = LegendEntry()
            average5Legend.label = "5"
            average5Legend.formColor = Color.rgb(219, 17, 179)
            val average10Legend = LegendEntry()
            average10Legend.label = "10"
            average10Legend.formColor = Color.rgb(11, 41, 175)
            val average20Legend = LegendEntry()
            average20Legend.label = "20"
            average20Legend.formColor = Color.rgb(234, 153, 1)
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

            mainIndicatorSettingBtn.setOnClickListener {
                val intent: Intent = Intent(context, MainIndicatorSettingActivity::class.java)
                intent.putExtra("mainIndicatorType", myViewModel.mainIndicatorType.value!!)
                intent.putExtra("mainIndicator", myViewModel.mainIndicator.value!!)
                getContent.launch(intent)
            }
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

    fun setChartLegend(mainIndicatorType: Int) {
        val legendList: List<LegendEntry> = when (mainIndicatorType) {
            MainIndicator.MOVING_AVERAGE -> {
                binding.priceChart.legend.isEnabled = true
                val movingAverageLegend = LegendEntry()
                movingAverageLegend.label = "단순 MA"
                movingAverageLegend.form = Legend.LegendForm.NONE
                val averageN1Legend = LegendEntry()
                averageN1Legend.label = myViewModel.mainIndicator.value!!.MA_N1.toString()
                averageN1Legend.formColor = Color.rgb(219, 17, 179)
                val averageN2Legend = LegendEntry()
                averageN2Legend.label = myViewModel.mainIndicator.value!!.MA_N2.toString()
                averageN2Legend.formColor = Color.rgb(11, 41, 175)
                val averageN3Legend = LegendEntry()
                averageN3Legend.label = myViewModel.mainIndicator.value!!.MA_N3.toString()
                averageN3Legend.formColor = Color.rgb(234, 153, 1)
                val averageN4Legend = LegendEntry()
                averageN4Legend.label = myViewModel.mainIndicator.value!!.MA_N4.toString()
                averageN4Legend.formColor = Color.rgb(253, 52, 0)
                val averageN5Legend = LegendEntry()
                averageN5Legend.label = myViewModel.mainIndicator.value!!.MA_N5.toString()
                averageN5Legend.formColor = Color.rgb(170, 170, 170)
                listOf(
                    movingAverageLegend,
                    averageN1Legend,
                    averageN2Legend,
                    averageN3Legend,
                    averageN4Legend,
                    averageN5Legend
                )
            }
            MainIndicator.BOLLINGER_BANDS -> {
                binding.priceChart.legend.isEnabled = true
                val upperLegend = LegendEntry()
                upperLegend.label = "Upper"
                upperLegend.formColor = Color.rgb(50, 51, 255)
                val middleLegend = LegendEntry()
                middleLegend.label = "Middle"
                middleLegend.formColor = Color.rgb(53, 153, 101)
                val lowerLegend = LegendEntry()
                lowerLegend.label = "Lower"
                lowerLegend.formColor = Color.rgb(255, 204, 1)
                listOf(upperLegend, middleLegend, lowerLegend)
            }
            MainIndicator.DAILY_BALANCE_TABLE -> {
                binding.priceChart.legend.isEnabled = true
                val transitionLegend = LegendEntry()
                transitionLegend.label = "전환 ${myViewModel.mainIndicator.value!!.DBT_1}"
                transitionLegend.formColor = Color.rgb(10, 154, 131)
                val baselineLegend = LegendEntry()
                baselineLegend.label = "기준 ${myViewModel.mainIndicator.value!!.DBT_2}"
                baselineLegend.formColor = Color.rgb(120, 120, 120)
                val trailingLegend = LegendEntry()
                trailingLegend.label = "후행 ${myViewModel.mainIndicator.value!!.DBT_3}"
                trailingLegend.formColor = Color.rgb(167, 32, 223)
                val leadingSpan1Legend = LegendEntry()
                leadingSpan1Legend.label = "선행1 ${myViewModel.mainIndicator.value!!.DBT_4}"
                leadingSpan1Legend.formColor = Color.rgb(247, 162, 107)
                val leadingSpan2Legend = LegendEntry()
                leadingSpan2Legend.label = "선행2 ${myViewModel.mainIndicator.value!!.DBT_5}"
                leadingSpan2Legend.formColor = Color.rgb(105, 140, 240)
                listOf(
                    transitionLegend,
                    baselineLegend,
                    trailingLegend,
                    leadingSpan1Legend,
                    leadingSpan2Legend
                )
            }
            MainIndicator.PIVOT -> {
                binding.priceChart.legend.isEnabled = true
                val resistanceLine2Legend = LegendEntry()
                resistanceLine2Legend.label = "저항2"
                resistanceLine2Legend.formColor = Color.rgb(244, 169, 225)
                val resistanceLine1Legend = LegendEntry()
                resistanceLine1Legend.label = "저항1"
                resistanceLine1Legend.formColor = Color.rgb(166, 162, 247)
                val pivotBaselineLegend = LegendEntry()
                pivotBaselineLegend.label = "피봇"
                pivotBaselineLegend.formColor = Color.rgb(157, 222, 242)
                val supportLine1Legend = LegendEntry()
                supportLine1Legend.label = "지지1"
                supportLine1Legend.formColor = Color.rgb(162, 236, 175)
                val supportLine2Legend = LegendEntry()
                supportLine2Legend.label = "지지2"
                supportLine2Legend.formColor = Color.rgb(254, 187, 160)
                listOf(
                    resistanceLine2Legend,
                    resistanceLine1Legend,
                    pivotBaselineLegend,
                    supportLine1Legend,
                    supportLine2Legend
                )
            }
            MainIndicator.ENVELOPES -> {
                binding.priceChart.legend.isEnabled = true
                val upperLimitLegend = LegendEntry()
                upperLimitLegend.label = "상한선"
                upperLimitLegend.formColor = Color.rgb(101, 204, 51)
                val baselineLegend = LegendEntry()
                baselineLegend.label = "중심선"
                baselineLegend.formColor = Color.rgb(51, 50, 203)
                val lowerBoundLegend = LegendEntry()
                lowerBoundLegend.label = "하한선"
                lowerBoundLegend.formColor = Color.rgb(255, 51, 156)
                val envelopesLegend = LegendEntry()
                envelopesLegend.label =
                    "${myViewModel.mainIndicator.value!!.ENV_N}, ${myViewModel.mainIndicator.value!!.ENV_K}"
                envelopesLegend.form = Legend.LegendForm.NONE
                listOf(upperLimitLegend, baselineLegend, lowerBoundLegend, envelopesLegend)
            }
            MainIndicator.PRICE_CHANNELS -> {
                binding.priceChart.legend.isEnabled = true
                val priceChannelsLegend = LegendEntry()
                priceChannelsLegend.label = "PC ${myViewModel.mainIndicator.value!!.PC_N}"
                priceChannelsLegend.form = Legend.LegendForm.NONE
                val upperLimitLegend = LegendEntry()
                upperLimitLegend.label = "상한선"
                upperLimitLegend.formColor = Color.rgb(85, 197, 99)
                val baselineLegend = LegendEntry()
                baselineLegend.label = "중심선"
                baselineLegend.formColor = Color.rgb(204, 204, 204)
                val lowerBoundLegend = LegendEntry()
                lowerBoundLegend.label = "하한선"
                lowerBoundLegend.formColor = Color.rgb(85, 197, 99)
                listOf(priceChannelsLegend, upperLimitLegend, baselineLegend, lowerBoundLegend)
            }
            else -> {
                binding.priceChart.legend.isEnabled = false
                Log.e("FragmentChart", "mainIndicator is $mainIndicatorType in setChartLegend()")
                listOf()
            }
        }
        if (legendList.isEmpty()) {
            Log.e("FragmentChart", "legendList is empty in setChartLegend()")
            return
        }

        priceChartLegendList.clear()
        priceChartLegendList.addAll(legendList)
//        val mEntries = legendList.toTypedArray()
//        binding.priceChart.legend.apply {
//            setCustom(mEntries)
//
//            Log.i("FragmentChart", mEntries.size.toString())
//            for (entry in mEntries) {
//                Log.i("FragmentChart", entry.label)
//            }
//
//            textColor = Color.WHITE
//            verticalAlignment = Legend.LegendVerticalAlignment.TOP
//            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//            orientation = Legend.LegendOrientation.HORIZONTAL
//            setDrawInside(true)
//        }
    }

    fun getMovingAverage(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val N1: Int = myViewModel.mainIndicator.value!!.MA_N1
        val N2: Int = myViewModel.mainIndicator.value!!.MA_N2
        val N3: Int = myViewModel.mainIndicator.value!!.MA_N3
        val N4: Int = myViewModel.mainIndicator.value!!.MA_N4
        val N5: Int = myViewModel.mainIndicator.value!!.MA_N5
        val averageN1Entries = ArrayList<Entry>()
        val averageN2Entries = ArrayList<Entry>()
        val averageN3Entries = ArrayList<Entry>()
        val averageN4Entries = ArrayList<Entry>()
        val averageN5Entries = ArrayList<Entry>()
        var count: Int = 0
        var sumN1: Float = 0.0f
        var sumN2: Float = 0.0f
        var sumN3: Float = 0.0f
        var sumN4: Float = 0.0f
        var sumN5: Float = 0.0f
        for (candle in candles) {
            count++
            sumN1 += candle.close
            sumN2 += candle.close
            sumN3 += candle.close
            sumN4 += candle.close
            sumN5 += candle.close
            val now = candles.indexOf(candle)
            if (count >= N5) {
                averageN5Entries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        sumN5 / N5.toFloat()
                    )
                )
                sumN5 -= candles[now - (N5 - 1)].close
            }
            if (count >= N4) {
                averageN4Entries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        sumN4 / N4.toFloat()
                    )
                )
                sumN4 -= candles[now - (N4 - 1)].close
            }
            if (count >= N3) {
                averageN3Entries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        sumN3 / N3.toFloat()
                    )
                )
                sumN3 -= candles[now - (N3 - 1)].close
            }
            if (count >= N2) {
                averageN2Entries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        sumN2 / N2.toFloat()
                    )
                )
                sumN2 -= candles[now - (N2 - 1)].close
            }
            if (count >= N1) {
                averageN1Entries.add(Entry(candle.createdAt.toFloat(), sumN1 / N1.toFloat()))
                sumN1 -= candles[now - (N1 - 1)].close
            }
        }

        val averageN1DataSet = LineDataSet(averageN1Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(219, 17, 179)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val averageN2DataSet = LineDataSet(averageN2Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(11, 41, 175)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val averageN3DataSet = LineDataSet(averageN3Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(234, 153, 1)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val averageN4DataSet = LineDataSet(averageN4Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(253, 52, 0)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val averageN5DataSet = LineDataSet(averageN5Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(170, 170, 170)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(averageN1DataSet)
        ret.addDataSet(averageN2DataSet)
        ret.addDataSet(averageN3DataSet)
        ret.addDataSet(averageN4DataSet)
        ret.addDataSet(averageN5DataSet)
        return ret
    }

    fun getBollingerBands(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val N: Int = myViewModel.mainIndicator.value!!.BB_N
        val K: Float = myViewModel.mainIndicator.value!!.BB_K
        val baselineEntries = ArrayList<Entry>()
        val upperLimitEntries = ArrayList<Entry>()
        val lowerBoundEntries = ArrayList<Entry>()
        var count: Int = 0
        // N일 동안의 평균값
        var sum: Float = 0.0f
        // N일 동안의 (E[X])^2
        var expValue: Float = 0.0f
        // N일 동안의 E[X^2]
        var expValue2: Float = 0.0f
        for (candle in candles) {
            count++
            sum += candle.close
            expValue += candle.close * 0.05f
            expValue2 += candle.close * candle.close * 0.05f
            val now = candles.indexOf(candle)
            if (count >= N) {
                val baselineValue: Float = sum / N.toFloat()
                baselineEntries.add(Entry(candle.createdAt.toFloat(), baselineValue))
                val sigma: Float = sqrt(expValue2 - (expValue * expValue))
                upperLimitEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        baselineValue + sigma * K
                    )
                )
                lowerBoundEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        baselineValue - sigma * K
                    )
                )

                sum -= candles[now - (N - 1)].close
                expValue -= candles[now - (N - 1)].close * 0.05f
                expValue2 -= candles[now - (N - 1)].close * candles[now - (N - 1)].close * 0.05f
            }
        }

        val baselineDataSet = LineDataSet(baselineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(53, 153, 101)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val upperLimitDataSet = LineDataSet(upperLimitEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(50, 51, 255)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val lowerBoundDataSet = LineDataSet(lowerBoundEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(255, 204, 1)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(baselineDataSet)
        ret.addDataSet(upperLimitDataSet)
        ret.addDataSet(lowerBoundDataSet)
        return ret
    }

    fun getDailyBalanceTable(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val DBT_1: Int = myViewModel.mainIndicator.value!!.DBT_1
        val DBT_2: Int = myViewModel.mainIndicator.value!!.DBT_2
        val DBT_3: Int = myViewModel.mainIndicator.value!!.DBT_3
        val DBT_4: Int = myViewModel.mainIndicator.value!!.DBT_4
        val DBT_5: Int = myViewModel.mainIndicator.value!!.DBT_5
        // 전환선
        val transitionLineEntries = ArrayList<Entry>()
        // 기준선
        val baselineEntries = ArrayList<Entry>()
        // 후행스팬
        val trailingSpanEntries = ArrayList<Entry>()
        // 선행스팬1
        val leadingSpan1Entries = ArrayList<Entry>()
        // 선행스팬2
        val leadingSpan2Entries = ArrayList<Entry>()
        var count: Int = 0
        var maxDuring1: Float = 0.0f
        var minDuring1: Float = 987654321.0f
        var maxDuring2: Float = 0.0f
        var minDuring2: Float = 987654321.0f
        var maxDuring5: Float = 0.0f
        var minDuring5: Float = 987654321.0f
        for (candle in candles) {
            count++
            maxDuring1 = 0.0f
            minDuring1 = 0987654321.0f
            maxDuring2 = 0.0f
            minDuring2 = 0987654321.0f
            maxDuring5 = 0.0f
            minDuring5 = 0987654321.0f
            val now = candles.indexOf(candle)
            if (count >= DBT_1) {
                for (i in 0..(DBT_1 - 1)) {
                    maxDuring1 = max(maxDuring1, candles[now - i].shadowHigh)
                    minDuring1 = min(minDuring1, candles[now - i].shadowLow)
                }
                val transitionValue: Float = (maxDuring1 + minDuring1) / 2.0f
                transitionLineEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        transitionValue
                    )
                )
            }
            if (count >= DBT_2) {
                for (i in 0..(DBT_2 - 1)) {
                    maxDuring2 = max(maxDuring2, candles[now - i].shadowHigh)
                    minDuring2 = min(minDuring2, candles[now - i].shadowLow)
                }
                val baselineValue: Float = (maxDuring2 + minDuring2) / 2.0f
                baselineEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        baselineValue
                    )
                )
            }
            if (count >= DBT_3) {
                trailingSpanEntries.add(
                    Entry(
                        candle.createdAt.toFloat() - DBT_3.toFloat(),
                        candle.close
                    )
                )
            }
            if (count >= DBT_4) {
                val transitionValue: Float = (maxDuring1 + minDuring1) / 2.0f
                val baselineValue: Float = (maxDuring2 + minDuring2) / 2.0f
                leadingSpan1Entries.add(
                    Entry(
                        candle.createdAt.toFloat() + 26.0f,
                        (transitionValue + baselineValue) / 2.0f
                    )
                )
            }
            if (count >= DBT_5) {
                for (i in 0..(DBT_5 - 1)) {
                    maxDuring5 = max(maxDuring5, candles[now - i].shadowHigh)
                    minDuring5 = min(minDuring5, candles[now - i].shadowLow)
                }
                leadingSpan2Entries.add(
                    Entry(
                        candle.createdAt.toFloat() + 26.0f,
                        (maxDuring5 + minDuring5) / 2.0f
                    )
                )
            }
        }

        val transitionDataSet = LineDataSet(transitionLineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(10, 154, 131)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val baselineDataSet = LineDataSet(baselineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(120, 120, 120)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val trailingSpanDataSet = LineDataSet(trailingSpanEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(167, 32, 223)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val leadingSpan1DataSet = LineDataSet(leadingSpan1Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(247, 162, 107)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val leadingSpan2DataSet = LineDataSet(leadingSpan2Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(105, 140, 240)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(transitionDataSet)
        ret.addDataSet(baselineDataSet)
        ret.addDataSet(trailingSpanDataSet)
        ret.addDataSet(leadingSpan1DataSet)
        ret.addDataSet(leadingSpan2DataSet)
        return ret
    }

    fun getPivot(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val pivotBaselineEntries = ArrayList<Entry>()
        val resistanceLine1Entries = ArrayList<Entry>()
        val resistanceLine2Entries = ArrayList<Entry>()
        val supportLine1Entries = ArrayList<Entry>()
        val supportLine2Entries = ArrayList<Entry>()
        for (candle in candles) {
            val pivotValue: Float = (candle.shadowHigh + candle.shadowLow + candle.close) / 3.0f
            pivotBaselineEntries.add(Entry(candle.createdAt.toFloat(), pivotValue))
            resistanceLine1Entries.add(
                Entry(
                    candle.createdAt.toFloat(),
                    2 * pivotValue - candle.shadowLow
                )
            )
            resistanceLine2Entries.add(
                Entry(
                    candle.createdAt.toFloat(),
                    pivotValue + candle.shadowHigh - candle.shadowLow
                )
            )
            supportLine1Entries.add(
                Entry(
                    candle.createdAt.toFloat(),
                    2 * pivotValue - candle.shadowHigh
                )
            )
            supportLine2Entries.add(
                Entry(
                    candle.createdAt.toFloat(),
                    pivotValue - candle.shadowHigh + candle.shadowLow
                )
            )
        }

        val pivotBaselineDataSet = LineDataSet(pivotBaselineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(157, 222, 242)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val resistanceLine1DataSet = LineDataSet(resistanceLine1Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(166, 162, 247)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val resistanceLine2DataSet = LineDataSet(resistanceLine2Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(244, 169, 225)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val supportLine1DataSet = LineDataSet(supportLine1Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(162, 236, 175)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val supprotLine2DataSet = LineDataSet(supportLine2Entries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(254, 187, 160)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(pivotBaselineDataSet)
        ret.addDataSet(resistanceLine1DataSet)
        ret.addDataSet(resistanceLine2DataSet)
        ret.addDataSet(supportLine1DataSet)
        ret.addDataSet(supprotLine2DataSet)
        return ret
    }

    fun getEnvelopes(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val ENV_N: Int = myViewModel.mainIndicator.value!!.ENV_N
        val ENV_K: Int = myViewModel.mainIndicator.value!!.ENV_K
        val baselineEntries = ArrayList<Entry>()
        val upperLimitEntries = ArrayList<Entry>()
        val lowerBoundEntries = ArrayList<Entry>()
        var count: Int = 0
        var sum: Float = 0.0f
        for (candle in candles) {
            count++
            sum += candle.close
            if (count >= ENV_N) {
                val average: Float = sum / ENV_N.toFloat()
                baselineEntries.add(Entry(candle.createdAt.toFloat(), average))
                upperLimitEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        average * (1 + 0.01f * ENV_K)
                    )
                )
                lowerBoundEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        average * (1 - 0.01f * ENV_K)
                    )
                )

                val now = candles.indexOf(candle)
                sum -= candles[now - (ENV_N - 1)].close
            }
        }

        val baselineDataSet = LineDataSet(baselineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(51, 50, 203)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val upperLimitDataSet = LineDataSet(upperLimitEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(101, 204, 51)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val lowerBoundDataSet = LineDataSet(lowerBoundEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(255, 51, 156)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(baselineDataSet)
        ret.addDataSet(upperLimitDataSet)
        ret.addDataSet(lowerBoundDataSet)
        return ret
    }

    fun getPriceChannels(candles: ArrayList<Candle>): LineData {
        val ret: LineData = LineData()
        val PC_N: Int = myViewModel.mainIndicator.value!!.PC_N
        val baselineEntries = ArrayList<Entry>()
        val upperLimitEntries = ArrayList<Entry>()
        val lowerBoundEntries = ArrayList<Entry>()
        var count: Int = 0
        for (candle in candles) {
            count++
            if (count >= PC_N) {
                val now = candles.indexOf(candle)
                var maxDuringN: Float = 0.0f
                var minDuringN: Float = 987654321.0f
                for (i in 0..(PC_N - 1)) {
                    maxDuringN = max(maxDuringN, candles[now - i].shadowHigh)
                    minDuringN = min(minDuringN, candles[now - 1].shadowLow)
                }
                baselineEntries.add(
                    Entry(
                        candle.createdAt.toFloat(),
                        (maxDuringN + minDuringN) / 2.0f
                    )
                )
                upperLimitEntries.add(Entry(candle.createdAt.toFloat(), maxDuringN))
                lowerBoundEntries.add(Entry(candle.createdAt.toFloat(), minDuringN))
            }
        }

        val baselineDataSet = LineDataSet(baselineEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(204, 204, 204)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val upperLimitDataSet = LineDataSet(upperLimitEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(85, 197, 99)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        val lowerBoundDataSet = LineDataSet(lowerBoundEntries, "").apply {
            setDrawCircles(false)
            color = Color.rgb(85, 197, 99)
            highLightColor = Color.TRANSPARENT
            valueTextSize = 0f
            lineWidth = 1.0f
        }
        ret.addDataSet(baselineDataSet)
        ret.addDataSet(upperLimitDataSet)
        ret.addDataSet(lowerBoundDataSet)
        return ret
    }

    fun getMainIndicator(candles: ArrayList<Candle>, type: Int): LineData {
        return when (type) {
            MainIndicator.MOVING_AVERAGE -> getMovingAverage(candles)
            MainIndicator.BOLLINGER_BANDS -> getBollingerBands(candles)
            MainIndicator.DAILY_BALANCE_TABLE -> getDailyBalanceTable(candles)
            MainIndicator.PIVOT -> getPivot(candles)
            MainIndicator.ENVELOPES -> getEnvelopes(candles)
            MainIndicator.PRICE_CHANNELS -> getPriceChannels(candles)
            else -> getMovingAverage(candles)
        }
    }

    inner class UpbitCandleHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                val flag = bundle.getInt("unitFlag")
                val candles = bundle.getSerializable("candles") as ArrayList<Candle>
                if (candles.isNotEmpty()) {
                    val priceEntries = ArrayList<CandleEntry>()
                    val transactionEntries = ArrayList<BarEntry>()
                    val barColor = ArrayList<Int>()
                    var count: Int = 0
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
                        tranAverage5 += candle.totalTradeVolume
                        tranAverage10 += candle.totalTradeVolume
                        tranAverage20 += candle.totalTradeVolume
                        val now = candles.indexOf(candle)
                        if (count >= 20) {
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
                    binding.priceChart.legend.apply {
                        setCustom(priceChartLegendList)
                        textColor = Color.WHITE
                        verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                        orientation = Legend.LegendOrientation.HORIZONTAL
                        setDrawInside(true)
                    }
                    binding.priceChart.apply {
                        val combinedData = CombinedData()
                        combinedData.setData(CandleData(priceDataSet))
                        val lineData =
                            getMainIndicator(candles, myViewModel.mainIndicatorType.value!!)
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

                setChartLegend(myViewModel.mainIndicatorType.value!!)

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