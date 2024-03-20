package com.pandacat.simplechaserun.data.params

import android.content.Context
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.utils.UnitsUtil

data class MonsterParam(val monsterType: MonsterType,
                        val runStartType: MonsterStartType,
                        val startParam: Long, //m if runType is DISTANCE, minutes if runType is TIME
                        val runnerHeadStartTimeSeconds: Int,
                        val stamina: Long, //m for DISTANCE, minutes for TIME
                        val speedKPH: Double,
    ){

    override fun toString(): String {
        return "monster: $monsterType, runStartType: $runStartType, startParam: $startParam, runnerHeadStart $runnerHeadStartTimeSeconds, stamina: $stamina, speedKph: $speedKPH "
    }
    fun getStartingDistanceToRunnerMeters(): Double {
        val metersPerSecond = (speedKPH * 1000) / (60 * 60)
        return metersPerSecond * runnerHeadStartTimeSeconds
    }

    fun getHeadStart() = UnitsUtil.getFormattedStopWatchTime(runnerHeadStartTimeSeconds * 1000L, false)

    fun getStartText(context: Context) = when(runStartType) {
            MonsterStartType.TIME->{
                UnitsUtil.getFormattedStopWatchTime(UnitsUtil.minutesToMillis(startParam.toDouble()), false)
            }
            MonsterStartType.DISTANCE->{
                UnitsUtil.getDistanceText(startParam.toDouble(), context)
            }
        }

    fun getStaminaText(context: Context) = when(runStartType) {
        MonsterStartType.TIME->{
            UnitsUtil.getFormattedStopWatchTime(UnitsUtil.minutesToMillis(stamina.toDouble()), false)
        }
        MonsterStartType.DISTANCE->{
            UnitsUtil.getDistanceText(stamina.toDouble(), context)
        }
    }

    fun getTotalDistanceRunMeters() = when(runStartType)
    {
        MonsterStartType.TIME->{
            speedKPH * stamina / 60.0
        }
        MonsterStartType.DISTANCE->{
            stamina.toDouble()
        }
    }

    fun getTotalTimeMillis() = when(runStartType)
    {
        MonsterStartType.TIME->{
            stamina * 60 * 1000
        }
        MonsterStartType.DISTANCE->{
            val metersPerMillis = speedKPH / 60.0 / 60.0
            (stamina/metersPerMillis).toLong()
        }
    }

}