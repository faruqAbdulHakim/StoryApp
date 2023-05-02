package com.faruqabdulhakim.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.faruqabdulhakim.storyapp.App
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.compressPhoto
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.data.local.room.StoryDatabase
import com.faruqabdulhakim.storyapp.data.StoryRemoteMediator
import com.faruqabdulhakim.storyapp.data.remote.response.GeneralResponse
import com.faruqabdulhakim.storyapp.data.remote.response.ListStoryResponse
import com.faruqabdulhakim.storyapp.data.remote.retrofit.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.net.UnknownHostException

class StoryRepository private constructor(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getStoryList(token: String): LiveData<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getStoryList()
            }
        ).liveData
    }

    fun getStoryListWithLocation(token: String): LiveData<MyResult<ListStoryResponse>> = liveData {
        emit(MyResult.Loading)
        try {
            val response = apiService.getAllStories(
                authorization = "Bearer $token",
                location = 1
            )
            emit(MyResult.Success(response))
        } catch (e: HttpException) {
            if (e.response() != null) {
                emit(MyResult.Error(e.response()!!.message()))
            } else {
                emit(MyResult.Error(e.message.toString()))
            }
        } catch (e: UnknownHostException) {
            emit(MyResult.Error(App.getResources().getString(R.string.unknown_host)))
        } catch (e: Exception) {
            emit(MyResult.Error(e.message.toString()))
        }
    }

    fun addStory(
        token: String,
        photoFile: File,
        description: String,
        lat: Double?,
        lon: Double?
    ): LiveData<MyResult<GeneralResponse>> = liveData {
        emit(MyResult.Loading)
        try {
            val compressedFile = compressPhoto(photoFile, 1_000_000)
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val latRequestBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val lonRequestBody = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val imageRequestBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData(
                "photo",
                compressedFile.name,
                imageRequestBody
            )
            val response =
                apiService.addNewStory(
                    "Bearer $token",
                    multipart,
                    descriptionRequestBody,
                    latRequestBody,
                    lonRequestBody
                )
            emit(MyResult.Success(response))
        } catch (e: HttpException) {
            if (e.response() != null) {
                emit(MyResult.Error(e.response()!!.message()))
            } else {
                emit(MyResult.Error(e.message.toString()))
            }
        } catch (e: UnknownHostException) {
            emit(MyResult.Error(App.getResources().getString(R.string.unknown_host)))
        } catch (e: Exception) {
            emit(MyResult.Error(e.message.toString()))
        }
    }

    companion object {
        private var INSTANCE: StoryRepository? = null

        fun getInstance(storyDatabase: StoryDatabase, apiService: ApiService): StoryRepository {
            return INSTANCE ?: synchronized(this) {
                StoryRepository(storyDatabase, apiService)
            }.also {
                INSTANCE = it
            }
        }
    }
}