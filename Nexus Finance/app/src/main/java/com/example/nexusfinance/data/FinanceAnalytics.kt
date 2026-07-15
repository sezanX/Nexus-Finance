package com.example.nexusfinance.data

import com.example.nexusfinance.model.*
import java.util.Calendar
import java.util.Locale

object FinanceAnalytics {

    fun filterTransactions(transactions: List<Transaction>, dateRange: DateRange): List<Transaction> {
        val calendar = Calendar.getInstance()
        
        return when (dateRange) {
            DateRange.ALL -> transactions
            DateRange.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                transactions.filter { it.date >= startOfDay }
            }
            DateRange.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startOfWeek = calendar.timeInMillis
                transactions.filter { it.date >= startOfWeek }
            }
            DateRange.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                transactions.filter { it.date >= startOfMonth }
            }
            DateRange.YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfYear = calendar.timeInMillis
                transactions.filter { it.date >= startOfYear }
            }
        }
    }

    fun calculateDashboardSnapshot(transactions: List<Transaction>, budgets: List<Budget>, currentMonth: Int, currentYear: Int): FinanceSnapshot {
        var income = 0.0
        var expense = 0.0
        
        transactions.forEach {
            if (it.type == TransactionType.INCOME) income += it.amount
            else expense += it.amount
        }
        
        val balance = income - expense
        val savingsRate = if (income > 0) ((income - expense) / income) * 100 else 0.0
        
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val dailySpendPace = if (currentDay > 0) expense / currentDay else 0.0

        val categories = getCategoryTotals(transactions)
        val topCategory = categories.firstOrNull()

        var overspentCount = 0
        budgets.forEach { budget ->
            val spent = transactions.filter { it.type == TransactionType.EXPENSE && it.category == budget.category && isSameMonthAndYear(it.date, budget.month, budget.year) }.sumOf { it.amount }
            if (spent > budget.limitAmount) overspentCount++
        }

        return FinanceSnapshot(
            balance = balance,
            income = income,
            expense = expense,
            savingsRate = savingsRate,
            dailySpendPace = dailySpendPace,
            topCategory = topCategory,
            overspentBudgets = overspentCount,
            isTrackingActive = transactions.isNotEmpty()
        )
    }

    fun getCategoryTotals(transactions: List<Transaction>): List<CategoryTotal> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }
        if (totalExpense == 0.0) return emptyList()

        return expenses.groupBy { it.category }
            .map { (category, list) ->
                val amount = list.sumOf { it.amount }
                CategoryTotal(category, amount, (amount / totalExpense).toFloat() * 100)
            }
            .sortedByDescending { it.amount }
    }

    fun getBudgetProgress(transactions: List<Transaction>, budgets: List<Budget>): List<BudgetProgress> {
        return budgets.map { budget ->
            val spent = transactions.filter { 
                it.type == TransactionType.EXPENSE && 
                it.category == budget.category && 
                isSameMonthAndYear(it.date, budget.month, budget.year) 
            }.sumOf { it.amount }
            
            BudgetProgress(budget, spent, budget.limitAmount, spent > budget.limitAmount)
        }
    }

    fun getCashflowByRange(transactions: List<Transaction>, rangeStr: String): List<CashflowPoint> {
        return when (rangeStr) {
            "Weekly" -> getWeeklyCashflow(transactions)
            "Yearly" -> getYearlyCashflow(transactions)
            "All Time" -> getAllTimeCashflow(transactions)
            else -> getSixMonthCashflow(transactions)
        }
    }

    private fun getWeeklyCashflow(transactions: List<Transaction>): List<CashflowPoint> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<CashflowPoint>()
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val day = calendar.get(Calendar.DAY_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            val label = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
            
            val dayTxs = transactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.DAY_OF_YEAR) == day && cal.get(Calendar.YEAR) == year
            }
            val income = dayTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = dayTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            result.add(CashflowPoint(label, income, expense))
        }
        return result
    }

    private fun getYearlyCashflow(transactions: List<Transaction>): List<CashflowPoint> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<CashflowPoint>()
        for (i in 4 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.YEAR, -i)
            val year = calendar.get(Calendar.YEAR)
            val label = year.toString()
            
            val yearTxs = transactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.YEAR) == year
            }
            val income = yearTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = yearTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            result.add(CashflowPoint(label, income, expense))
        }
        return result
    }

    private fun getAllTimeCashflow(transactions: List<Transaction>): List<CashflowPoint> {
        if (transactions.isEmpty()) return emptyList()
        val minYear = Calendar.getInstance().apply { timeInMillis = transactions.minOf { it.date } }.get(Calendar.YEAR)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        val result = mutableListOf<CashflowPoint>()
        for (year in minYear..currentYear) {
            val label = year.toString()
            val yearTxs = transactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.YEAR) == year
            }
            val income = yearTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = yearTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            result.add(CashflowPoint(label, income, expense))
        }
        return result
    }

    fun getSixMonthCashflow(transactions: List<Transaction>): List<CashflowPoint> {
        val calendar = Calendar.getInstance()
        val result = mutableListOf<CashflowPoint>()
        
        for (i in 5 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MONTH, -i)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val monthLabel = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
            
            val monthTxs = transactions.filter { isSameMonthAndYear(it.date, month, year) }
            val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            
            result.add(CashflowPoint(monthLabel, income, expense))
        }
        return result
    }

    private fun isSameMonthAndYear(timestamp: Long, targetMonth: Int, targetYear: Int): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.MONTH) + 1 == targetMonth && cal.get(Calendar.YEAR) == targetYear
    }

    fun generateInsightText(snapshot: FinanceSnapshot): String {
        if (!snapshot.isTrackingActive) return "Add some transactions to see your financial insights."
        if (snapshot.overspentBudgets > 0) return "You have overspent in ${snapshot.overspentBudgets} budget categories. Review your spending!"
        if (snapshot.expense > snapshot.income && snapshot.income > 0) return "Your expenses exceed your income this period."
        if (snapshot.savingsRate >= 20.0) return "Great job! Your savings rate is ${String.format(Locale.getDefault(), "%.1f", snapshot.savingsRate)}%."
        if (snapshot.topCategory != null) return "Your top spending category is ${snapshot.topCategory.category}."
        return "You are on track with your finances."
    }
}
