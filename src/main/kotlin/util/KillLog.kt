package me.onebone.minecombat.util

import cn.nukkit.Player
import cn.nukkit.utils.TextFormat as T

class KillLog (
		val damager: Player,
		val victim: Player,
		val tick: Int,
		val symbol: String
) {
	/**
	 * Provides message of kill log.
	 * `forEnemy` indicates if damager is enemy to receiver of kill log.
	 */
	fun getMessage(forEnemy: Boolean): String {
		return if(forEnemy)
			"" + T.RED + damager.name + T.WHITE + " " + symbol + " " + T.GREEN + victim.name + T.WHITE
		else
			"" + T.GREEN + damager.name + T.WHITE + " " + symbol + " " + T.RED + victim.name + T.WHITE
	}

	fun isDeletable(currentTick: Int): Boolean {
		return currentTick - tick >= Config.killLogTick
	}
}