package com.mobit.mobit

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog

class MyProgressBar {
    private var progressDialog: AppCompatDialog? = null

    fun progressON(activity: Activity?, message: String?) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressSET(message)
        } else {
            progressDialog = AppCompatDialog(activity)
            progressDialog!!.setCancelable(false)
            progressDialog!!.window
                ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog!!.setContentView(R.layout.progress_loading)
            progressDialog!!.show()
        }
        val tv_progress_message =
            progressDialog!!.findViewById<View>(R.id.tv_progress_message) as TextView?
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message!!.text = message
        }
    }

    fun progressSET(message: String?) {
        if (progressDialog == null || !progressDialog!!.isShowing) {
            return
        }
        val tv_progress_message =
            progressDialog!!.findViewById<View>(R.id.tv_progress_message) as TextView?
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message!!.text = message
        }
    }

    fun progressOFF() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }
}