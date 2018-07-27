package me.onebone.minecombat.game

import cn.nukkit.event.HandlerList
import cn.nukkit.event.Listener
import me.onebone.minecombat.MineCombat

class ScoreGame(plugin: MineCombat): SandGlassGame(
		plugin,
		20 * 60,
		20 * 60 * 5), Listener {
	init {
		plugin.server.pluginManager.registerEvents(this, plugin)
	}

	override fun onReady() {

		// TODO
	}

	override fun onStart() {
		this.clearTeamScores()
	}

	override fun close() {
		super.close()

		HandlerList.unregisterAll(this)
	}
}