package com.drevnitskaya.currencyconverter.presentation.currencyrate.adapter

import androidx.recyclerview.widget.DiffUtil
import com.drevnitskaya.currencyconverter.data.entities.CurrencyItemWrapper

class CurrencyFieldDiffCallback(
    private val oldItems: List<CurrencyItemWrapper>,
    private val newItems: List<CurrencyItemWrapper>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].currencyCode == newItems[newItemPosition].currencyCode
    }

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newItems[newItemPosition] == oldItems[oldItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return newItems[newItemPosition].amount
    }
}