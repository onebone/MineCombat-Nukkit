package me.onebone.minecombat.game;

import java.util.List;

import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;

public class GunMatch extends Game{
	public GunMatch(MineCombat plugin, String name, Position[] position){
		super(plugin, name, position);
	}
	
	@Override
	public String getScoreMessage(Participant participant){
		String[] teams = this.getTeams();

		int time = this.getLeftTicks();
		return this.plugin.getMessage("game.info",
			teams[participant.getTeam()], (time <= 20*10 ? TextFormat.RED + "" + time : TextFormat.GREEN + "" + time) + TextFormat.WHITE,
			this.getScoreString(participant));
	}

	@Override
	public boolean startGame(List<Participant> players){
		return true;
	}

	@Override
	public boolean standBy(List<Participant> players){
		return true;
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
