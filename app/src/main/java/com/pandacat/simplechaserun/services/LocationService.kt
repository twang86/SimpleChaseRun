package com.pandacat.simplechaserun.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.constants.Constants

class LocationService: Service() {

    companion object
    {
        val currentLocation: MutableLiveData<LatLng> = MutableLiveData(null)
    }

    private lateinit var locationProvider: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.first().let {
                currentLocation.value = LatLng(it.latitude, it.longitude)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action)
            {
                Constants.START_LOCATION_COMMAND ->
                    onTracking(true)
                Constants.STOP_LOCATION_COMMAND ->
                    onTracking(false)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun onTracking(tracking: Boolean)
    {
        if (tracking) {
            locationProvider.requestLocationUpdates(
                LocationRequest.Builder(Constants.FASTEST_LOCATION_INTERVAL).build(),
                locationCallback,
                Looper.getMainLooper())
        }
        else
        {
            locationProvider.removeLocationUpdates(locationCallback)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationProvider = LocationServices.getFusedLocationProviderClient(this)
    }

}