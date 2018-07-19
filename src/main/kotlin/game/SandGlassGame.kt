package me.onebone.minecombat.game

import cn.nukkit.Server
import me.onebone.minecombat.MineCombat
import me.onebone.minecombat.task.TickTask

abstract class SandGlassGame (
		plugin: MineCombat,
		private val readyTick: Int,
		private val gameTick: Int
): Game(plugin) {
	private var time: Int = 0
	private val taskId: Int

	init {
		this.taskId = this.plugin.server.scheduler.scheduleDelayedRepeatingTask(TickTask(plugin) {
			time++

			if(this.status == GAME_STATUS_ONGOING) this.stop()
			else this.start()
		}, 1, 1).taskId
	}

	override fun canStart(): Boolean {
		return this.status == GAME_STATUS_READY
				&& this.readyTick <= this.time
	}

	override fun canStop(): Boolean {
		return this.status == GAME_STATUS_ONGOING
				&& this.gameTick <= this.time
	}

	override fun close() {
		this.plugin.server.scheduler.cancelTask(this.taskId)
	}
}