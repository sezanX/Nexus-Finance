package com.example.nexusfinance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.nexusfinance.databinding.ActivityProfileBinding
import com.example.nexusfinance.data.FinanceRepository
import com.example.nexusfinance.model.Currency
import com.example.nexusfinance.ui.CurrencyAdapter
import kotlinx.coroutines.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var repository: FinanceRepository
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
                        val file = java.io.File(filesDir, fileName)
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()
                        
                        repository.saveSetting("user_photo", file.absolutePath)
                        
                        withContext(Dispatchers.Main) {
                            loadProfileData()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = FinanceRepository(this)
        setupNavigation()
        setupListeners()
        loadProfileData()
    }

    private fun setupListeners() {
        binding.btnReset.setOnClickListener {
            showResetConfirmation()
        }
        
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            repository.saveSetting("biometric_enabled", isChecked.toString())
            Toast.makeText(this, "Biometric security ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        binding.bottomNav.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.layoutCurrency.setOnClickListener {
            showCurrencyDialog()
        }

        binding.cardProfile.setOnClickListener {
            showEditNameDialog()
        }

        binding.ivProfileAvatar.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSignIn.setOnClickListener {
            repository.saveSetting("is_guest", "false")
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.layoutBudget.setOnClickListener {
            showBudgetDialog()
        }

        binding.layoutGraphRange.setOnClickListener {
            showGraphRangeDialog()
        }
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Wipe & Reset Data")
            .setMessage("Are you sure you want to delete all your financial data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                scope.launch(Dispatchers.IO) {
                    repository.deleteAllData()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ProfileActivity, "All data has been wiped.", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out? Your local data will remain on this device.")
            .setPositiveButton("Log Out") { _, _ ->
                repository.saveSetting("is_guest", "true")
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditNameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val etInput = dialogView.findViewById<android.widget.EditText>(R.id.etInput)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)

        tvTitle.text = "Edit Your Name"
        etInput.hint = "Name"
        etInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        etInput.setText(repository.getSetting("user_name", "Sezan Mahmood"))

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val newName = etInput.text.toString().trim()
            if (newName.isNotEmpty()) {
                repository.saveSetting("user_name", newName)
                loadProfileData()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val etInput = dialogView.findViewById<android.widget.EditText>(R.id.etInput)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)

        tvTitle.text = "Set Target Budget"
        etInput.hint = "Amount"
        etInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        etInput.setText(repository.getSetting("target_budget", "4000.00"))

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val newBudget = etInput.text.toString().trim()
            if (newBudget.isNotEmpty()) {
                repository.saveSetting("target_budget", newBudget)
                loadProfileData()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showGraphRangeDialog() {
        val options = arrayOf("Weekly", "Monthly", "Yearly", "All Time")
        val currentRange = repository.getSetting("chart_range", "Weekly")
        val checkedItem = options.indexOf(currentRange).takeIf { it >= 0 } ?: 0

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Graph Range")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val selected = options[which]
                repository.saveSetting("chart_range", selected)
                loadProfileData()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrencyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_currency, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setBackground(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            .create()

        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchCurrency)
        val rvCurrencies = dialogView.findViewById<RecyclerView>(R.id.rvCurrencies)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        val allCurrencies = listOf(
            Currency("USD", "$", "United States Dollar", "United States"),
            Currency("EUR", "€", "Euro", "European Union"),
            Currency("BDT", "৳", "Bangladeshi Taka", "Bangladesh"),
            Currency("GBP", "£", "British Pound", "United Kingdom"),
            Currency("JPY", "¥", "Japanese Yen", "Japan"),
            Currency("INR", "₹", "Indian Rupee", "India"),
            Currency("PKR", "₨", "Pakistani Rupee", "Pakistan"),
            Currency("ILS", "₪", "Palestinian Shekel", "Palestine"),
            Currency("IRR", "﷼", "Iranian Rial", "Iran"),
            Currency("AUD", "A$", "Australian Dollar", "Australia"),
            Currency("CAD", "C$", "Canadian Dollar", "Canada"),
            Currency("CHF", "Fr", "Swiss Franc", "Switzerland"),
            Currency("CNY", "¥", "Chinese Yuan", "China"),
            Currency("BRL", "R$", "Brazilian Real", "Brazil"),
            Currency("RUB", "₽", "Russian Ruble", "Russia"),
            Currency("ZAR", "R", "South African Rand", "South Africa"),
            Currency("SGD", "S$", "Singapore Dollar", "Singapore"),
            Currency("AED", "د.إ", "UAE Dirham", "United Arab Emirates"),
            Currency("SAR", "﷼", "Saudi Riyal", "Saudi Arabia")
        )

        val adapter = CurrencyAdapter(allCurrencies) { selectedCurrency ->
            repository.saveSetting("currency_symbol", selectedCurrency.symbol)
            repository.saveSetting("currency_code", selectedCurrency.code)
            repository.saveSetting("currency_name", selectedCurrency.name)
            loadProfileData()
            dialog.dismiss()
        }

        rvCurrencies.layoutManager = LinearLayoutManager(this)
        rvCurrencies.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filtered = allCurrencies.filter {
                    it.name.lowercase().contains(query) ||
                    it.country.lowercase().contains(query) ||
                    it.code.lowercase().contains(query)
                }
                adapter.submitList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun loadProfileData() {
        val userName = repository.getSetting("user_name", "Sezan Mahmood")
        val biometric = repository.getSetting("biometric_enabled", "false").toBoolean()
        
        val currName = repository.getSetting("currency_name", "United States Dollar")
        val currCode = repository.getSetting("currency_code", "USD")
        val currSymbol = repository.getSetting("currency_symbol", "$")
        val targetBudget = repository.getSetting("target_budget", "4000.00")
        val userPhoto = repository.getSetting("user_photo", "")
        
        binding.tvName.text = userName
        if (userPhoto.isNotEmpty()) {
            binding.ivProfileAvatar.imageTintList = null // Remove the tint so the real photo shows colors
            binding.ivProfileAvatar.load(userPhoto) {
                crossfade(true)
                transformations(CircleCropTransformation())
                error(R.drawable.bg_circle_dark)
            }
        }
        
        binding.switchBiometric.isChecked = biometric
        binding.tvCurrentCurrency.text = "$currName ($currCode)"
        binding.tvTargetBudget.text = "$currSymbol $targetBudget"
        
        val chartRange = repository.getSetting("chart_range", "Weekly")
        binding.tvCurrentGraphRange.text = chartRange

        val isGuest = repository.getSetting("is_guest", "false").toBoolean()
        binding.btnSignIn.visibility = if (isGuest) View.VISIBLE else View.GONE
        binding.btnLogout.visibility = if (!isGuest) View.VISIBLE else View.GONE
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
        binding.bottomNav.ivNavStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            overridePendingTransition(R.anim.ios_slide_in, R.anim.ios_slide_out)
            finish()
        }
        
        binding.bottomNav.ivNavProfile.setColorFilter(getColor(R.color.accent_red))
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
