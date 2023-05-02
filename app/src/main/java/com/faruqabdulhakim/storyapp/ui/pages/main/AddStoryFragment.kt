package com.faruqabdulhakim.storyapp.ui.pages.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.faruqabdulhakim.storyapp.R
import com.faruqabdulhakim.storyapp.createPhotoTempFile
import com.faruqabdulhakim.storyapp.data.MyResult
import com.faruqabdulhakim.storyapp.databinding.FragmentAddStoryBinding
import com.faruqabdulhakim.storyapp.factory.ViewModelFactory
import com.faruqabdulhakim.storyapp.uriToPhotoFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.io.File
import java.util.concurrent.TimeUnit

class AddStoryFragment : Fragment() {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(
            requireActivity()
        )
    }
    private var mediaPath: String? = null
    private var photoUri: Uri? = null
    private var photoFile: File? = null

    private lateinit var launcherIntentCamera: ActivityResultLauncher<Intent>
    private lateinit var launcherIntentGallery: ActivityResultLauncher<Intent>
    private lateinit var launcherAccessLocationPermission: ActivityResultLauncher<Array<String>>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupLaunchers()
        setupFusedLocation()

        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLaunchers() {
        launcherIntentCamera = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && mediaPath != null) {
                val imageBitmap = BitmapFactory.decodeFile(mediaPath)
                binding.apply {
                    ivPhoto.setImageBitmap(imageBitmap)
                    ivPhoto.visibility = View.VISIBLE
                    tvNoImage.visibility = View.GONE
                }
                photoFile = File(mediaPath!!)
            }
        }

        launcherIntentGallery = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri = result.data?.data as Uri
                photoUri?.let { uri ->
                    binding.apply {
                        ivPhoto.visibility = View.VISIBLE
                        ivPhoto.setImageURI(uri)
                        tvNoImage.visibility = View.GONE
                    }
                    photoFile = uriToPhotoFile(requireContext(), uri)
                }
            }
        }

        launcherAccessLocationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (!isAccessLocationPermissionGranted()) {
                Toast.makeText(requireContext(), "Permission not granted", Toast.LENGTH_SHORT)
                    .show()
                binding.switchAddLocation.isChecked = false
            }
        }
    }

    private fun setupFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        createLocationRequest()
    }

    private fun createLocationRequest() {
        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                TimeUnit.SECONDS.toMillis(5)
            ).setMaxUpdateDelayMillis(
                TimeUnit.SECONDS.toMillis(5)
            ).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                updateLocation()
            }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation() {
        if (isAccessLocationPermissionGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this.location = location
                }
            }
        } else {
            launcherAccessLocationPermission.launch(ACCESS_LOCATION_PERMISSIONS)
        }
    }

    private fun setupView() {
        if (photoFile != null) {
            val bitmap = BitmapFactory.decodeFile(photoFile!!.path)
            binding.apply {
                tvNoImage.visibility = View.GONE
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupAction() {
        binding.btnOpenCamera.setOnClickListener { openCamera() }
        binding.btnOpenGallery.setOnClickListener { openGallery() }
        binding.buttonAdd.setOnClickListener { addStory() }
        binding.switchAddLocation.setOnClickListener { addLocation() }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        createPhotoTempFile(requireActivity()).also {
            val uri = FileProvider.getUriForFile(
                requireActivity(),
                "com.faruqabdulhakim.storyapp",
                it
            )
            mediaPath = it.path
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        launcherIntentCamera.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun addStory() {
        showLoading(true)

        if (photoFile == null) {
            showLoading(false)
            Toast.makeText(
                requireActivity(),
                getString(R.string.no_image),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val description = binding.edAddDescription.text.toString()
        if (description.isEmpty()) {
            showLoading(false)
            Toast.makeText(
                requireActivity(),
                getString(R.string.description_blank_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val photoFile = this.photoFile as File

        viewModel.getToken().observe(viewLifecycleOwner) { token ->
            if (binding.switchAddLocation.isChecked) {
                if (location != null) {
                    addStory(token, photoFile, description, location?.latitude, location?.longitude)
                } else {
                    showLoading(false)
                    Toast.makeText(requireContext(), getString(R.string.no_location), Toast.LENGTH_SHORT).show()
                }
            } else {
                addStory(token, photoFile, description)
            }
        }
    }

    private fun addStory(
        token: String,
        photoFile: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null
    ) {
        viewModel.addStory(token, photoFile, description, lat, lon)
            .observe(viewLifecycleOwner) { result ->
                when (result) {
                    is MyResult.Success -> {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            result.data.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        // reload activity
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        requireActivity().finish()
                        startActivity(intent)
                    }

                    is MyResult.Error -> {
                        showLoading(false)
                        Toast.makeText(requireActivity(), result.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    is MyResult.Loading -> {
                        showLoading(true)
                        Toast.makeText(requireActivity(), "Uploading...", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun addLocation() {
        if (!isAccessLocationPermissionGranted()) {
            launcherAccessLocationPermission.launch(ACCESS_LOCATION_PERMISSIONS)
        }
    }

    private fun isAccessLocationPermissionGranted(): Boolean {
        return ACCESS_LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private val ACCESS_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}