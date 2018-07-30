package me.onebone.minecombat.game

import cn.nukkit.Server
import cn.nukkit.utils.TextFormat as T
import me.onebone.minecombat.MineCombat
import me.onebone.minecombat.task.TickTask

abstract class SandGlassGame (
		plugin: MineCombat,
		name: String,
		val readyTick: Int,
		val gameTick: Int
): Game(plugin, name) {
	private var time: Int = 0
	private val taskId: Int

	override val statusMessage: String
		get() =
			super.statusMessage + T.WHITE +
				("(%ss" + T.WHITE + " left)").format(
					if(this.isOngoing)
						"" + T.GREEN + ((this.gameTick - time)/20)
					else
						"" + T.YELLOW + ((this.readyTick - time)/20)
				)

	init {
		this.taskId = this.plugin.server.scheduler.scheduleDelayedRepeatingTask(TickTask(plugin) {
			time++
		}, 1, 1).taskId
	}

	override fun onStart() {
		this.time = 0
	}

	override fun onReady() {
		this.time = 0
	}

	override fun canStart(): Boolean {
		return !this.isOngoing
				&& this.readyTick <= this.time
	}

	override fun canStop(): Boolean {
		return this.isOngoing
				&& this.gameTick <= this.time
	}

	override fun close() {
		this.plugin.server.scheduler.cancelTask(this.taskId)
	}
}
