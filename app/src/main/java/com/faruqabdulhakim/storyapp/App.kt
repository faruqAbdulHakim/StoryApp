package com.faruqabdulhakim.storyapp

import android.app.Application
import android.content.res.Resources

/**
 * This class right now only used for getting resources (used in repository)
 */
class App : Application() {
    companion object {
        private lateinit var res: Resources
        fun getResources() = res
    }

    override fun onCreate() {
        super.onCreate()
        res = this.resources
    }
}