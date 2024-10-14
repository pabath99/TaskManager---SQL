package com.example.taskmanager.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityTimerBinding
import com.example.taskmanager.viewmodel.TimerViewModel
import java.util.Locale

class TimerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimerBinding
    private val timerViewModel: TimerViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timerViewModel.time.observe(this) { time ->
            val secs = (time / 1000).toInt()
            val minutes = secs / 60
            val hours = minutes / 60
            val milliseconds = (time % 1000).toInt()

            binding.tvTimer.text = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d:%03d",
                hours,
                minutes % 60,
                secs % 60,
                milliseconds
            )
        }

        binding.btnStart.setOnClickListener {
            timerViewModel.start()
            handler.post(updateRunnable)
        }

        binding.btnStop.setOnClickListener {
            timerViewModel.stop()
            handler.removeCallbacks(updateRunnable)
        }

        binding.btnReset.setOnClickListener {
            timerViewModel.reset()
            handler.removeCallbacks(updateRunnable)
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            timerViewModel.updateTime()
            handler.postDelayed(this, 50)
        }
    }
}
