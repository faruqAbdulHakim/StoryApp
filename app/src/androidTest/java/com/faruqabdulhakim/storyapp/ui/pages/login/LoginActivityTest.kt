package com.faruqabdulhakim.storyapp.ui.pages.login

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.data.remote.retrofit.ApiConfig
import com.faruqabdulhakim.storyapp.utils.EspressoIdlingResource
import com.faruqabdulhakim.storyapp.utils.JsonConverter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.faruqabdulhakim.storyapp.ui.pages.main.MainActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.BASE_URL = "http://127.0.0.1:8080/"
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun userLoginAndLogout_Success() {
        ActivityScenario.launch(LoginActivity::class.java)

        Intents.init()
        onView(withId(R.id.ed_login_email)).perform(ViewActions.typeText("espresso@mail.com"))
        onView(withId(R.id.ed_login_email)).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(ViewActions.typeText("12345678"))
        onView(withId(R.id.ed_login_password)).perform(ViewActions.closeSoftKeyboard())

        val mockLoginResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("login_success_response.json"))
        mockWebServer.enqueue(mockLoginResponse)
        onView(withId(R.id.btn_login)).perform(ViewActions.click())

        Thread.sleep(5000)

        Intents.intended(hasComponent(MainActivity::class.java.name))
        onView(withId(R.id.action_setting)).perform(ViewActions.click())
        onView(withId(R.id.action_logout)).perform(ViewActions.click())

        Thread.sleep(5000)

        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }

}