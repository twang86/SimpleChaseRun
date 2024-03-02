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
    INFECTED(4.0, 9.6, 20, 60);

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
    }
}