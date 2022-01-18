package com.mobit.mobit.renew.common.util

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*

object ConvertUtil {

    fun PixelToDp(context: Context, pixel: Int): Int {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val dp: Float = pixel / (metrics.densityDpi / 160f)
        return dp.toInt()
    }

    fun DpToPixel(context: Context, dp: Int): Int {
        val pixel: Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), context.resources.displayMetrics
        )
        return pixel.toInt()
    }

    fun diffOfDate(begin: String, end: String, format: String): Long {
        val formatter = SimpleDateFormat(format)

        val beginDate: Date = formatter.parse(begin)
        val endDate: Date = formatter.parse(end)

        val diff = endDate.time - beginDate.time
        val diffDays = diff / (24 * 60 * 60 * 1000)

        return diffDays
    }

    fun todayFormat(format: String): String {
        val formatter = SimpleDateFormat(format)

        val today = Date()
        val strToday = formatter.format(today)

        return strToday
    }

}