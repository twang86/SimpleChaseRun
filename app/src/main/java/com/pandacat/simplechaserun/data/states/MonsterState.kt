package com.pandacat.simplechaserun.data.states

import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.data.monsters.MonsterType
import kotlin.math.abs

data class MonsterState(val monsterType: MonsterType, val state: State, val distanceM: Double, val runnerStateAtStart: RunnerState) {
    enum class State
    {
        NOT_STARTED,
        ACTIVE,
        FINISH_SUCCESS,
        FINISH_FAILURE
    }

    override fun toString(): String {
        return "type $monsterType, state $state, distance $distanceM, startTime $runnerStateAtStart"
    }

    fun getDistanceFromRunner(runnerTotalDistance: Double) : Double{
        val runnerDistanceSinceStart = runnerTotalDistance - runnerStateAtStart.totalDistanceM
        return runnerDistanceSinceStart - distanceM
    }

    companion object
    {
        fun makeInitial(monsterType: MonsterType) = MonsterState(monsterType, State.NOT_STARTED, 0.0, RunnerState(
            LatLng(0.0,0.0), 0.0, 0)
        )
    }
}