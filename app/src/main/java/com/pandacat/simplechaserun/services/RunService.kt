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
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.MonsterParam
import com.pandacat.simplechaserun.data.params.RunParam
import com.pandacat.simplechaserun.data.params.RunType
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.services.support.AudioManager
import com.pandacat.simplechaserun.services.support.LocationProvider
import com.pandacat.simplechaserun.services.support.MonstersManager
import com.pandacat.simplechaserun.services.support.RunnerManager
import com.pandacat.simplechaserun.utils.PermissionUtil
import com.pandacat.simplechaserun.utils.RunUtil
import com.pandacat.simplechaserun.utils.UnitsUtil

class RunService: Service(){
    private val TAG = "RunService"
    companion object
    {
        val runState: MutableLiveData<RunState> = MutableLiveData(RunState(RunState.State.NOT_STARTED, SystemClock.elapsedRealtime()))
        val runnerState: MutableLiveData<RunnerState> = MutableLiveData(RunnerState(LatLng(0.0, 0.0), 0.0,0))
        val monsterStates: MutableLiveData<HashMap<Int, MonsterState>> = MutableLiveData(hashMapOf())
        val runParams: MutableLiveData<RunParam> = MutableLiveData(RunParam(RunType.DISTANCE, hashMapOf()))
    }


    //todo delete this later!
    private fun createTestParams()
    {
        val monsterParams = hashMapOf<Int, MonsterParam>()
        monsterParams[1] = MonsterParam(MonsterType.T_REX, 100, 5, 6000, 8.5)
        monsterParams[2] = MonsterParam(MonsterType.ZOMBIE, 8000, 5, 7000, 8.0)
        runParams.value = RunParam(RunType.DISTANCE, monsterParams)
    }

    private lateinit var locationProvider: LocationProvider
    private lateinit var baseNotificationBuilder: NotificationCompat.Builder
    private lateinit var curNotificationBuilder: NotificationCompat.Builder

    //run parameters
    private var runningStartTimeMillis: Long = 0

    private var serviceKilled : Boolean = false

    private val runnerManager = RunnerManager()
    private lateinit var audioManager: AudioManager
    private lateinit var monstersManager: MonstersManager

    override fun onCreate() {
        super.onCreate()
        locationProvider = LocationProvider(this)
        baseNotificationBuilder = createNotificationBuilder()
        curNotificationBuilder = baseNotificationBuilder
        audioManager = AudioManager(applicationContext)
        monstersManager = MonstersManager(audioManager)
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
        if(curState == RunState.State.NOT_STARTED) {
            createTestParams()
            monstersManager.initMonsters(runParams.value!!)
            monsterStates.value = monstersManager.getCurrentStates()
            startForegroundService()
        }

        updateRunState(RunState.State.ACTIVE)
        runnerManager.startRun()
        monstersManager.startRun()
        audioManager.startRun()
        locationProvider.startLocationTracking(object: LocationProvider.LocationListener {
            override fun onLocationReceived(location: LatLng) {
                runnerState.value = runnerManager.updateRunner(runnerState.value!!, location)
                monsterStates.value = monstersManager.updateMonsters(runnerState.value!!)
                updateNotificationMonster(runnerState.value!!, RunUtil.getActiveMonster(
                    monsterStates.value!!))
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
        runnerManager.pauseRun()
        monstersManager.pauseRun()
        audioManager.pauseRun()
    }

    private fun stopRun()
    {
        pauseRun()
        runnerManager.stopRun()
        monstersManager.stopRun()
        audioManager.stopRun()
        val curState = runState.value!!.activeState
        if (curState != RunState.State.PAUSED)
            return
        updateRunState(RunState.State.STOPPED)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateRunState(state: RunState.State)
    {
        runState.value = RunState(state, runningStartTimeMillis)
        updateNotificationTrackingState()
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
            notificationManager.notify(Constants.NOTIFICATION_ID_RUN, curNotificationBuilder.build())
        }
    }

    private fun updateNotificationMonster(runnerState: RunnerState, monster: MonsterState?)
    {
        if (!PermissionUtil.checkPermission(Manifest.permission.POST_NOTIFICATIONS, applicationContext))
            return

        var notificationString = "No monsters chasing"
        monster?.let {
            notificationString = "${it.monsterType.getDisplayName(applicationContext)} ${UnitsUtil.getDistanceText(it.getDistanceFromRunner(runnerState.totalDistanceM), applicationContext)} away"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = curNotificationBuilder.setContentText(notificationString)
        notificationManager.notify(Constants.NOTIFICATION_ID_RUN, notification.build())
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
            .setContentTitle(getString(R.string.app_friendly_name))
            .setContentText("00:00:00")
            .setContentIntent(pending)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        runningStartTimeMillis = SystemClock.elapsedRealtime()
        startForeground(Constants.NOTIFICATION_ID_RUN, baseNotificationBuilder.build())
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