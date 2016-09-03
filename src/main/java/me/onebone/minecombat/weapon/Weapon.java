package me.onebone.minecombat.weapon;

import cn.nukkit.entity.Entity;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;

public abstract class Weapon{
	public static final int STATUS_PAUSED = 0;
	public static final int STATUS_WORKING = 1;
	public static final int STATUS_NOT_WORKING = 2;
	public static final int STATUS_CLOSED = 3;
	
	private int status = STATUS_NOT_WORKING;
	
	private Participant player;
	protected MineCombat plugin;
	
	private boolean isHolding = false;
	
	public Weapon(MineCombat plugin, Participant player){
		this.plugin = plugin;
		
		this.player = player;
		
		this.status = STATUS_WORKING;
	}
	
	public void setHolding(boolean isHolding){
		this.isHolding = isHolding;
	}
	
	public void setHolding(){
		this.setHolding(true);
	}
	
	public boolean isHolding(){
		return this.isHolding;
	}
	
	public final Participant getHolder(){
		return this.player;
	}
	
	public final void setHolder(Participant participant){
		this.player = participant;
	}
	
	/**
	 * Called when equipped player triggers attack
	 * 
	 * @param entity	Entity which equipped player interacted with. 
	 */
	public abstract void attack(Entity entity);
	
	/**
	 * Called when holder of weapon left the game
	 */
	public void pause(){
		this.status = STATUS_PAUSED;
	}
	
	/**
	 * Called when holder of weapon rejoined the game
	 */
	public void resume(){
		this.status = STATUS_WORKING;
	}
	
	/**
	 * Called when weapon is destroyed.
	 */
	public void close(){
		this.status = STATUS_CLOSED;
	}
	
	public int getStatus(){
		return this.status;
	}
}