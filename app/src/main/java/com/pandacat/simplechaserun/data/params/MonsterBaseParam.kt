package com.pandacat.simplechaserun.data.params
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.states.RunnerState

abstract class MonsterBaseParam(val monsterType: MonsterType,
                                val runStartType: MonsterStartType,
                                val runnerHeadStartTimeSeconds: Int,
                                val speedKPH: Double,
) {

    abstract fun shouldStart(runnerState: RunnerState) : Boolean
    abstract fun getStaminaRemaining(activeTimeMillis: Long) : Float
}