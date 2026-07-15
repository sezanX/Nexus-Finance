package com.example.nexusfinance

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.nexusfinance.databinding.ActivityStatsBinding
import com.example.nexusfinance.data.FinanceRepository
import com.example.nexusfinance.data.FinanceAnalytics
import com.example.nexusfinance.model.*
import kotlinx.coroutines.*
import java.util.Calendar

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var repository: FinanceRepository
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = FinanceRepository(this)
        setupNavigation()
        setupListeners()
        loadStats()
    }

    private fun setupListeners() {
        binding.bottomNav.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.root.findViewById<View>(R.id.tvWeekTab)?.setOnClickListener { loadStats(DateRange.WEEK) }
        binding.root.findViewById<View>(R.id.tvMonthTab)?.setOnClickListener { loadStats(DateRange.MONTH) }
    }

    private fun loadStats(range: DateRange = DateRange.MONTH) {
        scope.launch(Dispatchers.IO) {
            val allTx = repository.getAllTransactions()
            val filteredTx = FinanceAnalytics.filterTransactions(allTx, range)
            
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            val budgets = repository.getBudgets(currentMonth, currentYear)
            
            val snapshot = FinanceAnalytics.calculateDashboardSnapshot(filteredTx, budgets, currentMonth, currentYear)
            
            val cashflow = if (range == DateRange.WEEK) {
                FinanceAnalytics.getCashflowByRange(allTx, "Weekly")
            } else {
                FinanceAnalytics.getCashflowByRange(allTx, "Monthly")
            }
            
            val symbol = repository.getSetting("currency_symbol", "$")
            
            val totalBudget = budgets.sumOf { it.limitAmount }

            withContext(Dispatchers.Main) {
                binding.mainChart.setLineColor(android.graphics.Color.BLACK)
                binding.mainChart.setData(cashflow)
                
                val peak = cashflow.maxByOrNull { it.expense }
                val low = cashflow.minByOrNull { it.expense }

                if (peak != null) {
                    binding.tvPeak.text = "↑ Peak: ${peak.monthLabel} ($symbol${String.format("%.0f", peak.expense)})"
                } else {
                    binding.tvPeak.text = "↑ Peak: -"
                }

                if (low != null) {
                    binding.tvLow.text = "↓ Low: ${low.monthLabel} ($symbol${String.format("%.0f", low.expense)})"
                } else {
                    binding.tvLow.text = "↓ Low: -"
                }
                
                binding.tvActualSpend.text = "$symbol${String.format("%.0f", snapshot.expense)}"
                binding.tvBudgetLimit.text = "Monthly Budget Limit: $symbol${String.format("%.0f", totalBudget)}"
                
                binding.pbForecast.max = totalBudget.toInt().coerceAtLeast(1)
                binding.pbForecast.progress = snapshot.expense.toInt()
                
                binding.tvDailyPace.text = "$symbol${String.format("%.0f", snapshot.dailySpendPace)} / day"
                
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val estMonthEnd = snapshot.dailySpendPace * daysInMonth
                binding.tvEstMonthEnd.text = "$symbol${String.format("%.0f", estMonthEnd)}"
                
                val overrun = estMonthEnd - totalBudget
                if (overrun > 0) {
                    binding.tvEstOverrun.text = "-$symbol${String.format("%.0f", overrun)}"
                    binding.tvEstOverrun.setTextColor(getColor(R.color.accent_red))
                    binding.tvWarning.text = "Warning: You're spending too fast! At this pace, you'll exceed your budget by $symbol${String.format("%.0f", overrun)}."
                    binding.tvWarning.visibility = View.VISIBLE
                } else {
                    binding.tvEstOverrun.text = "$symbol${String.format("%.0f", -overrun)}"
                    binding.tvEstOverrun.setTextColor(getColor(R.color.text_primary))
                    binding.tvWarning.visibility = View.GONE
                }
            }
        }
    }

    private fun setupNavigation() {
        binding.bottomNav.ivNavHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        binding.bottomNav.ivNavLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        binding.bottomNav.ivNavProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        
        binding.bottomNav.ivNavStats.setColorFilter(getColor(R.color.accent_red))
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
