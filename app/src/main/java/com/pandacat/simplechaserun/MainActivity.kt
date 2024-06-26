package com.pandacat.simplechaserun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.databinding.ActivityMainBinding
import com.pandacat.simplechaserun.services.RunService
import com.pandacat.simplechaserun.utils.PermissionUtil
import com.pandacat.simplechaserun.utils.PreferenceUtil

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        PermissionUtil.checkPermissionsAndRequest(this, navController)
        navigateToTrackingFragmentIfNeeded(intent)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (navController.currentDestination?.id == R.id.runSessionFragment)
            return
        if(intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.popBackStack()
            navController.navigate(R.id.globalToRunSession)
            return
        }
        RunService.runState.value?.let {
            if(it.activeState == RunState.State.ACTIVE || it.activeState == RunState.State.PAUSED) {
                navController.popBackStack()
                navController.navigate(R.id.globalToRunSession)
            }
        }
    }
}