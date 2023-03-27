package com.mobit.android.common.util

import android.content.Context
import android.util.TypedValue

object ConvertUtil {

    fun dpToPx(context: Context, dp: Int): Int = dpToPx(context, dp.toFloat()).toInt()

    fun dpToPx(context: Context, dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    fun pxToDp(context: Context, px: Int): Int = pxToDp(context, px.toFloat()).toInt()

    fun pxToDp(context: Context, px: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.resources.displayMetrics)
}