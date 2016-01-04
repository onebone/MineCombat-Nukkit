package me.onebone.minecombat;

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

public class ShootThread extends Thread{
	private BaseGun gun;
	private boolean active = true, closed = false;
	
	public ShootThread(BaseGun gun){
		this.gun = gun;
	}
	
	public void run(){
		while(true){
			if(closed) return;
			if(active){
				this.gun.shoot();
			}
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	public void setActive(boolean active){
		this.active = active;
	}
	
	public void close(){
		this.closed = true;
	}
}
