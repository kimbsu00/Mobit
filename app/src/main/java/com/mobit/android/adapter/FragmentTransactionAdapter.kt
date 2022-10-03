package com.mobit.android.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobit.android.R
import com.mobit.android.data.OrderBook
import java.math.RoundingMode
import java.text.DecimalFormat

class FragmentTransactionAdapter(var items: ArrayList<OrderBook>, var openPrice: Double) :
    RecyclerView.Adapter<FragmentTransactionAdapter.ViewHolder>() {

    val intFormatter = DecimalFormat("###,###").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter3 = DecimalFormat("###,###.###").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter2 = DecimalFormat("###,##0.00").apply {
        this.roundingMode = RoundingMode.DOWN
    }

    var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClicked(view: View, price: Double)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout
        val price: TextView
        val priceRate: TextView
        val orderSize: TextView

        init {
            linearLayout = itemView.findViewById(R.id.linearLayout)
            price = itemView.findViewById(R.id.price)
            priceRate = itemView.findViewById(R.id.priceRate)
            orderSize = itemView.findViewById(R.id.orderSize)

            val clickListener: View.OnClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (v != null) {

                    }
                }
            }
            price.setOnClickListener(clickListener)
            priceRate.setOnClickListener(clickListener)
            orderSize.setOnClickListener(clickListener)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_transaction_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val price = items[position].price
        val priceRate = (price - openPrice) / openPrice * 100
        holder.price.text =
            if (price > 100.0) intFormatter.format(price) else doubleFormatter2.format(price)
        holder.priceRate.text = holder.itemView.context.getString(
            R.string.transaction_price_rate_string,
            doubleFormatter2.format(priceRate)
        )
        holder.orderSize.text = doubleFormatter3.format(items[position].size)

        if (openPrice > price) {
            holder.price.setTextColor(Color.parseColor("#135fc1"))
            holder.priceRate.setTextColor(Color.parseColor("#135fc1"))
        } else if (openPrice < price) {
            holder.price.setTextColor(Color.parseColor("#bd4e3a"))
            holder.priceRate.setTextColor(Color.parseColor("#bd4e3a"))
        } else {
            holder.price.setTextColor(Color.parseColor("#FFFFFF"))
            holder.priceRate.setTextColor(Color.parseColor("#FFFFFF"))
        }

        if (position < 15) {
            holder.price.setBackgroundColor(Color.parseColor("#081d3a"))
            holder.priceRate.setBackgroundColor(Color.parseColor("#081d3a"))
            holder.orderSize.setBackgroundColor(Color.parseColor("#081d3a"))
        } else {
            holder.price.setBackgroundColor(Color.parseColor("#241a23"))
            holder.priceRate.setBackgroundColor(Color.parseColor("#241a23"))
            holder.orderSize.setBackgroundColor(Color.parseColor("#241a23"))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}