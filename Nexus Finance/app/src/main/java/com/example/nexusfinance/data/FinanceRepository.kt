package com.example.nexusfinance.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.nexusfinance.model.Budget
import com.example.nexusfinance.model.Transaction
import com.example.nexusfinance.model.TransactionType

class FinanceRepository(context: Context) {
    private val dbHelper = FinanceDatabaseHelper(context)

    fun addTransaction(transaction: Transaction): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("amount", transaction.amount)
            put("category", transaction.category)
            put("type", transaction.type.label)
            put("date", transaction.date)
            put("note", transaction.note)
        }
        return db.insert("transactions", null, values)
    }

    fun updateTransaction(transaction: Transaction): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("amount", transaction.amount)
            put("category", transaction.category)
            put("type", transaction.type.label)
            put("date", transaction.date)
            put("note", transaction.note)
        }
        return db.update("transactions", values, "id=?", arrayOf(transaction.id.toString()))
    }

    fun deleteTransaction(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete("transactions", "id=?", arrayOf(id.toString()))
    }

    fun getAllTransactions(): List<Transaction> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "transactions", null, null, null, null, null, "date DESC"
        )
        val list = mutableListOf<Transaction>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val amount = getDouble(getColumnIndexOrThrow("amount"))
                val category = getString(getColumnIndexOrThrow("category"))
                val typeStr = getString(getColumnIndexOrThrow("type"))
                val date = getLong(getColumnIndexOrThrow("date"))
                val note = getString(getColumnIndexOrThrow("note"))

                list.add(Transaction(id, amount, category, TransactionType.entries.firstOrNull { it.label == typeStr } ?: TransactionType.EXPENSE, date, note))
            }
            close()
        }
        return list
    }

    fun saveBudget(budget: Budget): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("category", budget.category)
            put("limit_amount", budget.limitAmount)
            put("month", budget.month)
            put("year", budget.year)
        }
        return db.insertWithOnConflict("budgets", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteBudget(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete("budgets", "id=?", arrayOf(id.toString()))
    }

    fun getBudgets(month: Int, year: Int): List<Budget> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "budgets", null, "month=? AND year=?", arrayOf(month.toString(), year.toString()), null, null, null
        )
        val list = mutableListOf<Budget>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val category = getString(getColumnIndexOrThrow("category"))
                val limitAmount = getDouble(getColumnIndexOrThrow("limit_amount"))
                list.add(Budget(id, category, limitAmount, month, year))
            }
            close()
        }
        return list
    }

    fun getSetting(key: String, defaultValue: String): String {
        val db = dbHelper.readableDatabase
        val cursor = db.query("settings", arrayOf("setting_value"), "setting_key=?", arrayOf(key), null, null, null)
        var result = defaultValue
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }

    fun saveSetting(key: String, value: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("setting_key", key)
            put("setting_value", value)
        }
        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteAllData() {
        val db = dbHelper.writableDatabase
        db.delete("transactions", null, null)
        db.delete("budgets", null, null)
    }
}
