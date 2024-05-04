package com.pandacat.simplechaserun.views

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.RunParam
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.databinding.FragmentRunSettingsBinding
import com.pandacat.simplechaserun.services.RunService
import com.pandacat.simplechaserun.utils.UnitsUtil
import com.pandacat.simplechaserun.views.adapters.MonsterAdapter

class RunSettingFragment: Fragment(), MonsterAdapter.Listener {
    private lateinit var binding: FragmentRunSettingsBinding
    private lateinit var monsterAdapter: MonsterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        monsterAdapter = MonsterAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentRunSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //todo this code needs to be in RunFinishedFragment
        if (RunService.runState.value!!.activeState == RunState.State.STOPPED)
            RunService.resetRun()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = binding.monsterSelection
        recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = monsterAdapter

        binding.addMonsterButton.setOnClickListener{
            findNavController().navigate(R.id.runSettingsToMonsterSettings)
        }

        binding.startRunButton.setOnClickListener{
            val command = bundleOf(Constants.NAV_ARG_START_RUN_COMMAND to true)
            findNavController().navigate(R.id.runSettingToRunSession, command)
        }

        RunService.runParams.observe(viewLifecycleOwner) {
            updateView(it)
        }

        RunService.runParams.value?.let {
            updateView(it)
        }
    }

    private fun updateView(runParam: RunParam)
    {
        monsterAdapter.updateMonsters(runParam.monsterParams)
        var minDistanceM = 0.0
        var minTimeMillis = 0L
        for(monsterParam in runParam.monsterParams)
        {
            minDistanceM += monsterParam.getTotalDistanceRunMeters()
            minTimeMillis += monsterParam.getTotalTimeMillis()
        }
        binding.totalMonstersText.text = runParam.monsterParams.count().toString()
        binding.minRunDistanceText.text = UnitsUtil.getDistanceText(minDistanceM, requireContext())
        binding.minRunTimeText.text = UnitsUtil.getFormattedStopWatchTime(minTimeMillis, false)
    }

    override fun onMonsterClicked(index: Int) {
        val bundle = bundleOf(Constants.NAV_ARG_MONSTER_INDEX to index)
        findNavController().navigate(R.id.runSettingsToMonsterSettings, bundle)
    }

    override fun onMonsterLongClicked(index: Int) {
        RunService.runParams.value?.monsterParams?.removeAt(index)
    }
}