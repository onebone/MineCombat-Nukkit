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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import me.onebone.minecombat.data.PlayerContainer;
import me.onebone.minecombat.gun.Pistol;
import me.onebone.minecombat.task.StartGameTask;
import me.onebone.minecombat.task.StopGameTask;
import me.onebone.minecombat.task.TickTask;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerItemHeldEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class MineCombat extends PluginBase implements Listener{
	public static final int STATUS_STOPPED = 0;
	public static final int STATUS_ONGOING = 1;
	
	public static final int TEAM_RED = 0;
	public static final int TEAM_BLUE = 1;
	
	public static final int GUN_ITEM_ID = 104;
	
	public static final String STATUS_FORMAT = 
			"%team %gun - %status\n"
			+ "Ammo: %ammo/%magazine";
	
	private Map<String, PlayerContainer> containers = null;
	private Map<String, Map<String, Object[]>> position = null;
	
	private int status = STATUS_STOPPED;
	
	public void onEnable(){
		this.saveDefaultConfig();
		Map<String, Map<String, Object[]>> spawns = this.getConfig().get("spawn-pos", new LinkedHashMap<String, Map<String, Object[]>>());
		position = new HashMap<>();
		if(spawns == null || spawns.size() == 0){
			this.getLogger().warning("No spawn position was given. Set spawns using /spawnpos");
		}else{
			containers = new HashMap<>();
			
			this.getServer().getPluginManager().registerEvents(this, this);
			
			this.getServer().getScheduler().scheduleDelayedRepeatingTask(new TickTask(this), 10, 10);
			this.getServer().getScheduler().scheduleDelayedTask(new StartGameTask(this), this.getConfig().get("prepare-game", 60) * 20);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if(this.status == STATUS_ONGOING){
			if(!containers.containsKey(player.getName())){
				int redTeam = this.countPlayers(TEAM_RED);
				int team = containers.size() - redTeam < redTeam ? TEAM_BLUE : TEAM_RED; 
				containers.put(player.getName(), new PlayerContainer(player, new Pistol(this, player), team));
			}
			
			containers.get(player.getName()).setActive();
		}
		
		player.getInventory().setItem(0, Item.get(GUN_ITEM_ID));
		player.getInventory().setHotbarSlotIndex(0, 3);
	}
	
	@EventHandler
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
	}
	
	@EventHandler
	public void onTouch(PlayerInteractEvent event){
		Player player = event.getPlayer();
		
		if(containers.containsKey(player.getName())){
			PlayerContainer container = containers.get(player.getName());
			
			container.shoot();
		}
	}
	
	@SuppressWarnings("serial")
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
			
			return true;
		}
		return false;
	}
	
	public void startGame(){
		this.status = STATUS_ONGOING;
		
		containers.clear();
		Map<String, Player> online = this.getServer().getOnlinePlayers();
		
		int red = 0, blue = 0;
		for(String username : online.keySet()){
			Player player = online.get(username);
			
			int team = red > blue ? TEAM_BLUE : TEAM_RED;
			containers.put(player.getName(), new PlayerContainer(player, new Pistol(this, player), team));
		}
		this.getServer().broadcastMessage(TextFormat.GREEN + "Game is started. Enjoy!");
		
		this.getServer().getScheduler().scheduleDelayedTask(new StopGameTask(this), this.getConfig().get("game-game", 300) * 20);
	}
	
	public void stopGame(){
		this.status = STATUS_ONGOING;
		
		containers.clear();
		
		this.getServer().broadcastMessage(TextFormat.YELLOW + "Game is finished.");
		this.getServer().getScheduler().scheduleDelayedRepeatingTask(new TickTask(this), 10, 10);
	}
	
	public int countPlayers(int team){
		if(this.status == STATUS_ONGOING){
			int ret = 0;
			
			for(String player : containers.keySet()){
				if(containers.get(player).getTeam() == team) ++ret; 
			}
			return ret;
		}
		return 0;
	}
	
	public void onTick(){
		Map<String, Player> online = this.getServer().getOnlinePlayers();
		for(String username : online.keySet()){
			Player player = online.get(username);
			
			switch(this.status){
			case STATUS_STOPPED:
				player.sendTip(TextFormat.GREEN + "Now preparing for the next game.");
				break;
			case STATUS_ONGOING:
				if(containers.containsKey(player.getName())){
					PlayerContainer container = containers.get(player.getName());
					
					player.sendTip(STATUS_FORMAT.replace("%team", "RED")
						.replace("%gun", container.getGun().getName())
						.replace("%status", container.isShooting() ? TextFormat.RED + "FIRING" + TextFormat.WHITE : TextFormat.GREEN + "SAFETY" + TextFormat.WHITE)
						.replace("%ammo", container.getGun().getAmmo() + "")
						.replace("%magazine", container.getGun().getMagazine() + "")
					);
				}
				break;
			}
		}
	}
}
