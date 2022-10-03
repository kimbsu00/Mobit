package com.mobit.android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobit.android.databinding.ActivityFirstSettingBinding

class FirstSettingActivity : AppCompatActivity() {

    lateinit var binding: ActivityFirstSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init() {
        binding.apply {
            confirmBtn.setOnClickListener {
                try {
                    val input: String = depositEditText.text.toString()
                    val depositValue: Double = input.toInt().toDouble()

                    if (depositValue in 0.0..2e10) {
                        val intent = Intent()
                        intent.putExtra("krw", depositValue)
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@FirstSettingActivity,
                            "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        this@FirstSettingActivity,
                        "지정 가능한 범위가 아닙니다.\n다시 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {

    }
}