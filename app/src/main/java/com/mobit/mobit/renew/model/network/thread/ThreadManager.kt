package com.mobit.mobit.renew.model.network.thread

import com.mobit.mobit.renew.model.network.NetworkFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors

open class ThreadManager {

    private val THREAD_SIZE: Int = 5
    private val executor: Executor

    init {
        executor = Executors.newFixedThreadPool(THREAD_SIZE, NetworkFactory())
    }

    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

}