package com.pandacat.simplechaserun.data.states

data class RunState(val runnerState: RunnerState,
                    val monsterStates: HashMap<Int, MonsterState>
    )