package com.example.placesdemo

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.placesdemo.databinding.FragmentPlacesBinding
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

private const val TAG = "PlacesFragment"
private const val DEFAULT_ZOOM = 15f

class PlacesFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentPlacesBinding? = null
    private val binding
            get() = checkNotNull(_binding) {
                "Unable to access binding. Is view created"
            }

    // location objects to fetch and retrieve location updates
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // location variables to store current location and permission grants
    private var currentLocation: Location? = null
    private var locationPermissionGranted: Boolean = false

    private lateinit var map: GoogleMap
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    /**
     * This variable refers to the popup that asks the user
     * if he/she allows the app to access his/her location
     */
    @SuppressLint("MissingPermission")
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        locationPermissionGranted = permissions.entries.all {
            it.value
        }

        if (locationPermissionGranted) {
            // starts requesting for location updates
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)

        // checks that the phones location services are enabled
        if (!locationEnabled()) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        // configuration object for requesting location updates
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        // the function that gets called when location requests are returned
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation

                if (currentLocation != null) {
                    Log.d(TAG, "$currentLocation")
                    updateMapLocation(currentLocation)
                    updateMapUI()
                    // once we get a location, we can stop requesting for updates
                    // if we do not do this, the phone will continually check for updates
                    // which will use battery power
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                }
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // checking if the app has the appropriate permissions
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
        else { // launch the dialog requesting for permissions
            permissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    /**
     * function that checks if location services is enabled
     */
    private fun locationEnabled(): Boolean {
        val locationManager: LocationManager = this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        updateMapUI()
        binding.mapView.onResume()
    }

    private fun updateMapUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateMapLocation(location: Location?) {
        if (!locationPermissionGranted || location == null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(defaultLocation.latitude, defaultLocation.longitude), DEFAULT_ZOOM))
            return
        }

        try {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))
        }
        catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}