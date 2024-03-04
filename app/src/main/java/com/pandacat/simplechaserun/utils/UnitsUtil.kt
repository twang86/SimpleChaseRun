package com.pandacat.simplechaserun.utils

import android.content.Context
import com.pandacat.simplechaserun.R
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

object UnitsUtil {
    fun minutesToMillis(minutes: Double) = (minutes * 60 * 1000).toLong()
    fun millisToMinutes(millis: Long) = millis / (60 * 1000.0)

    fun formatDouble(toFormat: Double, decimalPlaces: Int) : String
    {
        var formatterString = "#."
        repeat(decimalPlaces){
            formatterString = "$formatterString#"
        }
        return DecimalFormat(formatterString).format(toFormat)
    }

    val MILES_TO_KM = 1.60934
    val METERS_TO_FEET = 3.28084
    val MILES_TO_FEET = 5280.0
    val FEET_TO_INCHES = 12.0

    fun getSpeedText(kph: Double, context: Context) : String
    {
        if (kph <= 0)
            return "--"
        val useMinutesPerDist = PreferenceUtil.useMinutesPerDistance(context)
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMinutesPerDist)
        {
            val minutesPerKm = 60/kph
            if (useMetric)
                return "${formatDouble(minutesPerKm, 2)} ${context.getString(R.string.symbol_slash)}${context.getString(R.string.unit_kilometer_short)}"
            val minutesPerMile = 60 / (kph / MILES_TO_KM)
            return "${formatDouble(minutesPerMile, 2)} ${context.getString(R.string.symbol_slash)}${context.getString(R.string.unit_mile_short)}"
        }
        if (useMetric)
            return "${formatDouble(kph, 2)} ${context.getString(R.string.speed_unit_kph)}"
        return "${formatDouble(kph / MILES_TO_KM, 2)} ${context.getString(R.string.speed_unit_mph)}"
    }

    fun getDistanceText(distanceMeters: Double, context: Context) : String
    {
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMetric)
        {
            if (distanceMeters>1000)
                return "${formatDouble(distanceMeters/1000, 3)}${context.getString(R.string.unit_kilometer_short)}"
            return "${formatDouble(distanceMeters, 2)}${context.getString(R.string.unit_meter_short)}"
        }
        else
        {
            val distanceFt = distanceMeters * METERS_TO_FEET
            if (distanceFt > 60) // about .01 mile
                return "${formatDouble(distanceFt/ MILES_TO_FEET, 3)}${context.getString(R.string.unit_mile_short)}"
            return "${formatDouble(distanceFt, 2)}${context.getString(R.string.unit_feet_short)}"
        }
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if(!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }
}