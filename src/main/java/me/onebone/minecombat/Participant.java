package me.onebone.minecombat;

import cn.nukkit.Player;
import me.onebone.minecombat.game.Game;

public class Participant{
	private final Player player;
	private Game joined = null;

	public Participant(Player player){
		this.player = player;
	}

	public void joinGame(Game game){
		if(game == null){
			throw new IllegalArgumentException("Game cannot be null");
		}

		game.addPlayer(this);
		this.joined = game;
	}

	public void leaveGame(){
		this.joined.removePlayer(this);
		this.joined = null;
	}
}
