package me.onebone.minecombat.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;
import me.onebone.minecombat.weapon.AK47;
import me.onebone.minecombat.weapon.Gun;
import me.onebone.minecombat.weapon.Weapon;

public class GunMatch extends Game{
	private Map<String, List<Weapon>> weapons = new HashMap<>();
	private Map<String, Integer> prevTeam = new HashMap<>();
	
	public GunMatch(MineCombat plugin, String name, Position[] position, Position[] spawns){
		super(plugin, name, position, spawns);
	}
	
	@Override
	public String getScoreMessage(Participant participant){
		String[] teams = this.getTeams();

		int time = this.getLeftTicks();
		
		return this.getMode() == MineCombat.MODE_ONGOING ? 
				this.plugin.getMessage("game.info.ongoing",
				(time < 20*10 ? TextFormat.RED : TextFormat.GREEN) + "" + this.getTimeString(time) + TextFormat.WHITE,
				teams[participant.getTeam()], this.getScoreString(participant))
				: this.plugin.getMessage("game.info.standby",
						(time < 20*10 ? TextFormat.RED : TextFormat.GREEN) + "" + this.getTimeString(time) + TextFormat.WHITE,
						teams[participant.getTeam()], this.getScoreString(participant));
	}

	@Override
	public boolean startGame(List<Participant> players){
		for(Participant participant : players){
			participant.getArmed().forEach(weapon -> {
				if(weapon instanceof Gun){
					((Gun) weapon).resetAmmo();
				}
			});
		}
		
		return true;
	}

	@Override
	public boolean standBy(List<Participant> players){
		this.selectTeams();
		return true;
	}
	
	@Override
	public boolean addPlayer(Participant player){
		if(super.addPlayer(player)){
			String username = player.getPlayer().getName().toLowerCase();
			
			if(prevTeam.containsKey(username)){
				int team = prevTeam.get(username);
				
				player.setTeam(team);
				this.teams.get(team).add(player);
				
				this.prevTeam.remove(username);
			}else{
				this.selectTeam(player);
			}
			
			if(weapons.containsKey(username)){
				player.setArmed(this.weapons.get(username));
				
				weapons.remove(username);
			}else{
				player.armWeapon(new AK47(plugin, player));
			}
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removePlayer(Participant player){
		if(super.removePlayer(player)){
			this.weapons.put(player.getPlayer().getName().toLowerCase(), new ArrayList<Weapon>(player.getArmed()));
			this.prevTeam.put(player.getPlayer().getName().toLowerCase(), player.getTeam());
			
			player.dearmAll();
			
			return true;
		}
		return false;
	}

	@Override
	public void stopGame(){
		prevTeam.clear();
		weapons.clear();
	}

	@Override
	public void closeGame(){}

	@Override
	public boolean onParticipantMove(Participant player){
		return true;
	}
}
