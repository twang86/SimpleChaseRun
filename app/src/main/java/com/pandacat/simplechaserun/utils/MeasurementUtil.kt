package com.pandacat.simplechaserun.utils

object MeasurementUtil {
    fun minutesToMillis(minutes: Double) = (minutes * 60 * 1000).toLong()
    fun millisToMinutes(millis: Long) = millis / (60 * 1000.0)
}