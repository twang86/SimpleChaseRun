package com.pandacat.simplechaserun.data.params

import com.pandacat.simplechaserun.data.monsters.MonsterType

data class MonsterParam(val monsterType: MonsterType,
                        val runStartType: MonsterStartType,
                        val startParam: Long, //m if runType is DISTANCE, minutes if runType is TIME
                        val runnerHeadStartTimeSeconds: Int,
                        val stamina: Long, //m for DISTANCE, minutes for TIME
                        val speedKPH: Double,
    ){
    fun getStartingDistanceToRunnerMeters(): Double {
        val metersPerSecond = (speedKPH * 1000) / (60 * 60)
        return metersPerSecond * runnerHeadStartTimeSeconds
    }




}