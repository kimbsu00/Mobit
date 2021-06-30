package com.mobit.mobit

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.mobit.mobit.databinding.ActivityPopupResetConfirmBinding

class PopupResetConfirmActivity : Activity() {

    lateinit var binding: ActivityPopupResetConfirmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupResetConfirmBinding.inflate(layoutInflater)
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