package com.example.taskmanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    private val _time = MutableLiveData<Long>().apply { value = 0L }
    val time: LiveData<Long> get() = _time

    private var startTime = 0L
    private var timeBuffer = 0L
    private var isRunning = false

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
        }
    }

    fun stop() {
        if (isRunning) {
            timeBuffer += System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    fun reset() {
        startTime = 0L
        timeBuffer = 0L
        _time.value = 0L
        isRunning = false
    }

    fun updateTime() {
        if (isRunning) {
            val currentTime = System.currentTimeMillis()
            _time.value = timeBuffer + (currentTime - startTime)
        } else {
            _time.value = timeBuffer
        }
    }
}
