package com.pandacat.simplechaserun.data.states

import com.google.android.gms.maps.model.LatLng
import com.pandacat.simplechaserun.data.monsters.MonsterType
import kotlin.math.abs

data class MonsterState(val monsterType: MonsterType, val state: State, val distanceToRunner: Double, val staminaLeft: Float) {
    enum class State
    {
        NOT_STARTED,
        ACTIVE,
        FINISH_SUCCESS,
        FINISH_FAILURE
    }

    override fun toString(): String {
        return "type $monsterType, state $state, distance $distanceToRunner, staminaPercentage ${staminaLeft * 100}"
    }

    companion object
    {
        fun makeInitial(monsterType: MonsterType) = MonsterState(monsterType, State.NOT_STARTED, 0.0, 1F)
    }
}