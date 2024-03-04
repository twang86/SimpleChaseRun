package com.pandacat.simplechaserun.data.monsters

import android.content.Context
import com.pandacat.simplechaserun.R
import kotlin.math.roundToInt

enum class MonsterType(
    val minSpeedKPH: Double,
    val maxSpeedKPH: Double,
    val minChaseMinutes: Int,
    val maxChaseMinutes: Int) {

    ZOMBIE(2.5, 4.0, 20, 60),
    INFECTED(4.0, 9.6, 20, 60),
    T_REX(12.0, 15.0, 5, 10);

    fun getMinChaseKM() : Int{
        return (minSpeedKPH * (minChaseMinutes/60)).roundToInt()
    }

    fun getMaxChaseKM(): Int{
        return (maxSpeedKPH * (maxChaseMinutes/60)).roundToInt()
    }

    fun getDisplayName(context: Context) = when(this)
    {
        ZOMBIE->context.getString(R.string.monster_name_zombie)
        INFECTED->context.getString(R.string.monster_name_infected)
        T_REX->context.getString(R.string.monster_name_t_rex)
    }

    fun getDangerSound() = when(this)
    {
        ZOMBIE->R.raw.sound_zombie
        INFECTED->R.raw.sound_zombie
        T_REX->R.raw.sound_t_rex
    }
}