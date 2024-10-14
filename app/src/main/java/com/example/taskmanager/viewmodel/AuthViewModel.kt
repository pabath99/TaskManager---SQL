package com.example.taskmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.example.taskmanager.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        authRepository.register(email, password, onResult)
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        authRepository.login(email, password, onResult)
    }

    fun logout() {
        authRepository.logout()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
}
