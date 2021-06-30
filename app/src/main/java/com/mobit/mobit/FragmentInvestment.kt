package com.mobit.mobit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.mobit.mobit.adapter.InvestmentStateAdapter
import com.mobit.mobit.data.MyViewModel
import com.mobit.mobit.databinding.FragmentInvestmentBinding

/*
투자내역 기능이 구현될 Fragment 입니다.
*/
class FragmentInvestment : Fragment() {

    // UI 변수 시작
    lateinit var binding: FragmentInvestmentBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInvestmentBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    fun init() {
        binding.apply {
            viewPager.adapter = InvestmentStateAdapter(requireActivity())
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.investment_tab1)
                    1 -> getString(R.string.investment_tab2)
                    else -> ""
                }
            }.attach()
        }
    }

}