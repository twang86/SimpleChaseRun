package com.pandacat.simplechaserun.data.states

data class MonsterState(val activeState: State, val distanceM: Int, val activeTimeMillis: Long) {
    enum class State
    {
        NOT_STARTED,
        ACTIVE,
        FINISHED
    }
}