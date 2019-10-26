package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.drevnitskaya.currencyconverter.R
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper
import kotlinx.android.synthetic.main.item_currency_rate.view.*

const val BASE_CURRENCY_POSITION = 0

class CurrencyRateAdapter(
    private val onCurrencyClicked: (item: CurrencyItemWrapper) -> Unit,
    private val onValueUpdated: (value: String) -> Unit
) : RecyclerView.Adapter<CurrencyRateAdapter.CurrencyRateHolder>() {
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

    override fun onBindViewHolder(
        holder: CurrencyRateHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty() || position == BASE_CURRENCY_POSITION) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val amount = (payloads[0] as ArrayList<*>)[position - 1] as Double
            holder.updateAmount(amount)
        }
    }

    inner class CurrencyRateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            //todo: refactor it
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCurrencyClicked(items[adapterPosition])
                }
            }
            itemView.currencyAmount.setOnFocusChangeListener { _, hasFocus ->
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition != BASE_CURRENCY_POSITION && hasFocus) {
                    onCurrencyClicked(items[adapterPosition])
                }
            }
            itemView.currencyAmount.doOnTextChanged { text, _, _, _ ->
                if (adapterPosition == BASE_CURRENCY_POSITION) {
                    onValueUpdated(text.toString())
                }
            }
        }

        fun bind(item: CurrencyItemWrapper) = with(itemView) {
            val amountStr = "${item.amount}"
            testText.text = item.currencyCode
            currencyAmount.apply {
                setText(amountStr)
                if (item.isSelected) {
                    val inputLength = item.amount.toString().length
                    setSelection(inputLength)
                    requestFocus()
                } else {
                    clearFocus()
                }
            }
        }

        fun updateAmount(amount: Double) {
            itemView.currencyAmount.setText("$amount")
        }
    }
}