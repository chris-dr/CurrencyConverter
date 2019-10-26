package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import kotlinx.android.synthetic.main.item_currency_rate.view.*


class CurrencyRateAdapter(
    private val onCurrencyClicked: (item: CurrencyItemWrapper) -> Unit
) : RecyclerView.Adapter<CurrencyRateAdapter.CurrencyRateHolder>() {

    /*var items: List<CurrencyItemWrapper> by Delegates.observable(emptyList()) { _, oldItems, newItems ->
        val callback = CurrencyFieldDiffCallback(oldItems, newItems)
        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(this)
    }*/
    var items: List<CurrencyItemWrapper> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_rate, parent, false)
        return CurrencyRateHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CurrencyRateHolder, position: Int) {
        holder.bind(items[position])
    }

    /*override fun onBindViewHolder(
        holder: CurrencyRateHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val amount = (payloads[0] as Float)
            holder.updateAmount(amount)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }*/

    inner class CurrencyRateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCurrencyClicked(items[adapterPosition])
                }
            }
        }

        fun bind(item: CurrencyItemWrapper) = with(itemView) {
            if (item.isSelected) {
                currencyAmount.isFocusable = true
                currencyAmount.isFocusableInTouchMode = true
            } else {
                currencyAmount.isFocusable = false
                currencyAmount.isFocusableInTouchMode = false
                currencyAmount.clearFocus()
            }
            testText.text = "${item.currencyCode}"
            currencyAmount.setText("${item.amount}")
        }

        /*fun updateAmount(amount: Float) {
            itemView.currencyAmount.setText("$amount")
        }*/
    }
}