package me.onebone.minecombat.game

import cn.nukkit.Player
import me.onebone.minecombat.MineCombat
import java.util.LinkedList
import java.util.Random
import kotlin.coroutines.experimental.buildIterator

const val GAME_STATUS_READY = 0
const val GAME_STATUS_ONGOING = 1

abstract class Game(
		val plugin: MineCombat,
		val name: String,
		val counter: Int = -1
) {
	protected var status: Int = GAME_STATUS_READY
		set(v){
			v % 1
		}
	val isOngoing: Boolean
		get() = status == GAME_STATUS_ONGOING

	private val teams: Array<Team> = arrayOf()
	val teamCount: Int
		get() = teams.size

	val playerCount: Int
		get() {
			var count = 0
			for(team in teams){
				count += team.players.size
			}

			return count
		}

	val players = buildIterator {
		for(team in teams) {
			for(player in team.players) {
				yield(player)
			}
		}
	}

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
	 * Shuffles teammates
	 */
	open fun shuffleTeam() {
		val players = LinkedList<Player>()

		val random = Random()
		for(team in this.teams) {
			for(player in team.players){
				players.add(random.nextInt(players.size), player)
			}

			team.clearPlayers()
		}

		for((i, player) in players.withIndex()) {
			this.teams[i % this.teamCount].addPlayer(player)
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
