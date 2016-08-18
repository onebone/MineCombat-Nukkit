package me.onebone.minecombat;

import java.util.HashMap;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import me.onebone.minecombat.game.Game;

public class MineCombat extends PluginBase implements Listener{
	private HashMap<String, GameContainer> games = new HashMap<>();
	
	/**
	 * Provides game which player is participated in
	 * 
	 * @param player
	 * @return Game instance of player joined, null if not joined
	 */
	public Game getJoinedGame(Player player){
		for(String game : this.games.keySet()){
			GameContainer container = this.games.get(game);
			
			for(int index : container.game.getParticipants().keySet()){
				List<Player> players = container.game.getParticipants(index);
				if(players.contains(player)){
					return container.game;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		
		Game game;
		if((game = this.getJoinedGame(player)) != null){
			game.onParticipantMove(player);
		}
	}
	
	public boolean addGame(Game game){
		if(this.games.containsKey(game.getName().toLowerCase())){
			return false;
		}
		
		this.games.put(game.getName().toLowerCase(), new GameContainer(game));
		return true;
	}
	
	private class GameContainer{
		private int index = 0;
		private Game game = null;
		private HashMap<Integer, Position[]> ongoing;
		
		public GameContainer(Game game){
			if(game == null){
				throw new IllegalArgumentException("Cannot add null game");
			}
			
			this.game = game;
		}
		
		
		/**
		 * Starts game with positions and players. Provide position as null apply to all worlds
		 * 
		 * @param position
		 * @param participants
		 * @return
		 */
		public int startGame(Position[] position, List<Player> participants){
			if(position != null && position.length < 2){
				throw new IllegalArgumentException("Provide valid position");
			}
			
			if(game.startGame(position, participants)){
				this.ongoing.put(index, position);
				return index++;
			}
			
			return -1;
		}
	}
}
