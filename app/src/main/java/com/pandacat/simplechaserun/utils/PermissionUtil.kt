package com.pandacat.simplechaserun.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions

class PermissionUtil {
    companion object
    {
        fun getPermissionsRequired() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

    }


}