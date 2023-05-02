package com.faruqabdulhakim.storyapp.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository
import com.faruqabdulhakim.storyapp.data.repository.StoryRepository
import com.faruqabdulhakim.storyapp.di.Injection
import com.faruqabdulhakim.storyapp.ui.pages.login.LoginViewModel
import com.faruqabdulhakim.storyapp.ui.pages.main.MainViewModel
import com.faruqabdulhakim.storyapp.ui.pages.register.RegisterViewModel

class ViewModelFactory private constructor(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(authRepository, storyRepository) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown model class: ${modelClass.name}")
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE
                ?: synchronized(this) {
                    ViewModelFactory(
                        Injection.provideAuthRepository(context),
                        Injection.provideStoryRepository(context),
                    )
                }.also {
                    INSTANCE = it
                }
        }
    }
}