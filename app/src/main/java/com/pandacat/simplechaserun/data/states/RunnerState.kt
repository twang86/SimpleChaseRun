package com.pandacat.simplechaserun.data.states

import com.google.android.gms.maps.model.LatLng

data class RunnerState(val currentPosition: LatLng,
                       val totalDistanceM: Double,
                       val totalTimeMillis: Long)
{
    override fun toString(): String {
        return "position $currentPosition, totalDistance $totalDistanceM, totalTimeSeconds: ${totalTimeMillis/1000.0}"
    }

    fun getKPH() = if (totalTimeMillis <= 0) 0.0 else (totalDistanceM / 1000) / (totalTimeMillis / 1000.0 / 60 / 60)
}