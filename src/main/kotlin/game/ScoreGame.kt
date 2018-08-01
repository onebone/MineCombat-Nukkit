package me.onebone.minecombat.game

import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import me.onebone.minecombat.MineCombat
import me.onebone.minecombat.util.GameConfig

class ScoreGame(
		plugin: MineCombat,
		private val config: GameConfig
): SandGlassGame(
		plugin,
		config.prepareTime,
		config.gameTime,
		config.default), Listener {
	override val name: String
		get() = config.name

	init {
		plugin.server.pluginManager.registerEvents(this, plugin)
	}

	override fun onReady() {
		super.onReady()

		iteratePlayers().forEach {
			
		}
	}

	override fun onStart() {
		super.onStart()

		this.clearTeamScores()
	}

	override fun close() {
		super.close()

		HandlerList.unregisterAll(this)
	}
}
