package me.onebone.minecombat.game;

import java.util.List;

import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;
import me.onebone.minecombat.weapon.AK47;

public class GunMatch extends Game{
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
			this.selectTeam(player);
			player.armWeapon(new AK47(plugin, player));
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removePlayer(Participant player){
		if(super.removePlayer(player)){
			player.dearmAll();
			
			return true;
		}
		return false;
	}

	@Override
	public void stopGame(){
		
	}

	@Override
	public void closeGame(){
		
	}

	@Override
	public boolean onParticipantMove(Participant player){
		return false;
	}

}
