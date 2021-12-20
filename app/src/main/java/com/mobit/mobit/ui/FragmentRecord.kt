package com.mobit.mobit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobit.mobit.adapter.FragmentRecordAdapter
import com.mobit.mobit.viewmodel.MyViewModel
import com.mobit.mobit.data.Transaction
import com.mobit.mobit.databinding.FragmentRecordBinding

/*
거래내역 기능이 구현될 Fragment 입니다.
*/
class FragmentRecord : Fragment() {

    // UI 변수 시작
    lateinit var binding: FragmentRecordBinding
    // UI 변수 끝

    val myViewModel: MyViewModel by activityViewModels()

    lateinit var adapter: FragmentRecordAdapter
    val transactions: ArrayList<Transaction> = ArrayList<Transaction>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordBinding.inflate(layoutInflater)

        init()

        return binding.root
    }

    fun init() {
        myViewModel.transaction.observe(viewLifecycleOwner, Observer { transaction ->
            transactions.clear()
            transactions.addAll(transaction)
            adapter.notifyDataSetChanged()

            binding.apply {
                if (transactions.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    noRecordView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noRecordView.visibility = View.GONE
                }
            }
        })

        adapter = FragmentRecordAdapter(transactions, transactions)
        binding.apply {
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filter.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return true
                }
            })
        }
    }

}