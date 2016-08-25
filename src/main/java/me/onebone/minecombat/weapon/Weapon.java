package me.onebone.minecombat.weapon;

import cn.nukkit.entity.Entity;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;

public abstract class Weapon{
	private Participant player;
	protected MineCombat plugin;
	
	public Weapon(MineCombat plugin, Participant player){
		this.plugin = plugin;
		
		this.player = player;
	}
	
	public final Participant getEquippedBy(){
		return this.player;
	}
	
	/**
	 * Called when equipped player triggers attack
	 * 
	 * @param entity	Entity which equipped player interacted with. 
	 */
	public abstract void attack(Entity entity);
	
	/**
	 * Called when weapon is destroyed.
	 */
	public abstract void close();
}