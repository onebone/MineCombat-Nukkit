package me.onebone.minecombat.task

import cn.nukkit.scheduler.PluginTask
import me.onebone.minecombat.MineCombat

class TickTask(plugin: MineCombat, private val func: (Int) -> Unit): PluginTask<MineCombat>(plugin) {
	override fun onRun(currentTick: Int) {
		func(currentTick)
	}
}