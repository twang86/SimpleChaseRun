package com.pandacat.simplechaserun.data.monsters

import android.content.Context
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.params.MonsterParam
import com.pandacat.simplechaserun.data.params.MonsterStartType
import kotlin.math.roundToInt

enum class MonsterType(
    val defaultSpeedKPH: Double,
    val defaultChaseMinutes: Long) {

    ZOMBIE(2.5, 20),
    INFECTED(4.0, 20),
    T_REX(12.0, 10);

    fun getDefaultParams(startType: MonsterStartType): MonsterParam
    {
        return when(startType)
        {
            MonsterStartType.TIME->{
                MonsterParam(this, startType, Constants.MONSTER_MIN_START_TIME_MINUTES, Constants.MONSTER_MIN_HEAD_START_TIME_SECONDS, defaultChaseMinutes, defaultSpeedKPH)
            }
            MonsterStartType.DISTANCE-> {
                MonsterParam(this, startType, Constants.MONSTER_MIN_START_DISTANCE_METERS, Constants.MONSTER_MIN_HEAD_START_TIME_SECONDS, getDefaultChaseDistanceMeters().toLong(), defaultSpeedKPH)
            }
        }
    }

    fun getDefaultChaseDistanceMeters() = (defaultChaseMinutes / 60.0) * (defaultSpeedKPH * 1000)

    fun getDisplayName(context: Context) = when(this)
    {
        ZOMBIE->context.getString(R.string.monster_name_zombie)
        INFECTED->context.getString(R.string.monster_name_infected)
        T_REX->context.getString(R.string.monster_name_t_rex)
    }

    fun getSimpleImageResId() = when(this)
    {
        ZOMBIE->R.drawable.ic_monster_simple
        INFECTED->R.drawable.ic_monster_simple
        T_REX->R.drawable.ic_monster_simple
    }

    fun getDangerSound() = when(this)
    {
        ZOMBIE->R.raw.sound_zombie
        INFECTED->R.raw.sound_zombie
        T_REX->R.raw.sound_t_rex
    }
}