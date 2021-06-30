package com.mobit.mobit

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CandleStickChart
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
            priceChart.xAxis.apply {
                textColor = Color.TRANSPARENT
                position = XAxis.XAxisPosition.BOTTOM
                this.setDrawGridLines(true)
            }
            priceChart.axisLeft.apply {
                textColor = Color.WHITE
                isEnabled = false
            }
            priceChart.axisRight.apply {
                setLabelCount(7, false)
                textColor = Color.WHITE
                setDrawGridLines(true)
                setDrawAxisLine(false)
            }
            priceChart.legend.isEnabled = false

            transactionChart.description.isEnabled = false
            transactionChart.setMaxVisibleValueCount(200)
            transactionChart.setPinchZoom(false)
            transactionChart.setDrawGridBackground(false)
            transactionChart.xAxis.apply {
                textColor = Color.TRANSPARENT
                position = XAxis.XAxisPosition.BOTTOM
                this.setDrawGridLines(true)
            }
            transactionChart.axisLeft.apply {
                textColor = Color.WHITE
                isEnabled = false
            }
            transactionChart.axisRight.apply {
                setLabelCount(7, false)
                textColor = Color.WHITE
                setDrawGridLines(true)
                setDrawAxisLine(false)
            }
            transactionChart.legend.isEnabled = false

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

    fun syncCharts(mainChart: CandleStickChart, otherChart: BarChart) {
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

    fun syncCharts(mainChart: BarChart, otherChart: CandleStickChart) {
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

    inner class UpbitCandleHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val bundle: Bundle = msg.data
            if (!bundle.isEmpty) {
                val flag = bundle.getInt("unitFlag")
                val candles = bundle.getSerializable("candles") as ArrayList<Candle>
                val entries1 = ArrayList<CandleEntry>()
                val entries2 = ArrayList<BarEntry>()
                val barColor = ArrayList<Int>()
                for (candle in candles) {
                    entries1.add(
                        CandleEntry(
                            candle.createdAt.toFloat(),
                            candle.shadowHigh,
                            candle.shadowLow,
                            candle.open,
                            candle.close
                        )
                    )
                    entries2.add(BarEntry(candle.createdAt.toFloat(), candle.totalTradeVolume))
                    if (candle.close >= candle.open) {
                        barColor.add(Color.rgb(200, 74, 49))
                    } else {
                        barColor.add(Color.rgb(18, 98, 197))
                    }
                }

                val dataSet1 = CandleDataSet(entries1, "").apply {
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
                    highLightColor = Color.TRANSPARENT
                }
                val dataSet2 = BarDataSet(entries2, "").apply {
                    colors = barColor
                    setDrawValues(false)
                    highLightColor = Color.TRANSPARENT
                }
                binding.priceChart.apply {
                    this.data = CandleData(dataSet1)
                    invalidate()
                }
                binding.transactionChart.apply {
                    this.data = BarData(dataSet2)
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