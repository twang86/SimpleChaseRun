package com.pandacat.simplechaserun.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.pandacat.simplechaserun.R

object PermissionUtil {
    val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun getPermissionsRequired() : Array<String>
    {
        val permissions = ArrayList(LOCATION_PERMISSIONS.toList())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissions.add((Manifest.permission.POST_NOTIFICATIONS))

        return permissions.toTypedArray()

    }

    fun checkAllPermissions(context: Context) = checkPermissions(getPermissionsRequired(), context)

    fun checkPermissionsAndRequest(context: Context, navController: NavController)
    {
        if (!checkAllPermissions(context))
            navController.navigate(R.id.globalToPermissions)
    }

    fun requestPermissions(navController: NavController)
    {
        navController.navigate(R.id.globalToPermissions)
    }

    fun checkPermissions(permissionsNeeded: Array<String>, context: Context) : Boolean {
        var hasPermissions = true

        for (permission in permissionsNeeded) {
            if (!checkPermission(permission, context)) {
                hasPermissions = false
            }
        }
        return hasPermissions
    }

    fun checkPermission(permission: String, context: Context) : Boolean
    {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showBGAppSettingsDialog(context: Context)
    {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permissions_title))
            .setMessage(context.getString(R.string.permission_background_settings))
            .setPositiveButton(context.getString(R.string.ok)){_,_->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)}
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create().show()
    }
}