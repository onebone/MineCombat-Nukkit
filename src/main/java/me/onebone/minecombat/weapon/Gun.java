package me.onebone.minecombat.weapon;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ExplodePacket;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;
import me.onebone.minecombat.event.EntityDamageByGunEvent;

public abstract class Gun extends Weapon{
	public static final int CAUSE_GUN = 15;
	public static final int CAUSE_HEADSHOT = 16;
	
	protected boolean isShooting = false;
	protected Item gunItem = Item.get(Item.MELON_STEM);
	
	private ShootThread thr = null;
	
	private int loaded, magazine, defaultLoaded, defaultMagazine;
	
	public Gun(MineCombat plugin, Participant player, int loaded, int magazine){
		super(plugin, player);
		
		this.defaultLoaded = this.loaded = loaded;
		this.defaultMagazine = this.magazine = magazine;
		
		this.thr = new ShootThread();
		this.thr.start();
	}
	
	public int getLoaded(){
		return this.loaded;
	}
	
	public int getMagazine(){
		return this.magazine;
	}
	
	public int reloadAmmo(){
		int load = Math.min(this.getMaxLoaded() - this.loaded, this.magazine);
		if(load <= 0){
			return this.loaded;
		}
		
		this.magazine -= load;
		this.loaded += load;
		
		return this.loaded;
	}
	
	public void resetAmmo(){
		this.loaded = this.defaultLoaded;
		this.magazine = this.defaultMagazine;
	}
	
	/**
	 * @return Shoot interval in tick
	 */
	public int getShootInterval(){
		return 20;
	}
	
	public int getMaxLoaded(){
		return 10;
	}
	
	public int getHitDamage(double distance){
		return 1;
	}
	
	public int getShotDamage(double distance){
		return 5;
	}
	
	public int getHeadshotDamage(double distance){
		return 20;
	}
	
	public int getRange(){
		return 30;
	}
	
	public abstract String getName();
	
	public boolean canHit(Vector3 vec, Participant participant){
		Player player = participant.getPlayer();
		
		return (!this.getEquippedBy().getJoinedGame().isColleague(participant, this.getEquippedBy())) && (player.getX() - 1 < vec.getX() && vec.getX() < player.getX() + 1
				&& player.getY() < vec.getY() && vec.getY() < player.getY() + player.getHeight()
				&& player.getZ() - 1 < vec.getZ() && vec.getZ() < player.getZ() + 1);
	}
	
	public boolean isHeadshot(Vector3 vec, Participant participant){
		Player player = participant.getPlayer();
		
		return (player.getX() - 1 < vec.getX() && vec.getX() < player.getX() + 1
				&& player.getY() + player.getEyeHeight() < vec.getY() && vec.getY() < player.getY() + player.getHeight()
				&& player.getZ() - 1 < vec.getZ() && vec.getZ() < player.getZ() + 1);
	}
	
	public boolean shoot(){
		if(this.loaded <= 0){
			if(this.reloadAmmo() == 0){
				return false;
			}
		}
		
		Player owner = this.getEquippedBy().getPlayer();
		if(owner != null){
			this.loaded--;
			
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
			Participant[] players = this.getEquippedBy().getJoinedGame().getParticipants().stream().filter(participant -> {
				if(this.getEquippedBy().getPlayer().getLevel().getPlayers().containsValue(participant)){
					participant.getPlayer().dataPacket(pk);
					return true;
				}
				return false;
			}).toArray(Participant[]::new);
			
			double xcos = Math.cos((owner.getYaw() - 90) / 180 * Math.PI);
			double zsin = Math.sin((owner.getYaw() - 90) / 180 * Math.PI);
			double pcos = Math.cos((owner.getPitch() + 90) / 180 * Math.PI);
			
			for(int c = 0; c < this.getRange(); c++){
				Vector3 vec = new Vector3(_x - (c * xcos), _y + (c * pcos), _z - (c * zsin));
				level.addParticle(new DustParticle(vec, 0xb3, 0xb3, 0xb3));
				
				if(level.getBlock(new Vector3(Math.floor(vec.x), Math.floor(vec.y), Math.floor(vec.z))).isSolid()) return true;
				
				for(Participant player : players){
					if(player.getPlayer() == owner) continue;
					
					if(this.canHit(vec, player)){
						if(this.isHeadshot(vec, player)){
							player.getPlayer().attack(new EntityDamageByGunEvent(this.getEquippedBy(), player, Gun.CAUSE_HEADSHOT, this.getHeadshotDamage((owner.distance(player.getPlayer())))));
						}else{
							player.getPlayer().attack(new EntityDamageByGunEvent(this.getEquippedBy(), player, Gun.CAUSE_GUN, this.getHitDamage(owner.distance(player.getPlayer()))));
						}
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public void attack(Entity entity){}
	
	public class ShootThread extends Thread{
		@Override
		public void run(){
			while(true){
				unfire:
				while(Gun.this.isShooting){
					if(!Gun.this.shoot()){
						break unfire;
					}
					
					try{
						Thread.sleep(Gun.this.getShootInterval() * 50);
					}catch(InterruptedException e){}
				}
				try{
					Thread.sleep(50);
				}catch(InterruptedException e){}
			}
		}
	}
}
