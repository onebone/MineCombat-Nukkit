package me.onebone.minecombat.game

import cn.nukkit.Player

class Team {
	var players: Set<Player> = setOf()
	val teamSize: Int
			get() = players.size
	var score: Int = 0

	fun addPlayer(player: Player) {
		players += player
	}

	fun removePlayer(player: Player) {
		players -= player
	}

	fun clearPlayers() {
		players = setOf()
	}
}
