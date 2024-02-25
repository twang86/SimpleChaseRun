package com.pandacat.simplechaserun.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.MainActivity
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.services.support.LocationProvider

class RunService: Service() {
    companion object
    {
        val currentLocation: MutableLiveData<LatLng> = MutableLiveData(null)
    }

    private lateinit var locationProvider: LocationProvider

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action)
            {
                Constants.START_RUNNING_COMMAND ->
                    onTracking(true)
                Constants.STOP_RUNNING_COMMAND ->
                    onTracking(false)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun onTracking(tracking: Boolean)
    {
        if (tracking) {
            locationProvider.startLocationTracking(object: LocationProvider.LocationListener {
                override fun onLocationReceived(location: LatLng) {
                    currentLocation.value = location
                }
            })
        }
        else
        {
            locationProvider.stopLocationTracking()
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationProvider = LocationProvider(this)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run)
            .setContentTitle(getString(R.string.app_friendly_name))
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}