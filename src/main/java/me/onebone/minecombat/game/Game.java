package me.onebone.minecombat.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.nukkit.level.Position;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.TextFormat;
import me.onebone.minecombat.MineCombat;
import me.onebone.minecombat.Participant;

public abstract class Game{
	protected MineCombat plugin;
	protected List<Participant> players = new ArrayList<>();
	protected Position[] position;
	
	private int[] score;
	private int mode = MineCombat.MODE_STANDBY;
	private final String name;
	private int taskId = -1;
	private long startTime = 0;
	
	public Game(MineCombat plugin, String name, Position[] position){
		this.plugin = plugin;
		
		this.name = name;
		this.position = position;
		
		this.score = new int[this.getTeamCount()];
		
		this.showScore();
	}
	
	public String getName(){
		return this.name;
	}
	
	public void showScore(){
		this.taskId = this.plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(new PluginTask<MineCombat>(this.plugin){
			public void onRun(int currentTick){
				for(Participant player : Game.this.getParticipants()){
					// TODO
				}
			}
		}, 10, 10).getTaskId();
	}

	public String getScoreMessage(Participant participant){
		int time = this.getLeftTicks();
		return this.plugin.getMessage("game.info",
			(time <= 20*10 ? TextFormat.RED + "" + time : TextFormat.GREEN + "" + time) + TextFormat.WHITE,
			this.getScoreString(participant)); // TODO
	}

	private String getScoreString(Participant participant){
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < this.score.length; i++){
			if(i > 0) builder.append(" / ");
			builder.append((i == participant.getTeam() ? TextFormat.GREEN : TextFormat.RED)
					+ "" + score[i]);
		}

		return builder.toString();
	}
	
	public void cancelShowScore(){
		if(this.taskId >= 0 && this.plugin.getServer().getScheduler().isQueued(this.taskId)){
			this.plugin.getServer().getScheduler().cancelTask(taskId);
		}
	}

	public int getLeftTicks(){
		return this.getGameTime() - this.getElapsedTicks();
	}

	public int getElapsedTicks(){
		return (int)((System.currentTimeMillis() - this.startTime) * 50);
	}
	
	/**
	 * @return Stand by time in tick. Returns <= 0 if none.
	 */
	public int getStandByTime(){
		return 1200;
	}

	/**
	 * @return Game time in tick. Returns <= 0 if unlimited.
	 */
	public int getGameTime(){
		return 1200 * 15; // 15 min
	}
	
	public int getTeamCount(){
		return 2;
	}
	
	public boolean addTeamScore(int team, int score){
		if(this.score.length <= team){
			return false;
		}
		
		this.score[team] += score;
		
		return true;
	}
	
	public void addTeamScore(int team){
		this.addTeamScore(team, 1);
	}
	
	public void resetScore(int team){
		this.score[team] = 0;
	}
	
	public void resetScores(){
		this.score = new int[this.getTeamCount()];
	}
	
	public final List<Participant> getParticipants(){
		return new ArrayList<Participant>(this.players);
	}
	
	public final int getMode(){
		return this.mode;
	}
	
	/**
	 * Initializes each game.
	 * 
	 * @param players		Initially joined participants
	 * @return				`true` if successfully started, `false` if not.
	 */
	public abstract boolean startGame(List<Participant> players);
	
	/**
	 * Called when game is standing by
	 * 
	 * @param players
	 * @return				`true` if success, `false` if not.
	 */
	public abstract boolean standBy(List<Participant> players);
	
	public final boolean _standBy(List<Participant> players){
		if(this.standBy(players)){
			this.mode = MineCombat.MODE_STANDBY;
			
			return true;
		}
		
		return false;
	}
	
	public final boolean _startGame(List<Participant> players){
		if(this.startGame(players)){
			this.startTime = System.currentTimeMillis();
			this.mode = MineCombat.MODE_ONGOING;
			
			return true;
		}
		
		return false;
	}

	/**
	 * Called when time elasped
	 *
	 */
	public abstract void stopGame();

	public final void _stopGame(){
		this.stopGame();

		this.mode = MineCombat.MODE_STANDBY;
	}

	public abstract void closeGame();

	public final void _closeGame(){
		this.closeGame();

		this.cancelShowScore();
		this.mode = -1;
	}
	
	/**
	 * Called when participant of game moved.
	 * 
	 * @param player
	 * @return
	 */
	public abstract boolean onParticipantMove(Participant player);
	
	/**
	 * Add player to game
	 * 
	 * @param player
	 * @return true if approved, false if not
	 */
	public boolean addPlayer(Participant player){
		if(player.getJoinedGame() != null){
			return false;
		}

		players.add(player);
		return true;
	}
	
	/**
	 * Called when player left the game
	 * 
	 * @param player
	 */
	public void removePlayer(Participant player){
		players.remove(player);
	}
}
