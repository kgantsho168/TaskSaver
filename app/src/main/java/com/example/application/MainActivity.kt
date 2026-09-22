package com.example.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.application.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadTasks()
        observeNetworkStatus()
        setupBackgroundSync()

        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(emptyList()) { updatedTask ->
            lifecycleScope.launch(Dispatchers.IO) {
                database.taskDao().updateTask(updatedTask)
            }
        }
        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            database.taskDao().getAllTasks().collectLatest { tasks ->
                taskAdapter.updateTasks(tasks)
            }
        }
    }

    private fun observeNetworkStatus() {
        val networkObserver = NetworkObserver(this)
        lifecycleScope.launch {
            networkObserver.isOnline.collect { isOnline ->
                if (isOnline) {
                    binding.tvNetworkStatus.visibility = View.GONE
                } else {
                    binding.tvNetworkStatus.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TaskSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)

        AlertDialog.Builder(this)
            .setTitle("New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val category = etCategory.text.toString().trim().ifEmpty { "General" }

                if (title.isNotEmpty()) {
                    val newTask = TaskEntity(
                        title = title,
                        description = description,
                        category = category
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        database.taskDao().insertTask(newTask)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}