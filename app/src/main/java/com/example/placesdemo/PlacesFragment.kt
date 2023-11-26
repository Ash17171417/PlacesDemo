package com.example.placesdemo

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.placesdemo.databinding.FragmentPlacesBinding
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

private const val TAG = "PlacesFragment"
private const val DEFAULT_ZOOM = 15f

class PlacesFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentPlacesBinding? = null
    private val binding
            get() = checkNotNull(_binding) {
                "Unable to access binding. Is view created"
            }

    private val viewModel: PlacesViewModel by viewModels()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null
    private lateinit var map: GoogleMap
    private var locationPermissionGranted: Boolean = false
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    @SuppressLint("MissingPermission")
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        locationPermissionGranted = permissions.entries.all {
            it.value
        }

        if (locationPermissionGranted) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)

        if (!locationEnabled()) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation

                if (currentLocation != null) {
                    Log.d(TAG, "$currentLocation")
                    updateMapLocation(currentLocation)
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                }
            }
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (!locationPermissionGranted) {
            Log.d(TAG, "get location permission")
            getLocationPermission()
        }
        else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    private fun locationEnabled(): Boolean {
        val locationManager: LocationManager = this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
            locationPermissionGranted = true
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
        else {
            permissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        updateMapUI()
        binding.mapView.onResume()
    }

    private fun updateMapUI() {
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateMapLocation(location: Location?) {
        if (!locationPermissionGranted || location == null) {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(defaultLocation.latitude, defaultLocation.longitude), DEFAULT_ZOOM))
            return
        }

        try {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.latitude, location!!.longitude), DEFAULT_ZOOM))
        }
        catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}