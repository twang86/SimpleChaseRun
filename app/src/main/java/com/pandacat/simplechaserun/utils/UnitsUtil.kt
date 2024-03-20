package com.pandacat.simplechaserun.utils

import android.content.Context
import android.util.Log
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

    fun kphToSpeedValue(kph: Double, context: Context) : Double {
        val useMinutesPerDist = PreferenceUtil.useMinutesPerDistance(context)
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMinutesPerDist) {
            if (useMetric)
                return 60 / kph
            return 60 / (kph / MILES_TO_KM)
        }
        if (useMetric)
            return kph
        return kph / MILES_TO_KM
    }

    fun speedValueToKph(speedValue: Double, context: Context) : Double
    {
        val useMinutesPerDist = PreferenceUtil.useMinutesPerDistance(context)
        val useMetric = PreferenceUtil.useMetricSystem(context)
        var speedPerHour = speedValue
        if (useMinutesPerDist) {
            speedPerHour = 60/speedValue
        }
        if (useMetric)
            return speedPerHour
        return speedPerHour * MILES_TO_KM
    }

    fun getSpeedUnit(context: Context) : String
    {
        val useMinutesPerDist = PreferenceUtil.useMinutesPerDistance(context)
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMinutesPerDist)
        {
            if (useMetric)
                return "${context.getString(R.string.symbol_slash)}${context.getString(R.string.unit_kilometer_short)}"
            return "${context.getString(R.string.symbol_slash)}${context.getString(R.string.unit_mile_short)}"
        }
        if (useMetric)
            return  context.getString(R.string.speed_unit_kph)
        return context.getString(R.string.speed_unit_mph)
    }

    fun getSpeedText(kph: Double, context: Context) : String
    {
        if (kph <= 0)
            return "--"
        return "${formatDouble(kphToSpeedValue(kph, context), 2)}${getSpeedUnit(context)}"
    }

    fun getBasicDistanceUnit(context: Context) : String{
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMetric)
        {
            return context.getString(R.string.unit_kilometer_short)
        }
        return context.getString(R.string.unit_mile_short)
    }

    fun metersToDistanceValue(distanceMeters: Double, context: Context) : Double
    {
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMetric)
            return distanceMeters / 1000
        return distanceMeters / 1000 / MILES_TO_KM
    }

    fun distanceValueToMeters(basicValue: Double, context: Context) : Double
    {
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMetric)
            return basicValue * 1000
        return basicValue * MILES_TO_KM * 1000
    }

    fun getDistanceText(distanceMeters: Double, context: Context) = getDistanceText(distanceMeters, false, context)

    fun getDistanceText(distanceMeters: Double, useBasicUnits: Boolean, context: Context) : String
    {
        val useMetric = PreferenceUtil.useMetricSystem(context)
        if (useMetric)
        {
            if (distanceMeters<=1000 && !useBasicUnits)
                return "${formatDouble(distanceMeters, 2)}${context.getString(R.string.unit_meter_short)}"
            return "${formatDouble(distanceMeters/1000, 3)}${context.getString(R.string.unit_kilometer_short)}"
        }
        else
        {
            val distanceFt = distanceMeters * METERS_TO_FEET
            if (distanceFt <= 60 && !useBasicUnits) // about .01 mile
                return "${formatDouble(distanceFt, 2)}${context.getString(R.string.unit_feet_short)}"
            return "${formatDouble(distanceFt/ MILES_TO_FEET, 3)}${context.getString(R.string.unit_mile_short)}"
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