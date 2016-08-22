package me.onebone.minecombat;

import cn.nukkit.Player;
import me.onebone.minecombat.game.Game;

public class Participant{
	private final Player player;
	private Game joined = null;
	private int team = 0;

	public Participant(Player player){
		this.player = player;
	}

	public boolean joinGame(Game game){
		if(game == null){
			throw new IllegalArgumentException("Game cannot be null");
		}
		
		if(this.joined == null){
			game.addPlayer(this);
			this.joined = game;
			
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
