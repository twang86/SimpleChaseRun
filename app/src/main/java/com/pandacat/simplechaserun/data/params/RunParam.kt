package com.pandacat.simplechaserun.data.params

data class RunParam(val runType: RunType,
                    val monsterParams: HashMap<Int, MonsterParam>) {
}