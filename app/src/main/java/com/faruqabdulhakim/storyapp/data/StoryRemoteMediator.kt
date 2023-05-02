package com.faruqabdulhakim.storyapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.faruqabdulhakim.storyapp.data.entity.RemoteKeys
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.data.local.room.StoryDatabase
import com.faruqabdulhakim.storyapp.data.remote.retrofit.ApiService
import java.lang.Exception

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: String,
): RemoteMediator<Int, Story>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Story>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(0) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val responseData = apiService.getAllStories(
                "Bearer $token",
                page,
                state.config.pageSize,
                0
            )

            val endOfPaginationReached = responseData.listStory.isEmpty()

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.storyDao().deleteAll()
                    storyDatabase.remoteKeysDao().deleteAll()
                }

                storyDatabase.storyDao()
                    .insertStoryList(responseData.listStory.map {
                        Story(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            photoUrl = it.photoUrl,
                            createdAt = it.createdAt,
                            lat = it.lat,
                            lon = it.lon
                        )
                    })

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                storyDatabase.remoteKeysDao().insertList(
                    responseData.listStory.map {
                        RemoteKeys(
                            id = it.id,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }
                )
            }

            return MediatorResult.Success(endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let {
            storyDatabase.remoteKeysDao().getRemoteKeysById(it.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let {
            storyDatabase.remoteKeysDao().getRemoteKeysById(it.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let {
                storyDatabase.remoteKeysDao().getRemoteKeysById(it)
            }
        }
    }

    companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }
}