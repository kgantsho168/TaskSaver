package com.example.application

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = SharedPreferencesManager(this)

        binding.switchAutoSync.isChecked = prefsManager.isAutoSyncEnabled()

        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setAutoSync(isChecked)
            Toast.makeText(this, "Auto Sync: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Set initial checked state based on saved language preference
        val currentLang = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
            .getString("My_Lang", "en")

        when (currentLang) {
            "af" -> binding.radioAfrikaans.isChecked = true
            "zu" -> binding.radioZulu.isChecked = true
            else -> binding.radioEnglish.isChecked = true
        }

        // Handle radio group selection
        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguage = when (checkedId) {
                R.id.radioAfrikaans -> "af"
                R.id.radioZulu -> "zu"
                else -> "en"
            }

            setLocale(selectedLanguage)
            recreate()
        }

        binding.btnLogout.setOnClickListener {
            prefsManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setLocale(langCode: String) {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("My_Lang", langCode).apply()

        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun loadLocale() {
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("My_Lang", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}