package me.onebone.minecombat.util

import cn.nukkit.level.Position

data class GameConfig (
		val name: String,
		val prepareTime: Int,
		val gameTime: Int,
		val maxScore: Int,
		val redSpawn: Position,
		val blueSpawn: Position,
		val default: Boolean = false
)
