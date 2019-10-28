package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import androidx.recyclerview.widget.DiffUtil
import com.drevnitskaya.currencyconverter.data.entities.CurrencyConversionItem

class ConversionDiffCallback(
    private val oldItems: List<CurrencyConversionItem>,
    private val newItems: List<CurrencyConversionItem>
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
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return newItems[newItemPosition]
    }
}