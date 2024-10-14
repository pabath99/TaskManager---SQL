package com.example.taskmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.model.Task
import com.example.taskmanager.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)
    val tasks: LiveData<List<Task>> = repository.tasks

    // LiveData for Upcoming Tasks or High Priority Tasks
    private val upcomingTasks = MutableLiveData<List<Task>>()
    private val highPriorityTasks = MutableLiveData<List<Task>>()

    // Method to add a task
    fun addTask(task: Task, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(task) { success ->
                onResult(success)
                refreshTasks()  // Make sure to refresh tasks after adding
            }
        }
    }

    // Method to update a task
    fun updateTask(task: Task, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task) { success ->
                onResult(success)
                refreshTasks()  // Make sure to refresh tasks after updating
            }
        }
    }

    // Method to delete a task
    fun deleteTask(taskId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(taskId) { success ->
                onResult(success)
                refreshTasks()  // Make sure to refresh tasks after deleting
            }
        }
    }

    // Change to public so it can be accessed outside
    fun refreshTasks() {
        filterUpcomingTasks()
        filterHighPriorityTasks()
    }

    // Filter tasks that are due soon (e.g., within the next 7 days)
    private fun filterUpcomingTasks() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)

        val upcomingList = tasks.value?.filter { task ->
            task.dueDate > System.currentTimeMillis() && task.dueDate <= calendar.timeInMillis
        } ?: emptyList()

        upcomingTasks.value = upcomingList
    }

    // Filter tasks that have high priority
    private fun filterHighPriorityTasks() {
        val highPriorityList = tasks.value?.filter { task ->
            task.priority.equals("High", ignoreCase = true)
        } ?: emptyList()

        highPriorityTasks.value = highPriorityList
    }

    // Getter method for filtered upcoming tasks
    fun getUpcomingTasks(): LiveData<List<Task>> = upcomingTasks

    // Getter method for filtered high priority tasks
    fun getHighPriorityTasks(): LiveData<List<Task>> = highPriorityTasks
}
