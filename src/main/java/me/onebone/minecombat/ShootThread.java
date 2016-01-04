package me.onebone.minecombat;

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
