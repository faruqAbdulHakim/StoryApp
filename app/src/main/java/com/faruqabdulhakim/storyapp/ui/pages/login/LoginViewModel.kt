package com.faruqabdulhakim.storyapp.ui.pages.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun login(email: String, password: String) = authRepository.login(email, password)

    fun getToken() = authRepository.getToken()

    fun setToken(token: String) {
        viewModelScope.launch {
            authRepository.setToken(token)
        }
    }
}