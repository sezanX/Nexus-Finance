package com.example.nexusfinance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.nexusfinance.databinding.ActivityLoginBinding
import com.example.nexusfinance.data.FinanceRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: FinanceRepository

    companion object {
        private const val RC_SIGN_IN = 1001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = FinanceRepository(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if already logged in
        val userEmail = repository.getSetting("user_email", "")
        val isGuest = repository.getSetting("is_guest", "false").toBoolean()
        
        if (userEmail.isNotEmpty() || isGuest) {
            val biometricEnabled = repository.getSetting("biometric_enabled", "false").toBoolean()
            if (biometricEnabled) {
                binding.btnGoogleSignIn.visibility = View.GONE
                binding.btnGuestMode.visibility = View.GONE
                binding.btnUnlock.visibility = View.VISIBLE
                binding.tvWelcome.text = "Welcome Back"
                
                binding.btnUnlock.setOnClickListener {
                    showBiometricPrompt()
                }
                
                showBiometricPrompt()
            } else {
                navigateToMain()
            }
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnGuestMode.setOnClickListener {
            repository.saveSetting("is_guest", "true")
            repository.saveSetting("user_name", "Elite Guest")
            navigateToMain()
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    Log.d(TAG, "Google Sign-In successful: ${account.email}")
                    repository.saveSetting("user_email", account.email ?: "")
                    repository.saveSetting("user_name", account.displayName ?: "Elite User")
                    repository.saveSetting("user_photo", account.photoUrl?.toString() ?: "")
                    repository.saveSetting("is_guest", "false")
                    navigateToMain()
                } else {
                    Log.e(TAG, "Google Sign-In failed: Account is null")
                    Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed: Error code ${e.statusCode}, Message: ${e.message}")
                Toast.makeText(this, "Google Sign-In Error (${e.statusCode}). Check your connection.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed: Unexpected error", e)
                Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Biometric auth error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToMain()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Nexus Finance")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
