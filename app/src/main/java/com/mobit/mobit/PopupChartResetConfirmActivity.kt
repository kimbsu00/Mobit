package com.mobit.mobit

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.mobit.mobit.databinding.ActivityPopupChartResetConfirmBinding

class PopupChartResetConfirmActivity : Activity() {

    lateinit var binding: ActivityPopupChartResetConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupChartResetConfirmBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.root)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.apply {
            cancelBtn.setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }

            confirmBtn.setOnClickListener {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onBackPressed() {

    }
}