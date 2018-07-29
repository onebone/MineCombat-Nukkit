package me.onebone.minecombat

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.plugin.PluginBase
import cn.nukkit.level.Position
import cn.nukkit.utils.TextFormat as T
import com.google.gson.GsonBuilder
import me.onebone.minecombat.game.Game
import me.onebone.minecombat.game.ScoreGame
import me.onebone.minecombat.gun.Pistol
import me.onebone.minecombat.task.TickTask
import me.onebone.minecombat.util.GameConfig
import me.onebone.minecombat.util.PositionDeserializer
import java.io.File
import java.util.UUID
import kotlin.text.StringBuilder

class MineCombat: PluginBase(), Listener {
	private var games: Map<String, Game> = mapOf()

	fun addGame(name: String, game: Game) {
		games += Pair(name, game)
	}

	fun removeGame(name: String) {
		games -= name
	}

	override fun onEnable(){
		games = mapOf()

		val f = File(this.dataFolder, "games")
		if(!f.exists()) f.mkdirs()

		val gson = GsonBuilder()
				.registerTypeAdapter(Position::class.java, PositionDeserializer(this.server))
				.create()

		var count = 0
		f.walk().maxDepth(1).filter { it.isFile && it.extension == "json" }.forEach {
			val config = gson.fromJson(it.reader(), GameConfig::class.java)

			this.addGame(this.generateId(), ScoreGame(this, it.nameWithoutExtension, config))

			count++
		}

		this.logger.info("Loaded %d games from games/ directory".format(count))

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
			
		}else if(args[0] == "list") {
			sender.sendMessage("Showing list of games:")
			for((id, game) in this.games) {
				sender.sendMessage(("[" + T.GRAY + "%s" + T.WHITE + "]: " + T.GREEN + "%s").format(id, game.name))
			}
		}
		return true
	}

	private fun generateId(): String {
		return UUID.randomUUID().toString().substring(0, 8)
	}
}
