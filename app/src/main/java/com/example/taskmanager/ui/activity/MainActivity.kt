package com.example.taskmanager.ui.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.adapter.TaskAdapter
import com.example.taskmanager.databinding.ActivityMainBinding
import com.example.taskmanager.model.Task
import com.example.taskmanager.viewmodel.AuthViewModel
import com.example.taskmanager.viewmodel.TaskViewModel
import com.example.taskmanager.R
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import android.view.Menu
import android.view.MenuItem
import java.util.*

class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var taskList = mutableListOf<Task>()

    private lateinit var auth: FirebaseAuth  // FirebaseAuth instance
    private lateinit var preferences: SharedPreferences  // SharedPreferences instance

    // Declare binding object for View Binding
    private lateinit var binding: ActivityMainBinding

    // Register for activity result for handling task add/edit results
    private val addEditTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            taskViewModel.refreshTasks()  // Refresh tasks after adding or editing
        }
    }

    // Register the permission request launcher for POST_NOTIFICATIONS permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        preferences = getSharedPreferences("TaskManagerPrefs", MODE_PRIVATE)

        // Check if user is logged in
        if (!authViewModel.isUserLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        observeTasks()

        // Set click listeners using View Binding
        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            addEditTaskLauncher.launch(intent)
        }

        binding.btnOpenTimer.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        createNotificationChannel()
        requestNotificationPermission()

        // Handle Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the task list when MainActivity is resumed
        taskViewModel.refreshTasks()  // Assuming this method fetches the latest tasks
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Handle logout action here
                auth.signOut() // Ensure you are signed out from Firebase

                // Reset first-time flag when user logs out
                preferences.edit().putBoolean("isFirstTime", true).apply()

                // Navigate back to the Splash screen after logout
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        // Use binding to reference the RecyclerView
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(this, taskList) { task, action ->
            when (action) {
                "edit" -> {
                    val intent = Intent(this, AddEditTaskActivity::class.java)
                    intent.putExtra("taskId", task.id)
                    addEditTaskLauncher.launch(intent)
                }
                "delete" -> {
                    taskViewModel.deleteTask(task.id) { success ->
                        if (!success) {
                            Toast.makeText(this, "Error deleting task", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.rvTasks.adapter = taskAdapter
    }

    private fun observeTasks() {
        taskViewModel.tasks.observe(this) { tasks ->
            val upcomingTasks = tasks.filter { task ->
                // Show tasks with a due date that's in the future
                task.dueDate > System.currentTimeMillis()
            }.sortedBy { it.dueDate } // Sort tasks by due date

            taskList.clear()
            taskList.addAll(upcomingTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "task_manager_channel"
            val channelName = "Task Manager Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for task manager notifications"
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}


