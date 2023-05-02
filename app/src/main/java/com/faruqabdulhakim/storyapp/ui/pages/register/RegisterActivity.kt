package com.faruqabdulhakim.storyapp.ui.pages.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.databinding.ActivityRegisterBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.ui.pages.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        setupAnimation()
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener { register() }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun setupAnimation() {
        ObjectAnimator.ofFloat(binding.illustration, View.TRANSLATION_X, -30F, 30F).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }

        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.edRegisterNameLayout, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.edRegisterEmailLayout, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.edRegisterPasswordLayout, View.ALPHA, 1f).setDuration(500)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            startDelay = 500
            playSequentially(title, name, email, password, btnRegister, btnLogin)
            start()
        }
    }

    private fun register() {
        binding.apply {
            edRegisterName.validate(edRegisterName.text.toString())
            edRegisterEmail.validate(edRegisterEmail.text.toString())
            edRegisterPassword.validate(edRegisterPassword.text.toString())
        }

        if (allInputValid()) {
            viewModel.register(
                binding.edRegisterName.text.toString(),
                binding.edRegisterEmail.text.toString(),
                binding.edRegisterPassword.text.toString()
            ).observe(this@RegisterActivity) { result ->
                when (result) {
                    is MyResult.Success -> {
                        showLoading(false)
                        Toast.makeText(this@RegisterActivity, result.data.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                }
            }
        } else {
            Toast.makeText(this@RegisterActivity, getString(R.string.check_input), Toast.LENGTH_SHORT).show()
        }
    }

    private fun login() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun allInputValid(): Boolean {
        return binding.edRegisterName.error == null
                && binding.edRegisterEmail.error == null
                && binding.edRegisterPassword.error == null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}