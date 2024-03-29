package com.pandacat.simplechaserun.views

import android.Manifest
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.databinding.FragmentPermissionsBinding
import com.pandacat.simplechaserun.utils.PermissionUtil

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
    private var notifyPermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission())
    {
        refreshViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object:
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!PermissionUtil.checkPermissions(PermissionUtil.getPermissionsRequired(), requireContext())) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.permissions_title))
                        .setMessage(getString(R.string.permissions_quit_message))
                        .setPositiveButton(getString(R.string.cancel), null)
                        .setNegativeButton(getString(R.string.ok)){_,_->
                            findNavController().popBackStack()
                        }
                        .create().show()
                    return
                }
            }

        })
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
                    PermissionUtil.showBGAppSettingsDialog(requireContext())
                bgTries++
            }
        }
        else {
            binding.locationBGGroup.visibility = View.GONE
        }

        var hasNotifyPermissions = false
        var hasFGServiceLocationPermission = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            val hasNotifyPermissions = PermissionUtil.checkPermission(Manifest.permission.POST_NOTIFICATIONS, requireContext())
            binding.notifyGroup.visibility = View.VISIBLE
            binding.notifyPermissionButton.visibility = if (hasNotifyPermissions) View.GONE else View.VISIBLE
            binding.notifyPermissionGranted.visibility = if (hasNotifyPermissions) View.VISIBLE else View.GONE
            binding.notifyPermissionButton.setOnClickListener {
                notifyPermissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        else{
            binding.notifyGroup.visibility = View.GONE
        }
        binding.locationPermissionsButton.visibility = if (hasLocationPermissions) View.GONE else View.VISIBLE
        binding.locationGrantedText.visibility = if (hasLocationPermissions) View.VISIBLE else View.GONE
        binding.locationPermissionsButton.setOnClickListener{
            permissionsRequestLauncher.launch(PermissionUtil.LOCATION_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshViews()
        if (PermissionUtil.checkPermissions(PermissionUtil.getPermissionsRequired(), requireContext()))
            findNavController().popBackStack()
    }
}