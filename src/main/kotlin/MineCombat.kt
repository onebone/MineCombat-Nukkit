package me.onebone.minecombat

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.plugin.PluginBase
import me.onebone.minecombat.gun.Pistol

class MineCombat: PluginBase(), Listener {
	override fun onEnable(){
		this.server.pluginManager.registerEvents(this, this)
	}

	@EventHandler
	fun onPlayerTouch(event: PlayerInteractEvent){
		Pistol(event.player).shoot()
	}
}