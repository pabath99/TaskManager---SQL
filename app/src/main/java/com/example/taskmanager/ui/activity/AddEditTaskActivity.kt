package com.example.taskmanager.ui.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.R
import com.example.taskmanager.databinding.ActivityAddEditTaskBinding
import com.example.taskmanager.model.Task
import com.example.taskmanager.ui.receiver.ReminderReceiver
import com.example.taskmanager.viewmodel.TaskViewModel
import java.util.*

class AddEditTaskActivity : AppCompatActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var binding: ActivityAddEditTaskBinding
    private var taskId: String? = null
    private var isEditing: Boolean = false
    private var task: Task? = null
    private var reminderTime: Long = 0L
    private var dueDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskId = intent.getStringExtra("taskId")
        isEditing = taskId != null

        if (isEditing) {
            loadTask()
        }

        setupPrioritySpinner()

        // Set click listeners for buttons
        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }

        binding.btnSetReminder.setOnClickListener {
            pickDateTime()
        }

        binding.btnSetDueDate.setOnClickListener {
            pickDueDate()
        }

        binding.btnCancelTask.setOnClickListener {
            redirectToTaskList()
        }
    }

    private fun setupPrioritySpinner() {
        val priorityLevels = resources.getStringArray(R.array.priority_levels)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        if (isEditing) {
            task?.let {
                val priorityPosition = adapter.getPosition(it.priority)
                binding.spinnerPriority.setSelection(priorityPosition)
            }
        }
    }

    private fun loadTask() {
        taskViewModel.tasks.value?.let {
            task = it.find { task -> task.id == taskId?.toLongOrNull() } // Convert to Long if taskId is String
            task?.let { task ->
                binding.etTaskTitle.setText(task.title)
                binding.etTaskDescription.setText(task.description)
                reminderTime = task.reminderTime
                dueDate = task.dueDate

                val adapter = binding.spinnerPriority.adapter
                if (adapter is ArrayAdapter<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val priorityAdapter = adapter as ArrayAdapter<String>
                    val priorityPosition = priorityAdapter.getPosition(task.priority)
                    binding.spinnerPriority.setSelection(priorityPosition)
                }
            }
        }
    }


    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString()

        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Title required"
            binding.etTaskTitle.requestFocus()
            return
        }

        if (isEditing) {
            task?.let {
                // Use the existing task's id since it's an update
                val updatedTask = Task(it.id, title, description, priority, dueDate, it.isCompleted, reminderTime)
                taskViewModel.updateTask(updatedTask) { success ->
                    if (success) {
                        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
                        if (reminderTime > 0L) {
                            scheduleTaskReminder(updatedTask)
                        }
                        redirectToTaskList()
                    } else {
                        Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Create a new task without specifying the id (it will be auto-generated)
            val newTask = Task(0L, title, description, priority, dueDate, false, reminderTime)

            taskViewModel.addTask(newTask) { success ->
                if (success) {
                    Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
                    if (reminderTime > 0L) {
                        scheduleTaskReminder(newTask)
                    }
                    redirectToTaskList()
                } else {
                    Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun redirectToTaskList() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun pickDateTime() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        reminderTime = calendar.timeInMillis
                        Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun pickDueDate() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        dueDate = calendar.timeInMillis  // Store the complete due date with time
                        Toast.makeText(this, "Due Date and Time set", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true  // Use 24-hour format
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun scheduleTaskReminder(task: Task) {
        if (reminderTime <= 0L) return

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("taskTitle", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (reminderTime > System.currentTimeMillis()) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }
}
