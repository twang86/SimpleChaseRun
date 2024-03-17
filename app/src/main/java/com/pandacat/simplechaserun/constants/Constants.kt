package com.pandacat.simplechaserun.constants

object Constants {
    //nav arg keys
    const val NAV_ARG_MONSTER_INDEX = "simplechaserun.monster.index"

    //monster run params
    const val MONSTER_MAX_SPEED_KPH = 30.0
    const val MONSTER_MIN_SPEED_KPH = 2.0
    const val MONSTER_MAX_RUN_TIME_MINUTES = 360
    const val MONSTER_MIN_RUN_TIME_MINUTES = 10
    const val MONSTER_MIN_START_DISTANCE_METERS = 50L
    const val MONSTER_MIN_START_TIME_MINUTES = 5L
    const val MONSTER_MIN_HEAD_START_TIME_SECONDS = 5
    const val MONSTER_MIN_RUN_DISTANCE_METERS = (MONSTER_MIN_SPEED_KPH * 1000 / 60) * MONSTER_MIN_RUN_TIME_MINUTES
    const val MONSTER_MAX_RUN_DISTANCE_METERS = (MONSTER_MAX_SPEED_KPH * 1000 / 60) * MONSTER_MAX_RUN_TIME_MINUTES

    //location provider
    const val LOCATION_TIME_OUT = 10000L
    const val LOCATION_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 1000L

    //run service
    const val MAX_DISTANCE_PER_SECOND_THRESHOLD_METERS = 5
    const val START_RUNNING_COMMAND = "simplechaserun.start.run"
    const val STOP_RUNNING_COMMAND = "simplechaserun.stop.run"
    const val PAUSE_RUNNING_COMMAND = "simplechaserun.pause.run"
    const val NOTIFICATION_CHANNEL_ID = "simplechaserun.running.channel"
    const val NOTIFICATION_CHANNEL_NAME = "simplechaserun.Running"
    const val NOTIFICATION_ID_RUN = 1
    const val ACTION_SHOW_TRACKING_FRAGMENT = "simplechaserun.show.tracking.fragment"

    //monsters
    const val MONSTER_DANGER_CLOSE_SECONDS = 10L
    const val MONSTER_START_MIN_SECONDS = MONSTER_DANGER_CLOSE_SECONDS
    const val MONSTER_START_MAX_SECONDS = 2 * MONSTER_DANGER_CLOSE_SECONDS
}