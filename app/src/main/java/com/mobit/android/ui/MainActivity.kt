package com.mobit.android.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mobit.android.R
import com.mobit.android.data.MobitMarketData
import com.mobit.android.databinding.ActivityMainBinding
import com.mobit.android.feature.base.view.BaseActivity
import com.mobit.android.network.NetworkManager
import com.mobit.android.viewmodel.MobitViewModel

class MainActivity : BaseActivity() {

    private val model: MobitViewModel by lazy {
        ViewModelProvider(
            this,
            MobitViewModel.Factory(application)
        )[MobitViewModel::class.java]
    }
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // region Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Mobit)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mobitMarketData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("mobit_market_data", MobitMarketData::class.java)
        } else {
            intent.getSerializableExtra("mobit_market_data") as MobitMarketData
        }

        if (mobitMarketData != null) {
            setObserver()
            init(mobitMarketData)
        } else {
            showErrorMsg()
        }
    }
    // endregion Activity Lifecycle

    private fun setObserver() {
        model.progressFlag.observe(this, Observer { progressFlag ->
            if (progressFlag) {
                showProgress("")
            } else {
                dismissProgress()
            }
        })

        model.apiFailMsg.observe(this, Observer { failMsg ->
            if (failMsg.isNotEmpty()) {
                model.setProgressFlag(false)
                showToastMsg(failMsg)
            }
        })

        model.exceptionData.observe(this, Observer { exception ->
            model.setProgressFlag(false)
            showErrorMsg()
        })
    }

    private fun init(pMobitMarketData: MobitMarketData) {
        binding.apply {

        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: androidx.fragment.app.FragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        val tempTime: Long = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            val msg: String = "뒤로 가기를 한 번 더 누르면 종료됩니다."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG: String = "MainActivity"

        private const val FINISH_INTERVAL_TIME: Long = 2000
        private var backPressedTime: Long = 0
    }

}