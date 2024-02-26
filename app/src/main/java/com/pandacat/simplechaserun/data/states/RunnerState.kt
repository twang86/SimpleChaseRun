package com.pandacat.simplechaserun.data.states

import com.google.android.gms.maps.model.LatLng

data class RunnerState(val currentPosition: LatLng,
                       val totalDistanceM: Double,
                       val totalTimeMillis: Long)