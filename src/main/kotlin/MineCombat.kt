package me.onebone.minecombat

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.plugin.PluginBase
import com.google.gson.GsonBuilder
import me.onebone.minecombat.gun.Pistol
import me.onebone.minecombat.util.GameConfig
import me.onebone.minecombat.util.PositionDeserializer
import java.io.File

class MineCombat: PluginBase(), Listener {
	override fun onEnable(){
		val f = File(this.dataFolder, "games")
		if(!f.exists()) f.mkdirs()

		val gson = GsonBuilder()
				.registerTypeAdapter(PositionDeserializer::class.java, PositionDeserializer(this.server))
				.create()
		f.walk().maxDepth(1).filter { it.isFile && it.extension == "json" }.forEach {
			val config = gson.fromJson(it.reader(), GameConfig::class.java) // FIXME

			//println(config.blueSpawn.toString())
		}

		this.server.pluginManager.registerEvents(this, this)
	}

	@EventHandler
	fun onPlayerTouch(event: PlayerInteractEvent){
		Pistol(event.player).shoot()
	}
}