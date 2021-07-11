package com.mobit.mobit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import com.mobit.mobit.data.MainIndicator
import com.mobit.mobit.databinding.ActivityPopupMainIndicatorSettingBinding

class PopupMainIndicatorSettingActivity : Activity() {

    lateinit var binding: ActivityPopupMainIndicatorSettingBinding

    var mainIndicatorType: Int = MainIndicator.MOVING_AVERAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupMainIndicatorSettingBinding.inflate(layoutInflater)
        requestWindowFeature (Window.FEATURE_NO_TITLE);
        setContentView(binding.root)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        init()
    }

    fun init() {
        mainIndicatorType = intent.getIntExtra("mainIndicatorType", MainIndicator.MOVING_AVERAGE)

        binding.apply {
            when (mainIndicatorType) {
                MainIndicator.MOVING_AVERAGE -> {
                    val _MA1: Int = intent.getIntExtra("MA1", 5)
                    val _MA2: Int = intent.getIntExtra("MA2", 10)
                    val _MA3: Int = intent.getIntExtra("MA3", 20)
                    val _MA4: Int = intent.getIntExtra("MA4", 60)
                    val _MA5: Int = intent.getIntExtra("MA5", 120)
                    MA1.setText(_MA1.toString())
                    MA2.setText(_MA2.toString())
                    MA3.setText(_MA3.toString())
                    MA4.setText(_MA4.toString())
                    MA5.setText(_MA5.toString())
                    movingAverageLayout.visibility = View.VISIBLE
                }
                MainIndicator.BOLLINGER_BANDS -> {
                    val _BB1: Int = intent.getIntExtra("BB1", 20)
                    val _BB2: Float = intent.getFloatExtra("BB2", 2.0f)
                    BB1.setText(_BB1.toString())
                    BB2.setText(_BB2.toString())
                    bollingerBandsLayout.visibility = View.VISIBLE
                }
                MainIndicator.DAILY_BALANCE_TABLE -> {
                    val _DBT1: Int = intent.getIntExtra("DBT1", 9)
                    val _DBT2: Int = intent.getIntExtra("DBT2", 26)
                    val _DBT3: Int = intent.getIntExtra("DBT3", 26)
                    val _DBT4: Int = intent.getIntExtra("DBT4", 26)
                    val _DBT5: Int = intent.getIntExtra("DBT5", 52)
                    DBT1.setText(_DBT1.toString())
                    DBT2.setText(_DBT2.toString())
                    DBT3.setText(_DBT3.toString())
                    DBT4.setText(_DBT4.toString())
                    DBT5.setText(_DBT5.toString())
                    dailyBalanceTableLayout.visibility = View.VISIBLE
                }
                MainIndicator.ENVELOPES -> {
                    val _ENV1: Int = intent.getIntExtra("ENV1", 20)
                    val _ENV2: Int = intent.getIntExtra("ENV2", 6)
                    ENV1.setText(_ENV1.toString())
                    ENV2.setText(_ENV2.toString())
                    envelopesLayout.visibility = View.VISIBLE
                }
                MainIndicator.PRICE_CHANNELS -> {
                    val _PC: Int = intent.getIntExtra("PC", 5)
                    PC.setText(_PC.toString())
                    priceChannelsLayout.visibility = View.VISIBLE
                }
            }

            resetBtn.setOnClickListener {
                MA1.setText("5")
                MA2.setText("10")
                MA3.setText("20")
                MA4.setText("60")
                MA5.setText("120")

                BB1.setText("20")
                BB2.setText("2.0")

                DBT1.setText("9")
                DBT2.setText("26")
                DBT3.setText("26")
                DBT4.setText("26")
                DBT5.setText("52")

                ENV1.setText("20")
                ENV2.setText("6")

                PC.setText("5")
            }
            cancelBtn.setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            confirmBtn.setOnClickListener {
                val intent: Intent = Intent()
                intent.putExtra("mainIndicatorType", mainIndicatorType)
                when (mainIndicatorType) {
                    MainIndicator.MOVING_AVERAGE -> {
                        var MA1: Int = MA1.text.toString().toInt()
                        if (MA1 <= 0) MA1 = 5
                        var MA2: Int = MA2.text.toString().toInt()
                        if (MA2 <= 0) MA2 = 10
                        var MA3: Int = MA3.text.toString().toInt()
                        if (MA3 <= 0) MA3 = 20
                        var MA4: Int = MA4.text.toString().toInt()
                        if (MA4 <= 0) MA4 = 60
                        var MA5: Int = MA5.text.toString().toInt()
                        if (MA5 <= 0) MA5 = 120
                        intent.putExtra("MA1", MA1)
                        intent.putExtra("MA2", MA2)
                        intent.putExtra("MA3", MA3)
                        intent.putExtra("MA4", MA4)
                        intent.putExtra("MA5", MA5)
                    }
                    MainIndicator.BOLLINGER_BANDS -> {
                        var BB1: Int = BB1.text.toString().toInt()
                        if (BB1 <= 0) BB1 = 20
                        var BB2: Float = BB2.text.toString().toFloat()
                        if (BB2 <= 0 || BB2 > 100) BB2 = 2.0f
                        intent.putExtra("BB1", BB1)
                        intent.putExtra("BB2", BB2)
                    }
                    MainIndicator.DAILY_BALANCE_TABLE -> {
                        var DBT1: Int = DBT1.text.toString().toInt()
                        if (DBT1 <= 0) DBT1 = 9
                        var DBT2: Int = DBT2.text.toString().toInt()
                        if (DBT2 <= 0) DBT2 = 26
                        var DBT3: Int = DBT3.text.toString().toInt()
                        if (DBT3 <= 0) DBT3 = 26
                        var DBT4: Int = DBT4.text.toString().toInt()
                        if (DBT4 <= 0) DBT4 = 26
                        var DBT5: Int = DBT5.text.toString().toInt()
                        if (DBT5 <= 0) DBT5 = 52
                        intent.putExtra("DBT1", DBT1)
                        intent.putExtra("DBT2", DBT2)
                        intent.putExtra("DBT3", DBT3)
                        intent.putExtra("DBT4", DBT4)
                        intent.putExtra("DBT5", DBT5)
                    }
                    MainIndicator.ENVELOPES -> {
                        var ENV1: Int = ENV1.text.toString().toInt()
                        if (ENV1 <= 0) ENV1 = 20
                        var ENV2: Int = ENV2.text.toString().toInt()
                        if (ENV2 <= 0 || ENV2 > 100) ENV2 = 6
                        intent.putExtra("ENV1", ENV1)
                        intent.putExtra("ENV2", ENV2)
                    }
                    MainIndicator.PRICE_CHANNELS -> {
                        var PC: Int = PC.text.toString().toInt()
                        if (PC <= 0) PC = 5
                        intent.putExtra("PC", PC)
                    }
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {

    }
}