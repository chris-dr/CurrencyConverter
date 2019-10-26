package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import kotlinx.android.synthetic.main.item_currency_rate.view.*


class CurrencyRateAdapter(
    private val onCurrencyClicked: (item: CurrencyItemWrapper) -> Unit,
    private val onValueUpdated: (value: String) -> Unit
) : RecyclerView.Adapter<CurrencyRateAdapter.CurrencyRateHolder>() {
    var items: List<CurrencyItemWrapper> = emptyList()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_rate, parent, false)
        return CurrencyRateHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].currencyCode.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: CurrencyRateHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onBindViewHolder(
        holder: CurrencyRateHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty() || position == 0) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val amount = (payloads[0] as ArrayList<*>)[position] as Double
            holder.updateAmount(amount)
        }
    }

    inner class CurrencyRateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCurrencyClicked(items[adapterPosition])
                }
            }
            itemView.currencyAmount.setOnFocusChangeListener { _, hasFocus ->
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition != 0 && hasFocus) {
                    onCurrencyClicked(items[adapterPosition])
                }
            }
            itemView.currencyAmount.doOnTextChanged { text, _, _, _ ->
                if (adapterPosition == 0) {
                    onValueUpdated(text.toString())
                }
            }
        }

        fun bind(item: CurrencyItemWrapper) = with(itemView) {
            if (item.isSelected) {
                currencyAmount.requestFocus()
            } else {
                currencyAmount.clearFocus()
            }
            testText.text = "${item.currencyCode}"
            currencyAmount.setText("${item.amount}")
            val pos = item.amount.toString().length
            currencyAmount.setSelection(pos)
        }

        fun updateAmount(amount: Double) {
            itemView.currencyAmount.setText("$amount")
        }
    }
}