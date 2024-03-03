package com.pandacat.simplechaserun.utils

import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunnerState
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RunUtil {

    fun calculateDistanceMeters(loc1: LatLng, loc2: LatLng) = calculateDistanceMeters(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude)

    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = R * c * 1000 // Convert to meters
        return distance
    }

    fun getActiveMonsterDistanceFromUser(monsterState: HashMap<Int, MonsterState>, runnerState: RunnerState) : Double?
    {
        getActiveMonster(monsterState)?.let {
            return it.getDistanceFromRunner(runnerState.totalDistanceM)
        }
        return null
    }

    fun getActiveMonster(monsterState: HashMap<Int, MonsterState>) : MonsterState? {
        for (monster in monsterState.values)
        {
            if (monster.state == MonsterState.State.ACTIVE) {
                return monster
            }
        }
        return null
    }
}