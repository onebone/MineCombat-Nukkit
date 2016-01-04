package me.onebone.minecombat.data;

/*
 * MineCombat: FP..S? for Nukkit
 * Copyright (C) 2016 onebone <jyc00410@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import me.onebone.minecombat.gun.BaseGun;
import cn.nukkit.Player;

public class PlayerContainer{
	private Player player;
	private BaseGun gun;
	private int team;
	
	private boolean active = true;
	
	public PlayerContainer(Player player, BaseGun gun, int team){
		this.player = player;
		this.gun = gun;
		this.team = team;
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
	
	public int getTeam(){
		return this.team;
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
