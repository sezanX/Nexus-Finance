package com.example.nexusfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexusfinance.R
import com.example.nexusfinance.model.Currency

class CurrencyAdapter(
    private var currencies: List<Currency>,
    private val onCurrencySelected: (Currency) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    fun submitList(newList: List<Currency>) {
        currencies = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]
        holder.bind(currency)
    }

    override fun getItemCount(): Int = currencies.size

    inner class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSymbol: TextView = itemView.findViewById(R.id.tvCurrencySymbol)
        private val tvName: TextView = itemView.findViewById(R.id.tvCurrencyName)
        private val tvDetails: TextView = itemView.findViewById(R.id.tvCurrencyDetails)

        fun bind(currency: Currency) {
            tvSymbol.text = currency.symbol
            tvName.text = currency.name
            tvDetails.text = "${currency.country} • ${currency.code}"

            itemView.setOnClickListener {
                onCurrencySelected(currency)
            }
        }
    }
}
