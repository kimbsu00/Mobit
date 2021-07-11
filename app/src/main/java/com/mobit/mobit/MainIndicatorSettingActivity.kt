package com.mobit.mobit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mobit.mobit.data.MainIndicator
import com.mobit.mobit.databinding.ActivityMainIndicatorSettingBinding

class MainIndicatorSettingActivity : AppCompatActivity() {

    // PopupChartResetConfirmActivity's Callback
    val resetContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                mainIndicatorType = MainIndicator.MOVING_AVERAGE
                binding.radioGroup.check(R.id.movingAverageBtn)
                mainIndicator!!.resetVariable()

                val intent: Intent = Intent()
                intent.putExtra("mainIndicatorType", mainIndicatorType)
                intent.putExtra("mainIndicator", mainIndicator)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    // PopupMainIndicatorSettingActivity's Callback
    val changeContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    mainIndicatorType = it.data!!.getIntExtra("mainIndicatorType", -1)
                    when (mainIndicatorType) {
                        MainIndicator.MOVING_AVERAGE -> {
                            mainIndicator!!.MA_N1 = it.data!!.getIntExtra("MA1", 5)
                            mainIndicator!!.MA_N2 = it.data!!.getIntExtra("MA2", 10)
                            mainIndicator!!.MA_N3 = it.data!!.getIntExtra("MA3", 20)
                            mainIndicator!!.MA_N4 = it.data!!.getIntExtra("MA4", 60)
                            mainIndicator!!.MA_N5 = it.data!!.getIntExtra("MA5", 120)
                        }
                        MainIndicator.BOLLINGER_BANDS -> {
                            mainIndicator!!.BB_N = it.data!!.getIntExtra("BB1", 20)
                            mainIndicator!!.BB_K = it.data!!.getFloatExtra("BB2", 2.0f)
                        }
                        MainIndicator.DAILY_BALANCE_TABLE -> {
                            mainIndicator!!.DBT_1 = it.data!!.getIntExtra("DBT1", 9)
                            mainIndicator!!.DBT_2 = it.data!!.getIntExtra("DBT2", 26)
                            mainIndicator!!.DBT_3 = it.data!!.getIntExtra("DBT3", 26)
                            mainIndicator!!.DBT_4 = it.data!!.getIntExtra("DBT4", 26)
                            mainIndicator!!.DBT_5 = it.data!!.getIntExtra("DBT5", 52)
                        }
                        MainIndicator.ENVELOPES -> {
                            mainIndicator!!.ENV_N = it.data!!.getIntExtra("ENV1", 20)
                            mainIndicator!!.ENV_K = it.data!!.getIntExtra("ENV2", 6)
                        }
                        MainIndicator.PRICE_CHANNELS -> {
                            mainIndicator!!.PC_N = it.data!!.getIntExtra("PC", 5)
                        }
                    }

                    val intent: Intent = Intent()
                    intent.putExtra("mainIndicatorType", mainIndicatorType)
                    intent.putExtra("mainIndicator", mainIndicator)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

    lateinit var binding: ActivityMainIndicatorSettingBinding

