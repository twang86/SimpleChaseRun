package com.pandacat.simplechaserun.services.support

import android.util.Log
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.monsters.MonsterType
import com.pandacat.simplechaserun.data.params.RunParam
import com.pandacat.simplechaserun.data.params.RunType
import com.pandacat.simplechaserun.data.states.MonsterState
import com.pandacat.simplechaserun.data.states.RunnerState
import com.pandacat.simplechaserun.utils.UnitsUtil
import kotlin.math.roundToLong

class MonstersManager(val listener: MonsterListener): RunManagerBase {
    private val TAG = "MonstersManager"
    private val monsterStates = hashMapOf<Int, MonsterState>()
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
            val shouldBeActive: Boolean = when(runParam.runType) {
                RunType.DISTANCE->
                    runnerState.totalDistanceM >= monsterParam.value.startParam
                RunType.TIME->
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
                        monsterParam.value.getMonsterStartingPositionMeters(),
                        runnerState)
                    listener.onMonsterStarted(current.monsterType)
                    Log.i(TAG, "creating monster: $current")
                }

                if (current.state == MonsterState.State.ACTIVE)
                {
                    val timeSinceStartMillis = runnerState.totalTimeMillis - current.runnerStateAtStart.totalTimeMillis
                    val metersPerSecond = (monsterParam.value.speedKPH * 1000) / (60 * 60)
                    val metersPerMillis = metersPerSecond / 1000
                    val totalDistanceTraveledMeters = metersPerMillis * timeSinceStartMillis + monsterParam.value.getMonsterStartingPositionMeters()

                    current = MonsterState(current.monsterType, current.state, totalDistanceTraveledMeters, current.runnerStateAtStart)
                    activeMonster = current

                    val distanceToRunnerMeters = current.getDistanceFromRunner(runnerState.totalDistanceM)

                    Log.i(TAG, "updateMonsters: runner ${runnerState.totalDistanceM} distance to runner $distanceToRunnerMeters")
                    Log.i(TAG, "updateMonsters: $current")
                    val caughtRunner = distanceToRunnerMeters <= 0
                    val selfFinished = when(runParam.runType) {
                        RunType.DISTANCE->
                            totalDistanceTraveledMeters >= monsterParam.value.stamina
                        RunType.TIME->
                            UnitsUtil.millisToMinutes(timeSinceStartMillis) >= monsterParam.value.stamina
                    }
                    if (caughtRunner)
                    {
                        current = MonsterState(current.monsterType,MonsterState.State.FINISH_FAILURE, totalDistanceTraveledMeters, current.runnerStateAtStart)
                        listener.onMonsterFinished(current.monsterType, false)
                        activeMonster = null
                    }
                    else if (selfFinished)
                    {
                        current = MonsterState(current.monsterType,MonsterState.State.FINISH_SUCCESS, totalDistanceTraveledMeters, current.runnerStateAtStart)
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