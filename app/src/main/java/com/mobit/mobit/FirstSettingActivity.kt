package com.mobit.mobit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mobit.mobit.databinding.ActivityFirstSettingBinding

class FirstSettingActivity : AppCompatActivity() {

    lateinit var binding: ActivityFirstSettingBinding
    var depositValue: Double = 1000000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    fun init() {
        // spinner 아이템을 보여주는 view를 커스텀하기 위해서 adapter를 만들어준다
        val spinnerAdapter = ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            resources.getStringArray(R.array.depositSpinner)
        )

        binding.apply {
            depositSpinner.adapter = spinnerAdapter
            depositSpinner.setSelection(0, false)
            depositSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    depositValue = when (position) {
                        0 -> 1000000.0
                        1 -> 3000000.0
                        2 -> 5000000.0
                        3 -> 10000000.0
                        4 -> 30000000.0
                        5 -> 50000000.0
                        else -> 1000000.0
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            confirmBtn.setOnClickListener {
                val intent = Intent()
                intent.putExtra("krw", depositValue)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {

    }
}