package com.example.taskmanager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.databinding.TaskItemBinding
import com.example.taskmanager.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val context: Context,
    private val tasks: List<Task>,
    private val itemClickListener: (Task, String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            // Set task title and description
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description

            // Set task due date (convert timestamp to readable date format)
            val dueDateText = if (task.dueDate > 0L) {
                val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(task.dueDate))
                context.getString(R.string.due_date, formattedDate)
            } else {
                context.getString(R.string.no_due_date)
            }
            binding.tvTaskDueDate.text = dueDateText

            // Set task priority using string resource
            binding.tvTaskPriority.text = context.getString(R.string.priority, task.priority)

            // Set click listener for Edit
            binding.ivEditTask.setOnClickListener {
                itemClickListener(task, "edit")
            }

            // Set click listener for Delete
            binding.ivDeleteTask.setOnClickListener {
                itemClickListener(task, "delete")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}

