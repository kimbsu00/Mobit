package com.mobit.mobit.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobit.mobit.R
import com.mobit.mobit.data.CoinInfo
import java.math.RoundingMode
import java.text.DecimalFormat

class FragmentCoinListAdapter(
    var items: ArrayList<CoinInfo>,
    var filteredItems: ArrayList<CoinInfo>
) :
    RecyclerView.Adapter<FragmentCoinListAdapter.ViewHolder>(), Filterable {

    val intFormatter = DecimalFormat("###,###").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter2 = DecimalFormat("###,###.##").apply {
        this.roundingMode = RoundingMode.DOWN
    }
    val doubleFormatter2Zero = DecimalFormat("###,##0.00").apply {
        this.roundingMode = RoundingMode.DOWN
    }

    var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClicked(view: View, code: String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout
        val korCoinName: TextView
        val engCoinName: TextView
        val realTimePrice: TextView
        val changeRate: TextView
        val totalTradePrice: TextView

        init {
            linearLayout = itemView.findViewById(R.id.linearLayout)
            korCoinName = itemView.findViewById(R.id.korCoinName)
            engCoinName = itemView.findViewById(R.id.engCoinName)
            realTimePrice = itemView.findViewById(R.id.realTimePrice)
            changeRate = itemView.findViewById(R.id.changeRate)
            totalTradePrice = itemView.findViewById(R.id.totalTradePrice)

            val clickListener: View.OnClickListener = object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (v != null) {
                        if (adapterPosition < filteredItems.size) {
                            listener?.onItemClicked(v, filteredItems[adapterPosition].code)
                        }
                    }
                }
            }
            linearLayout.setOnClickListener(clickListener)
            korCoinName.setOnClickListener(clickListener)
            engCoinName.setOnClickListener(clickListener)
            realTimePrice.setOnClickListener(clickListener)
            changeRate.setOnClickListener(clickListener)
            totalTradePrice.setOnClickListener(clickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_coinlist_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.korCoinName.text = when (filteredItems[position].warning) {
            // 투자 유의 종목이 아닌 경우
            "NONE" -> {
                filteredItems[position].name
            }
            // 투자 유의 종목인 경우
            "CAUTION" -> {
                val sb: SpannableStringBuilder =
                    SpannableStringBuilder("${filteredItems[position].name}  유")
                sb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#d3d4d6")),
                    filteredItems[position].name.length + 2,
                    filteredItems[position].name.length + 3,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sb.setSpan(
                    BackgroundColorSpan(Color.rgb(220, 126, 54)),
                    filteredItems[position].name.length + 2,
                    filteredItems[position].name.length + 3,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sb
            }
            else -> {
                filteredItems[position].name
            }
        }
        holder.itemView.context.apply {
            holder.engCoinName.text = getString(
                R.string.coin_list_eng_coin_name_string,
                filteredItems[position].code.split('-')[1]
            )
            holder.realTimePrice.text =
                if (filteredItems[position].price.realTimePrice > 100.0)
                    intFormatter.format(filteredItems[position].price.realTimePrice)
                else
                    doubleFormatter2.format(filteredItems[position].price.realTimePrice)
            holder.changeRate.text =
                getString(
                    R.string.coin_list_change_rate_string,
                    doubleFormatter2Zero.format(filteredItems[position].price.changeRate * 100)
                )
            val temp = (filteredItems[position].price.totalTradePrice24 / 1000000).toInt()
            holder.totalTradePrice.text =
                getString(R.string.coin_list_total_trade_price_string, intFormatter.format(temp))
        }

        if (filteredItems[position].price.changeRate > 0) {
            holder.realTimePrice.setTextColor(Color.parseColor("#bd4e3a"))
            holder.changeRate.setTextColor(Color.parseColor("#bd4e3a"))
        } else if (filteredItems[position].price.changeRate < 0) {
            holder.realTimePrice.setTextColor(Color.parseColor("#135fc1"))
            holder.changeRate.setTextColor(Color.parseColor("#135fc1"))
        } else {
            holder.realTimePrice.setTextColor(Color.parseColor("#d3d4d6"))
            holder.changeRate.setTextColor(Color.parseColor("#d3d4d6"))
        }

        if (filteredItems[position].price.realTimePriceDiff > 0) {
            holder.realTimePrice.setBackgroundResource(R.drawable.coin_list_realtimeprice_border_red)
        } else if (filteredItems[position].price.realTimePriceDiff < 0) {
            holder.realTimePrice.setBackgroundResource(R.drawable.coin_list_realtimeprice_border_blue)
        } else {
            holder.realTimePrice.setBackgroundResource(0)
        }
    }

    override fun getItemCount(): Int {
        return filteredItems.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val str: String = constraint.toString()

                if (str.isBlank()) {
                    filteredItems = items
                } else {
                    val filteringList: ArrayList<CoinInfo> = ArrayList()
                    for (coinInfo in items) {
                        val coinName = coinInfo.name
                        if (coinName.contains(str)) {
                            filteringList.add(coinInfo)
                        }
                    }
                    filteredItems = filteringList
                }
                val result: FilterResults = FilterResults()
                result.values = filteredItems

                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredItems = results.values as ArrayList<CoinInfo>
                    notifyDataSetChanged()
                }
            }

        }
    }
}