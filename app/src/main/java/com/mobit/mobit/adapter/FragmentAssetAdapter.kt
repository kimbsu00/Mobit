package com.mobit.mobit.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobit.mobit.R
import com.mobit.mobit.data.CoinAsset
import java.text.DecimalFormat

/*
FragmentAsset에서 보유 자산을 보여줄 때 사용하는 adapter 입니다.
*/
class FragmentAssetAdapter(var items: ArrayList<CoinAsset>) :
    RecyclerView.Adapter<FragmentAssetAdapter.ViewHolder>() {

    val formatter = DecimalFormat("###,###")
    val formatter2 = DecimalFormat("###,###.##")
    val formatter3 = DecimalFormat("###,###.####")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView
        val codeView: TextView
        val gainAndLossView: TextView
        val yieldView: TextView
        val retainedNumView: TextView
        val averagePriceView: TextView
        val evaluationView: TextView
        val buyPriceView: TextView

        init {
            nameView = itemView.findViewById(R.id.nameView)
            codeView = itemView.findViewById(R.id.codeView)
            gainAndLossView = itemView.findViewById(R.id.gainAndLossView)
            yieldView = itemView.findViewById(R.id.yieldView)
            retainedNumView = itemView.findViewById(R.id.retainedNumView)
            averagePriceView = itemView.findViewById(R.id.averagePriceView)
            evaluationView = itemView.findViewById(R.id.evaluationView)
            buyPriceView = itemView.findViewById(R.id.buyPriceView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_asset_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameView.text = items[position].name
        val code = items[position].code.split("-")[1]
        holder.codeView.text = "($code)"
        holder.retainedNumView.text =
            "${formatter3.format(items[position].number)} $code"
        holder.averagePriceView.text =
            if (items[position].averagePrice > 100.0)
                "${formatter.format(items[position].averagePrice)} KRW"
            else "${formatter2.format(items[position].averagePrice)} KRW"
        holder.evaluationView.text = "${formatter.format(items[position].amount)} KRW"

        val buyPrice = items[position].averagePrice * items[position].number
        holder.buyPriceView.text = "${formatter.format(buyPrice)} KRW"

        val gainAndLoss = items[position].amount - buyPrice
        val yieldValue = gainAndLoss / buyPrice * 100
        holder.gainAndLossView.text = formatter.format(gainAndLoss)
        holder.yieldView.text = formatter2.format(yieldValue) + "%"

        val rgb = if (gainAndLoss > 0) Color.rgb(
            207,
            80,
            71
        ) else if (gainAndLoss < 0) Color.rgb(
            25,
            96,
            186
        ) else Color.rgb(211, 212, 214)
        holder.gainAndLossView.setTextColor(rgb)
        holder.yieldView.setTextColor(rgb)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}