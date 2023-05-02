package com.faruqabdulhakim.storyapp.ui.pages.main

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.data.entity.Story
import com.faruqabdulhakim.storyapp.databinding.FragmentMapsBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.withDateFormat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapsBinding? = null
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
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.overlay.visibility = View.VISIBLE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.clear()
        setMapStyle(googleMap)

        binding.progressIndicator.visibility = View.GONE

        viewModel.getToken().observe(viewLifecycleOwner) { token ->
            viewModel.getStoryListWithLocation(token).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is MyResult.Success -> {
                        binding.overlay.visibility = View.GONE
                        val storyList = result.data.listStory.map {
                            Story(
                                id = it.id,
                                name = it.name,
                                description = it.description,
                                photoUrl = it.photoUrl,
                                createdAt = it.createdAt,
                                lat = it.lat,
                                lon = it.lon
                            )
                        }
                        updateView(googleMap, storyList)
                    }

                    is MyResult.Error -> {
                        binding.apply {
                            overlay.visibility = View.VISIBLE
                            tvErrorMsg.text = result.message
                            tvErrorMsg.visibility = View.VISIBLE
                            progressIndicator.visibility = View.GONE
                        }
                    }

                    is MyResult.Loading -> {
                        binding.apply {
                            overlay.visibility = View.VISIBLE
                            tvErrorMsg.visibility = View.GONE
                            progressIndicator.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun updateView(googleMap: GoogleMap, storyList: List<Story>) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-0.8, 114.8), 3f))

        lifecycleScope.launch {
            for ((index, story) in storyList.reversed().withIndex()) {
                if (story.lat != null && story.lon != null) {
                    val latLng = LatLng(story.lat, story.lon)
                    val bitmap = withContext(Dispatchers.IO) {
                        Glide.with(this@MapsFragment)
                            .asBitmap()
                            .load(story.photoUrl)
                            .override(120, 120)
                            .circleCrop()
                            .submit()
                            .get()
                    }
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .icon(bitmapDescriptor)
                            .title(story.name)
                            .snippet(story.createdAt.withDateFormat())
                            .zIndex(index.toFloat())
                    )
                }
            }
        }
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success =
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                ))
            if (!success) {
                Log.e(TAG, "Style parsing fail")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style: Error: $e")
        }
    }

    companion object {
        val TAG: String = MapsFragment::class.java.simpleName
    }
}