package me.onebone.minecombat;

/*
 * MineCombat: FP..S? for Nukkit
 * Copyright (C) 2016 onebone <jyc00410@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import me.onebone.minecombat.data.PlayerContainer;
import me.onebone.minecombat.event.EntityDamageByGunEvent;
import me.onebone.minecombat.gun.BaseGun;
import me.onebone.minecombat.gun.Pistol;
import me.onebone.minecombat.task.MortalTask;
import me.onebone.minecombat.task.StartGameTask;
import me.onebone.minecombat.task.StopGameTask;
import me.onebone.minecombat.task.TickTask;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.StringEntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.TextContainer;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerFoodLevelChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerRespawnEvent;
import cn.nukkit.event.server.DataPacketSendEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class MineCombat extends PluginBase implements Listener{
	public static final int STATUS_STOPPED = 0;
	public static final int STATUS_ONGOING = 1;
	
	public static final int TEAM_RED = 0;
	public static final int TEAM_BLUE = 1;
	
	public static final int CAUSE_GUN = 15;
	public static final int CAUSE_HEADSHOT = 16;
	
	public static final int GUN_ITEM_ID = 104;
	
	public static final String STATUS_FORMAT = 
			"%red / %blue\n"
			+ "%team %gun - %status\n"
			+ "Ammo: %ammo/%magazine";
	
	public static final String PREPARE_FORMAT = 
			"Last match : %red / %blue\n"
			+ TextFormat.GREEN + "Now preparing for the next game.\n" + TextFormat.WHITE
			+ "Next: " + TextFormat.GOLD + "%next";
	
	private ShootThread thr = null;
	
	private Map<String, PlayerContainer> containers = null;
	private List<String> immortal = null;
	private Map<String[], Integer[]> kills = null;
	
	private Map<String, Map<String, Object[]>> position = null;
	private Object[] nextPos = null;
	private Position[] spawn = null;
	
	private int[] scores = new int[2];
	
	private int status = STATUS_STOPPED;
	
	public void registerGun(BaseGun gun){
		this.thr.registerGun(gun);
	}
	
	public void removeGun(BaseGun gun){
		this.thr.removeGun(gun);
	}
	
	public void onEnable(){
		this.saveDefaultConfig();
		Map<String, Map<String, Object[]>> spawns = this.getConfig().get("spawn-pos", new LinkedHashMap<String, Map<String, Object[]>>());
		position = new HashMap<>();
		if(spawns == null || spawns.size() == 0){
			this.getLogger().warning("No spawn position was given. Set spawns using /spawnpos");
		}else{
			containers = new HashMap<>();
			kills = new HashMap<>();
			immortal = new ArrayList<>();
			
			this.chooseNext();
			
			this.getServer().getPluginManager().registerEvents(this, this);
			
			this.getServer().getScheduler().scheduleDelayedRepeatingTask(new TickTask(this), 10, 10);
			
			this.getServer().getScheduler().scheduleDelayedTask(new StartGameTask(this), this.getConfig().get("prepare-time", 60) * 20);
		}
	}
	
	public void onDisable(){
		if(this.thr != null){
			this.closeAllContainers();
			this.thr.close();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEquipmentChange(PlayerItemHeldEvent event){
		Player player = event.getPlayer();
		Item item = event.getItem();
		
		if(this.status == STATUS_ONGOING){
			if(item.getId() == GUN_ITEM_ID){
				if(containers.containsKey(player.getName())){
					containers.get(player.getName()).startShoot();
				}
			}else{
				if(containers.containsKey(player.getName())){
					containers.get(player.getName()).stopShoot();
				}
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		
		if(containers.containsKey(player.getName())){
			containers.get(player.getName()).quit();
		}
		event.setAutoSave(false);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onTouch(PlayerInteractEvent event){
		Player player = event.getPlayer();
		
		if(containers.containsKey(player.getName())){
			PlayerContainer container = containers.get(player.getName());
			
			container.shoot();
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event){
		event.setDeathMessage(new TextContainer(""));
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			EntityDamageEvent cause = player.getLastDamageCause();
			if(cause instanceof EntityDamageByGunEvent && (cause.getCause() == CAUSE_GUN || cause.getCause() == CAUSE_HEADSHOT)){
				Entity damager = ((EntityDamageByGunEvent)player.getLastDamageCause()).getDamager();
				if(damager instanceof Player){
					Player causePlayer = (Player)damager;
					kills.put(new String[]{
						causePlayer.getName(), player.getName()
					}, new Integer[]{this.getServer().getTick(), cause.getCause()});
					
					if(getTeam(causePlayer.getName()) == TEAM_RED){
						scores[TEAM_RED]++;
					}else{
						scores[TEAM_BLUE]++;
					}
				}
			}
			
			if(containers.containsKey(player.getName())){
				PlayerContainer container = containers.get(player.getName());
				container.getGun().reset();
				container.quit();
			}else{
				player.setHealth(20);
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event){
		Player player = event.getPlayer();
		if(this.status == STATUS_ONGOING){
			if(!containers.containsKey(player.getName())){
				int team = this.countPlayers(TEAM_BLUE) < this.countPlayers(TEAM_RED) ? TEAM_BLUE : TEAM_RED; 
				containers.put(player.getName(), new PlayerContainer(this, player, new Pistol(this, player), team));
			}
			
			if(containers.containsKey(player.getName())){
				containers.get(player.getName()).setActive();
			}
			
			containers.get(player.getName()).setActive();
			containers.get(player.getName()).getGun().setOwner(player);
			
			immortal.add(player.getName());
			this.getServer().getScheduler().scheduleDelayedTask(new MortalTask(this, player.getName()), 100);
			
			event.setRespawnPosition(spawn[this.getTeam(player.getName())]);
			player.setSpawn(spawn[this.getTeam(player.getName())]);
		}
		
		player.getInventory().setItem(0, Item.get(GUN_ITEM_ID));
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		this.getServer().getOnlinePlayers().values().forEach((player) -> {
			event.getPlayer().sendData(player);
		});
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event){
		event.setCancelled();
	}
	
	@EventHandler
	public void onPickup(InventoryPickupItemEvent event){
		InventoryHolder holder = event.getInventory().getHolder();
		if(holder instanceof Player){
			Player player = (Player)holder;
			if(this.status == STATUS_ONGOING){
				if(containers.containsKey(player.getName())){
					containers.get(player.getName()).getGun().addAmmo(30);
				}
			}
		}
		event.getItem().kill();
		event.setCancelled();
	}
	
	@EventHandler
	public void onFoodLevelChange(PlayerFoodLevelChangeEvent event){
		event.setFoodLevel(20);
		event.setFoodSaturationLevel(20);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event){
		if(this.status == STATUS_STOPPED){
			if(event.getEntity() instanceof Player){
				event.setCancelled();
			}
		}else{
			if(event.getEntity() instanceof Player){
				if(immortal.contains(((Player)event.getEntity()).getName())){
					event.setCancelled();
				}
			}
		}
	}
	
	@EventHandler
	public void onSendPacket(DataPacketSendEvent event){
		if(this.status == STATUS_ONGOING){
			if(event.getPacket() instanceof SetEntityDataPacket){
				SetEntityDataPacket pk = (SetEntityDataPacket) event.getPacket();
				
				if(pk.eid != 0){
					Player player = event.getPlayer();
					
					try{
						Player to = this.getServer().getOnlinePlayers().values().stream().filter((v) -> {
							return v.getId() == pk.eid;
						}).findFirst().get();
						
						pk.metadata.put(Entity.DATA_NAMETAG, new StringEntityData((this.isColleague(player.getName(), to.getName()) ? TextFormat.GREEN : TextFormat.RED) + to.getName()));
					}catch(NoSuchElementException e){}
				}
			}
		}
	}
	
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args){
		switch(command.getName()){
		case "spawnpos":
			if(args.length < 2){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(TextFormat.RED + "Please use this command in-game.");
				return true;
			}
			final Player player = (Player)sender;
			
			switch(args[0].toLowerCase()){
			case "red":
				if(!position.containsKey(args[1])){
					position.put(args[1], new HashMap<String, Object[]>());
				}
				position.get(args[1]).put("red", new Object[]{
						player.getX(), player.getY(), player.getZ(), player.getLevel().getFolderName()
				});
				sender.sendMessage(TextFormat.GREEN + "Position " + args[1] + " for red team was set.");
				return true;
			case "blue":
				if(!position.containsKey(args[1])){
					position.put(args[1], new HashMap<String, Object[]>());
				}
				position.get(args[1]).put("blue", new Object[]{
						player.getX(), player.getY(), player.getZ(), player.getLevel().getFolderName()
				});
				sender.sendMessage(TextFormat.GREEN + "Position " + args[1] + " for blue team was set.");
				return true;
			case "create":
				if(position.containsKey(args[1])){
					Map<String, Map<String, Object[]>> spawns = this.getConfig().get("spawn-pos", new LinkedHashMap<String, Map<String, Object[]>>());
					spawns.put(args[1], position.get(args[1]));
					this.getConfig().set("spawn-pos", spawns);
					// this.getConfig().setNested("spawn-pos." + args[1], position.get(args[1]));
					this.getConfig().save();
					sender.sendMessage(TextFormat.GREEN + "Position " + args[1] + "was set successfully if you did everything right.");
				}else{
					sender.sendMessage(TextFormat.RED + "Position " + args[1] + " does not exist!");
				}
				return true;
			}
			return true;
		case "momap":
			if(args.length < 0){
				sender.sendMessage(TextFormat.RED + "Usage: " + command.getUsage());
				return true;
			}
			
			if(this.status == STATUS_STOPPED){
				Map<String, Map<String, ArrayList<Object>>> spawns = this.getConfig().get("spawn-pos", new LinkedHashMap<String, Map<String, ArrayList<Object>>>());
				if(spawns.containsKey(args[0])){
					nextPos = new Object[]{
						args[0], spawns.get(args[0])	
					};
					sender.sendMessage("Next spawn position was set to: " + TextFormat.GREEN + args[0]);
				}else{
					sender.sendMessage("There is no spawn position: " + TextFormat.RED + args[0]);
				}
			}else{
				sender.sendMessage(TextFormat.RED + "The game is ongoing. Try again later.");
			}
			return true;
		}
		return false;
	}
	
	public void startGame(){
		this.status = STATUS_ONGOING;
		
		this.thr = new ShootThread();

		scores = new int[2];
		this.closeAllContainers();
		
		Map<String, Player> online = this.getServer().getOnlinePlayers();

		int red = 0, blue = 0;
		List<String> keys = new ArrayList<>(online.keySet());
		Collections.shuffle(keys);
		
		@SuppressWarnings("unchecked")
		Map<String, ArrayList<Object>> spawns = (Map<String, ArrayList<Object>>)nextPos[1];
		spawn = new Position[]{
				new Position((double)spawns.get("red").get(0), (double)spawns.get("red").get(1), (double)spawns.get("red").get(2), this.getServer().getLevelByName((String)spawns.get("red").get(3))),
				new Position((double)spawns.get("blue").get(0), (double)spawns.get("blue").get(1), (double)spawns.get("blue").get(2), this.getServer().getLevelByName((String)spawns.get("blue").get(3))),
		};
		
		for(String username : keys){
			Player player = online.get(username);
			if(player.getHealth() <= 0){
				player.kick("AFK");
				continue;
			}
			
			int team = red > blue ? TEAM_BLUE : TEAM_RED;
			if(team == TEAM_RED){
				red++;
				player.teleport(spawn[TEAM_RED]);
			}else{
				player.teleport(spawn[TEAM_BLUE]);
				blue++;
			}
			
			player.setHealth(20);

			PlayerContainer container = new PlayerContainer(this, player, new Pistol(this, player), team);
			container.setActive();
			containers.put(player.getName(), container);
			
			player.setSpawn(spawn[this.getTeam(player.getName())]);
		}
		
		this.sendAllNameTags();
		this.thr.start();
		
		this.getServer().broadcastMessage(TextFormat.GREEN + "Game is started. Enjoy!");
		this.getServer().getScheduler().scheduleDelayedTask(new StopGameTask(this), this.getConfig().get("game-time", 300) * 20);
	}
	
	private void chooseNext(){
		Map<String, Map<String, ArrayList<Object>>> spawns = this.getConfig().get("spawn-pos", new LinkedHashMap<String, Map<String, ArrayList<Object>>>());
		
		List<String> keys = new ArrayList<>(spawns.keySet());
		Collections.shuffle(keys);
		
		nextPos = new Object[]{
				keys.get(0), spawns.get(keys.get(0))
		};
	}
	
	private void closeAllContainers(){
		for(String key : containers.keySet()){
			containers.get(key).quit();
		}
	}
	
	public void stopGame(){
		this.status = STATUS_STOPPED;
		
		this.closeAllContainers();
		this.thr.close();
		
		this.getServer().broadcastMessage(TextFormat.YELLOW + "Game is finished.");
		
		Map<String, Player> online = this.getServer().getOnlinePlayers();
		for(String username : online.keySet()){
			Player player = online.get(username);
			if(this.getTeam(player.getName()) == TEAM_RED && scores[TEAM_RED] > scores[TEAM_BLUE] || this.getTeam(player.getName()) == TEAM_BLUE && scores[TEAM_BLUE] > scores[TEAM_RED]){
				player.sendMessage(TextFormat.GREEN + "Your team has won the game!");
			}else if(scores[TEAM_RED] == scores[TEAM_BLUE]){
				player.sendMessage(TextFormat.YELLOW + "The game has tied!");
			}else{
				player.sendMessage(TextFormat.RED + "Your team has lost the game. :(");
			}
		}
		
		for(String username : containers.keySet()){
			containers.get(username).quit();
		}
		containers.clear();
		
		this.chooseNext();
		
		this.getServer().getScheduler().scheduleDelayedTask(new StartGameTask(this), this.getConfig().get("prepare-time", 60) * 20);
	}
	
	public int countPlayers(int team){
		if(this.status == STATUS_ONGOING){
			int ret = 0;
			
			for(String player : containers.keySet()){
				if(containers.get(player).isActive() && containers.get(player).getTeam() == team) ++ret; 
			}
			return ret;
		}
		return 0;
	}
	
	public void onTick(int now){
		kills.forEach((k, v) -> {
			if(now - 80 > v[0]){
				kills.remove(k);
			}
		});
		
		this.getServer().getOnlinePlayers().values().forEach((player) -> {
			switch(this.status){
			case STATUS_STOPPED:
				player.sendPopup(PREPARE_FORMAT
					.replace("%red", TextFormat.RED + scores[TEAM_RED] + TextFormat.WHITE)
					.replace("%blue", TextFormat.BLUE + scores[TEAM_BLUE] + TextFormat.BLUE)
					.replace("%next", nextPos[0] + "")
				);
				break;
			case STATUS_ONGOING:
				if(containers.containsKey(player.getName())){
					if(kills.size() > 0){
					StringBuilder killMessage = new StringBuilder();
						for(String[] players : kills.keySet()){
							killMessage.append(
								(isColleague(players[0], player.getName()) ? TextFormat.GREEN : TextFormat.RED) + players[0] + TextFormat.WHITE
								+ (kills.get(players)[1] == CAUSE_GUN ? " -> " : " -HEAD>")
								+ (isColleague(players[1], player.getName()) ? TextFormat.GREEN : TextFormat.RED) + players[1] + TextFormat.WHITE + "\n"
							);
						}
						killMessage.substring(0, killMessage.length() - 1);
						player.sendTip(killMessage.toString());
					}
					
					PlayerContainer container = containers.get(player.getName());
					
					player.sendPopup(STATUS_FORMAT.replace("%team", container.getTeam() == TEAM_RED ? TextFormat.RED + "RED" + TextFormat.WHITE : TextFormat.BLUE + "BLUE" + TextFormat.WHITE)
						.replace("%red", TextFormat.RED + scores[TEAM_RED] + TextFormat.WHITE)
						.replace("%blue", TextFormat.BLUE + scores[TEAM_BLUE] + TextFormat.WHITE)
						.replace("%gun", container.getGun().getName())
						.replace("%status", container.isShooting() ? TextFormat.RED + "FIRING" + TextFormat.WHITE : TextFormat.GREEN + "SAFETY" + TextFormat.WHITE)
						.replace("%ammo", container.getGun().getAmmo() + "")
						.replace("%magazine", container.getGun().getMagazine() + "")
					);
				}
				break;
			}
		});
	}
	
	public void setMortal(String player){
		immortal.remove(player);
	}
	
	public boolean isColleague(String player1, String player2){
		if(containers.containsKey(player1) && containers.containsKey(player2)){
			return (containers.get(player1).getTeam() == containers.get(player2).getTeam()); 
		}
		return true;
	}
	
	public int getTeam(String player){
		if(containers.containsKey(player)){
			return containers.get(player).getTeam();
		}
		return -1;
	}
	
	private void sendAllNameTags(){
		containers.values().forEach((container) -> {
			Player player = container.getPlayer();
			
			containers.values().forEach((container1) -> {
				Player to = container1.getPlayer();
				
				to.sendData(player);
			});
		});
	}
}
