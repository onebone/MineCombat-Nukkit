package me.onebone.minecombat.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import me.onebone.minecombat.MineCombat;

public abstract class Game{
	protected MineCombat plugin;
	protected List<Player> players = new ArrayList<>();
	protected Position[] position;
	
	private int mode = MineCombat.MODE_STANDBY;
	private final String name;
	
	public Game(MineCombat plugin, String name, Position[] position){
		this.plugin = plugin;
		
		this.name = name;
		this.position = position;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return Stand by time in tick. Returns <= 0 if none.
	 */
	public int getStandByTime(){
		return 1200;
	}
	
	public final List<Player> getParticipants(){
		return new ArrayList<Player>(this.players);
	}
	
	public final int getMode(){
		return this.mode;
	}
	
	/**
	 * Initializes each game.
	 * 
	 * @param players		Initially joined participants
	 * @return				`true` if successfully started, `false` if not.
	 */
	public abstract boolean startGame(List<Player> players);
	
	/**
	 * Called when game is standing by
	 * 
	 * @param players
	 * @return				`true` if success, `false` if not.
	 */
	public abstract boolean standBy(List<Player> players);
	
	public boolean _standBy(List<Player> players){
		if(this.standBy(players)){
			this.mode = MineCombat.MODE_STANDBY;
			
			return true;
		}
		
		return false;
	}
	
	public boolean _startGame(List<Player> players){
		if(this.startGame(players)){
			this.mode = MineCombat.MODE_ONGOING;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Called when participant of game moved.
	 * 
	 * @param player
	 * @return
	 */
	public abstract boolean onParticipantMove(Player player);
	
	/**
	 * Add player to game
	 * 
	 * @param player
	 * @return true if approved, false if not
	 */
	public boolean addPlayer(int index, Player player){
		if(plugin.getJoinedGame(player) != null){
			return false;
		}
		
		players.add(player);
		return true;
	}
	
	/**
	 * Called when player left the game
	 * 
	 * @param player
	 */
	public void leavePlayer(Player player){
		players.remove(player);
	}
}
