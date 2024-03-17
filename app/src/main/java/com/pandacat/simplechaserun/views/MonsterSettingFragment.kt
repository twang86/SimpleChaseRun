package com.pandacat.simplechaserun.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.MonsterParam
import com.pandacat.simplechaserun.data.params.MonsterStartType
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
        for (monsterType in monsterValues) {
            monsterArray.add(monsterType.getDisplayName(requireContext()))
            if (monsterType == monsterParam.monsterType)
                break
            monsterIndex++
        }
        val monsterSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.item_large_text, monsterArray)

        val startTypeArray = arrayListOf<String>()
        val startTypeValues = MonsterStartType.values()
        var startTypeIndex = 0
        for (startType in startTypeValues) {
            startTypeArray.add(startType.getFriendlyName(requireContext()))
            if (startType == monsterParam.runStartType)
                break
            startTypeIndex++
        }
        val startTypeSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.item_large_text, startTypeArray)

        val monsterSpinner = binding.monsterSpinner
        monsterSpinner.adapter = monsterSpinnerAdapter
        monsterSpinner.setSelection(monsterIndex)
        monsterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val monsterType = monsterValues[pos]
                setParamValues(monsterType.getDefaultParams(monsterParam.runStartType))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val startTypeSpinner = binding.startTypeSpinner
        startTypeSpinner.adapter = startTypeSpinnerAdapter
        startTypeSpinner.setSelection(startTypeIndex)
        startTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val startType = startTypeValues[pos]
                setParamValues(monsterParam.monsterType.getDefaultParams(startType))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val startValueEdit = binding.startEdit
        val startValueUnit = binding.startValueUnit

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

        startValueUnit.text = when (monsterParam.runStartType) {
            MonsterStartType.TIME -> {
                requireContext().getString(R.string.unit_minutes_short)
            }

            MonsterStartType.DISTANCE -> {
                UnitsUtil.getBasicDistanceUnit(requireContext())
            }
        }

        val minStamina: Float = when (monsterParam.runStartType) {
            MonsterStartType.TIME -> Constants.MONSTER_MIN_RUN_TIME_MINUTES.toFloat()
            MonsterStartType.DISTANCE -> Constants.MONSTER_MIN_RUN_DISTANCE_METERS.toFloat()
        }

        val maxStamina: Float = when (monsterParam.runStartType) {
            MonsterStartType.TIME -> Constants.MONSTER_MAX_RUN_TIME_MINUTES.toFloat()
            MonsterStartType.DISTANCE -> Constants.MONSTER_MAX_RUN_DISTANCE_METERS.toFloat()
        }

        val staminaSlider = binding.staminaSlider
        val staminaEdit = binding.staminaEdit
        staminaSlider.valueFrom = minStamina
        staminaSlider.valueTo = maxStamina
        staminaSlider.stepSize = 1f
        staminaSlider.value = monsterParam.stamina.toFloat()

        binding.startHolder.hint = when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> UnitsUtil.getFormattedStopWatchTime(monsterParam.stamina * 60 * 1000)
            MonsterStartType.DISTANCE -> UnitsUtil.getDistanceText(monsterParam.stamina.toDouble(), requireContext())
        }

        staminaEdit.setText(when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> UnitsUtil.getFormattedStopWatchTime(monsterParam.stamina * 60 * 1000)
            MonsterStartType.DISTANCE -> UnitsUtil.getDistanceText(monsterParam.stamina.toDouble(), requireContext())
        })
        staminaEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    try{
                        val num = it.toString().toFloat()
                        staminaSlider.value = num
                    } catch (e:Exception)
                    {
                        //nothing
                    }
                }
            }
        })
        staminaSlider.addOnChangeListener{_,value,_->
            staminaEdit.setText(value.toString())
        }

        binding.staminaHolder.hint = when(monsterParam.runStartType)
        {
            MonsterStartType.TIME -> requireContext().getString(R.string.unit_minutes_short)
            MonsterStartType.DISTANCE -> UnitsUtil.getBasicDistanceUnit(requireContext())
        }

        val speedSlider = binding.speedSlider
        val speedText = binding.speedEdit
        speedSlider.valueFrom = Constants.MONSTER_MIN_SPEED_KPH.toFloat()
        speedSlider.valueTo = Constants.MONSTER_MAX_SPEED_KPH.toFloat()
        speedSlider.value = monsterParam.speedKPH.toFloat()
        speedText.setText(UnitsUtil.getSpeedText(monsterParam.speedKPH, requireContext()))
        speedText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                text?.let {
                    try{
                        val num = it.toString().toFloat()
                        speedSlider.value = num
                    } catch (e:Exception)
                    {
                        //nothing
                    }
                }
            }
        })
        speedSlider.addOnChangeListener{_,value,_->
            speedText.setText(UnitsUtil.getSpeedText(value.toDouble(), requireContext()))
        }

        binding.speedHolder.hint = UnitsUtil.getSpeedUnit(requireContext())

        binding.restoreDefaultButton.setOnClickListener{
            setParamValues(monsterParam.monsterType.getDefaultParams(monsterParam.runStartType))
        }

        val deleteButton = binding.deleteMonsterButton
        deleteButton.visibility = if(isEditMode()) View.VISIBLE else View.GONE

        binding.deleteMonsterButton.setOnClickListener{
            getSelectedMonsterIndex()?.let {
                RunService.runParams.value!!.monsterParams.remove(it)
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

        if (staminaValue == null){
            binding.staminaHolder.error = requireContext().getString(R.string.error_input_invalid)
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

        return MonsterParam(monsterType, startType, trueStartValue, Constants.MONSTER_MIN_HEAD_START_TIME_SECONDS, trueStaminaValue, speedValueKph)
    }
    private fun stopSelf()
    {
        findNavController().popBackStack()
    }
}