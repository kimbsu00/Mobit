package com.mobit.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.mobit.android.data.MobitMarketData
import com.mobit.android.data.network.NetworkResult
import com.mobit.android.respository.MobitRepository
import kotlinx.coroutines.launch

class MobitViewModel(
    private val mobitRepository: MobitRepository
) : ViewModel() {

    private val _mobitMarketData: MutableLiveData<MobitMarketData> = MutableLiveData()
    val mobitMarketData: LiveData<MobitMarketData> get() = _mobitMarketData

    fun requestCoinDataList() {
        viewModelScope.launch {
            val result = mobitRepository.makeCoinListRequest()

            when (result) {
                is NetworkResult.Success<MobitMarketData> -> {
                    Log.i("${TAG}_requestCoinDataList", result.data.toString())
                    val mData = result.data
                    _mobitMarketData.value = mData
                }
                is NetworkResult.Error -> {
                    Log.e("${TAG}_requestCoinDataList", result.exception.toString())
                }
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MobitViewModel(MobitRepository(application)) as T
        }
    }

    companion object {
        const val TAG: String = "MobitViewModel"
    }

}