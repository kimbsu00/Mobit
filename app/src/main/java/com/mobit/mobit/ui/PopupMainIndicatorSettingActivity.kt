package com.mobit.mobit.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import com.mobit.mobit.data.MainIndicator
import com.mobit.mobit.databinding.ActivityPopupMainIndicatorSettingBinding

class PopupMainIndicatorSettingActivity : Activity() {

    lateinit var binding: ActivityPopupMainIndicatorSettingBinding

    var mainIndicatorType: Int = MainIndicator.MOVING_AVERAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupMainIndicatorSettingBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                        var MA1_V: Int = 5
                        var MA2_V: Int = 10
                        var MA3_V: Int = 20
                        var MA4_V: Int = 60
                        var MA5_V: Int = 120
                        try {
                            MA1_V = MA1.text.toString().toInt()
                            if (MA1_V <= 0) MA1_V = 5
                            MA2_V = MA2.text.toString().toInt()
                            if (MA2_V <= 0) MA2_V = 10
                            MA3_V = MA3.text.toString().toInt()
                            if (MA3_V <= 0) MA3_V = 20
                            MA4_V = MA4.text.toString().toInt()
                            if (MA4_V <= 0) MA4_V = 60
                            MA5_V = MA5.text.toString().toInt()
                            if (MA5_V <= 0) MA5_V = 120
                        } catch (e: NumberFormatException) {
                            MA1_V = 5
                            MA2_V = 10
                            MA3_V = 20
                            MA4_V = 60
                            MA5_V = 120
                            Toast.makeText(
                                applicationContext,
                                "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        } catch (e: Exception) {
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        }
                        intent.putExtra("MA1", MA1_V)
                        intent.putExtra("MA2", MA2_V)
                        intent.putExtra("MA3", MA3_V)
                        intent.putExtra("MA4", MA4_V)
                        intent.putExtra("MA5", MA5_V)
                    }
                    MainIndicator.BOLLINGER_BANDS -> {
                        var BB1_V: Int = 20
                        var BB2_V: Float = 2.0f
                        try {
                            BB1_V = BB1.text.toString().toInt()
                            if (BB1_V <= 0) BB1_V = 20
                            BB2_V = BB2.text.toString().toFloat()
                            if (BB2_V <= 0 || BB2_V > 100) BB2_V = 2.0f
                        } catch (e: NumberFormatException) {
                            BB1_V = 20
                            BB2_V = 2.0f
                            Toast.makeText(
                                applicationContext,
                                "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        } catch (e: Exception) {
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        }
                        intent.putExtra("BB1", BB1_V)
                        intent.putExtra("BB2", BB2_V)
                    }
                    MainIndicator.DAILY_BALANCE_TABLE -> {
                        var DBT1_V: Int = 9
                        var DBT2_V: Int = 26
                        var DBT3_V: Int = 26
                        var DBT4_V: Int = 26
                        var DBT5_V: Int = 52
                        try {
                            DBT1_V = DBT1.text.toString().toInt()
                            if (DBT1_V <= 0) DBT1_V = 9
                            DBT2_V = DBT2.text.toString().toInt()
                            if (DBT2_V <= 0) DBT2_V = 26
                            DBT3_V = DBT3.text.toString().toInt()
                            if (DBT3_V <= 0) DBT3_V = 26
                            DBT4_V = DBT4.text.toString().toInt()
                            if (DBT4_V <= 0) DBT4_V = 26
                            DBT5_V = DBT5.text.toString().toInt()
                            if (DBT5_V <= 0) DBT5_V = 52
                        } catch (e: NumberFormatException) {
                            DBT1_V = 9
                            DBT2_V = 26
                            DBT3_V = 26
                            DBT4_V = 26
                            DBT5_V = 52
                            Toast.makeText(
                                applicationContext,
                                "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        } catch (e: Exception) {
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        }
                        intent.putExtra("DBT1", DBT1_V)
                        intent.putExtra("DBT2", DBT2_V)
                        intent.putExtra("DBT3", DBT3_V)
                        intent.putExtra("DBT4", DBT4_V)
                        intent.putExtra("DBT5", DBT5_V)
                    }
                    MainIndicator.ENVELOPES -> {
                        var ENV1_V: Int = 20
                        var ENV2_V: Int = 6
                        try {
                            ENV1_V = ENV1.text.toString().toInt()
                            if (ENV1_V <= 0) ENV1_V = 20
                            ENV2_V = ENV2.text.toString().toInt()
                            if (ENV2_V <= 0 || ENV2_V > 100) ENV2_V = 6
                        } catch (e: NumberFormatException) {
                            ENV1_V = 20
                            ENV2_V = 6
                            Toast.makeText(
                                applicationContext,
                                "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        } catch (e: Exception) {
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        }
                        intent.putExtra("ENV1", ENV1_V)
                        intent.putExtra("ENV2", ENV2_V)
                    }
                    MainIndicator.PRICE_CHANNELS -> {
                        var PC_V: Int = 5
                        try {
                            PC_V = PC.text.toString().toInt()
                            if (PC_V <= 0) PC_V = 5
                        } catch (e: NumberFormatException) {
                            PC_V = 5
                            Toast.makeText(
                                applicationContext,
                                "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        } catch (e: Exception) {
                            Log.e("PopupMainIndicatorSettingActivity", e.toString())
                        }
                        intent.putExtra("PC", PC_V)
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