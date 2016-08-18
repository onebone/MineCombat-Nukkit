package me.onebone.minecombat.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import me.onebone.minecombat.MineCombat;

public abstract class Game{
	protected MineCombat plugin;
	protected HashMap<Integer, List<Player>> players = new HashMap<>();
	
	private final String name;
	
	public Game(MineCombat plugin, String name){
		this.plugin = plugin;
		
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	/**
	 * @return Stand by time in tick. Returns <= 0 if none.
	 */
	public int getStandByTime(){
		return 300;
	}
	
	public final HashMap<Integer, List<Player>> getParticipants(){
		return new HashMap<Integer, List<Player>>(this.players);
	}
	
	public final List<Player> getParticipants(int index){
		return new ArrayList<Player>(this.players.get(index));
	}
	
	/**
	 * Initializes each game.
	 * 
	 * @param position 		Start, end position of game. null if world is not limited.
	 * @param players		Initially joined participants
	 * @return				`true` if successfully started, `false` if not.
	 */
	public abstract boolean startGame(Position[] position, List<Player> players);
	
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
		
		List<Player> list = null;
		if(!players.containsKey(index)){
			list = players.get(index);
			
			if(list == null){
				players.put(index, list = new ArrayList<Player>());
			}
		}
		
		list.add(player);
		return true;
	}
	
	/**
	 * Called when player left the game
	 * 
	 * @param player
	 */
	public void leavePlayer(Player player){
		for(int index : players.keySet()){
			List<Player> list = players.get(index);
			
			if(list != null){
				if(list.remove(player)) return;
			}
		}
	}
}
