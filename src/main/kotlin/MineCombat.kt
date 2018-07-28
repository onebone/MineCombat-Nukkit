package me.onebone.minecombat

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.plugin.PluginBase
import cn.nukkit.level.Position
import com.google.gson.GsonBuilder
import me.onebone.minecombat.game.Game
import me.onebone.minecombat.game.ScoreGame
import me.onebone.minecombat.gun.Pistol
import me.onebone.minecombat.task.TickTask
import me.onebone.minecombat.util.GameConfig
import me.onebone.minecombat.util.PositionDeserializer
import java.io.File

class MineCombat: PluginBase(), Listener {
	private var games: Map<String, Game> = mapOf()

	fun addGame(name: String, game: Game) {
		games += Pair(name, game)
	}

	fun removeGame(name: String) {
		games -= name
	}

	override fun onEnable(){
		val f = File(this.dataFolder, "games")
		if(!f.exists()) f.mkdirs()

		val gson = GsonBuilder()
				.registerTypeAdapter(Position::class.java, PositionDeserializer(this.server))
				.create()
		f.walk().maxDepth(1).filter { it.isFile && it.extension == "json" }.forEach {
			val config = gson.fromJson(it.reader(), GameConfig::class.java)

			this.addGame(it.nameWithoutExtension, ScoreGame(this, config))
		}

		games = mapOf()

		this.server.scheduler.scheduleRepeatingTask(TickTask(this) {
			for(game in games.values) {
				if(game.isOngoing) {
					if(game.canStop()) game.stop()
				}else if(game.canStart()) game.start()
			}
		}, 1)

		this.server.pluginManager.registerEvents(this, this)
	}

	@EventHandler
	fun onPlayerTouch(event: PlayerInteractEvent){
		Pistol(event.player).shoot()
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if(args[0] == "join") {
			
		}
		return true
	}
}
