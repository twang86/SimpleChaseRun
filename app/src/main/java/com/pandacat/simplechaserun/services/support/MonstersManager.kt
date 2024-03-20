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
        runParam.monsterParams.forEachIndexed{key, monsterParam->

            //checks if monsters should be active based on params
            val shouldBeActive: Boolean = when(monsterParam.runStartType) {
                MonsterStartType.DISTANCE->
                    runnerState.totalDistanceM >= monsterParam.startParam
                MonsterStartType.TIME->
                    UnitsUtil.millisToMinutes(runnerState.totalTimeMillis) >= monsterParam.startParam
            }

            if (shouldBeActive)
            {
                var current = monsterStates[key]
                //check if monster hasn't been started yet
                if (current == null || current.state == MonsterState.State.NOT_STARTED)
                {
                    current = MonsterState(
                        monsterParam.monsterType,
                        MonsterState.State.ACTIVE,
                        monsterParam.getStartingDistanceToRunnerMeters(),
                        1F)
                    listener.onMonsterStarted(current.monsterType)
                    monsterStartingPositions[key] = runnerState
                    Log.i(TAG, "creating monster: $current")
                }

                if (current.state == MonsterState.State.ACTIVE)
                {
                    val startingState = monsterStartingPositions[key]!!
                    val timeSinceStartMillis = runnerState.totalTimeMillis - startingState.totalTimeMillis
                    val metersPerSecond = (monsterParam.speedKPH * 1000) / (60 * 60)
                    val metersPerMillis = metersPerSecond / 1000
                    val totalDistanceTraveled = metersPerMillis * timeSinceStartMillis
                    val runnerTotalDistanceTravelled = runnerState.totalDistanceM - startingState.totalDistanceM
                    val distanceToRunnerMeters = runnerTotalDistanceTravelled + monsterParam.getStartingDistanceToRunnerMeters() - totalDistanceTraveled
                    val staminaLeft = when(monsterParam.runStartType)
                    {
                        MonsterStartType.DISTANCE -> {
                            if (monsterParam.stamina <= totalDistanceTraveled) 0F else 1 - (totalDistanceTraveled / monsterParam.stamina).toFloat()
                        }
                        MonsterStartType.TIME -> {
                            if (monsterParam.stamina <= timeSinceStartMillis / 1000F / 60F) 0F else 1 - (timeSinceStartMillis / 1000F / 60F) / monsterParam.stamina
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
                    monsterStates[key] = current
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
        runParam.monsterParams.forEachIndexed{index, monsterParam->
            monsterStates[index] = MonsterState.makeInitial(monsterParam.monsterType)
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