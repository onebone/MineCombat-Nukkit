package me.onebone.minecombat.game

import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import me.onebone.minecombat.MineCombat
import me.onebone.minecombat.util.GameConfig

class ScoreGame(
		plugin: MineCombat,
		name: String,
		private val config: GameConfig
): SandGlassGame(
		plugin,
		name,
		config.prepareTime,
		config.gameTime), Listener {
	init {
		plugin.server.pluginManager.registerEvents(this, plugin)
	}

	override fun onReady() {
		super.onReady()

		players.forEach {
			
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
