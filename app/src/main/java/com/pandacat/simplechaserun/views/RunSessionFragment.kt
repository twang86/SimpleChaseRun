package com.pandacat.simplechaserun.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.databinding.FragmentRunSessionBinding
import com.pandacat.simplechaserun.services.RunService
import com.pandacat.simplechaserun.utils.PermissionUtil
import com.pandacat.simplechaserun.utils.UnitsUtil
import kotlin.math.roundToInt

class RunSessionFragment : Fragment() {
    private val TAG = "RunSessionFragment"
    private lateinit var binding: FragmentRunSessionBinding
    private var map: GoogleMap? = null

    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RunService.runState.observe(this) {
            updateControls()
            RunService.runState.value?.let {
                Log.i("navDebug", "update view run state: ${it.activeState} ")
                if (it.activeState == RunState.State.STOPPED)
                {
                    gotoSettings()
                }
            }
        }
        RunService.runnerState.observe(this) {
            updateView()
        }
        RunService.monsterStates.observe(this) {
            updateView()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRunSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
//        binding.mapView.getMapAsync{
//            map = it
//
//            val options = MarkerOptions()
//                .position(LatLng(39.161607200033515, -77.27608037908082))
//                .title("Test Marker")
//
//            BitmapUtil.getBitmapDescFromVector(requireContext(), R.drawable.ic_directions_run)?.let {icon->
//                options.icon(icon)
//            }
//
//            marker = it.addMarker(options)
//            marker?.showInfoWindow()
//            it.moveCamera(CameraUpdateFactory.newLatLngZoom(options.position, 16.0f))
//        }
        arguments?.let {
            if (it.getBoolean(Constants.NAV_ARG_START_RUN_COMMAND, false))
                sendCommandToService(Constants.START_RUNNING_COMMAND)
        }
    }

    private fun updateControls()
    {
        val curState = RunService.runState.value!!.activeState
        if (curState == RunState.State.ACTIVE) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
            binding.playPauseButton.setOnClickListener{
                    sendCommandToService(Constants.PAUSE_RUNNING_COMMAND)
            }
        } else
        {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow)
            binding.playPauseButton.setOnClickListener{
                if (checkPermissionAndInformUser())
                    sendCommandToService(Constants.START_RUNNING_COMMAND)
            }
        }

        binding.stopButton.visibility = if (curState == RunState.State.PAUSED) View.VISIBLE else View.GONE
        binding.stopButton.setOnClickListener{
            sendCommandToService(Constants.STOP_RUNNING_COMMAND)
        }
    }

    private fun updateView()
    {
        updateControls()
        val runnerState = RunService.runnerState.value!!
        val monsterState = RunService.monsterStates.value!!

        marker?.position = runnerState.currentPosition
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(runnerState.currentPosition, 16.0f))
        binding.distanceText.text = UnitsUtil.getDistanceText(runnerState.totalDistanceM, requireContext())
        binding.timeText.text = UnitsUtil.getFormattedStopWatchTime(runnerState.totalTimeMillis)
        binding.avgSpeedText.text = UnitsUtil.getSpeedText(runnerState.getKPH(), requireContext())


        var activeMonster: Map.Entry<Int, MonsterState>? = null
        var monstersLeft = 0
        for(entry in monsterState.entries)
        {
            if (entry.value.state == MonsterState.State.ACTIVE) {
                activeMonster = entry
            }
            if (entry.value.state == MonsterState.State.NOT_STARTED)
                monstersLeft++
        }
        binding.monsterLeftText.text = getString(R.string.monster_left_text, monstersLeft)

        activeMonster?.let {
            binding.monsterDistText.text = UnitsUtil.getDistanceText(it.value.distanceToRunner, requireContext())
            binding.monsterTypeText.text = it.value.monsterType.getDisplayName(requireContext())
            binding.monsterStaminaText.text = "${(it.value.staminaLeft * 100).roundToInt()}%"
            RunService.runParams.value!!.monsterParams[it.key]?.let {activeParam->
                binding.monsterSpeedText.text = UnitsUtil.getSpeedText(activeParam.speedKPH, requireContext())
            }?: run {
                binding.monsterSpeedText.text = getString(R.string.not_available)
            }
        } ?: run {
            binding.monsterDistText.text = getString(R.string.not_available)
            binding.monsterTypeText.text = getString(R.string.not_available)
            binding.monsterSpeedText.text = getString(R.string.not_available)
            binding.monsterStaminaText.text = getString(R.string.not_available)
        }
    }

    private fun checkPermissionAndInformUser() : Boolean
    {
        if (!PermissionUtil.checkAllPermissions(requireContext()))
        {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.permissions_title))
                .setMessage(getString(R.string.permissions_explanation))
                .setPositiveButton(getString(R.string.permissions_button_text)) {_,_->
                    PermissionUtil.requestPermissions(findNavController())
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .create().show()
            return false
        }
        return true
    }
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), RunService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun gotoSettings()
    {
        findNavController().navigate(R.id.runSessionToRunSettings)
    }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}