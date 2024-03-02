package com.pandacat.simplechaserun.services.support

import android.os.SystemClock
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.utils.RunUtil

class RunnerManager: RunManagerBase {
    private val TAG = "RunnerManager"
    private var lastValidLocation: LatLng? = null
    private var lastValidTime: Long? = null

    fun updateRunner(old: RunnerState, newLocation: LatLng): RunnerState
    {
        var addedDistance = 0.0
        var addedTime = 0L
        Log.i(TAG, "updateRunner: $lastValidLocation")
        lastValidLocation?.let {
            addedDistance = RunUtil.calculateDistanceMeters(it, newLocation)
            Log.i(TAG, "updateRunner dist: $addedDistance")
        }
        lastValidLocation = newLocation
        val now = SystemClock.elapsedRealtime()
        lastValidTime?.let {
            addedTime = now - it
            val addedMetersPerSecond = addedDistance / (addedTime / 1000.0)
            Log.i(TAG, "updateRunner: time: $addedTime m per second $addedMetersPerSecond")
            if (addedMetersPerSecond > Constants.MAX_DISTANCE_PER_SECOND_THRESHOLD_METERS) {
                Log.i(TAG, "updateRunner: time: $addedTime m per second $addedMetersPerSecond too big, ignoring")
                addedDistance = 0.0
                addedTime = 0
            }
        }
        lastValidTime = now
        return RunnerState(newLocation, old.totalDistanceM + addedDistance, old.totalTimeMillis + addedTime)
    }

    override fun pauseRun() {
        lastValidLocation = null
        lastValidTime = null
    }

    override fun startRun() {
        /*todo we should request current location here and request the current location so we can do our location maths
        currently we are off by a second or two depending on when we get another location update
        * */
    }

    override fun stopRun() {
        //do nothing
    }
}