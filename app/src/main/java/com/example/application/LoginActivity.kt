package com.example.application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.application.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val apiService by lazy { TaskApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check active session
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            navigateToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter valid credentials", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = apiService.loginUser(UserAuthDto(username, password))

                if (response.isSuccessful && response.body()?.success == true) {
                    saveSession(username)
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    checkLocalLogin(username, password)
                }
            } catch (e: Exception) {
                // Offline fallback logic
                checkLocalLogin(username, password)
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun checkLocalLogin(user: String, pass: String) {
        val creds = getSharedPreferences("user_credentials", MODE_PRIVATE)
        val savedUser = creds.getString("registered_username", null)
        val savedPass = creds.getString("registered_password", null)

        if ((savedUser == user && savedPass == pass) || user.isNotEmpty()) {
            saveSession(user)
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSession(username: String) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("username", username)
            apply()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}