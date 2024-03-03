package com.pandacat.simplechaserun.utils

import android.content.Context

object PreferenceUtil {
    private val PREFERENCE_FILE_NAME = "chaserun_prefs"
    private val PREFERENCE_DISTANCE_UNIT_USE_METRIC = "chaserun_distance_units"
    private val PREFERENCE_SPEED_UNIT_USE_MINUTES_PER_DISTANCE = "chaserun_speed_units_minutes_per_dist"

    fun getPrefs(context: Context) = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    fun useMetricSystem(context: Context) = getPrefs(context).getBoolean(
        PREFERENCE_DISTANCE_UNIT_USE_METRIC, false)

    fun useMinutesPerDistance(context: Context) = getPrefs(context).getBoolean(
        PREFERENCE_SPEED_UNIT_USE_MINUTES_PER_DISTANCE, true)
}