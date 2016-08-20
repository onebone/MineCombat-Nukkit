package me.onebone.minecombat;

import java.util.HashMap;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import me.onebone.minecombat.game.Game;

public class MineCombat extends PluginBase implements Listener{
	public static final int MODE_STANDBY = 0;
	public static final int MODE_ONGOING = 1;
	
	private HashMap<Integer, GameContainer> ongoing = new HashMap<>();
	private HashMap<String, Class<? extends Game>> games = new HashMap<>();
	private HashMap<String, Participant> players = new HashMap<>();

	private int index = 0;
	
	/**
	 * Provides game which player is participated in
	 * 
	 * @param player
	 * @return Game instance of player joined, null if not joined
	 */
	public Game getJoinedGame(Participant player){
		for(int index : this.ongoing.keySet()){
			GameContainer container = this.ongoing.get(index);
			if(container.game.getParticipants().contains(player)){
				return container.game;
			}
		}
		
		return null;
	}

	public boolean initGame(String name, Position[] position, final List<Participant> players){
		if(!games.containsKey(name)){
			return false;
		}
		
		try{
			Game game = (Game) games.get(name).getConstructor(MineCombat.class, String.class, Position[].class).newInstance(this, name, position);
			final GameContainer container = new GameContainer(game);
	
			if(game.getStandByTime() > 0){
				if(this.standBy(container, players)){
					this.ongoing.put(index++, container);
					return true;
				}

				return false;
			}

			this.ongoing.put(index++, container);
			
			return container.startGame(players);
		}catch(Exception e){
			return false;
		}
	}

	private boolean standBy(final GameContainer container, final List<Participant> players){
		if(container.standBy(players)){
			container.taskId = this.getServer().getScheduler().scheduleDelayedTask(new PluginTask<MineCombat>(this){
				public void onRun(int currentTick){
					if(!container.startGame(players)){
						MineCombat.this.standBy(container, players);
					}
				}
			}, container.game.getStandByTime()).getTaskId();

			return true;
		}
		return false;
	}
	
	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		String username = player.getName().toLowerCase();
		
		if(players.containsKey(username)){
			Participant participant = players.get(username);

			Game game;
			if((game = this.getJoinedGame(participant)) != null){
				game.onParticipantMove(participant);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();

		String username = player.getName().toLowerCase();
		if(players.containsKey(username)){
			players.remove(username);
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
		private int taskId = -1;
		
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
		public boolean startGame(List<Participant> participants){			
			return game._startGame(participants);
		}
		
		/**
		 * Stand by players
		 * 
		 * @return
		 */
		public boolean standBy(List<Participant> participants){
			return game._standBy(participants);
		}
	}
}
