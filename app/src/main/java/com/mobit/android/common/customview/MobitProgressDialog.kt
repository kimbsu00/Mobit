package com.mobit.android.common.customview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.mobit.android.R
import com.mobit.android.databinding.DialogMobitProgressBinding

class MobitProgressDialog(
    context: Context,
    private val message: String = ""
) : Dialog(context, R.style.progressBarTheme) {

    private val binding: DialogMobitProgressBinding by lazy {
        DialogMobitProgressBinding.inflate(layoutInflater)
    }

    private var _animFlag: Boolean = false
    private val animFlag: Boolean get() = _animFlag

    private val scaleDownAnim: Animation =
        AnimationUtils.loadAnimation(context, R.anim.mobit_progress_scale_down).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animatino: Animation?) {

                }

                override fun onAnimationEnd(animatino: Animation?) {
                    binding.ivMobitProgress.startAnimation(scaleUpAnim)
                }

                override fun onAnimationRepeat(animatino: Animation?) {

                }
            })
        }
    private val scaleUpAnim: Animation =
        AnimationUtils.loadAnimation(context, R.anim.mobit_progress_scale_up).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animatino: Animation?) {

                }

                override fun onAnimationEnd(animatino: Animation?) {
                    if (animFlag)
                        startAnimation()
                }

                override fun onAnimationRepeat(animatino: Animation?) {

                }
            })
        }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                // Dialog Message 변경
                HANDLER_WHAT_SET_MESSAGE -> {
                    binding.apply {
                        tvMobitProgress.text = message
                        tvMobitProgress.visibility =
                            if (message.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                // Dialog Animation
                HANDLER_WHAT_START_ANIM -> {
                    if (animFlag)
                        binding.ivMobitProgress.startAnimation(scaleDownAnim)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.attributes = WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.6f
        }

        setContentView(binding.root)
        init()
    }

    override fun show() {
        super.show()

        _animFlag = true
        startAnimation()
    }

    override fun dismiss() {
        super.dismiss()
        _animFlag = false
    }

    private fun init() {
        setMessage(message)
    }

    private fun startAnimation() {
        val msg = Message.obtain().apply {
            what = HANDLER_WHAT_START_ANIM
        }
        mHandler.sendMessage(msg)
    }

    fun setMessage(pMessage: String) {
        val msg = Message.obtain().apply {
            what = HANDLER_WHAT_SET_MESSAGE
            obj = pMessage
        }
        mHandler.sendMessage(msg)
    }

    companion object {
        private const val TAG: String = "MobitProgressDialog"

        private const val HANDLER_WHAT_SET_MESSAGE: Int = 0
        private const val HANDLER_WHAT_START_ANIM: Int = 1
    }

}