package com.pandacat.simplechaserun.views

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.databinding.FragmentPermissionsBinding
import com.pandacat.simplechaserun.utils.PermissionUtil
import pub.devrel.easypermissions.AppSettingsDialog

class PermissionFragment : Fragment()  {
    private val TAG = "PermissionFragment"
    private lateinit var binding: FragmentPermissionsBinding
    private var bgTries = 0

    private var permissionsRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refreshViews() }
    private var backgroundPermissionsRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    {
        refreshViews() }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshViews()
    }

    private fun refreshViews()
    {
        val hasLocationPermissions = PermissionUtil.checkPermissions(PermissionUtil.LOCATION_PERMISSIONS, requireContext())
        val hasBGPermissions: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasLocationPermissions)
        {
            binding.locationBGGroup.visibility = View.VISIBLE
            hasBGPermissions = PermissionUtil.checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, requireContext())
            binding.locationBGPermissionsButton.visibility = if (hasBGPermissions) View.GONE else View.VISIBLE
            binding.locationBGGrantedText.visibility = if (hasBGPermissions) View.VISIBLE else View.GONE
            binding.locationBGPermissionsButton.setOnClickListener {
                if (bgTries < 1)
                    backgroundPermissionsRequestLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                else
                {
                    AppSettingsDialog.Builder(this)
                        .setTitle(getString(R.string.permission_background_settings)).build().show()
                }
                bgTries++
            }
        }
        else {
            binding.locationBGGroup.visibility = View.GONE
        }
        binding.locationPermissionsButton.visibility = if (hasLocationPermissions) View.GONE else View.VISIBLE
        binding.locationGrantedText.visibility = if (hasLocationPermissions) View.VISIBLE else View.GONE
        binding.locationPermissionsButton.setOnClickListener{
            permissionsRequestLauncher.launch(PermissionUtil.LOCATION_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "PermissionFragment: ")
        refreshViews()
        if (PermissionUtil.checkPermissions(PermissionUtil.getPermissionsRequired(), requireContext()))
            findNavController().popBackStack()
    }
}