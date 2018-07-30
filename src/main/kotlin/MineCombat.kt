package me.onebone.minecombat

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.player.PlayerDeathEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.plugin.PluginBase
import cn.nukkit.level.Position
import cn.nukkit.utils.TextFormat as T
import com.google.gson.GsonBuilder
import me.onebone.minecombat.event.EntityDamageByGunEvent
import me.onebone.minecombat.game.Game
import me.onebone.minecombat.game.ScoreGame
import me.onebone.minecombat.gun.Pistol
import me.onebone.minecombat.task.TickTask
import me.onebone.minecombat.util.Config
import me.onebone.minecombat.util.GameConfig
import me.onebone.minecombat.util.KillLog
import me.onebone.minecombat.util.PositionDeserializer
import java.io.File
import java.util.UUID

class MineCombat: PluginBase(), Listener {
	private var games: Map<String, Game> = mapOf()

	fun addGame(name: String, game: Game) {
		games += Pair(name, game)
	}

	fun removeGame(name: String) {
		games -= name
	}

	fun findPlayer(player: Player): Game? {
		for(game in this.games.values) {
			game.iteratePlayers().forEach {
				if(it == player) return game
			}
		}

		return null
	}

	override fun onEnable(){
		games = mapOf()

		this.saveDefaultConfig()

		Config.maxKillLogLength = this.config.getInt("maxKillLogLength", 4)
		Config.killLogTick = this.config.getInt("killLogTick", 40)

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
				game.tick(it)

				if(game.isOngoing) game.stop()
				else game.start()
			}
		}, 1)

		this.server.pluginManager.registerEvents(this, this)
	}

	@EventHandler
	fun onPlayerTouch(event: PlayerInteractEvent) {
		Pistol(event.player).shoot()
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val cause = event.entity.lastDamageCause
		if(cause is EntityDamageByGunEvent) {
			val damager = cause.damager
			val victim = event.entity

			if(damager is Player && victim is Player) {
				findPlayer(damager)?.let {
					if (it.isGamer(victim)) {
						it.addKillLog(KillLog(damager, victim, this.server.tick, cause.symbol))
					}
				}
			}
		}
	}

	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
		if(args[0] == "join") {
			if(sender !is Player) {
				sender.sendMessage("" + T.RED + "Please run this command in game.")
				return true
			}

			if(findPlayer(sender) == null) {
				if(args.size != 2) {
					sender.sendMessage("Usage: /mc join <id>")
				}else{
					if(args[1] in this.games) {
						val game = this.games[args[1]]!!
						val team = game.addPlayer(sender)
						if(team == -1) {
							sender.sendMessage("Team does not exist. Please call administrator of the game.")
						}else {
							sender.sendMessage("You've joined game. You are team #%d".format(team))
						}
					}
				}
			}else{
				sender.sendMessage("You already have game you've joined.")
			}
		}else if(args[0] == "list") {
			sender.sendMessage("Showing list of games:")
			for((id, game) in this.games) {
				sender.sendMessage((
						"[" + T.GRAY + "%s" + T.WHITE + "]: "
						+ "%s "
						+ T.WHITE + "%s" + T.WHITE
				).format(
						id,
						game.statusMessage,
						game.name))
			}
		}
		return true
	}

	private fun generateId(): String {
		return UUID.randomUUID().toString().substring(0, 8)
	}
}
