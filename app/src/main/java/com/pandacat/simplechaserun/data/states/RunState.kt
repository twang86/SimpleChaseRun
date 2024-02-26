package com.pandacat.simplechaserun.data.states

data class RunState(val activeState: State,
                    val runStartTimeMillis: Long
    )
{
    enum class State{
        NOT_STARTED,
        ACTIVE,
        PAUSED,
        STOPPED
    }
}