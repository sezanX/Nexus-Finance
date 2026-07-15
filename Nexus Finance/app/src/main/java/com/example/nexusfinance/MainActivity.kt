package com.example.nexusfinance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.nexusfinance.databinding.ActivityMainBinding
import com.example.nexusfinance.data.FinanceRepository
import com.example.nexusfinance.data.FinanceAnalytics
import com.example.nexusfinance.model.*
import com.example.nexusfinance.ui.TransactionAdapter
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.DatePickerDialog
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: FinanceRepository
    private lateinit var transactionAdapter: TransactionAdapter
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private var currentSnapshot: FinanceSnapshot? = null
    private var currentTotalBudget: Double = 0.0
    private var currentSymbol: String = "৳"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = FinanceRepository(this)

        setupRecyclerView()
        setupListeners()
        setupNavigation()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onEditClick = { transaction ->
                showTransactionDialog(transaction)
            },
            onDeleteClick = { transaction ->
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this ${transaction.category} transaction?")
                    .setPositiveButton("Delete") { _, _ ->
                        scope.launch(Dispatchers.IO) {
                            repository.deleteTransaction(transaction.id)
                            loadDashboardData()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvRecentTransactions.adapter = transactionAdapter

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val transaction = transactionAdapter.getTransactionAt(position)
                    scope.launch(Dispatchers.IO) {
                        repository.deleteTransaction(transaction.id)
                        loadDashboardData()
                        
                        withContext(Dispatchers.Main) {
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "Transaction deleted",
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                            ).setAction("Undo") {
                                scope.launch(Dispatchers.IO) {
                                    repository.addTransaction(transaction)
                                    loadDashboardData()
                                }
                            }.show()
                        }
                    }
                }

                override fun onSelectedChanged(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, actionState: Int) {
                    if (viewHolder != null) {
                        val foregroundView = (viewHolder as TransactionAdapter.ViewHolder).foregroundCard
                        androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(foregroundView)
                    }
                }

                override fun onChildDraw(
                    c: android.graphics.Canvas,
                    recyclerView: androidx.recyclerview.widget.RecyclerView,
                    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val foregroundView = (viewHolder as TransactionAdapter.ViewHolder).foregroundCard
                    androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive
                    )
                }

                override fun clearView(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
                    val foregroundView = (viewHolder as TransactionAdapter.ViewHolder).foregroundCard
                    androidx.recyclerview.widget.ItemTouchHelper.Callback.getDefaultUIUtil().clearView(foregroundView)
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(binding.rvRecentTransactions)
    }

    private fun setupNavigation() {
        binding.bottomNav.ivNavLogs.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
        }
        binding.bottomNav.ivNavStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
        }
        binding.bottomNav.ivNavProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
        }
        
        // Highlight current tab
        binding.bottomNav.ivNavHome.setColorFilter(getColor(R.color.accent_red))
    }

    private fun setupListeners() {
        binding.bottomNav.fabAddTransaction.setOnClickListener {
            showTransactionDialog(null)
        }
        
        binding.btnViewInsights.setOnClickListener {
            showAiAdvisor()
        }

        binding.ivAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
        }
        
        binding.ivNotification.setOnClickListener {
            showNotificationsBottomSheet()
        }

        
        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
        }
        
        setupQuickSpendHub()
    }

    private fun setupQuickSpendHub() {
        val foodView = binding.hsvHub.findViewById<View>(R.id.hubFood)
        foodView.findViewById<TextView>(R.id.tvCategoryName).text = "Food"
        foodView.findViewById<ImageView>(R.id.ivCategoryIcon).setImageResource(R.drawable.ic_colorful_food)
        foodView.setOnClickListener { showQuickAddDialog("Food") }

        val otherView = binding.hsvHub.findViewById<View>(R.id.hubOther)
        otherView.findViewById<TextView>(R.id.tvCategoryName).text = "Other"
        otherView.findViewById<ImageView>(R.id.ivCategoryIcon).setImageResource(R.drawable.ic_colorful_other)
        otherView.setOnClickListener { showQuickAddDialog("Other") }

        val transView = binding.hsvHub.findViewById<View>(R.id.hubTransport)
        transView.findViewById<TextView>(R.id.tvCategoryName).text = "Transport"
        transView.findViewById<ImageView>(R.id.ivCategoryIcon).setImageResource(R.drawable.ic_colorful_transport)
        transView.setOnClickListener { showQuickAddDialog("Transport") }

        val studyView = binding.hsvHub.findViewById<View>(R.id.hubStudy)
        studyView.findViewById<TextView>(R.id.tvCategoryName).text = "Study"
        studyView.findViewById<ImageView>(R.id.ivCategoryIcon).setImageResource(R.drawable.ic_colorful_study)
        studyView.setOnClickListener { showQuickAddDialog("Study") }
    }

    private fun showQuickAddDialog(category: String) {
        // Simple transaction dialog with category pre-filled
        val safeCategory = if (category.isBlank()) "Uncategorized" else category
        showTransactionDialog(Transaction(category = safeCategory, amount = 0.0, type = TransactionType.EXPENSE, date = System.currentTimeMillis()))
    }

    private fun showAiAdvisor() {
        val snapshot = currentSnapshot ?: return
        val summary = "Balance: $currentSymbol${snapshot.balance}, Income: $currentSymbol${snapshot.income}, Expense: $currentSymbol${snapshot.expense}, Top Category: ${snapshot.topCategory?.category ?: "None"}"
        
        val intent = Intent(this, AiChatActivity::class.java).apply {
            putExtra("EXTRA_FINANCE_CONTEXT", summary)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
    }

    private fun loadDashboardData() {
        scope.launch(Dispatchers.IO) {
            val allTx = repository.getAllTransactions()
            val filteredTx = FinanceAnalytics.filterTransactions(allTx, DateRange.MONTH)
            
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            val budgets = repository.getBudgets(currentMonth, currentYear)
            
            val snapshot = FinanceAnalytics.calculateDashboardSnapshot(filteredTx, budgets, currentMonth, currentYear)
            val chartRange = repository.getSetting("chart_range", "Weekly")
            val cashflow = FinanceAnalytics.getCashflowByRange(allTx, chartRange)
            val symbol = repository.getSetting("currency_symbol", "$")
            val userName = repository.getSetting("user_name", "Sezan Mahmood")
            val userPhoto = repository.getSetting("user_photo", "")

            withContext(Dispatchers.Main) {
                currentSnapshot = snapshot
                currentTotalBudget = budgets.sumOf { it.limitAmount }
                currentSymbol = symbol
                
                binding.tvUserName.text = userName
                if (userPhoto.isNotEmpty()) {
                    binding.ivAvatar.imageTintList = null
                    binding.ivAvatar.load(userPhoto) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                        error(R.drawable.bg_circle_dark)
                    }
                }
                
                binding.tvBalance.text = "$symbol${String.format("%.2f", snapshot.balance)}"
                binding.tvSpentLabel.text = "$symbol${String.format("%.2f", snapshot.expense)} spent of monthly target"
                
                binding.tvRecentSub.text = when (chartRange) {
                    "Weekly" -> "Last 7 days spend trend"
                    "Monthly" -> "Last 6 months spend trend"
                    "Yearly" -> "Last 5 years spend trend"
                    "All Time" -> "All time spend trend"
                    else -> "Spend trend"
                }
                
                binding.miniChart.setLineColor(android.graphics.Color.WHITE)
                binding.miniChart.setData(cashflow)
                
                transactionAdapter.submitList(allTx.take(4), symbol)
                

            }
        }
    }

    private fun showTransactionDialog(existingTx: Transaction?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transaction, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgType)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val tvDateSelect = dialogView.findViewById<TextView>(R.id.tvDateSelect)
        
        var selectedTimestamp = existingTx?.date ?: System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        fun updateDateDisplay() {
            tvDateSelect.text = "Date: ${dateFormat.format(Date(selectedTimestamp))}"
        }
        updateDateDisplay()
        
        tvDateSelect.setOnClickListener {
            val calendar = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedTimestamp = calendar.timeInMillis
                updateDateDisplay()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        if (existingTx != null) {
            if (existingTx.amount > 0) etAmount.setText(existingTx.amount.toString())
            etCategory.setText(existingTx.category)
            etNote.setText(existingTx.note)
            if (existingTx.type == TransactionType.INCOME) rbIncome.isChecked = true
            else rbExpense.isChecked = true
        }

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            .create()
            
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val rawCategory = etCategory.text.toString().trim()
            val category = if (rawCategory.isBlank()) "Uncategorized" else rawCategory
            val note = etNote.text.toString().trim()
            val type = if (rbIncome.isChecked) TransactionType.INCOME else TransactionType.EXPENSE
            
            if (amount > 0 && category.isNotEmpty()) {
                val newTx = Transaction(
                    id = existingTx?.id ?: 0,
                    amount = amount,
                    category = category,
                    type = type,
                    date = selectedTimestamp,
                    note = note
                )
                
                scope.launch(Dispatchers.IO) {
                    if (newTx.id == 0L) repository.addTransaction(newTx)
                    else repository.updateTransaction(newTx)
                    loadDashboardData()
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showNotificationsBottomSheet() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this, R.style.Theme_NexusFinance_BottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_notifications, null)
        bottomSheet.setContentView(view)
        
        val rvNotifications = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvNotifications)
        val layoutEmpty = view.findViewById<android.widget.LinearLayout>(R.id.layoutEmptyNotifications)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnCloseNotifications)
        val tvMarkAllRead = view.findViewById<android.widget.TextView>(R.id.tvMarkAllRead)

        // Generate dynamic notifications
        val notifications = mutableListOf<com.example.nexusfinance.ui.NotificationItem>()
        
        // 1. Budget Warning
        val snapshot = currentSnapshot
        if (snapshot != null && currentTotalBudget > 0 && snapshot.expense > currentTotalBudget) {
            val exceedAmount = snapshot.expense - currentTotalBudget
            notifications.add(
                com.example.nexusfinance.ui.NotificationItem(
                    id = "1",
                    title = "Budget Exceeded",
                    message = "You have exceeded your monthly budget by $currentSymbol${String.format("%.2f", exceedAmount)}.",
                    time = "Just now",
                    iconResId = R.drawable.ic_alert_luxury,
                    iconTintResId = R.color.accent_red
                )
            )
        }
        
        // 2. Welcome Notification (if no warning)
        if (notifications.isEmpty()) {
             // Let's add a welcome notification to make it "feature able" and populated
             notifications.add(
                 com.example.nexusfinance.ui.NotificationItem(
                    id = "welcome",
                    title = "Welcome to Nexus AI",
                    message = "Elevate your financial future with intelligent insights.",
                    time = "Today",
                    iconResId = R.drawable.ic_home_luxury,
                    iconTintResId = R.color.text_primary
                )
             )
        }

        if (notifications.isEmpty()) {
            rvNotifications.visibility = android.view.View.GONE
            layoutEmpty.visibility = android.view.View.VISIBLE
            tvMarkAllRead.visibility = android.view.View.GONE
        } else {
            rvNotifications.visibility = android.view.View.VISIBLE
            layoutEmpty.visibility = android.view.View.GONE
            tvMarkAllRead.visibility = android.view.View.VISIBLE
            
            rvNotifications.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
            rvNotifications.adapter = com.example.nexusfinance.ui.NotificationAdapter(notifications)
        }

        tvMarkAllRead.setOnClickListener {
            // "Mark all as read" logic
            rvNotifications.visibility = android.view.View.GONE
            layoutEmpty.visibility = android.view.View.VISIBLE
            tvMarkAllRead.visibility = android.view.View.GONE
        }

        btnClose.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}