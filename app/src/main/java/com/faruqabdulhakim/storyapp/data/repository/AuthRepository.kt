package com.faruqabdulhakim.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.faruqabdulhakim.storyapp.App
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.data.local.preferences.AuthPreferences
import com.faruqabdulhakim.storyapp.data.remote.response.GeneralResponse
import com.faruqabdulhakim.storyapp.data.remote.response.LoginResponse
import com.faruqabdulhakim.storyapp.data.remote.retrofit.ApiService
import com.faruqabdulhakim.storyapp.utils.wrapEspressoIdlingResource
import retrofit2.HttpException
import java.net.UnknownHostException

class AuthRepository private constructor(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences
) {
    fun login(email: String, password: String): LiveData<MyResult<LoginResponse>> = liveData {
        emit(MyResult.Loading)
        wrapEspressoIdlingResource {
            try {
                val response = apiService.login(email, password)
                emit(MyResult.Success(response))
            } catch(e: HttpException) {
                when (e.response()?.code()) {
                    400 -> emit(MyResult.Error(App.getResources().getString(R.string.bad_request)))
                    401 -> emit(MyResult.Error(App.getResources().getString(R.string.wrong_email_or_password)))
                    else -> emit(MyResult.Error(App.getResources().getString(R.string.login_fail)))
                }
            } catch (e: UnknownHostException) {
                emit(MyResult.Error(App.getResources().getString(R.string.unknown_host)))
            } catch (e: Exception) {
                emit(MyResult.Error(e.message.toString()))
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<MyResult<GeneralResponse>> = liveData {
        emit(MyResult.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(MyResult.Success(response))
        } catch(e: HttpException) {
            when (e.response()?.code()) {
                400 -> emit(MyResult.Error(App.getResources().getString(R.string.bad_request)))
                else -> emit(MyResult.Error(App.getResources().getString(R.string.register_fail)))
            }
        } catch (e: UnknownHostException) {
            emit(MyResult.Error(App.getResources().getString(R.string.unknown_host)))
        } catch (e: Exception) {
            emit(MyResult.Error(e.message.toString()))
        }
    }

    fun getToken() = authPreferences.getToken()

    suspend fun setToken(token: String) = authPreferences.setToken(token)

    suspend fun removeToken() = authPreferences.removeToken()

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(apiService: ApiService, authPreferences: AuthPreferences): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                AuthRepository(apiService, authPreferences)
            }.also {
                INSTANCE = it
            }
        }
    }
}