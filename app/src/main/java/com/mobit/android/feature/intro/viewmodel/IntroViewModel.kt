package com.mobit.android.feature.intro.viewmodel

import com.mobit.android.feature.base.viewmodel.BaseViewModel
import com.mobit.android.feature.intro.repository.IntroRepository

class IntroViewModel(
    private val repository: IntroRepository
) : BaseViewModel() {

    companion object {
        private const val TAG: String = "IntroViewModel"
    }

}