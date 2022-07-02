package com.geekydroid.backgroundlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.geekydroid.backgroundlocation.databinding.ActivityMainBinding
import com.google.android.gms.location.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.d(TAG, "Fine location accuracy granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d(TAG, "Coarse location accuracy granted")
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false) -> {
                Log.d(TAG, "Background location permission granted")
            }
        }
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult?:return
            for (location in locationResult.locations)
            {
                val text = "Lat "+location.latitude+" Long "+location.longitude+" accuracy "+location.accuracy
                updateUi(text)
            }
        }
    }

    private fun updateUi(text: String) {
        binding.tvLocationUpdates.text = text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnForegroundLocation.setOnClickListener {
            checkIfForegroundPermissionGranted()
        }

        binding.btnBackgroundLocation.setOnClickListener {
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    checkIfBackgroundLocationPermissionGranted()
                } else {
                    checkIfForegroundPermissionGranted()
                }
            } else {
                checkIfForegroundPermissionGranted()
            }
        }

        binding.btnStartLocationUpdates.setOnClickListener {

            if ((ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                startLocationUpdates()
            } else {
                requestFineLocationPermission()
                if (SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermission()
                }
            }
        }
    }

    private fun startLocationUpdates() {

        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkIfBackgroundLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Background location permission granted")
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            showEducationalUIForBackGroundLocation()
        } else {
            requestBackgroundLocationPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showEducationalUIForBackGroundLocation() {

        val builder = AlertDialog.Builder(this)
            .setTitle("Location permission request")
            .setMessage("Please give the background location permission to access this feature")
            .setPositiveButton(
                "Ok"
            )
            { _, _ ->
                requestBackgroundLocationPermission()
            }.create()

        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    private fun checkIfForegroundPermissionGranted() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permission access given")
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showEducationalUI()
        } else {
            requestFineLocationPermission()
        }
    }

    private fun showEducationalUI() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Location permission request")
            .setMessage("Please give the location permission to access this feature")
            .setPositiveButton(
                "Ok"
            )
            { _, _ ->
                requestFineLocationPermission()
            }.create()

        builder.show()
    }

    private fun requestFineLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    }


}