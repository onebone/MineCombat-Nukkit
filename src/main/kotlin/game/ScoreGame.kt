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
		20 * 60,
		20 * 60 * 5), Listener {
	init {
		plugin.server.pluginManager.registerEvents(this, plugin)
	}

	override fun onReady() {
		players.forEach {
			
		}
	}

	override fun onStart() {
		this.clearTeamScores()
	}

	override fun close() {
		super.close()

		HandlerList.unregisterAll(this)
	}
}
