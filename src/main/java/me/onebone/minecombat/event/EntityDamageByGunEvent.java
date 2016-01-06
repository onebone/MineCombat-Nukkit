package me.onebone.minecombat.event;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.entity.EntityDamageEvent;

public class EntityDamageByGunEvent extends EntityDamageEvent{
	private static final HandlerList handlers = new HandlerList();

	private Entity damager;
	
    public static HandlerList getHandlers() {
        return handlers;
    }
    
	public EntityDamageByGunEvent(Entity damager, Entity entity, int cause, float damage) {
		super(entity, cause, damage);
		
		this.damager = damager;
	}
	
	public Entity getDamager(){
		return this.damager;
	}
}
