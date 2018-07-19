package me.onebone.minecombat.game

import cn.nukkit.Player
import me.onebone.minecombat.MineCombat

const val GAME_STATUS_READY = 0
const val GAME_STATUS_ONGOING = 1

abstract class Game(val plugin: MineCombat) {
	protected var status: Int = GAME_STATUS_READY
		set(v){
			v % 1
		}
	private val teams: Array<Team> = arrayOf()
	val teamCount: Int
		get() = teams.size

	/**
	 * Returns if player is in the game
	 */
	fun isGamer(player: Player): Boolean {
		for(team in teams){
			if(player in team.players) return true
		}

		return false
	}

	fun start(force: Boolean = false): Boolean {
		if(this.status == GAME_STATUS_READY) {
			if(this.canStart() || force){
				this.status = GAME_STATUS_ONGOING

				this.onStart()
				return true
			}
		}
		return false
	}

	fun stop(force: Boolean = false): Boolean {
		if(this.status == GAME_STATUS_ONGOING){
			if(this.canStop() || force){
				this.status = GAME_STATUS_READY

				this.onReady()
				return true
			}
		}
		return false
	}

	open fun clearTeamScores() {
		for(team in teams){
			team.score = 0
		}
	}

	/**
	 * Called when status of game is set to ready
	 */
	abstract fun onReady()

	/**
	 * Called when game is started
	 */
	abstract fun onStart()
	abstract fun canStop(): Boolean
	abstract fun canStart(): Boolean

	/**
	 * Called when game instance is destroyed by the plugin
	 */
	abstract fun close()
}