package com.mobit.android.feature.intro.view

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mobit.android.common.util.DLog
import com.mobit.android.databinding.ActivityIntroBinding
import com.mobit.android.feature.base.view.BaseActivity
import com.mobit.android.feature.base.viewmodel.BaseViewModel
import com.mobit.android.feature.intro.viewmodel.IntroViewModel

class IntroActivity : BaseActivity() {

    private val model: IntroViewModel by lazy {
        ViewModelProvider(
            this,
            BaseViewModel.Factory(application)
        )[IntroViewModel::class.java]
    }
    private val binding: ActivityIntroBinding by lazy {
        ActivityIntroBinding.inflate(layoutInflater)
    }

    // region Activity LifeCycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setObserver()
        init()
    }
    // endregion Activity LifeCycle

    private fun setObserver() {
        model.progressFlag.observe(this, Observer { progressFlag ->

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

        model.mobitMarketData.observe(this, Observer { mobitMarketData ->
            DLog.d(TAG, "mobitMarketData=$mobitMarketData")
        })
    }

    private fun init() {
        model.requestMarketCode()
    }

    companion object {
        private const val TAG: String = "IntroActivity"
    }

}