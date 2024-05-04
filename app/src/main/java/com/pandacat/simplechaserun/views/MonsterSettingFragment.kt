package com.pandacat.simplechaserun.views

import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.MonsterParam
import com.pandacat.simplechaserun.data.params.MonsterStartType
import com.pandacat.simplechaserun.data.params.RunParam
import com.pandacat.simplechaserun.data.states.RunState
import com.pandacat.simplechaserun.databinding.FragmentMonsterSettingsBinding
import com.pandacat.simplechaserun.services.RunService
import com.pandacat.simplechaserun.utils.UnitsUtil
import kotlin.math.roundToLong

class MonsterSettingFragment : Fragment() {

    private lateinit var binding: FragmentMonsterSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = FragmentMonsterSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getSelectedMonsterParam() =
        getSelectedMonsterIndex()?.let {
            RunService.runParams.value!!.monsterParams[it]
        } ?: run {
            MonsterType.ZOMBIE.getDefaultParams(MonsterStartType.DISTANCE)
        }

    private fun getSelectedMonsterIndex() = arguments?.let {
        val index = it.getInt(Constants.NAV_ARG_MONSTER_INDEX, -1)
        if (index == -1)
            return null
        index
    } ?: run {
        null
    }

    private fun isEditMode() = getSelectedMonsterIndex() != null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setParamValues(getSelectedMonsterParam())
    }

    private fun setParamValues(monsterParam: MonsterParam) {
        val monsterArray = arrayListOf<String>()
        val monsterValues = MonsterType.values()
        var monsterIndex = 0
        monsterValues.forEachIndexed{index, monsterType ->
            monsterArray.add(monsterType.getDisplayName(requireContext()))
            if (monsterType == monsterParam.monsterType)
                monsterIndex = index
        }

        val monsterSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.item_large_text, monsterArray)

        val startTypeArray = arrayListOf<String>()
        val startTypeValues = MonsterStartType.values()
        var startTypeIndex = 0
        startTypeValues.forEachIndexed {index, startType->
            startTypeArray.add(startType.getFriendlyName(requireContext()))
            if (startType == monsterParam.runStartType)
                startTypeIndex = index
        }
        val startTypeSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.item_large_text, startTypeArray)

        val monsterSpinner = binding.monsterSpinner
        monsterSpinner.adapter = monsterSpinnerAdapter
        monsterSpinner.setSelection(monsterIndex)
        monsterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (monsterIndex != pos)
                {
                    Log.i("monsterSettings", "onMonsterSelected: $pos")
                    val monsterType = monsterValues[pos]
                    setParamValues(monsterType.getDefaultParams(monsterParam.runStartType))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val startTypeSpinner = binding.startTypeSpinner
        startTypeSpinner.adapter = startTypeSpinnerAdapter
        startTypeSpinner.onItemSelectedListener = null
        startTypeSpinner.setSelection(startTypeIndex)
        startTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (startTypeIndex != pos) {
                    Log.i("monsterSettings", "onStartTypeSelected: $pos")
                    val startType = startTypeValues[pos]
                    setParamValues(monsterParam.monsterType.getDefaultParams(startType))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val startValueEdit = binding.startEdit

        startValueEdit.setText(
            when (monsterParam.runStartType) {
                MonsterStartType.TIME -> {
                    monsterParam.startParam.toString()
                }

                MonsterStartType.DISTANCE -> {
                    UnitsUtil.formatDouble(
                        UnitsUtil.metersToDistanceValue(
                            monsterParam.startParam.toDouble(),
                            requireContext()
                        ), 2
                    )
                }
            }
        )

        binding.startHolder.hint = when (monsterParam.runStartType) {
            MonsterStartType.TIME -> {
                requireContext().getString(R.string.unit_minutes_short)
            }

            MonsterStartType.DISTANCE -> {
                UnitsUtil.getBasicDistanceUnit(requireContext())
            }
        }

        binding.startHolder.hint = when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> requireContext().getString(R.string.unit_minutes_short)
            MonsterStartType.DISTANCE -> UnitsUtil.getBasicDistanceUnit(requireContext())
        }

        val staminaEdit = binding.staminaEdit

        staminaEdit.setText(when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> monsterParam.stamina.toString()
            MonsterStartType.DISTANCE -> UnitsUtil.formatDouble(UnitsUtil.metersToDistanceValue(monsterParam.stamina.toDouble(), requireContext()), 2)
        })

        binding.staminaHolder.hint = when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> requireContext().getString(R.string.unit_minutes_short)
            MonsterStartType.DISTANCE -> UnitsUtil.getBasicDistanceUnit(requireContext())
        }

        val speedText = binding.speedEdit
        val speedValue = UnitsUtil.kphToSpeedValue(monsterParam.speedKPH, requireContext())
        speedText.setText(UnitsUtil.formatDouble(speedValue, 2))

        binding.speedHolder.hint = UnitsUtil.getSpeedUnit(requireContext())

        binding.restoreDefaultButton.setOnClickListener{
            setParamValues(monsterParam.monsterType.getDefaultParams(monsterParam.runStartType))
        }

        val deleteButton = binding.deleteMonsterButton
        deleteButton.visibility = if(isEditMode()) View.VISIBLE else View.GONE

        binding.deleteMonsterButton.setOnClickListener{
            getSelectedMonsterIndex()?.let {
                RunService.runParams.value!!.monsterParams.removeAt(it)
                stopSelf()
            }
        }

        binding.saveMonsterButton.setOnClickListener{
            val newParams = getMonsterParams()
            newParams?.let { params->
                val monsterParams = RunService.runParams.value!!.monsterParams
                getSelectedMonsterIndex()?.let {
                    monsterParams[it] = params
                } ?: run {
                    monsterParams.add(params)
                }
                RunService.runParams.value = RunParam(monsterParams)
                stopSelf()
            }
        }
    }

    private fun getMonsterParams() : MonsterParam?
    {
        val monsterSpinner = binding.monsterSpinner
        val startSpinner = binding.startTypeSpinner
        val startEdit = binding.startEdit
        val speedEdit = binding.speedEdit
        val staminaEdit = binding.staminaEdit

        val monsterIndex = monsterSpinner.selectedItemPosition
        val monsterType = MonsterType.values()[monsterIndex]
        val startIndex = startSpinner.selectedItemPosition
        val startType = MonsterStartType.values()[startIndex]

        val startValue = try {
            startEdit.text.toString().toDouble()
        } catch (e: Exception)
        {
            null
        }

        if (startValue == null) {
            binding.startHolder.error = requireContext().getString(R.string.error_input_invalid)
            return null
        }

        val trueStartValue = when(startType)
        {
            MonsterStartType.TIME->{
                startValue.roundToLong()
            }
            MonsterStartType.DISTANCE->{
                UnitsUtil.distanceValueToMeters(startValue, requireContext()).roundToLong()
            }
        }

        val staminaValue = try {
            staminaEdit.text.toString().toDouble()
        } catch (e: Exception)
        {
            null
        }

        val minStamina: Float = when (startType) {
            MonsterStartType.TIME -> Constants.MONSTER_MIN_RUN_TIME_MINUTES.toFloat()
            MonsterStartType.DISTANCE -> (Constants.MONSTER_MIN_RUN_DISTANCE_METERS/1000).toFloat()
        }

        val maxStamina: Float = when (startType) {
            MonsterStartType.TIME -> Constants.MONSTER_MAX_RUN_TIME_MINUTES.toFloat()
            MonsterStartType.DISTANCE -> (Constants.MONSTER_MAX_RUN_DISTANCE_METERS/1000).toFloat()
        }

        if (staminaValue == null || minStamina > staminaValue || maxStamina < staminaValue){
            binding.staminaHolder.error = requireContext().getString(R.string.error_input_invalid)
            Toast.makeText(requireContext(), "min: $minStamina max: $maxStamina", Toast.LENGTH_SHORT).show()
            return null
        }

        val trueStaminaValue = when(startType)
        {
            MonsterStartType.TIME->{
                staminaValue.roundToLong()
            }
            MonsterStartType.DISTANCE->{
                UnitsUtil.distanceValueToMeters(staminaValue, requireContext()).roundToLong()
            }
        }

        val speedValue = try {
            speedEdit.text.toString().toDouble()
        } catch (e: Exception)
        {
            null
        }

        if (speedValue == null){
            binding.speedHolder.error = requireContext().getString(R.string.error_input_invalid)
            return null
        }

        val speedValueKph = UnitsUtil.speedValueToKph(speedValue, requireContext())

        if (speedValueKph > Constants.MONSTER_MAX_SPEED_KPH || speedValueKph < Constants.MONSTER_MIN_SPEED_KPH)
        {
            binding.speedHolder.error = requireContext().getString(R.string.error_input_invalid)
            return null
        }

        return MonsterParam(monsterType, startType, trueStartValue, Constants.MONSTER_MIN_HEAD_START_TIME_SECONDS, trueStaminaValue, speedValueKph)
    }
    private fun stopSelf()
    {
        findNavController().popBackStack()
    }
}