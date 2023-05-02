package com.faruqabdulhakim.storyapp.ui.pages.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.databinding.ActivityDetailBinding
import com.faruqabdulhakim.storyapp.withDateFormat

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        val name = intent.getStringExtra(EXTRA_NAME)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val createdAt = intent.getStringExtra(EXTRA_CREATED_AT)

        photoUrl?.let {
            Glide.with(this@DetailActivity)
                .load(photoUrl)
                .into(binding.ivDetailPhoto)
        }
        binding.apply {
            tvDetailName.text = resources.getString(R.string.story_by, name)
            tvDetailDescription.text = description
            tvDetailCreatedAt.text = getString(R.string.created_at, createdAt?.withDateFormat())
        }
    }

    companion object {
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_CREATED_AT = "extra_created_at"
    }
}