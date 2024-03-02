package com.pandacat.simplechaserun.constants

object Constants {
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
    const val MONSTER_DANGER_CLOSE_SECONDS = 5L
    const val MONSTER_START_MIN_SECONDS = MONSTER_DANGER_CLOSE_SECONDS
    const val MONSTER_START_MAX_SECONDS = 2 * MONSTER_DANGER_CLOSE_SECONDS
}