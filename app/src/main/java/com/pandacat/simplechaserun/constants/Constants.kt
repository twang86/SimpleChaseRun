package com.pandacat.simplechaserun.constants

object Constants {
    //location provider
    const val LOCATION_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 1000L

    //run service
    const val START_RUNNING_COMMAND = "simplechaserun.start.run"
    const val STOP_RUNNING_COMMAND = "simplechaserun.stop.run"
    const val PAUSE_RUNNING_COMMAND = "simplechaserun.pause.run"
    const val NOTIFICATION_CHANNEL_ID = "simplechaserun.running.channel"
    const val NOTIFICATION_CHANNEL_NAME = "simplechaserun.Running"
    const val NOTIFICATION_ID = 1
    const val ACTION_SHOW_TRACKING_FRAGMENT = "simplechaserun.show.tracking.fragment"
}