package com.faruqabdulhakim.storyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.databinding.ItemStoryBinding
import com.faruqabdulhakim.storyapp.withDateFormat

class ListStoryAdapter : PagingDataAdapter<Story, ListStoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var itemOnClickCallback: ItemOnClickCallback = object : ItemOnClickCallback {}

    inner class ViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Story) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(item.photoUrl)
                    .into(ivItemPhoto)
                tvItemName.text = root.resources.getString(R.string.story_by, item.name)
                tvItemCreatedAt.text = root.resources.getString(R.string.created_at, item.createdAt.withDateFormat())
                tvItemDescription.text = item.description

                itemView.setOnClickListener { itemOnClickCallback.onClick(item, binding) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    fun setItemOnClickCallback(itemOnClickCallback: ItemOnClickCallback) {
        this.itemOnClickCallback = itemOnClickCallback
    }

    interface ItemOnClickCallback {
        fun onClick(item: Story, itemBinding: ItemStoryBinding) {}
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Story,
                newItem: Story
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}