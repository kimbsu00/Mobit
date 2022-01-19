package com.mobit.mobit.renew.model.network.datamodel

import com.mobit.mobit.renew.model.network.listener.NetworkResultListener

class NetworkData {

    lateinit var url: String
    var requestCode: Int = -1
    var responseCode: Int = -1
    lateinit var message: String
    lateinit var params: HashMap<String, String>

    lateinit var listener: NetworkResultListener

    fun isValidData(): Boolean {
        return this::url.isInitialized && this::listener.isInitialized && requestCode > -1
    }

}