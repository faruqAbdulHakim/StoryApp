package com.faruqabdulhakim.storyapp.di

import android.content.Context
import com.faruqabdulhakim.storyapp.data.local.preferences.AuthPreferences
import com.faruqabdulhakim.storyapp.data.local.room.StoryDatabase
import com.faruqabdulhakim.storyapp.data.remote.retrofit.ApiConfig
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository
import com.faruqabdulhakim.storyapp.data.repository.StoryRepository
import com.faruqabdulhakim.storyapp.dataStore

object Injection {
    fun provideAuthRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        val authPreferences = AuthPreferences.getInstance(context.dataStore)
        return AuthRepository.getInstance(apiService, authPreferences)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val storyDatabase = StoryDatabase.getInstance(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(storyDatabase, apiService)
    }
}