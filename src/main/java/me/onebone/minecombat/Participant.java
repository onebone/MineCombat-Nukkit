package me.onebone.minecombat;

import java.util.ArrayList;
import java.util.List;

import cn.nukkit.Player;
import me.onebone.minecombat.game.Game;
import me.onebone.minecombat.weapon.Weapon;

public class Participant{
	private final Player player;
	private List<Weapon> armed;
	private Game joined = null;
	private int team = 0;

	public Participant(Player player){
		this.player = player;
		
		armed = new ArrayList<>();
	}
	
	public boolean armWeapon(Weapon weapon){
		return this.armed.add(weapon);
	}
	
	public void setArmed(List<Weapon> weapons){
		this.armed = new ArrayList<Weapon>(weapons);
	}
	
	public List<Weapon> getArmed(){
		return new ArrayList<Weapon>(this.armed);
	}
	
	public boolean dearmWeapon(Weapon weapon){
		return this.armed.remove(weapon);
	}
	
	public void dearmAll(){
		this.armed.clear();
	}

	public boolean joinGame(Game game){
		if(game == null){
			throw new IllegalArgumentException("Game cannot be null");
		}
		
		if(this.joined == null){
			this.joined = game;
			game.addPlayer(this);
			
			return true;
		}
		return false;
	}

	public boolean leaveGame(){
		if(this.joined != null){
			this.joined.removePlayer(this);
			this.joined = null;
			return true;
		}
		return false;
	}

	public Game getJoinedGame(){
		return this.joined;
	}
	
	public Player getPlayer(){
		return this.player;
	}

	public void setTeam(int team){
		this.team = team;
	}

	public int getTeam(){
		return this.team;
	}
}
