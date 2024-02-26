package com.pandacat.simplechaserun.services.support

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.utils.PermissionUtil

class LocationProvider(private val context: Context) {
    private val TAG = "LocationProvider"
    private var locationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationListener: LocationListener? = null
    private var started = false
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for((index, loc) in result.locations.withIndex())
            {
                Log.i(TAG, "locations: $index $loc")
            }
            result.locations.first().let {
                locationListener?.onLocationReceived(LatLng(it.latitude, it.longitude))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking(listener: LocationListener)
    {
        if (!PermissionUtil.checkPermissions(PermissionUtil.getPermissionsRequired(), context))
            return
        locationListener = listener
        locationClient.requestLocationUpdates(
            LocationRequest.Builder(Constants.FASTEST_LOCATION_INTERVAL)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            locationCallback,
            Looper.getMainLooper())
        started = true
    }

    fun stopLocationTracking()
    {
        locationListener = null
        if (!started)
            return
        locationClient.removeLocationUpdates(locationCallback)
        started = false
    }

    interface LocationListener {
        fun onLocationReceived(location:LatLng)
    }

}