package com.pandacat.simplechaserun.data.params

import android.content.Context
import com.pandacat.simplechaserun.R

enum class MonsterStartType(val nameResId : Int) {
    DISTANCE(R.string.monster_setting_start_distance),
    TIME(R.string.monster_setting_start_time);

    fun getFriendlyName(context: Context) = context.getString(nameResId)
}