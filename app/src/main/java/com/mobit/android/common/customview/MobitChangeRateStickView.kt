package com.mobit.android.common.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.mobit.android.R
import com.mobit.android.common.util.ConvertUtil
import kotlin.math.max

/**
 * 코인의 등락률을 보여주는 CustomView
 */
class MobitChangeRateStickView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    /**
     * 부호가 있는 가격 변화율
     */
    private var _signedChangeRate: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    val signedChangeRate: Float get() = _signedChangeRate

    /**
     * 시가
     */
    private var _openingPrice: Float = 100f
    val openingPrice: Float get() = _openingPrice

    /**
     * 고가
     */
    private var _highPrice: Float = 150f
    val highPrice: Float get() = _highPrice

    /**
     * 저가
     */
    private var _lowPrice: Float = 80f
    val lowPrice: Float get() = _lowPrice

    // region Resource
    @ColorInt
    private val secondaryColor: Int = ContextCompat.getColor(context, R.color.secondary)

    @ColorInt
    private val coinBlueColor: Int = ContextCompat.getColor(context, R.color.coin_blue)

    @ColorInt
    private val coinRedColor: Int = ContextCompat.getColor(context, R.color.coin_red)

    @ColorInt
    private val coinGrayColor: Int = ContextCompat.getColor(context, R.color.coin_gray)
    // endregion Resource

    // region Paint
    private val bluePaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 5f
        strokeCap = Paint.Cap.SQUARE
        color = coinBlueColor
        letterSpacing = -0.05f
    }

    private val redPaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 5f
        strokeCap = Paint.Cap.SQUARE
        color = coinRedColor
        letterSpacing = -0.05f
    }

    private val grayPaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 5f
        strokeCap = Paint.Cap.SQUARE
        color = coinGrayColor
        letterSpacing = -0.05f
    }
    // endregion Paint

    init {
        setBackgroundColor(secondaryColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ConvertUtil.dpToPx(context, 15)
        val height = max(MeasureSpec.getSize(heightMeasureSpec), ConvertUtil.dpToPx(context, 40))
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            drawStick(canvas)
        }
    }

    private fun getHighPriceRate() = (highPrice - openingPrice) / openingPrice

    private fun getLowPriceRate() = (lowPrice - openingPrice) / openingPrice

    private fun drawStick(pCanvas: Canvas) {
        val width = measuredWidth
        val height = measuredHeight

        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val unitWidth = ConvertUtil.dpToPx(context, 1f)
        val unitHeight = height * 0.01f

        val left = paddingLeft.toFloat()
        val top = halfHeight - unitHeight
        val right = width - paddingRight.toFloat()
        val bottom = halfHeight

        pCanvas.drawRect(left, top, right, bottom, grayPaint)

        val highPriceTop = bottom - halfHeight * getHighPriceRate()
        val lowPriceBottom = bottom - halfHeight * getLowPriceRate()

        val tmpChangeRate = signedChangeRate
        if (tmpChangeRate > 0) {
            val redHeight = halfHeight * tmpChangeRate / 100
            pCanvas.drawRect(left, bottom - redHeight, right, bottom, redPaint)
            pCanvas.drawRect(
                halfWidth - unitWidth,
                highPriceTop,
                halfWidth + unitWidth,
                lowPriceBottom,
                redPaint
            )
        } else if (tmpChangeRate < 0) {
            val blueHeight = halfHeight * (-tmpChangeRate) / 100
            pCanvas.drawRect(left, bottom, right, bottom + blueHeight, bluePaint)
            pCanvas.drawRect(
                halfWidth - unitWidth,
                highPriceTop,
                halfWidth + unitWidth,
                lowPriceBottom,
                bluePaint
            )
        } else {
            pCanvas.drawRect(
                halfWidth - unitWidth,
                highPriceTop,
                halfWidth + unitWidth,
                lowPriceBottom,
                grayPaint
            )
        }
    }

    /**
     * @param pSignedChangeRate     부호가 있는 가격 변화율
     * @param pOpeningPrice         시가
     * @param pHighPrice            고가
     * @param pLowPrice             저가
     */
    fun setPriceInfo(
        pSignedChangeRate: Float,
        pOpeningPrice: Float,
        pHighPrice: Float,
        pLowPrice: Float
    ) {
        _openingPrice = pOpeningPrice
        _highPrice = pHighPrice
        _lowPrice = pLowPrice
        _signedChangeRate = pSignedChangeRate
    }

}