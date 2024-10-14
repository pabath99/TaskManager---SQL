package com.example.taskmanager.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.example.taskmanager.database.TaskDao
import com.example.taskmanager.database.TaskDatabase
import com.example.taskmanager.model.Task
import com.example.taskmanager.widget.TaskWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val context: Context) {

    private val taskDao: TaskDao = TaskDatabase.getDatabase(context).taskDao()

    // LiveData to observe task list from Room database
    val tasks: LiveData<List<Task>> = taskDao.getAllTasks()

    // Function to add a new task to the Room database
    suspend fun addTask(task: Task, onResult: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            taskDao.insertTask(task)
        }
        onResult(true)
        notifyWidgetUpdate()
    }

    // Function to update an existing task in the Room database
    suspend fun updateTask(task: Task, onResult: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            taskDao.updateTask(task)
        }
        onResult(true)
        notifyWidgetUpdate()
    }

    // Function to delete a task by ID from the Room database
    suspend fun deleteTask(taskId: Long, onResult: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            taskDao.deleteTaskById(taskId)
        }
        onResult(true)
        notifyWidgetUpdate()
    }

    // Notify widget to update when tasks are changed
    private fun notifyWidgetUpdate() {
        val intent = Intent(context, TaskWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, TaskWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    fun getUpcomingTasksSync(): List<Task> {
        val currentTime = System.currentTimeMillis()
        return taskDao.getTasksDueAfter(currentTime)
    }

}
