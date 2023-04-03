package com.mobit.android.feature.intro.view

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mobit.android.R
import com.mobit.android.common.customview.MobitMessageDialog
import com.mobit.android.common.util.NetworkUtil
import com.mobit.android.databinding.ActivityIntroBinding
import com.mobit.android.feature.base.view.BaseActivity
import com.mobit.android.feature.base.viewmodel.BaseViewModel
import com.mobit.android.feature.intro.viewmodel.IntroViewModel
import com.mobit.android.feature.main.view.MainActivity

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

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // region Activity LifeCycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics

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
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("mobit_market_data", mobitMarketData)
            }
            startActivity(mainIntent)
            finish()
        })
    }

    private fun init() {
        if (NetworkUtil.checkNetworkEnable(this)) {
            model.requestMarketCode()
        } else {
            showNetworkDialog()
        }
    }

    // region Dialog
    private fun showNetworkDialog() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is MobitMessageDialog) {
                return
            }
        }

        MobitMessageDialog(resources.getString(R.string.network_connection_need)) { finish() }
            .show(supportFragmentManager, MobitMessageDialog.TAG)
    }
    // endregion Dialog

    companion object {
        private const val TAG: String = "IntroActivity"
    }

}