    var mainIndicatorType: Int = MainIndicator.MOVING_AVERAGE
    var mainIndicator: MainIndicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainIndicatorSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init() {
        mainIndicatorType = intent.getIntExtra("mainIndicatorType", MainIndicator.MOVING_AVERAGE)
        mainIndicator = intent.getSerializableExtra("mainIndicator") as MainIndicator

        binding.apply {
            when (mainIndicatorType) {
                MainIndicator.MOVING_AVERAGE -> {
                    movingAverageBtn.isChecked = true
                }
                MainIndicator.BOLLINGER_BANDS -> {
                    bollingerBandsBtn.isChecked = true
                }
                MainIndicator.DAILY_BALANCE_TABLE -> {
                    dailyBalanceTableBtn.isChecked = true
                }
                MainIndicator.PIVOT -> {
                    pivotBtn.isChecked = true
                }
                MainIndicator.ENVELOPES -> {
                    envelopesBtn.isChecked = true
                }
                MainIndicator.PRICE_CHANNELS -> {
                    priceChannelsBtn.isChecked = true
                }
            }

            movingAverageBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.MOVING_AVERAGE
                clearRadioButton()
                movingAverageBtn.isChecked = true
            }
            bollingerBandsBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.BOLLINGER_BANDS
                clearRadioButton()
                bollingerBandsBtn.isChecked = true
            }
            dailyBalanceTableBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.DAILY_BALANCE_TABLE
                clearRadioButton()
                dailyBalanceTableBtn.isChecked = true
            }
            pivotBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.PIVOT
                clearRadioButton()
                pivotBtn.isChecked = true
            }
            envelopesBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.ENVELOPES
                clearRadioButton()
                envelopesBtn.isChecked = true
            }
            priceChannelsBtn.setOnClickListener {
                mainIndicatorType = MainIndicator.PRICE_CHANNELS
                clearRadioButton()
                priceChannelsBtn.isChecked = true
            }

            mainIndicatorTitle.setOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            resetBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupChartResetConfirmActivity::class.java
                )
                resetContent.launch(intent)
            }
            saveBtn.setOnClickListener {
                val intent: Intent = Intent()
                intent.putExtra("mainIndicatorType", mainIndicatorType)
                intent.putExtra("mainIndicator", mainIndicator)
                setResult(RESULT_OK, intent)
                finish()
            }

            movingAverageSettingBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupMainIndicatorSettingActivity::class.java
                )
                intent.putExtra("mainIndicatorType", MainIndicator.MOVING_AVERAGE)
                intent.putExtra("MA1", mainIndicator!!.MA_N1)
                intent.putExtra("MA2", mainIndicator!!.MA_N2)
                intent.putExtra("MA3", mainIndicator!!.MA_N3)
                intent.putExtra("MA4", mainIndicator!!.MA_N4)
                intent.putExtra("MA5", mainIndicator!!.MA_N5)
                changeContent.launch(intent)
            }
            bollingerBandsSettingBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupMainIndicatorSettingActivity::class.java
                )
                intent.putExtra("mainIndicatorType", MainIndicator.BOLLINGER_BANDS)
                intent.putExtra("BB1", mainIndicator!!.BB_N)
                intent.putExtra("BB2", mainIndicator!!.BB_K)
                changeContent.launch(intent)
            }
            dailyBalanceTableSettingBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupMainIndicatorSettingActivity::class.java
                )
                intent.putExtra("mainIndicatorType", MainIndicator.DAILY_BALANCE_TABLE)
                intent.putExtra("DBT1", mainIndicator!!.DBT_1)
                intent.putExtra("DBT2", mainIndicator!!.DBT_2)
                intent.putExtra("DBT3", mainIndicator!!.DBT_3)
                intent.putExtra("DBT4", mainIndicator!!.DBT_4)
                intent.putExtra("DBT5", mainIndicator!!.DBT_5)
                changeContent.launch(intent)
            }
            envelopesSettingBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupMainIndicatorSettingActivity::class.java
                )
                intent.putExtra("mainIndicatorType", MainIndicator.ENVELOPES)
                intent.putExtra("ENV1", mainIndicator!!.ENV_N)
                intent.putExtra("ENV2", mainIndicator!!.ENV_K)
                changeContent.launch(intent)
            }
            priceChannelsSettingBtn.setOnClickListener {
                val intent: Intent = Intent(
                    this@MainIndicatorSettingActivity,
                    PopupMainIndicatorSettingActivity::class.java
                )
                intent.putExtra("mainIndicatorType", MainIndicator.PRICE_CHANNELS)
                intent.putExtra("PC", mainIndicator!!.PC_N)
                changeContent.launch(intent)
            }
        }
    }

    fun clearRadioButton() {
        binding.apply {
            movingAverageBtn.isChecked = false
            bollingerBandsBtn.isChecked = false
            dailyBalanceTableBtn.isChecked = false
            pivotBtn.isChecked = false
            envelopesBtn.isChecked = false
            priceChannelsBtn.isChecked = false
        }
    }
}