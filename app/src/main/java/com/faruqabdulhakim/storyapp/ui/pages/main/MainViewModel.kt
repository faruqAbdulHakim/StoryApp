package com.faruqabdulhakim.storyapp.ui.pages.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository
import com.faruqabdulhakim.storyapp.data.repository.StoryRepository
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val authRepository: AuthRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getToken() = authRepository.getToken()

    fun removeToken() {
        viewModelScope.launch {
            authRepository.removeToken()
        }
    }

    fun getStoryList(token: String) = storyRepository.getStoryList(token)

    fun getStoryListWithLocation(token: String) = storyRepository.getStoryListWithLocation(token)

    fun addStory(
        token: String,
        photoFile: File,
        description: String,
        lat: Double?,
        lon: Double?
    ) = storyRepository.addStory(token, photoFile, description, lat, lon)
}