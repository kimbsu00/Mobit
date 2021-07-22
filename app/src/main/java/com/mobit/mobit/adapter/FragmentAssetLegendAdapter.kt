package com.mobit.mobit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.LegendEntry
import com.mobit.mobit.R

class FragmentAssetLegendAdapter(var items: ArrayList<LegendEntry>) :
    RecyclerView.Adapter<FragmentAssetLegendAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codeView: TextView
        val percentView: TextView

        init {
            codeView = itemView.findViewById(R.id.codeView)
            percentView = itemView.findViewById(R.id.percentView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_asset_legend_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val temp = items[position].label.split('-')
        val code: String = temp[0].trim()
        val percent: String = temp[1].trim()

        holder.codeView.text = code
        holder.percentView.text = percent

        when (position) {
            0 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_1,
                0,
                0,
                0
            )
            1 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_2,
                0,
                0,
                0
            )
            2 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_3,
                0,
                0,
                0
            )
            3 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_4,
                0,
                0,
                0
            )
            4 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_5,
                0,
                0,
                0
            )
            5 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_6,
                0,
                0,
                0
            )
            6 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_7,
                0,
                0,
                0
            )
            7 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_8,
                0,
                0,
                0
            )
            8 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_9,
                0,
                0,
                0
            )
            9 -> holder.codeView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_baseline_circle_10,
                0,
                0,
                0
            )
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}