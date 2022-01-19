package com.mobit.mobit.renew.model.network

import java.util.concurrent.ThreadFactory

class NetworkFactory : ThreadFactory {

    private var idx: Int = 1

    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(r)
        thread.name = "MobitThread_${idx++}"
        return thread
    }

}