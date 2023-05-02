package com.faruqabdulhakim.storyapp.ui.pages.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.databinding.ActivityMainBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.ui.pages.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIsAlreadyLoggedIn()
        setupAction()
    }

    private fun checkIsAlreadyLoggedIn() {
        viewModel.getToken().observe(this) { token ->
            if (token.isEmpty()) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupAction() {
        val fragmentManager = supportFragmentManager
        val homeFragment = HomeFragment()
        val mapsFragment = MapsFragment()
        val addStoryFragment = AddStoryFragment()
        val settingFragment = SettingFragment()

        fragmentManager.commit {
            replace(binding.frameContainer.id, homeFragment, HomeFragment::class.java.simpleName)
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    fragmentManager.commit {
                        replace(binding.frameContainer.id, homeFragment, HomeFragment::class.java.simpleName)
                    }
                    true
                }
                R.id.action_map -> {
                    fragmentManager.commit {
                        replace(binding.frameContainer.id, mapsFragment, MapsFragment::class.java.simpleName)
                    }
                    true
                }
                R.id.action_add_story -> {
                    fragmentManager.commit {
                        replace(binding.frameContainer.id, addStoryFragment, AddStoryFragment::class.java.simpleName)
                    }
                    true
                }
                R.id.action_setting -> {
                    fragmentManager.commit {
                        replace(binding.frameContainer.id, settingFragment, SettingFragment::class.java.simpleName)
                    }
                    true
                }
                else -> false
            }
        }
    }
}