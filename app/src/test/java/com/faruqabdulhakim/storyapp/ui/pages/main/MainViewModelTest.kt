package com.faruqabdulhakim.storyapp.ui.pages.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.faruqabdulhakim.storyapp.adapter.ListStoryAdapter
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.data.repository.AuthRepository
import com.faruqabdulhakim.storyapp.data.repository.StoryRepository
import com.faruqabdulhakim.storyapp.utils.DataDummy
import com.faruqabdulhakim.storyapp.utils.MainDispatcherRule
import com.faruqabdulhakim.storyapp.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var authRepository: AuthRepository
    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel
    private val dummyStoryList = DataDummy.generateDummyStoryList()

    @Before
    fun setup() {
        mainViewModel = MainViewModel(authRepository, storyRepository)
    }

    @Test
    fun `When load story is Success`() = runTest {
        val randomToken = "12345678"
        val expectedStoryList = MutableLiveData<PagingData<Story>>()
        expectedStoryList.value = PagingData.from(dummyStoryList)

        `when`(storyRepository.getStoryList(randomToken)).thenReturn(expectedStoryList)

        val actualStoryList = mainViewModel.getStoryList(randomToken).getOrAwaitValue {  }
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = UPDATE_CALLBACK
        )
        differ.submitData(actualStoryList)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStoryList.size, differ.snapshot().size)
        assertEquals(dummyStoryList[0], differ.snapshot()[0])
    }

    @Test
    fun `When load story retrieve empty data`() = runTest {
        val randomToken = "12345678"
        val expectedStoryList = MutableLiveData<PagingData<Story>>()
        expectedStoryList.value = PagingData.from(emptyList())

        `when`(storyRepository.getStoryList(randomToken)).thenReturn(expectedStoryList)

        val actualStoryList = mainViewModel.getStoryList(randomToken).getOrAwaitValue {  }
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = UPDATE_CALLBACK
        )
        differ.submitData(actualStoryList)

        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    companion object {
        val UPDATE_CALLBACK = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
        }
    }
}