package com.mobit.android.feature.intro.view

import android.os.Bundle
import com.mobit.android.databinding.ActivityIntroBinding
import com.mobit.android.feature.base.view.BaseActivity

class IntroActivity : BaseActivity() {

    private val binding: ActivityIntroBinding by lazy {
        ActivityIntroBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    companion object {
        private const val TAG: String = "IntroActivity"
    }

}