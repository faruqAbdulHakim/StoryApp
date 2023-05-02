package com.faruqabdulhakim.storyapp.ui.pages.main

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.faruqabdulhakim.storyapp.adapter.ListStoryAdapter
import com.faruqabdulhakim.storyapp.adapter.LoadingStateAdapter
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.databinding.FragmentHomeBinding
import com.faruqabdulhakim.storyapp.databinding.ItemStoryBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.ui.pages.detail.DetailActivity

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel> { ViewModelFactory.getInstance(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    override fun onResume() {
        super.onResume()
        binding.rvListStory.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupView() {
        val layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager(requireActivity()).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
        } else {
            GridLayoutManager(requireActivity(), 2)
        }

        binding.rvListStory.layoutManager = layoutManager

        val adapter = ListStoryAdapter()
        adapter.setItemOnClickCallback(object : ListStoryAdapter.ItemOnClickCallback {
            override fun onClick(item: Story, itemBinding: ItemStoryBinding) {
                super.onClick(item, itemBinding)
                val intent = Intent(requireActivity(), DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_PHOTO_URL, item.photoUrl)
                intent.putExtra(DetailActivity.EXTRA_NAME, item.name)
                intent.putExtra(DetailActivity.EXTRA_DESCRIPTION, item.description)
                intent.putExtra(DetailActivity.EXTRA_CREATED_AT, item.createdAt)
                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    itemBinding.itemView.context as Activity,
                    Pair(itemBinding.ivItemPhoto, "photo"),
                    Pair(itemBinding.tvItemName, "name"),
                    Pair(itemBinding.tvItemCreatedAt, "createdAt"),
                    Pair(itemBinding.tvItemDescription, "description")
                )
                startActivity(intent, optionsCompat.toBundle())
            }
        })

        binding.rvListStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        viewModel.getToken().observe(viewLifecycleOwner) { token ->
            viewModel.getStoryList(token).observe(viewLifecycleOwner) {
                adapter.submitData(lifecycle, it)
            }
        }
    }
}