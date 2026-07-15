package com.example.nexusfinance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexusfinance.databinding.ActivityLogsBinding
import com.example.nexusfinance.data.FinanceRepository
import com.example.nexusfinance.model.Transaction
import com.example.nexusfinance.ui.TransactionAdapter
import kotlinx.coroutines.*

class LogsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogsBinding
    private lateinit var repository: FinanceRepository
    private lateinit var adapter: TransactionAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val exportLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { exportLogsToUri(it) }
    }

    private val importLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importLogsFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = FinanceRepository(this)
        setupRecyclerView()
        setupListeners()
        setupNavigation()
        loadLogs()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(onEditClick = { /* Handle click */ })
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnImport.setOnClickListener {
            showImportGuide()
        }
        binding.btnExport.setOnClickListener {
            exportLauncher.launch("nexus_finance_logs.csv")
        }
        binding.bottomNav.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLogs(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterLogs(query: String) {
        scope.launch(Dispatchers.IO) {
            val transactions = repository.getAllTransactions()
            val filtered = if (query.isEmpty()) transactions 
                          else transactions.filter { it.category.contains(query, ignoreCase = true) || it.note.contains(query, ignoreCase = true) }
            val symbol = repository.getSetting("currency_symbol", "$")
            withContext(Dispatchers.Main) {
                adapter.submitList(filtered, symbol)
            }
        }
    }

    private fun showImportGuide() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_guide, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.btnUpload).setOnClickListener { 
            importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
            dialog.dismiss() 
        }
        
        dialog.show()
    }

    private fun loadLogs() {
        scope.launch(Dispatchers.IO) {
            val transactions = repository.getAllTransactions()
            val symbol = repository.getSetting("currency_symbol", "$")
            withContext(Dispatchers.Main) {
                adapter.submitList(transactions, symbol)
            }
        }
    }

    private fun exportLogsToUri(uri: android.net.Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                val transactions = repository.getAllTransactions()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    java.io.OutputStreamWriter(outputStream).use { writer ->
                        writer.write("amount,category,type,date,note\n")
                        for (t in transactions) {
                            val note = t.note.replace(",", " ")
                            val category = t.category.replace(",", " ")
                            writer.write("${t.amount},${category},${t.type.name},${t.date},${note}\n")
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(this@LogsActivity, "Logs exported successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(this@LogsActivity, "Failed to export logs", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun importLogsFromUri(uri: android.net.Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.io.BufferedReader(java.io.InputStreamReader(inputStream)).use { reader ->
                        var headerSkipped = false
                        reader.forEachLine { line ->
                            if (!headerSkipped) {
                                headerSkipped = true
                                return@forEachLine
                            }
                            val parts = line.split(",")
                            if (parts.size >= 5) {
                                val amount = parts[0].toDoubleOrNull() ?: 0.0
                                val category = parts[1]
                                val typeStr = parts[2]
                                val type = if (typeStr == "INCOME") com.example.nexusfinance.model.TransactionType.INCOME else com.example.nexusfinance.model.TransactionType.EXPENSE
                                val date = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                                val note = parts[4]
                                
                                val tx = com.example.nexusfinance.model.Transaction(
                                    amount = amount, 
                                    category = category, 
                                    type = type, 
                                    date = date, 
                                    note = note
                                )
                                repository.addTransaction(tx)
                            }
                        }
                    }
                }
                loadLogs()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(this@LogsActivity, "Logs imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(this@LogsActivity, "Failed to import logs", android.widget.Toast.LENGTH_SHORT).show()
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
        binding.bottomNav.ivNavStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        binding.bottomNav.ivNavProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        
        binding.bottomNav.ivNavLogs.apply {
            setImageResource(R.drawable.ic_logs_pro)
            setColorFilter(getColor(R.color.accent_red))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
