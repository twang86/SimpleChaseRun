package com.pandacat.simplechaserun.services.support

import android.util.Log
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.RunParam
import com.pandacat.simplechaserun.data.params.MonsterStartType
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.utils.UnitsUtil
import kotlin.math.roundToLong

class MonstersManager(val listener: MonsterListener): RunManagerBase {
    private val TAG = "MonstersManager"
    private val monsterStates = hashMapOf<Int, MonsterState>()
    private val monsterStartingPositions = hashMapOf<Int, RunnerState>()
    private var params : RunParam? = null

    interface MonsterListener{
        fun onMonsterStarted(type: MonsterType)
        fun onMonsterMoved(type: MonsterType, distInSeconds: Long)
        fun onMonsterFinished(type: MonsterType, success: Boolean)
    }
    fun updateMonsters(runnerState: RunnerState) : HashMap<Int, MonsterState>
    {
        if (params == null)
            return hashMapOf()
        val runParam = params!!
        var activeMonster: MonsterState? = null
        //update monsters
        //only one monster can active at any time
        //this function assumes that the monster list is sorted from earliest monster to latest monster
        for(monsterParam in runParam.monsterParams.entries)
        {
            //if any monsters are already active, ignore
            if (activeMonster != null)
                break

            //checks if monsters should be active based on params
            val shouldBeActive: Boolean = when(monsterParam.value.runStartType) {
                MonsterStartType.DISTANCE->
                    runnerState.totalDistanceM >= monsterParam.value.startParam
                MonsterStartType.TIME->
                    UnitsUtil.millisToMinutes(runnerState.totalTimeMillis) >= monsterParam.value.startParam
            }

            if (shouldBeActive)
            {
                var current = monsterStates[monsterParam.key]
                //check if monster hasn't been started yet
                if (current == null || current.state == MonsterState.State.NOT_STARTED)
                {
                    current = MonsterState(
                        monsterParam.value.monsterType,
                        MonsterState.State.ACTIVE,
                        monsterParam.value.getStartingDistanceToRunnerMeters(),
                        1F)
                    listener.onMonsterStarted(current.monsterType)
                    monsterStartingPositions[monsterParam.key] = runnerState
                    Log.i(TAG, "creating monster: $current")
                }

                if (current.state == MonsterState.State.ACTIVE)
                {
                    val startingState = monsterStartingPositions[monsterParam.key]!!
                    val timeSinceStartMillis = runnerState.totalTimeMillis - startingState.totalTimeMillis
                    val metersPerSecond = (monsterParam.value.speedKPH * 1000) / (60 * 60)
                    val metersPerMillis = metersPerSecond / 1000
                    val totalDistanceTraveled = metersPerMillis * timeSinceStartMillis
                    val runnerTotalDistanceTravelled = runnerState.totalDistanceM - startingState.totalDistanceM
                    val distanceToRunnerMeters = runnerTotalDistanceTravelled + monsterParam.value.getStartingDistanceToRunnerMeters() - totalDistanceTraveled
                    val staminaLeft = when(monsterParam.value.runStartType)
                    {
                        MonsterStartType.DISTANCE -> {
                            if (monsterParam.value.stamina <= totalDistanceTraveled) 0F else 1 - (totalDistanceTraveled / monsterParam.value.stamina).toFloat()
                        }
                        MonsterStartType.TIME -> {
                            if (monsterParam.value.stamina <= timeSinceStartMillis / 1000F / 60F) 0F else 1 - (timeSinceStartMillis / 1000F / 60F) / monsterParam.value.stamina
                        }
                    }

                    current = MonsterState(current.monsterType, current.state, distanceToRunnerMeters, staminaLeft)
                    activeMonster = current

                    Log.i(TAG, "updateMonsters: $current")
                    val caughtRunner = distanceToRunnerMeters <= 0
                    val selfFinished = staminaLeft == 0F
                    if (caughtRunner)
                    {
                        current = MonsterState(current.monsterType,MonsterState.State.FINISH_FAILURE, current.distanceToRunner, current.staminaLeft)
                        listener.onMonsterFinished(current.monsterType, false)
                        activeMonster = null
                    }
                    else if (selfFinished)
                    {
                        current = MonsterState(current.monsterType,MonsterState.State.FINISH_SUCCESS, current.distanceToRunner, current.staminaLeft)
                        listener.onMonsterFinished(current.monsterType, true)
                        activeMonster = null
                    }
                    else
                    {
                        val secondsUntilCapture = distanceToRunnerMeters/metersPerSecond
                        listener.onMonsterMoved(current.monsterType, secondsUntilCapture.roundToLong())
                    }
                    monsterStates[monsterParam.key] = current
                }
            }
        }
        return monsterStates
    }

    fun initMonsters(runParam: RunParam)
    {
        params = runParam
        monsterStates.clear()
        monsterStartingPositions.clear()
        for(monsterParam in runParam.monsterParams.entries)
        {
            monsterStates[monsterParam.key] = MonsterState.makeInitial(monsterParam.value.monsterType)
        }
    }

    fun getCurrentStates() = monsterStates
    override fun pauseRun() {
    }
    override fun startRun() {
    }

    override fun stopRun() {
        params = null
    }
}