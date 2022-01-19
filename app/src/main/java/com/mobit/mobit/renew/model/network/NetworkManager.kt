package com.mobit.mobit.renew.model.network

import android.content.Context
import com.mobit.mobit.renew.model.network.datamodel.NetworkData
import com.mobit.mobit.renew.model.network.thread.ThreadManager

object NetworkManager : ThreadManager() {

    fun execute(networkData: NetworkData, context: Context, urlState: String) {
        execute(NetworkRunnable(networkData, context, urlState))
    }

}