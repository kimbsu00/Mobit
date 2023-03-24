package com.mobit.android.feature.intro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobit.android.data.MobitMarketData
import com.mobit.android.data.network.NetworkResult
import com.mobit.android.feature.base.viewmodel.BaseViewModel
import com.mobit.android.feature.intro.repository.IntroRepository
import kotlinx.coroutines.launch

class IntroViewModel(
    private val repository: IntroRepository
) : BaseViewModel() {

    private val _mobitMarketData: MutableLiveData<MobitMarketData> = MutableLiveData()
    val mobitMarketData: LiveData<MobitMarketData> get() = _mobitMarketData

    fun requestMarketCode() {
        viewModelScope.launch {
            setProgressFlag(true)

            when (val result = repository.makeMarketCodeRequest()) {
                is NetworkResult.Success<MobitMarketData> -> {
                    val data = result.data
                    _mobitMarketData.value = data
                }
                is NetworkResult.Fail -> {
                    Log.e("${TAG}_requestMarketCode", "failMsg=${result.failMsg}")
                    setApiFailMsg(result.failMsg)
                }
                is NetworkResult.Error -> {
                    Log.e("${TAG}_requestMarketCode", result.exception.message, result.exception)
                }
            }
        }
    }

    companion object {
        private const val TAG: String = "IntroViewModel"
    }

}