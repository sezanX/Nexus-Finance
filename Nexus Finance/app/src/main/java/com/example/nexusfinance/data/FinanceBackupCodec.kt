package com.example.nexusfinance.data

import com.example.nexusfinance.model.Budget
import com.example.nexusfinance.model.Transaction
import com.google.gson.Gson

data class BackupMetadata(
    val app: String = "Nexus Finance",
    val schemaVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val accountEmail: String,
    val apiKeysIncluded: Boolean = false
)

data class BackupSettings(
    val currencySymbol: String
)

data class BackupData(
    val metadata: BackupMetadata,
    val settings: BackupSettings,
    val transactions: List<Transaction>,
    val budgets: List<Budget>
)

object FinanceBackupCodec {
    private val gson = Gson()

    fun encodeBackup(
        email: String,
        currencySymbol: String,
        transactions: List<Transaction>,
        budgets: List<Budget>
    ): String {
        val backupData = BackupData(
            metadata = BackupMetadata(accountEmail = email),
            settings = BackupSettings(currencySymbol = currencySymbol),
            transactions = transactions,
            budgets = budgets
        )
        return gson.toJson(backupData)
    }

    fun decodeBackup(json: String): BackupData? {
        return try {
            gson.fromJson(json, BackupData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
