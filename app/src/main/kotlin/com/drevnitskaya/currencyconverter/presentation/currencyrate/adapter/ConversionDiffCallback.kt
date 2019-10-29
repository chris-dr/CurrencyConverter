package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import androidx.recyclerview.widget.DiffUtil
import com.drevnitskaya.currencyconverter.data.entities.CurrencyConversionItem

class ConversionDiffCallback(
    private val oldItems: List<CurrencyConversionItem>,
    private val newItems: List<CurrencyConversionItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItems[oldItemPosition].currencyCode == newItems[newItemPosition].currencyCode

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItems[oldItemPosition] == newItems[newItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
        newItems[newItemPosition]
}