package com.mobit.mobit.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobit.mobit.FragmentAsset
import com.mobit.mobit.FragmentRecord

class InvestmentStateAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    val fragmentAsset: Fragment = FragmentAsset()
    val fragmentRecord: Fragment = FragmentRecord()

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> fragmentAsset
            1 -> fragmentRecord
            else -> fragmentAsset
        }
    }
}