package com.mobit.android.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mobit.android.R
import com.mobit.android.databinding.ActivityMainBinding
import com.mobit.android.network.NetworkManager
import com.mobit.android.viewmodel.MobitViewModel

class MainActivity : AppCompatActivity() {

    private val mobitViewModel: MobitViewModel by lazy {
        ViewModelProvider(this, MobitViewModel.Factory(application))[MobitViewModel::class.java]
    }
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    // region Activity Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Mobit)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 네트워크가 연결되어 있는 경우
        if (NetworkManager.checkNetworkState(this)) {
            setObserver()
            init()
        }
        // 네트워크가 연결되어 있지 않은 경우
        else {
            // TODO: 네트워크 연결이 필요하다는 팝업 띄우고 종료
        }
    }
    // endregion Activity Lifecycle

    private fun setObserver() {
        mobitViewModel.mobitMarketData.observe(this, Observer { mobitMarketData ->

        })
    }

    private fun init() {
        binding.apply {

        }

        mobitViewModel.requestCoinDataList()
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

        if (0 <= intervalTime && intervalTime <= FINISH_INTERVAL_TIME) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            val msg: String = "뒤로 가기를 한 번 더 누르면 종료됩니다."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

}