package com.mobit.mobit.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobit.mobit.R
import com.mobit.mobit.data.Transaction
import java.text.DecimalFormat

// blue -> #1561bf
// red -> #b35241
class FragmentRecordAdapter(
    var items: ArrayList<Transaction>,
    var filteredList: ArrayList<Transaction>
) :
    RecyclerView.Adapter<FragmentRecordAdapter.ViewHolder>(), Filterable {

    val formatter = DecimalFormat("###,###")
    val formatter2 = DecimalFormat("###,###.##")
    val formatter3 = DecimalFormat("###,###.####")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView
        val typeView: TextView
        val timeView: TextView
        val tradePriceView: TextView
        val tradeNumView: TextView
        val unitPriceView: TextView
        val feeView: TextView
        val totalPriceView: TextView

        init {
            nameView = itemView.findViewById(R.id.nameView)
            typeView = itemView.findViewById(R.id.typeView)
            timeView = itemView.findViewById(R.id.timeView)
            tradePriceView = itemView.findViewById(R.id.tradePriceView)
            tradeNumView = itemView.findViewById(R.id.tradeNumView)
            unitPriceView = itemView.findViewById(R.id.unitPriceView)
            feeView = itemView.findViewById(R.id.feeView)
            totalPriceView = itemView.findViewById(R.id.totalPriceView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_record_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("FragmentRecordAdapter", filteredList[position].code)
        val code = filteredList[position].code.split("-")[1]
//        val code = filteredList[position].code
        holder.nameView.text = "${filteredList[position].name}($code)"

        val tradePrice = filteredList[position].tradePrice
        val fee = filteredList[position].fee
        when (filteredList[position].type) {
            // 매도
            Transaction.ASK -> {
                holder.typeView.text = "매도"
                holder.typeView.setTextColor(Color.rgb(26, 96, 184))
                holder.totalPriceView.text = "${formatter.format(tradePrice - fee)} KRW"
            }
            // 매수
            Transaction.BID -> {
                holder.typeView.text = "매수"
                holder.typeView.setTextColor(Color.rgb(188, 79, 59))
                holder.totalPriceView.text = "${formatter.format(tradePrice + fee)} KRW"
            }
        }

        val times = filteredList[position].time.split("T")
        holder.timeView.text = "${times[0]} ${times[1].substring(0, 5)}"
        holder.tradePriceView.text = "${formatter.format(tradePrice)} KRW"
        holder.tradeNumView.text = "${formatter3.format(filteredList[position].quantity)} $code"
        holder.unitPriceView.text =
            if (filteredList[position].unitPrice > 100.0)
                "${formatter.format(filteredList[position].unitPrice)} KRW"
            else "${formatter2.format(filteredList[position].unitPrice)} KRW"
        holder.feeView.text = "${formatter2.format(fee)} KRW"
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val str: String = constraint.toString()

                if (str.isBlank()) {
                    filteredList = items
                } else {
                    val filteringList: ArrayList<Transaction> = ArrayList<Transaction>()
                    for (transaction in items) {
                        if (transaction.name.contains(str)) {
                            filteringList.add(transaction)
                        }
                    }
                    filteredList = filteringList
                }

                val filterResults: FilterResults = FilterResults()
                filterResults.values = filteredList

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    filteredList = results.values as ArrayList<Transaction>
                    notifyDataSetChanged()
                }
            }
        }
    }
}