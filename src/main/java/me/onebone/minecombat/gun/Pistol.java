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
	public int getDefaultMagazine(){
		return 50;
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
