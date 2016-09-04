package me.onebone.minecombat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import me.onebone.minecombat.game.Game;
import me.onebone.minecombat.game.GunMatch;

public class MineCombat extends PluginBase implements Listener{
	public static final int MODE_STANDBY = 0;
	public static final int MODE_ONGOING = 1;

	private Map<String, String> lang;

	
	private HashMap<Integer, GameContainer> ongoing = new HashMap<>();
	private HashMap<String, Class<? extends Game>> games = new HashMap<>();
	private HashMap<String, Participant> players = new HashMap<>();

	private int index = 0;

	private boolean joinRandom = true;

	/**
	 * Returns participant instance by player instance
	 *
	 * @param player
	 * @return
	 */
	public Participant getParticipant(Player player){
		return this.players.get(player.getName().toLowerCase());
	}

	/**
	 * Make player to join the game
	 *
	 * @param game
	 * @param player
	 * @return `true` if success, `false` if not
	 */
	public boolean joinGame(Game game, Participant player){
		if(player.getJoinedGame() != null){
			return false;
		}

		return player.joinGame(game);
	}

	/**
	 * Makes player to leave the game
	 *
	 * @param game
	 * @param player
	 * @return `true` if success, `false` if not
	 */
	public boolean leaveGame(Participant player){
		this.players.remove(player);
		
		return player.leaveGame();
	}

	public boolean initGame(String name, Position[] position, Position[] spawns, String tag, final List<Participant> players){
		if(!games.containsKey(name)){
			return false;
		}
		
		if(position.length < 2 || position[0] == null || position[1] ==null){
			position = null;
		}
		try{
			Game game = (Game) games.get(name).getConstructor(MineCombat.class, String.class, Position[].class, Position[].class).newInstance(this, tag, position, spawns);
			final GameContainer container = new GameContainer(game);
	
			if(game.getStandByTime() > 0){
				if(this.standBy(container)){
					this.ongoing.put(index++, container);
					return true;
				}

				return false;
			}

			this.ongoing.put(index++, container);
			
			return container.startGame();
		}catch(Exception e){
			return false;
		}
	}
	
	public boolean stopGame(Game game){
		for(int index : this.ongoing.keySet()){
			GameContainer container = this.ongoing.get(index);

			if(container.game == game){
				if(container.taskId != -1 && this.getServer().getScheduler().isQueued(container.taskId)){
					this.getServer().getScheduler().cancelTask(container.taskId);
				}
				this.ongoing.remove(index);
				return true;
			}
		}
		return false;
	}

	private boolean standBy(final GameContainer container){
		if(container.standBy()){
			container.taskId = this.getServer().getScheduler().scheduleDelayedTask(new PluginTask<MineCombat>(this){
				public void onRun(int currentTick){
					if(!container.startGame()){
						MineCombat.this.standBy(container);
					}
				}
			}, container.game.getStandByTime()).getTaskId();

			return true;
		}
		return false;
	}
	
	public String getMessage(String key, Object... params){
		if(this.lang.containsKey(key)){
			return replaceMessage(this.lang.get(key), params);
		}
		return "Could not find message with " + key;
	}
	
	private String replaceMessage(String lang, Object[] params){
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0; i < lang.length(); i++){
			char c = lang.charAt(i);
			if(c == '{'){
				int index;
				if((index = lang.indexOf('}', i)) != -1){
					try{
						String p = lang.substring(i + 1, index);
						int param = Integer.parseInt(p);
						
						if(params.length > param){
							i = index;
							
							builder.append(params[param]);
							continue;
						}
					}catch(NumberFormatException e){}
				}
			}
			builder.append(c);
		}
		
