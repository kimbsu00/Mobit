package com.mobit.android.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.mobit.android.databinding.ActivityPopupBuySellBinding
import java.math.RoundingMode
import java.text.DecimalFormat

class PopupBuySellActivity : Activity() {

    lateinit var binding: ActivityPopupBuySellBinding
    val intFormatter = DecimalFormat("###,###").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter8 = DecimalFormat("###,###.########").apply {
        this.roundingMode = RoundingMode.DOWN
    }

    var type: Int? = null
    var code: String? = null
    var name: String? = null
    var unitPrice: Double? = null
    var count: Double? = null
    var totalPrice: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBuySellBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.root)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 1-> 매수
        // 2-> 매도
        type = intent.getIntExtra("type", 1)
        code = intent.getStringExtra("code")!!
        name = intent.getStringExtra("name")!!
        unitPrice = intent.getDoubleExtra("unitPrice", 0.0)
        count = intent.getDoubleExtra("count", 0.0)
        totalPrice = unitPrice!! * count!!

        binding.apply {
            when (type) {
                1 -> {
                    title.setTextColor(Color.parseColor("#c34c34"))
                    confirmBtn.setTextColor(Color.parseColor("#c34c34"))
                    title.text = "매수주문 확인"
                    confirmBtn.text = "매수확인"
                }
                2 -> {
                    title.setTextColor(Color.parseColor("#1262c5"))
                    confirmBtn.setTextColor(Color.parseColor("#1262c5"))
                    title.text = "매도주문 확인"
                    confirmBtn.text = "매도확인"
                }
            }
            coin.text = "$name(${code!!.split('-')[1]}/KRW)"
            orderPrice.text = intFormatter.format(unitPrice)
            orderCount.text = doubleFormatter8.format(count)
            orderTotalPrice.text = intFormatter.format(totalPrice)

            confirmBtn.setOnClickListener {
                val intent: Intent = Intent()
                intent.putExtra("code", code)
                intent.putExtra("name", name)
                intent.putExtra("unitPrice", unitPrice)
                intent.putExtra("count", count)
                setResult(RESULT_OK, intent)
                Log.i("PopupBuySellActivity", "RESULT_OK")
                finish()
            }
            cancelBtn.setOnClickListener {
                setResult(RESULT_CANCELED)
                Log.i("PopupBuySellActivity", "RESULT_CANCELED")
                finish()
            }
        }
    }
}