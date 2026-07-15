package com.example.nexusfinance.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.nexusfinance.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InsightsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(
            budgetUsedRatio: Float,
            remainingBudget: Double,
            topCategoryName: String,
            topCategorySpent: Double,
            topCategoryPercent: Float,
            symbol: String
        ): InsightsBottomSheet {
            val args = Bundle().apply {
                putFloat("budgetUsedRatio", budgetUsedRatio)
                putDouble("remainingBudget", remainingBudget)
                putString("topCategoryName", topCategoryName)
                putDouble("topCategorySpent", topCategorySpent)
                putFloat("topCategoryPercent", topCategoryPercent)
                putString("symbol", symbol)
            }
            val fragment = InsightsBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_insights, container, false)
        
        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
        
        val args = arguments ?: return view
        val symbol = args.getString("symbol", "৳")
        
        val budgetUsedRatio = args.getFloat("budgetUsedRatio", 0f)
        val remainingBudget = args.getDouble("remainingBudget", 0.0)
        
        val tvBudgetAlertTitle = view.findViewById<TextView>(R.id.tvBudgetAlertTitle)
        val tvBudgetAlertDesc = view.findViewById<TextView>(R.id.tvBudgetAlertDesc)
        
        if (budgetUsedRatio > 1.0f) {
            tvBudgetAlertTitle.text = "Budget Exceeded!"
            tvBudgetAlertTitle.setTextColor(android.graphics.Color.parseColor("#E53935"))
            tvBudgetAlertDesc.text = "${(budgetUsedRatio * 100).toInt()}% used — Over by $symbol${String.format("%.0f", -remainingBudget)} this month."
        } else {
            tvBudgetAlertTitle.text = "Budget On Track"
            tvBudgetAlertDesc.text = "${(budgetUsedRatio * 100).toInt()}% used — $symbol${String.format("%.0f", remainingBudget)} remaining this month."
        }
        
        val topCategoryName = args.getString("topCategoryName", "None")
        val topCategorySpent = args.getDouble("topCategorySpent", 0.0)
        val topCategoryPercent = args.getFloat("topCategoryPercent", 0f)
        
        view.findViewById<TextView>(R.id.tvTopCategoryTitle).text = "Top Category: $topCategoryName"
        view.findViewById<TextView>(R.id.tvTopCategoryDesc).text = "$symbol${String.format("%.0f", topCategorySpent)} spent (${(topCategoryPercent * 100).toInt()}% of total). Consider diversifying."
        
        return view
    }
}
