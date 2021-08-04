package com.mobit.mobit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.mobit.mobit.databinding.ActivityPopupBuySellBinding
import java.text.DecimalFormat

class PopupBuySellActivity : Activity() {

    lateinit var binding: ActivityPopupBuySellBinding
    val formatter = DecimalFormat("###,###")
    val formatter2 = DecimalFormat("###,###.####")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBuySellBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.root)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 1-> 매수
        // 2-> 매도
        val type: Int = intent.getIntExtra("type", 1)
        val code: String = intent.getStringExtra("code")!!
        val name: String = intent.getStringExtra("name")!!
        val unitPrice: Double = intent.getDoubleExtra("unitPrice", 0.0)
        val count: Double = intent.getDoubleExtra("count", 0.0)
        val totalPrice: Double = unitPrice * count

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
            coin.text = "$name(${code.split('-')[1]}/KRW)"
            orderPrice.text = formatter.format(unitPrice)
            orderCount.text = formatter2.format(count)
            orderTotalPrice.text = formatter.format(totalPrice)

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