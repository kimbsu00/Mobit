package com.mobit.android.feature.base.view

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobit.android.R
import com.mobit.android.common.customview.MobitProgressDialog

open class BaseActivity : AppCompatActivity() {

    private val mobitProgressDialog: MobitProgressDialog by lazy {
        MobitProgressDialog(applicationContext)
    }

    protected fun showProgress(pMsg: String = "") {
        mobitProgressDialog.setMessage(pMsg)
        mobitProgressDialog.show()
    }

    protected fun dismissProgress() {
        mobitProgressDialog.dismiss()
    }

    protected fun showToastMsg(pMsg: String) {
        Toast.makeText(this, pMsg, Toast.LENGTH_SHORT).show()
    }

    protected fun showErrorMsg() {
        Toast.makeText(
            this,
            resources.getText(R.string.toast_msg_error_001),
            Toast.LENGTH_SHORT
        ).show()
    }

}