		return TextFormat.colorize(builder.toString());
	}
	
	@Override
	public void onLoad(){
		this.addGame("gunmatch", GunMatch.class);
	}
	
	@Override
	public void onEnable(){
		this.saveDefaultConfig();

		this.joinRandom = this.getConfig().get("join.random", true);

		String name = this.getConfig().get("language", "eng");
		InputStream is = this.getResource("lang_" + name + ".json");
		if(is == null){
			this.getLogger().critical("Could not load language file. Changing to default.");
			
			is = this.getResource("lang_eng.json");
		}
		
		try{
			lang = new GsonBuilder().create().fromJson(Utils.readFile(is), new TypeToken<LinkedHashMap<String, String>>(){}.getType());
		}catch(JsonSyntaxException | IOException e){
			this.getLogger().critical(e.getMessage());
		}
		
		if(!name.equals("eng")){
			try{
				LinkedHashMap<String, String> temp = new GsonBuilder().create().fromJson(Utils.readFile(this.getResource("lang_eng.json")), new TypeToken<LinkedHashMap<String, String>>(){}.getType());
				temp.forEach((k, v) -> {
					if(!lang.containsKey(k)){
						lang.put(k, v);
					}
				});
			}catch(IOException e){
				this.getLogger().critical(e.getMessage());
			}
		}

		this.startGames();

		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@SuppressWarnings("unchecked")
	private void startGames(){
		Map<String, Map<String, Object>> list = this.getConfig().get("games", new LinkedHashMap<String, Map<String, Object>>());

		for(String key : list.keySet()){
			Map<String, Object> game = list.get(key);

			String type = (String) game.getOrDefault("type", "gunmatch").toString().toLowerCase();
			String pos1 = (String) game.getOrDefault("pos1", "null");
			String pos2 = (String) game.getOrDefault("pos2", "null");

			Position start = null, end = null;
			if(pos1 != null && pos2 != null){
				String[] pos = pos1.split(" ");
				if(pos.length >= 4){
					try{
						Double x = Double.parseDouble(pos[0]);
						Double y = Double.parseDouble(pos[1]);
						Double z = Double.parseDouble(pos[2]);
						String world = pos[3];
						
						Level level = this.getServer().getLevelByName(world);
						if(level == null){
							this.getLogger().warning(this.getMessage("game.unlimitedWorld", key));
						}else{
							start = new Position(x, y, z, this.getServer().getLevelByName(world));
						}
						
					}catch(Exception e){}
				}

				pos = pos2.split(" ");
				if(pos.length >= 4){
					try{
						Double x = Double.parseDouble(pos[0]);
						Double y = Double.parseDouble(pos[1]);
						Double z = Double.parseDouble(pos[2]);
						String world = pos[3];
						
						Level level = this.getServer().getLevelByName(world);
						if(level == null){
							this.getLogger().warning(this.getMessage("game.unlimitedWorld", key));
						}else{
							end = new Position(x, y, z, level);
						}
					}catch(Exception e){}
				}
			}

			if(end == null || start == null){
				end = start = null;

				this.getLogger().notice(this.getMessage("game.unlimitedWorld", key));
			}
			
			
			List<String> temp = null;
			try{
				temp = (List<String>)game.getOrDefault("spawns", new ArrayList<String>());
			}catch(ClassCastException e){
				this.getLogger().warning(this.getMessage("game.invalidSpawn", key));
			}
			
			Position[] spawns = null;
			if(temp != null){
				spawns = new Position[temp.size()];
				
				for(int i = 0; i < spawns.length; i++){
					String[] pos = temp.get(i).split(" ");
					if(pos.length >= 4){
						try{
							Double x = Double.parseDouble(pos[0]);
							Double y = Double.parseDouble(pos[1]);
							Double z = Double.parseDouble(pos[2]);
							String world = pos[3];

							Level level = this.getServer().getLevelByName(world);
							if(level == null){
								this.getLogger().warning(this.getMessage("game.invalidSpawnWorld", key));
								continue;
							}
							spawns[i] = new Position(x, y, z, level);
						}catch(Exception e){}
					}
				}
			}
			

			if(this.games.containsKey(type)){
				this.initGame(type, new Position[]{
					start, end
				}, spawns, key, new ArrayList<Participant>());
			}
		}
	}

	@Override
	public void onDisable(){
		this.ongoing.values().forEach(container -> {
			container.game._closeGame();
		});
		this.ongoing.clear();
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		String username = player.getName().toLowerCase();
		
		if(players.containsKey(username)){
			Participant participant = players.get(username);

			Game game;
			if((game = participant.getJoinedGame()) != null){
				game.onParticipantMove(participant);
			}
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event){
		Player player = event.getPlayer();

		Participant participant = new Participant(player);
		this.players.put(player.getName().toLowerCase(), participant);

		if(this.joinRandom){
			if(this.ongoing.size() > 0){
				Random random = new Random();
				Game game = this.ongoing.get(random.nextInt(this.ongoing.size())).game;
				
				this.joinGame(game, participant);
			}else{
				player.sendMessage(this.getMessage("join.failed"));
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event){
		Player player = event.getPlayer();
		String username = player.getName().toLowerCase();
		
		if(this.players.containsKey(username)){
			Participant participant = this.players.get(username);
			participant.getJoinedGame().respawnParticipant(event, participant);
		}
	}
	
	@EventHandler
	public void onDataPacketSend(DataPacketSendEvent event){
		if(event.getPacket() instanceof SetEntityDataPacket){
			SetEntityDataPacket pk = (SetEntityDataPacket) event.getPacket();
			
			if(pk.eid != 0){
				Player player = event.getPlayer();
				
				Participant participant = this.getParticipant(player);
				if(participant != null && participant.getJoinedGame() != null){
					for(Participant of : participant.getJoinedGame().getParticipants()){
						if(of.getPlayer().getId() == pk.eid){
							if(participant.getJoinedGame() == of.getJoinedGame()){
								String tag = participant.getJoinedGame().onSetNameTag(participant, of);
								
								pk.metadata.put(
									new StringEntityData(Entity.DATA_NAMETAG, tag)
								);
							}
							
							break;
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Entity entity = event.getEntity();
		if(entity instanceof Player){
			Player player = (Player) entity;
			String username = player.getName().toLowerCase();
			
			if(this.players.containsKey(username)){
				Participant participant = this.players.get(username);
				participant.getJoinedGame().onParticipantKilled(event, participant);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();

		String username = player.getName().toLowerCase();
		if(players.containsKey(username)){
			Game game;
			if((game = players.get(username).getJoinedGame()) != null){
				game.removePlayer(players.get(username));
			}
			players.remove(username);
		}
	}
	
	public boolean addGame(String name, Class<? extends Game> game, boolean force){
		if(!force && this.games.containsKey(name.toLowerCase())){
			return false;
		}
		
		this.games.put(name.toLowerCase(), game);
		return true;
	}

	public boolean addGame(String name, Class<? extends Game> game){
		return this.addGame(name, game, false);
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
		public boolean startGame(){
			if(game._startGame()){
				this.taskId = MineCombat.this.getServer().getScheduler().scheduleDelayedTask(new PluginTask<MineCombat>(MineCombat.this){
					@Override
					public void onRun(int currentTick){
						GameContainer.this.stopGame();
						
						if(game.getStandByTime() > 0){
							if(!MineCombat.this.standBy(GameContainer.this)){
								game._closeGame();
								MineCombat.this.ongoing.remove(game);
							}
						}
					}
				}, game.getGameTime()).getTaskId();

				return true;
			}
			return false;
		}

		public void stopGame(){
			game._stopGame();
		}
		
		/**
		 * Stand by players
		 * 
		 * @return
		 */
		public boolean standBy(){
			return game._standBy();
		}
	}
}
