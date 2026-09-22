package com.example.application

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)

        binding.switchAutoSync.isChecked = prefsManager.isAutoSyncEnabled()

        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoSync(isChecked)
            Toast.makeText(this, "Auto Sync: $isChecked", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            prefsManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}