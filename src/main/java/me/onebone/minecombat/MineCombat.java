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
	private HashMap<Integer, GameContainer> ongoing = new HashMap<>();
	private HashMap<String, Class<? extends Game>> games = new HashMap<>();

	private int index = 0;
	
	/**
	 * Provides game which player is participated in
	 * 
	 * @param player
	 * @return Game instance of player joined, null if not joined
	 */
	public Game getJoinedGame(Player player){
		for(int index : this.ongoing.keySet()){
			GameContainer container = this.ongoing.get(index);
			if(container.game.getParticipants().contains(player)){
				return container.game;
			}
		}
		
		return null;
	}

	public boolean startGame(String name, Position[] position, List<Player> players){
		if(!games.containsKey(name)){
			return false;
		}
		
		try{
			Game game = (Game) games.get(name).getConstructor(MineCombat.class, String.class, Position[].class).newInstance(this, name, position);
			GameContainer container = new GameContainer(game);
			container.startGame(players);
			this.ongoing.put(index++, container);
		}catch(Exception e){
			return false;
		}

		return true;
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
	
	public boolean addGame(Class<? extends Game> game){
		if(this.games.containsKey(game.getName().toLowerCase())){
			return false;
		}
		
		this.games.put(game.getName().toLowerCase(), game);
		return true;
	}
	
	private class GameContainer{
		private Game game = null;
		
		public GameContainer(Game game){
			if(game == null){
				throw new IllegalArgumentException("Cannot add null game");
			}
			
			this.game = game;
		}
		
		
		/**
		 * Starts game with positions and players. Provide position as null apply to all worlds
		 * 
		 * @param participants
		 * @return
		 */
		public boolean startGame(List<Player> participants){			
			game.startGame(participants);
			
			return true;
		}
	}
}
