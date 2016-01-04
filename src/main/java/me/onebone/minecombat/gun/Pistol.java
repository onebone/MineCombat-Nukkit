package me.onebone.minecombat.gun;

import me.onebone.minecombat.MineCombat;
import cn.nukkit.Player;

public class Pistol extends BaseGun{
	public Pistol(MineCombat plugin, Player owner){
		super(plugin, owner);
	}

	@Override
	public boolean canShoot(long fromLastShoot){
		if(fromLastShoot > 400){
			return true;
		}
		return false;
	}

	@Override
	public int getRange() {
		return 30;
	}

	@Override
	public int getMaxAmmo() {
		return 8;
	}

	@Override
	public int getDamage(double distance){
		return 5;
	}
	
	@Override
	public String getName(){
		return "Pistol";
	}
}
