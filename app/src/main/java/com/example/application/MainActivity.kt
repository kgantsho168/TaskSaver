package com.example.application

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.application.databinding.ActivityMainBinding
import com.example.application.databinding.DialogAddTaskBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var taskAdapter: TaskAdapter
    private var allTasksList: List<TaskEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

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

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { updatedTask ->
            lifecycleScope.launch {
                database.taskDao().updateTask(updatedTask)
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
            .setTitle("Create New Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
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
                        Toast.makeText(this@MainActivity, "Task Added", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTopBar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
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
                        binding.tvSyncStatus.text = "Offline Mode - Changes queued locally"
                    }
                }
            }
        )
    }
}