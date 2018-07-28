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
			if(target == player) continue

			val n = target.directionVector.apply { y = 0.0 }
			val b = target.position
			val d = -n.x*b.x -n.y*b.y -n.z*b.z

			val t = (-n.x*a.x - n.y*a.y - n.z*a.z - d) /
					(n.x*u.x + n.y*u.y + n.z*u.z)

			val hitX = u.x*t + a.x
			val hitY = u.y*t + a.y
			val hitZ = u.z*t + a.z

			if(target.x - target.width/2.0 <= hitX && hitX <= target.x + target.width/2.0
			&& target.z - target.length/2.0 <= hitZ && hitZ <= target.z + target.length/2.0
			&& target.y <= hitY && hitY <= target.y + target.height){
				this.onHit(target)
			}
		}
	}

	open fun onHit(p: Player) {
		p.attack(EntityDamageEvent(p, EntityDamageEvent.DamageCause.CUSTOM, 5.0F))
	}
}
