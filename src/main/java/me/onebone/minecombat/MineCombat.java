package me.onebone.minecombat;

import java.util.HashMap;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import me.onebone.minecombat.game.Game;

public class MineCombat extends PluginBase implements Listener{
	public static final int MODE_STANDBY = 0;
	public static final int MODE_ONGOING = 1;
	
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

	public boolean initGame(String name, Position[] position, final List<Player> players){
		if(!games.containsKey(name)){
			return false;
		}
		
		try{
			Game game = (Game) games.get(name).getConstructor(MineCombat.class, String.class, Position[].class).newInstance(this, name, position);
			final GameContainer container = new GameContainer(game);
			
			this.ongoing.put(index++, container);
			
			if(game.getStandByTime() > 0){
				container.standBy(players);
				
				this.getServer().getScheduler().scheduleDelayedTask(new PluginTask<MineCombat>(MineCombat.this){
					public void onRun(int currentTick){
						container.startGame(players);
					}
				}, game.getStandByTime());
				return true;
			}
			
			container.startGame(players);
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
			return game._startGame(participants);
		}
		
		/**
		 * Stand by players
		 * 
		 * @return
		 */
		public boolean standBy(List<Player> participants){
			return game._standBy(participants);
		}
	}
}
