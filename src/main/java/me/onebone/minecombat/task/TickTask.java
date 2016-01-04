package me.onebone.minecombat.task;

import me.onebone.minecombat.MineCombat;

import cn.nukkit.scheduler.PluginTask;

public class TickTask extends PluginTask<MineCombat>{
	public TickTask(MineCombat plugin){
		super(plugin);
	}
	
	public void onRun(int currentTick){
		this.getOwner().onTick();
	}
}
