package me.onebone.minecombat.event

import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent

class EntityDamageByGunEvent(
		damager: Entity, entity : Entity,
		damage: Float,
		val isCritical: Boolean,
		val symbol: String = "->"
): EntityDamageByEntityEvent(damager, entity, EntityDamageEvent.DamageCause.CUSTOM, damage)