package com.example.application

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.application.databinding.ActivityMainBinding
import com.example.application.databinding.DialogAddTaskBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var notificationHelper: NotificationHelper
    private var allTasksList: List<TaskEntity> = emptyList()

    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)

        // Request runtime notification permission for API 33+
        requestNotificationPermission()

        // Set logged-in username in top app bar
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val activeUsername = prefs.getString("username", "User")
        binding.topAppBar.title = "TaskPulse - $activeUsername"

        setupRecyclerView()
        setupTopBar()
        setupPriorityTabs()
        setupNetworkMonitoring()
        observeDatabase()

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the saved locale is applied instantly when returning from SettingsActivity
        val prefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("My_Lang", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { updatedTask ->
            lifecycleScope.launch {
                database.taskDao().updateTask(updatedTask)

                // Trigger push notification when a task is marked complete
                if (updatedTask.isCompleted) {
                    notificationHelper.sendNotification(
                        title = "Task Completed! 🎉",
                        message = "Great job finishing: '${updatedTask.title}'"
                    )
                }
            }
        }
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_create_task_title))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val title = dialogBinding.etTitle.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                val category = dialogBinding.etCategory.text.toString().trim().ifEmpty { "General" }

                val priority = when (dialogBinding.rgPriority.checkedRadioButtonId) {
                    R.id.rbHigh -> "High"
                    R.id.rbLow -> "Low"
                    else -> "Medium"
                }

                if (title.isNotEmpty()) {
                    lifecycleScope.launch {
                        val newTask = TaskEntity(
                            title = title,
                            description = description,
                            category = category,
                            priority = priority,
                            isSynced = false
                        )
                        database.taskDao().insertTask(newTask)
                        Toast.makeText(this@MainActivity, getString(R.string.msg_task_added), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.msg_title_empty), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun setupTopBar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish() // Finishes MainActivity so it cleanly recreates itself with the new language on return
                    true
                }
                R.id.action_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            // 1. Wipe local Room database so next account starts clean
            database.taskDao().deleteAllTasks()

            // 2. Clear active user session
            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 3. Navigate back to LoginActivity and clear activity stack
            Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupPriorityTabs() {
        binding.tabPriorityFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterTasksByPriority(tab?.text.toString())
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeDatabase() {
        lifecycleScope.launch {
            database.taskDao().getAllTasks().collectLatest { tasks ->
                allTasksList = tasks
                val selectedTabPosition = binding.tabPriorityFilter.selectedTabPosition
                val currentTab = binding.tabPriorityFilter.getTabAt(
                    if (selectedTabPosition >= 0) selectedTabPosition else 0
                )?.text.toString()

                filterTasksByPriority(currentTab)
                updateStreakCounter(tasks)
            }
        }
    }

    private fun filterTasksByPriority(priority: String) {
        val filtered = when (priority) {
            "High" -> allTasksList.filter { it.priority.equals("High", ignoreCase = true) }
            "Medium" -> allTasksList.filter { it.priority.equals("Medium", ignoreCase = true) }
            "Low" -> allTasksList.filter { it.priority.equals("Low", ignoreCase = true) }
            else -> allTasksList
        }
        taskAdapter.updateTasks(filtered)
    }

    private fun updateStreakCounter(tasks: List<TaskEntity>) {
        val completedCount = tasks.count { it.isCompleted }
        binding.tvStreakCounter.text = "🔥 $completedCount Tasks Done"
        binding.progressHabit.progress = if (tasks.isNotEmpty()) {
            (completedCount * 100) / tasks.size
        } else 0
    }

    private fun setupNetworkMonitoring() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()

        connectivityManager.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        binding.tvSyncStatus.visibility = View.GONE
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        binding.tvSyncStatus.visibility = View.VISIBLE
                        binding.tvSyncStatus.text = getString(R.string.offline_mode_msg)
                    }
                }
            }
        )
    }
}