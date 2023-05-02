package com.faruqabdulhakim.storyapp.data.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faruqabdulhakim.storyapp.data.entity.Story

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryList(storyList: List<Story>)

    @Query("SELECT * FROM story")
    fun getStoryList(): PagingSource<Int, Story>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}