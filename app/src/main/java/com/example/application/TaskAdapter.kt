package com.example.application

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.application.databinding.ItemTaskBinding

class TaskAdapter(
    private var tasks: List<TaskEntity>,
    private val onTaskToggle: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.binding.apply {
            tvTitle.text = task.title
            tvDescription.text = task.description
            chipCategory.text = task.category
            cbCompleted.isChecked = task.isCompleted

            // Apply strike-through decoration if task is marked complete
            if (task.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onTaskToggle(task.copy(isCompleted = isChecked))
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskEntity>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}