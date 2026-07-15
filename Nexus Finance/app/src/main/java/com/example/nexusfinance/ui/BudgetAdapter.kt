package com.example.nexusfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexusfinance.R
import com.example.nexusfinance.model.BudgetProgress
import java.text.NumberFormat
import java.util.Locale

class BudgetAdapter(
    private val onClick: (BudgetProgress) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.ViewHolder>() {

    private var budgets = listOf<BudgetProgress>()
    private var currencySymbol = "$"

    fun submitList(list: List<BudgetProgress>, symbol: String = "$") {
        budgets = list
        currencySymbol = symbol
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvRemaining: TextView = view.findViewById(R.id.tvRemaining)
        val tvSpent: TextView = view.findViewById(R.id.tvSpent)
        val tvLimit: TextView = view.findViewById(R.id.tvLimit)
        val pbProgress: ProgressBar = view.findViewById(R.id.pbProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val progress = budgets[position]
        holder.tvCategory.text = progress.budget.category
        
        val format = NumberFormat.getNumberInstance(Locale.getDefault())
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 2
        
        holder.tvSpent.text = "Spent $currencySymbol${format.format(progress.spent)}"
        holder.tvLimit.text = "Limit $currencySymbol${format.format(progress.limitAmount)}"
        
        holder.pbProgress.progress = (progress.ratio * 100).toInt().coerceAtMost(100)
        
        if (progress.isOverspent) {
            holder.tvRemaining.text = "Over by $currencySymbol${format.format(-progress.remaining)}"
            holder.tvRemaining.setTextColor(android.graphics.Color.parseColor("#E53935")) // Red
        } else {
            holder.tvRemaining.text = "$currencySymbol${format.format(progress.remaining)} left"
            holder.tvRemaining.setTextColor(android.graphics.Color.parseColor("#43A047")) // Green
        }

        holder.itemView.setOnClickListener { onClick(progress) }
    }

    override fun getItemCount() = budgets.size
}
