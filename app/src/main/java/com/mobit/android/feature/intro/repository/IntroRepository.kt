package com.mobit.android.feature.intro.repository

import android.app.Application
import com.mobit.android.feature.base.repository.BaseNetworkRepository

class IntroRepository(
    private val application: Application
) : BaseNetworkRepository(application, TAG) {

    companion object {
        private const val TAG: String = "IntroRepository"
    }

}