package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyConversionItem
import kotlinx.android.synthetic.main.item_currency_conversion.view.*
import kotlin.properties.Delegates
import androidx.core.widget.doOnTextChanged
import com.drevnitskaya.currencyconverter.data.source.local.currencyFlags
import com.drevnitskaya.currencyconverter.extensions.format
import com.drevnitskaya.currencyconverter.extensions.showSoftKeyboard


const val BASE_CURRENCY_POSITION = 0

class CurrencyConversionAdapter(
    private val onCurrencyClicked: (position: Int) -> Unit,
    private val onBaseAmountUpdated: (value: String) -> Unit
) : RecyclerView.Adapter<CurrencyConversionAdapter.CurrencyRateHolder>() {

    var items: List<CurrencyConversionItem> by Delegates.observable(emptyList()) { _, oldItems, newItems ->
        val callback = ConversionDiffCallback(oldItems, newItems)
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_conversion, parent, false)
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
            val item = payloads[0] as CurrencyConversionItem
            holder.updateAmount(item.amount)
            holder.updateFocus(item.isSelected)
        }
    }

    inner class CurrencyRateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            with(itemView) {
                setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onCurrencyClicked(adapterPosition)
                    }
                }
                currencyAmount.setOnClickListener {
                    itemView.performClick()
                }
                currencyAmount.doOnTextChanged { input, _, _, _ ->
                    if (adapterPosition == BASE_CURRENCY_POSITION && itemView.currencyAmount.isFocused) {
                        onBaseAmountUpdated(input.toString())
                    }
                }
            }
        }

        fun bind(item: CurrencyConversionItem) = with(itemView) {
            currencyCode.text = item.currencyCode
            val flagResId = currencyFlags[item.currencyCode] ?: R.mipmap.ic_launcher
            currencyFlag.setImageDrawable(ContextCompat.getDrawable(context, flagResId))
            val isItemSelected = item.isSelected
            if (isItemSelected) {
                val amount = if (item.amount == 0.0) "" else item.amount.format()
                currencyAmount.setText(amount)
                if (amount.isNotBlank()) {
                    currencyAmount.setSelection(amount.length)
                }
            } else {
                currencyAmount.setText(item.amount.format())
            }
            updateFocus(isItemSelected)
        }

        fun updateAmount(amount: Double) = with(itemView) {
            currencyAmount.setText(amount.format())
        }

        fun updateFocus(isItemSelected: Boolean) = with(itemView) {
            if (isItemSelected) {
                currencyAmount.isFocusableInTouchMode = true
                currencyAmount.requestFocus()
                currencyAmount.showSoftKeyboard()
            } else {
                currencyAmount.isFocusableInTouchMode = false
                currencyAmount.clearFocus()
            }
        }
    }
}
