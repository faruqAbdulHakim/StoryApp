package com.faruqabdulhakim.storyapp.utils

import com.faruqabdulhakim.storyapp.data.entity.Story

object DataDummy {
    fun generateDummyStoryList(): List<Story> {
        val storyList = ArrayList<Story>()
        for (i in 0 until 100) {
            val story = Story(
                id = "ID $i",
                name = "User $i",
                description = "Description",
                photoUrl = "https://via.placeholder.com/320",
                createdAt = "2023-04-28T22:22:22Z",
                lon = -10.212,
                lat = -16.002
            )
            storyList.add(story)
        }
        return storyList
    }
}