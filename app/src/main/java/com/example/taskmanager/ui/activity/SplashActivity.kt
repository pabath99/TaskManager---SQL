package com.example.taskmanager.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import android.view.View

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth and SharedPreferences
        auth = FirebaseAuth.getInstance()
        preferences = getSharedPreferences("TaskManagerPrefs", MODE_PRIVATE)

        // Initialize ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if the user is opening the app for the first time
        val isFirstTime = preferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            binding.btnGetStarted.visibility = View.VISIBLE
            binding.btnGetStarted.setOnClickListener {
                preferences.edit().putBoolean("isFirstTime", false).apply()
                navigateToLogin()
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                checkUserStatus()
            }, 2000)
        }
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
