package com.faruqabdulhakim.storyapp.ui.pages.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faruqabdulhakim.storyapp.databinding.FragmentSettingBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.ui.pages.info.InfoActivity

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(
            requireActivity()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAction() {
        binding.actionLocaleSetting.setOnClickListener { localeSetting() }
        binding.actionInfo.setOnClickListener { appInformation() }
        binding.actionLogout.setOnClickListener { logout() }
    }

    private fun localeSetting() {
        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        startActivity(intent)
    }

    private fun appInformation() {
        val intent = Intent(requireActivity(), InfoActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        viewModel.removeToken()
    }
}