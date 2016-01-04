package me.onebone.minecombat;

import java.util.HashMap;
import java.util.Map;

import me.onebone.minecombat.data.PlayerContainer;
import me.onebone.minecombat.gun.Pistol;
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
	
	public static final int GUN_ITEM_ID = 104;
	
	public static final String STATUS_FORMAT = 
			"%team %gun - %status\n"
			+ "Ammo: %ammo/%magazine";
	
	private Map<String, PlayerContainer> containers = null;
	
	private int status = STATUS_ONGOING; // TODO
	
	public void onEnable(){
		containers = new HashMap<>();
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		this.getServer().getScheduler().scheduleDelayedRepeatingTask(new TickTask(this), 10, 10);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if(!containers.containsKey(player.getName())){
			containers.put(player.getName(), new PlayerContainer(player, new Pistol(this, player)));
		}
		
		containers.get(player.getName()).setActive();
		
		player.getInventory().setItem(0, Item.get(GUN_ITEM_ID));
		player.getInventory().setHotbarSlotIndex(0, 3);
	}
	
	@EventHandler
	public void onEquipmentChange(PlayerItemHeldEvent event){
		Player player = event.getPlayer();
		Item item = event.getItem();
		
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
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		switch(command.getName()){
		case "spawnpos":
			
			return true;
		case "momap":
			
			return true;
		}
		return false;
	}
	
	public void startGame(){
		
	}
	
	public void stopGame(){
		
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
