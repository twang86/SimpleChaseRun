package com.pandacat.simplechaserun.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.MainActivity
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.services.support.LocationProvider
import com.pandacat.simplechaserun.utils.PermissionUtil
import com.pandacat.simplechaserun.utils.RunUtil

class RunService: Service() {
    private val TAG = "RunService"
    companion object
    {
        val runState: MutableLiveData<RunState> = MutableLiveData(RunState(RunState.State.NOT_STARTED, SystemClock.elapsedRealtime()))
        val runnerState: MutableLiveData<RunnerState> = MutableLiveData(RunnerState(LatLng(0.0, 0.0), 0,0))
        val monsterStates: MutableLiveData<HashMap<Int, MonsterState>> = MutableLiveData(hashMapOf())
    }

    private lateinit var locationProvider: LocationProvider
    private lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    private var runningStartTime: Long = 0

    private var serviceKilled : Boolean = false

    override fun onCreate() {
        super.onCreate()
        locationProvider = LocationProvider(this)
        baseNotificationBuilder = createNotificationBuilder()
        curNotificationBuilder = baseNotificationBuilder
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action)
            {
                Constants.START_RUNNING_COMMAND ->
                    startRun()
                Constants.PAUSE_RUNNING_COMMAND->
                    pauseRun()
                Constants.STOP_RUNNING_COMMAND ->
                    stopRun()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startRun()
    {
        val curState = runState.value!!.activeState
        if (curState != RunState.State.NOT_STARTED && curState != RunState.State.PAUSED)
            return
        if(curState == RunState.State.NOT_STARTED)
            startForegroundService()

        updateRunState(RunState.State.ACTIVE)
        locationProvider.startLocationTracking(object: LocationProvider.LocationListener {
            override fun onLocationReceived(location: LatLng) {
                updateRunnerState(location)
            }
        })
    }

    private fun pauseRun()
    {
        val curState = runState.value!!.activeState
        if (curState != RunState.State.ACTIVE)
            return
        updateRunState(RunState.State.PAUSED)
        locationProvider.stopLocationTracking()
    }

    private fun stopRun()
    {
        pauseRun()
        val curState = runState.value!!.activeState
        if (curState != RunState.State.PAUSED)
            return
        updateRunState(RunState.State.STOPPED)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateRunState(state: RunState.State)
    {
        runState.value = RunState(state, runningStartTime)
        updateNotificationTrackingState()
    }

    private fun updateRunnerState(newLocation: LatLng)
    {
        val old = runnerState.value!!
        //todo do some time and distance math here
        runnerState.value = RunnerState(newLocation, old.totalDistanceM, old.totalTimeMillis)
    }

    private fun updateNotificationTrackingState() {
        val isTracking = runState.value!!.activeState == RunState.State.ACTIVE
        val notificationActionText = if (isTracking) getString(R.string.pause) else getString(R.string.resume)
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, RunService::class.java).apply {
                action = Constants.PAUSE_RUNNING_COMMAND
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            val resumeIntent = Intent(this, RunService::class.java).apply {
                action = Constants.START_RUNNING_COMMAND
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled && PermissionUtil.checkPermission(Manifest.permission.POST_NOTIFICATIONS, applicationContext)) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(Constants.NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    private fun updateNotificationTime(runTimeMillis: Long)
    {
        if (!PermissionUtil.checkPermission(Manifest.permission.POST_NOTIFICATIONS, applicationContext))
            return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = curNotificationBuilder.setContentText(RunUtil.getFormattedStopWatchTime(runTimeMillis, false))
        notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationBuilder() : NotificationCompat.Builder{
        val pending = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(pending)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        runningStartTime = SystemClock.elapsedRealtime()
        startForeground(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
    }

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