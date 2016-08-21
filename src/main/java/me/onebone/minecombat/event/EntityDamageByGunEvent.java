package me.onebone.minecombat.event;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.entity.EntityDamageEvent;
import me.onebone.minecombat.Participant;

public class EntityDamageByGunEvent extends EntityDamageEvent{
	private static final HandlerList handlers = new HandlerList();

	private Participant damager, entity;
	
    public static HandlerList getHandlers(){
        return handlers;
    }
    
	public EntityDamageByGunEvent(Participant damager, Participant entity, int cause, float damage) {
		super(entity.getPlayer(), cause, damage);
		
		this.entity = entity;
		this.damager = damager;
	}
	
	public Participant getDamager(){
		return this.damager;
	}
	
	public Participant getDamaged(){
		return this.entity;
	}
}