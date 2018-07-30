package me.onebone.minecombat.game

import cn.nukkit.Player
import cn.nukkit.utils.TextFormat as T
import me.onebone.minecombat.MineCombat
import me.onebone.minecombat.util.KillLog
import java.util.LinkedList
import java.util.Random
import kotlin.coroutines.experimental.buildIterator

const val GAME_STATUS_READY = 0
const val GAME_STATUS_ONGOING = 1

abstract class Game(
		val plugin: MineCombat,
		val name: String,
		val counter: Int = -1,
		teamCount: Int = 2
) {
	protected var status: Int = GAME_STATUS_READY
		set(v){
			field = v % 2
		}
	val isOngoing: Boolean
		get() = status == GAME_STATUS_ONGOING

	open val statusMessage: String
		get() =
			if(this.isOngoing)
				"" + T.GREEN + "ONGOING"
			else
				"" + T.YELLOW + "READY"

	private var teams: Array<Team> = arrayOf()
	val teamCount: Int
		get() = teams.size

	private var lastAddedTeam: Int = 0
		set(v) {
			field = v % teams.size + 1
		}

	val playerCount: Int
		get() {
			var count = 0
			for(team in teams){
				count += team.players.size
			}

			return count
		}

	var killLog: Array<KillLog> = arrayOf()

	fun iteratePlayers(): Iterator<Player> {
		return buildIterator {
			for (team in teams) {
				for (player in team.players) {
					yield(player)
				}
			}
		}
	}

	init {
		for(i in 0..teamCount) {
			this.teams += Team()
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

	fun addPlayer(player: Player): Int {
		if(teamCount == 0) return -1

		teams[++lastAddedTeam].addPlayer(player)
		return lastAddedTeam
	}

	fun isEnemy(p1: Player, p2: Player): Boolean {
		var found = false

		for(team in teams) {
			for(player in team.players) {
				if(player == p1 || player == p2) {
					if(found) return false
					found = true
				}
			}
			found = false
		}

		return true
	}

	fun addKillLog(log: KillLog) {
		if(isGamer(log.damager) && isGamer(log.victim))
			killLog += log
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

	open fun tick(currentTick: Int) {
		killLog = killLog.filter { !it.isDeletable(currentTick) }.toTypedArray().also {
			it.forEach {log ->
				iteratePlayers().forEach {player ->
					player.sendTip(log.getMessage(isEnemy(player, log.damager)))
				}
			}
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
