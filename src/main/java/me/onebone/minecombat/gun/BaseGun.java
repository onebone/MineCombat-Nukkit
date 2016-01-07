package me.onebone.minecombat.gun;

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


import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ExplodePacket;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.ShootThread;
import me.onebone.minecombat.event.EntityDamageByGunEvent;

abstract public class BaseGun {
	private MineCombat plugin;
	private Player owner;
	
	private int loadedAmmo = 0, magazine = 0;
	
	protected long lastShoot = 0;
	private boolean isShooting = false, unsetFire = false;
	
	private ShootThread thr;
	
	public BaseGun(MineCombat plugin, Player owner, int magazine){
		this.magazine = magazine;
		this.owner = owner;
		
		this.plugin = plugin;
		
		this.thr = new ShootThread(this);
		this.thr.start();
	}
	
	public BaseGun(MineCombat plugin, Player owner){
		this(plugin, owner, 50);
	}
	
	public void setOwner(Player owner){
		this.owner = null;
		
		this.owner = owner;
	}
	
	public void reset(){
		this.loadedAmmo = 0;
		this.magazine = this.getDefaultMagazine();
	}
	
	public int reload(){
		if(magazine <= 0) return 0;
		
		int transfer = Math.min(this.magazine, this.getMaxAmmo());
		this.loadedAmmo += transfer;
		this.magazine -= transfer;
		
		return transfer;
	}
	
	public void addAmmo(int amount){
		this.magazine += amount;
	}
	
	public int getAmmo(){
		return this.loadedAmmo;
	}
	
	public int getMagazine(){
		return this.magazine;
	}
	
	public boolean isShooting(){
		return this.isShooting;
	}
	
	public void setShooting(boolean shoot){
		this.isShooting = shoot;
	}
	
	public void setShooting(){
		this.setShooting(true);
	}
	
	public void setShootOnce(){
		this.setShooting(true);
		this.unsetFire = true;
	}
	
	final public boolean shoot(){
		if(this.isShooting){
			if(this.unsetFire){
				this.unsetFire = false;
				this.isShooting = false;
			}
			long now = System.currentTimeMillis();
			if(!this.canShoot(now - this.lastShoot)){
				return false;
			}
			
			if(this.onShoot()){
				this.loadedAmmo--;
			}else{
				this.isShooting = false;
			}
			
			this.lastShoot = now;
			return true;
		}
		return false;
	}
	
	public boolean onShoot(){
		if(this.loadedAmmo <= 0){
			if(this.reload() == 0){
				return false;
			}
		}
		
		Server server = this.plugin.getServer();
		
		if(owner != null){
			Level level = owner.getLevel();
			double _x = owner.getX();
			double _y = owner.getY() + owner.getEyeHeight();
			double _z = owner.getZ();
			
			ExplodePacket pk = new ExplodePacket();
			pk.x = (float) _x;
			pk.y = (float) _y;
			pk.z = (float) _z;
			pk.radius = 0.1F;
			pk.records = new Vector3[]{};
			Map<Long, Player> players = level.getPlayers();
			for(Long key : players.keySet()){
				players.get(key).dataPacket(pk);
			}
			
			double xcos = Math.cos((owner.getYaw() - 90) / 180 * Math.PI);
			double zsin = Math.sin((owner.getYaw() - 90) / 180 * Math.PI);
			double pcos = Math.cos((owner.getPitch() + 90) / 180 * Math.PI);
			
			Map<String, Player> online = server.getOnlinePlayers();
			for(int c = 0; c < this.getRange(); c++){
				Vector3 vec = new Vector3(_x - (c * xcos), _y + (c * pcos), _z - (c * zsin));
				level.addParticle(new DustParticle(vec, 0xb3, 0xb3, 0xb3));
				
				if(level.getBlock(new Vector3(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z))).isSolid()) return true;
				for(String username : online.keySet()){
					Player player = online.get(username);
					
					if(player == owner) continue;
					
					if(this.canHit(vec, player)){
						if(this.isHeadshot(vec, player)){
							player.attack(new EntityDamageByGunEvent(owner, player, MineCombat.CAUSE_HEADSHOT, this.getHeadshotDamage(owner.distance(player))));
						}else{
							player.attack(new EntityDamageByGunEvent(owner, player, MineCombat.CAUSE_GUN, this.getDamage(owner.distance(player))));
						}
						return true;
					}
				}
			}
		}
		return true;
	}
	
	public boolean canHit(Vector3 vec, Player player){
		return (!this.plugin.isColleague(player.getName(), this.owner.getName())) && (player.getX() - 1 < vec.getX() && vec.getX() < player.getX() + 1
				&& player.getY() < vec.getY() && vec.getY() < player.getY() + player.getHeight()
				&& player.getZ() - 1 < vec.getZ() && vec.getZ() < player.getZ() + 1);
	}
	
	public boolean isHeadshot(Vector3 vec, Player player){
		return (player.getX() - 1 < vec.getX() && vec.getX() < player.getX() + 1
				&& player.getY() + player.getEyeHeight() < vec.getY() && vec.getY() < player.getY() + player.getHeight()
				&& player.getZ() - 1 < vec.getZ() && vec.getZ() < player.getZ() + 1);
	}
	
	public int getHeadshotDamage(double distance){
		return 20;
	}
	
	abstract public boolean canShoot(long fromLastShoot);
	abstract public int getRange();
	abstract public int getMaxAmmo();
	abstract public int getDefaultMagazine();
	abstract public int getDamage(double distance);
	abstract public String getName();
}
