package me.onebone.minecombat

import cn.nukkit.Player
import cn.nukkit.event.entity.EntityDamageEvent

abstract class Gun (
		private val player: Player
) {
	open fun shoot() {
		this.checkHit()
	}

	protected fun checkHit() {
		val u = this.player.directionVector
		val a = this.player.position.add(0.0, player.eyeHeight.toDouble(), 0.0)

		for(target in player.level.players.values){
			if(target == this.player) continue

			val b = target.position
			val t = (u.x*(b.x-a.x)+u.y*(b.y-a.y)+u.z*(b.z-a.z)) /
						(u.x*u.x + u.y*u.y + u.z*u.z)

			val dist = Math.sqrt(
						Math.pow(u.x*t+a.x-b.x, 2.0)
						+ Math.pow(u.y*t+a.y-b.y, 2.0)
						+ Math.pow(u.z*t+a.z-b.z, 2.0))

			if(dist < 0.5){
				this.onHit(target)
			}
		}
	}

	open fun onHit(p: Player) {
		p.attack(EntityDamageEvent(p, EntityDamageEvent.DamageCause.CUSTOM, 5.0F))
	}
}