package me.onebone.minecombat.data;

import me.onebone.minecombat.gun.BaseGun;
import cn.nukkit.Player;

public class PlayerContainer{
	private Player player;
	private BaseGun gun;
	
	private boolean active = true;
	
	public PlayerContainer(Player player, BaseGun gun){
		this.player = player;
		this.gun = gun;
	}
	
	public void quit(){
		if(this.active){
			this.gun.setShooting(false);
			this.active = false;
		}
	}
	
	public BaseGun getGun(){
		return this.gun;
	}
	
	public void setActive(){
		this.active = true;
	}
	
	public boolean isActive(){
		return this.active;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public void startShoot(){
		if(this.active){
			this.gun.setShooting(true);
		}
	}
	
	public void stopShoot(){
		if(this.active){
			this.gun.setShooting(false);
		}
	}
	
	public void shoot(){
		if(this.active){
			this.gun.setShootOnce();
		}
	}
	
	public boolean isShooting(){
		return this.active && this.gun.isShooting(); 
	}
}
