package com.example.nexusfinance.model

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense")
}

enum class DateRange {
    ALL, TODAY, WEEK, MONTH, YEAR
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Long,
    val note: String = ""
)

data class Budget(
    val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int
)

data class CategoryTotal(
    val category: String,
    val amount: Double,
    val percentOfTotal: Float
)

data class CashflowPoint(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

data class BudgetProgress(
    val budget: Budget,
    val spent: Double,
    val limitAmount: Double,
    val isOverspent: Boolean
) {
    val remaining: Double get() = limitAmount - spent
    val ratio: Float get() = if (limitAmount > 0) (spent / limitAmount).toFloat() else 0f
}

data class FinanceSnapshot(
    val balance: Double,
    val income: Double,
    val expense: Double,
    val savingsRate: Double,
    val dailySpendPace: Double,
    val topCategory: CategoryTotal?,
    val overspentBudgets: Int,
    val isTrackingActive: Boolean
)
