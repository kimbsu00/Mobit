package com.mobit.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobit.android.R
import com.mobit.android.viewmodel.MyViewModel
import com.mobit.android.databinding.FragmentSettingBinding
import com.mobit.android.feature.main.view.MainActivity

/*
설정 기능이 구현될 Fragment 입니다.
 */
class FragmentSetting : Fragment() {

    lateinit var binding: FragmentSettingBinding

    val myViewModel: MyViewModel by activityViewModels()
    val myProgressBar: MyProgressBar = MyProgressBar()

    lateinit var getContent: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(layoutInflater)
        getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                myProgressBar.progressON(activity, "초기화 하는중..")
                val thread = object : Thread() {
                    override fun run() {
                        myViewModel.myDBHelper!!.clearDB()
                    }
                }
                thread.start()
                try {
                    if (thread.isAlive) {
                        thread.join()
                    }
                } catch (e: InterruptedException) {
                    Log.e("Setting Reset Error", e.toString())
                }
                myProgressBar.progressOFF()
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        init()

        return binding.root
    }

    fun init() {
        binding.apply {
            guideTitle.setOnClickListener {
                when (guideLayout.visibility) {
                    View.VISIBLE -> {
                        guideLayout.visibility = View.GONE
                        guideTitle.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_baseline_keyboard_arrow_down_24,
                            0
                        )
                    }
                    View.GONE -> {
                        guideLayout.visibility = View.VISIBLE
                        guideTitle.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_baseline_keyboard_arrow_up_24,
                            0
                        )
                    }
                }
            }

            resetView.setOnClickListener {
                val intent = Intent(context, PopupResetConfirmActivity::class.java)
                getContent.launch(intent)
            }
        }
    }

}