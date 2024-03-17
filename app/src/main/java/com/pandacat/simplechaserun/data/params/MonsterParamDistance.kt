package com.pandacat.simplechaserun.data.params

import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.states.RunnerState

class MonsterParamDistance(monsterType: MonsterType,
                           runStartType: MonsterStartType,
                           runnerHeadStartTimeSeconds: Int,
                           speedKPH: Double,
                           val startKM: Double,
                           val staminaKM: Double) : MonsterBaseParam(monsterType, runStartType, runnerHeadStartTimeSeconds, speedKPH) {
    override fun shouldStart(runnerState: RunnerState): Boolean {
        return runnerState.totalDistanceM >= startKM
    }

    override fun getStaminaRemaining(activeTimeMillis: Long): Float {
        val kmPerSecond = speedKPH / (60 * 60)
        val metersPerMillis = kmPerSecond / 1000
        val totalDistanceTraveledKM = metersPerMillis * activeTimeMillis
        return if (staminaKM <= totalDistanceTraveledKM) 0F else 1 - (totalDistanceTraveledKM / staminaKM).toFloat()
    }
}