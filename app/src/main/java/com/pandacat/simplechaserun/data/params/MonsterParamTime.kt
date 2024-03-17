package com.pandacat.simplechaserun.data.params

import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.utils.UnitsUtil

class MonsterParamTime(monsterType: MonsterType,
                       runStartType: MonsterStartType,
                       runnerHeadStartTimeSeconds: Int,
                       speedKPH: Double,
                       val startTimeMinutes: Long,
                       val staminaMinutes: Long) : MonsterBaseParam(monsterType, runStartType, runnerHeadStartTimeSeconds, speedKPH){
    override fun shouldStart(runnerState: RunnerState): Boolean {
        return UnitsUtil.millisToMinutes(runnerState.totalTimeMillis) >= startTimeMinutes
    }

    override fun getStaminaRemaining(activeTimeMillis: Long): Float {
        return if (staminaMinutes <= activeTimeMillis / 1000F / 60F) 0F else 1 - (activeTimeMillis / 1000F / 60F) / staminaMinutes
    }

}