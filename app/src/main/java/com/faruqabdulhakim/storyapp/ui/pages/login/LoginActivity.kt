package com.faruqabdulhakim.storyapp.ui.pages.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.databinding.ActivityLoginBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.ui.pages.register.RegisterActivity
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.ui.pages.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIsAlreadyLoggedIn()
        setupAction()
        setupAnimation()
    }

    private fun checkIsAlreadyLoggedIn() {
        viewModel.getToken().observe(this) {token ->
            if (token.isNotEmpty()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener { register() }
    }

    private fun setupAnimation() {
        ObjectAnimator.ofFloat(binding.illustration, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edLoginEmailLayout, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.edLoginPasswordLayout, View.ALPHA, 1f).setDuration(500)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            startDelay = 500
            playSequentially(title, email, password, btnLogin, btnRegister)
            start()
        }
    }

    private fun login() {
        binding.apply {
            edLoginEmail.validate(edLoginEmail.text.toString())
            edLoginPassword.validate(edLoginPassword.text.toString())
        }

        if (allInputValid()) {
            viewModel.login(
                binding.edLoginEmail.text.toString(),
                binding.edLoginPassword.text.toString()
            ).observe(this@LoginActivity) { result ->
                when (result) {
                    is MyResult.Success -> {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, result.data.message, Toast.LENGTH_SHORT).show()
                        viewModel.setToken(result.data.loginResult.token)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                }
            }
        } else {
            Toast.makeText(this@LoginActivity, getString(R.string.check_input), Toast.LENGTH_SHORT).show()
        }
    }

    private fun register() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun allInputValid(): Boolean {
        return binding.edLoginEmail.error == null
                && binding.edLoginPassword.error == null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}