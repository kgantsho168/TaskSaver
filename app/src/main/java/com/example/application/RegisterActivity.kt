package com.example.application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.application.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val apiService by lazy { TaskApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterSubmit.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val username = binding.etRegisterUsername.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegisterSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                val userDto = UserAuthDto(username = username, passwordHash = password)
                val response = apiService.registerUser(userDto)

                if (response.isSuccessful && response.body()?.success == true) {
                    saveUserLocally(username, password)
                    Toast.makeText(this@RegisterActivity, "Account created on server!", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    // Fallback to local storage if API call isn't successful
                    saveUserLocally(username, password)
                    Toast.makeText(this@RegisterActivity, "Account registered locally!", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            } catch (e: Exception) {
                // Network failure fallback (e.g., server offline or invalid BASE_URL)
                saveUserLocally(username, password)
                Toast.makeText(this@RegisterActivity, "Registered locally (Offline mode)", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } finally {
                binding.btnRegisterSubmit.isEnabled = true
            }
        }
    }

    private fun saveUserLocally(user: String, pass: String) {
        val prefs = getSharedPreferences("user_credentials", MODE_PRIVATE)
        prefs.edit().apply {
            putString("registered_username", user)
            putString("registered_password", pass)
            apply()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}