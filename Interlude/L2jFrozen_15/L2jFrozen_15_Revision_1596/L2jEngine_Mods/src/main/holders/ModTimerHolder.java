package main.holders;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import main.EngineModsManager;
import main.concurrent.ThreadPool;
import main.engine.AbstractMod;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;

public class ModTimerHolder
{
	protected static final Logger LOG = Logger.getLogger(ModTimerHolder.class.getName());
	
	protected final String modName;
	protected final String timerName;
	protected final NpcHolder npc;
	protected final PlayerHolder player;
	
	private ScheduledFuture<?> schedular;
	
	public ModTimerHolder(AbstractMod mod, String name, NpcHolder npc, PlayerHolder player, long time, boolean repeating)
	{
		modName = mod.getClass().getSimpleName();
		timerName = name;
		this.npc = npc;
		this.player = player;
		
		if (repeating)
		{
			schedular = ThreadPool.scheduleAtFixedRate(new ScheduleTimerTask(repeating), time, time);
		}
		else
		{
			schedular = ThreadPool.schedule(new ScheduleTimerTask(repeating), time);
		}
	}
	
	protected final class ScheduleTimerTask implements Runnable
	{
		boolean isRepeating;
		
		public ScheduleTimerTask(boolean repeating)
		{
			isRepeating = repeating;
		}
		
		@Override
		public void run()
		{
			if (!EngineModsManager.getMod(modName).isStarting())
			{
				schedular.cancel(true);
				cancel();
				return;
			}
			
			if (schedular == null)
			{
				return;
			}
			
			EngineModsManager.getMod(modName).onTimer(timerName, npc, player);
			
			if (!isRepeating)
			{
				cancel();
			}
		}
	}
	
	public final void cancel()
	{
		if (schedular != null)
		{
			schedular.cancel(false);
			schedular = null;
		}
		EngineModsManager.getMod(modName).removeTimer(this);
	}
	
	/**
	 * public method to compare if this timer matches with the key attributes passed.
	 * @param  mod       : Mod instance to which the timer is attached
	 * @param  timerName : Name of the timer
	 * @param  npc       : Npc instance attached to the desired timer (null if no npc attached)
	 * @param  player    : Player instance attached to the desired timer (null if no player attached)
	 * @return           boolean
	 */
	public final boolean equals(String timerName, NpcHolder npc, PlayerHolder player)
	{
		if ((timerName == null) || !this.timerName.equals(timerName))
		{
			return false;
		}
		
		return (this.npc == npc) && (this.player == player);
	}
	
	public String getName()
	{
		return timerName;
	}
}
