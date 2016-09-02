package me.onebone.minecombat.game;

import cn.nukkit.utils.TextFormat;

public class KillNotice{
	private long end;
	private String attacker, victim;
	private int teamAttacker, teamVictim;
	
	public KillNotice(String attacker, int teamAttacker, String victim, int teamVictim){
		this.end = System.currentTimeMillis() + 1000 * 4;
		
		this.attacker = attacker;
		this.victim = victim;
		this.teamAttacker = teamAttacker;
		this.teamVictim = teamVictim;
	}
	
	public boolean isExpired(){
		return System.currentTimeMillis() > this.end;
	}
	
	public String compose(int showerTeam){
		return ((teamAttacker == showerTeam) ? TextFormat.GREEN : TextFormat.RED) + "" + attacker + TextFormat.WHITE
				+ " -> " + ((teamVictim == showerTeam) ? TextFormat.GREEN : TextFormat.RED) + victim;
	}
}
