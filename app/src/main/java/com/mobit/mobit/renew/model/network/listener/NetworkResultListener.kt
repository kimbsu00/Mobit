package com.mobit.mobit.renew.model.network.listener

import com.mobit.mobit.renew.model.network.datamodel.NetworkData

interface NetworkResultListener {
    fun onResult(networkData: NetworkData)
    fun onSuccessResult(networkData: NetworkData)
    fun onFailResult(networkData: NetworkData)
}