package com.faruqabdulhakim.storyapp.ui.pages.register

import androidx.lifecycle.ViewModel
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun register(name: String, email: String, password: String) = authRepository.register(name, email, password)
}