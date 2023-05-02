package com.faruqabdulhakim.storyapp.data

sealed class MyResult<out R> private constructor() {
    data class Success<T>(val data: T) : MyResult<T>()
    data class Error(val message: String) : MyResult<Nothing>()
    object Loading : MyResult<Nothing>()
}