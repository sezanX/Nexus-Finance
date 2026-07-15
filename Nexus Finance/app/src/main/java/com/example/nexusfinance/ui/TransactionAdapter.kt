package com.example.nexusfinance.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexusfinance.R
import com.example.nexusfinance.model.Transaction
import com.example.nexusfinance.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit = {}
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val expandedItems = mutableSetOf<Long>()
    private var transactions = listOf<Transaction>()
    private var currencySymbol = "$"

    fun submitList(list: List<Transaction>, symbol: String = "$") {
        transactions = list
        currencySymbol = symbol
        notifyDataSetChanged()
    }
    
    fun getTransactionAt(position: Int): Transaction = transactions[position]

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val ivIcon: android.widget.ImageView = view.findViewById(R.id.ivIcon)
        val foregroundCard: View = view.findViewById(R.id.foreground_card)
        val actionBackground: View = view.findViewById(R.id.action_background)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
        val btnEdit: View = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tx = transactions[position]
        holder.tvCategory.text = tx.category
        holder.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(tx.date))
        
        val format = NumberFormat.getNumberInstance(Locale.getDefault())
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        val amountStr = format.format(tx.amount)
        
        if (tx.type == TransactionType.INCOME) {
            holder.tvAmount.text = "+$currencySymbol$amountStr"
            holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#43A047"))
        } else {
            holder.tvAmount.text = "-$currencySymbol$amountStr"
            holder.tvAmount.setTextColor(android.graphics.Color.parseColor("#E53935"))
        }
        
        when (tx.category.lowercase(Locale.getDefault())) {
            "food" -> holder.ivIcon.setImageResource(R.drawable.ic_colorful_food)
            "transport", "transportation" -> holder.ivIcon.setImageResource(R.drawable.ic_colorful_transport)
            "study", "study materials" -> holder.ivIcon.setImageResource(R.drawable.ic_colorful_study)
            "other" -> holder.ivIcon.setImageResource(R.drawable.ic_colorful_other)
            else -> holder.ivIcon.setImageResource(R.drawable.ic_colorful_tx)
        }

        val isExpanded = expandedItems.contains(tx.id)
        val density = holder.itemView.resources.displayMetrics.density
        val offset = -110 * density

        holder.foregroundCard.translationX = if (isExpanded) offset else 0f

        holder.foregroundCard.setOnClickListener {
            if (expandedItems.contains(tx.id)) {
                expandedItems.remove(tx.id)
                holder.foregroundCard.animate().translationX(0f).setDuration(200).start()
            } else {
                expandedItems.add(tx.id)
                holder.foregroundCard.animate().translationX(offset).setDuration(200).start()
            }
        }
        
        holder.btnEdit.setOnClickListener {
            expandedItems.remove(tx.id)
            holder.foregroundCard.translationX = 0f
            onEditClick(tx)
        }

        holder.btnDelete.setOnClickListener {
            expandedItems.remove(tx.id)
            holder.foregroundCard.translationX = 0f
            onDeleteClick(tx)
        }
    }

    override fun getItemCount() = transactions.size
}
