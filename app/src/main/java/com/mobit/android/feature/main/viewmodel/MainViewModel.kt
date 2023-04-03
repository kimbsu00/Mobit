package com.mobit.android.feature.main.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.mobit.android.data.MobitMarketData
import com.mobit.android.feature.base.viewmodel.BaseViewModel
import com.mobit.android.repository.MobitRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val mobitRepository: MobitRepository
) : BaseViewModel() {

    private val _mobitMarketData: MutableLiveData<MobitMarketData> = MutableLiveData()
    val mobitMarketData: LiveData<MobitMarketData> get() = _mobitMarketData

    fun initMobitMarketData(pMobitMarketData: MobitMarketData) {
        _mobitMarketData.value = pMobitMarketData
        requestCoinTicker()
    }

    /**
     * KRW 마켓에 포함되는 코인 현재가 정보를 요청하는 함수
     */
    fun requestCoinTicker() {
        viewModelScope.launch {
            setProgressFlag(true)


        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(MobitRepository(application)) as T
        }
    }

    companion object {
        const val TAG: String = "MobitViewModel"
    }

}