package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import kotlinx.android.synthetic.main.item_currency_rate.view.*
import kotlin.properties.Delegates
import androidx.core.widget.doOnTextChanged


const val BASE_CURRENCY_POSITION = 0

class CurrencyRateAdapter(
    private val onCurrencyClicked: (position: Int) -> Unit,
    private val onValueUpdated: (value: String) -> Unit
) : RecyclerView.Adapter<CurrencyRateAdapter.CurrencyRateHolder>() {

    var items: List<CurrencyItemWrapper> by Delegates.observable(emptyList()) { _, old, new ->
        val callback = TestDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_rate, parent, false)
        return CurrencyRateHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CurrencyRateHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onBindViewHolder(
        holder: CurrencyRateHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty() || position == BASE_CURRENCY_POSITION) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = payloads[0] as CurrencyItemWrapper
            holder.updateAmount(item.amount)
            holder.updateFocus(item)
        }
    }

    inner class CurrencyRateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCurrencyClicked(adapterPosition)
                }
            }
            itemView.currencyAmount.doOnTextChanged { text, _, _, _ ->
                if (adapterPosition == BASE_CURRENCY_POSITION) {
                    if (itemView.currencyAmount.isFocused) {
                        onValueUpdated(text.toString())
                    }
                }
            }
        }

        fun bind(item: CurrencyItemWrapper) = with(itemView) {
            val amountStr = "${item.amount}"
            testText.text = item.currencyCode
            currencyAmount.apply {
                if (item.isSelected) {
                    setText(amountStr)
                    if (item.amount == 0.0) {
                        setText("")
                    } else {
                        val inputLength = item.amount.toString().length
                        setSelection(inputLength)
                    }
                    isFocusableInTouchMode = true
                    requestFocus()
                } else {
                    setText(item.amount.toString())
                    isFocusableInTouchMode = false
                    clearFocus()
                }
            }
        }

        fun updateAmount(amount: Double) {
            itemView.currencyAmount.setText("$amount")
        }

        fun updateFocus(item: CurrencyItemWrapper) {
            if (item.isSelected) {
                itemView.currencyAmount.isFocusableInTouchMode = true
                itemView.currencyAmount.requestFocus()
            } else {
                itemView.currencyAmount.isFocusableInTouchMode = false
                itemView.currencyAmount.clearFocus()
            }
        }
    }
}

class TestDiffCallback(
    private val oldItems: List<CurrencyItemWrapper>,
    private val newItems: List<CurrencyItemWrapper>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].currencyCode == newItems[newItemPosition].currencyCode
    }

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem.amount.compareTo(newItem.amount) == 0
                && oldItem.isSelected == newItem.isSelected
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return newItems[newItemPosition]
    }
}
