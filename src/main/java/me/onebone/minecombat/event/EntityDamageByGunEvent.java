package me.onebone.minecombat.event;

import cn.nukkit.event.HandlerList;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import me.onebone.minecombat.Participant;

public class EntityDamageByGunEvent extends EntityDamageByEntityEvent{
	private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers(){
        return handlers;
    }
    
	public EntityDamageByGunEvent(Participant damager, Participant entity, int cause, float damage) {
		super(damager.getPlayer(), entity.getPlayer(), cause, damage);
	}
}