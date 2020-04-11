package com.l2jfrozen.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class QuestTimer
{
	private boolean isActive = true;
	private final String name;
	private final Quest quest;
	private final L2NpcInstance npc;
	private final L2PcInstance player;
	private final boolean isRepeating;
	private ScheduledFuture<?> schedular;
	
	public QuestTimer(final Quest quest, final String name, final long time, final L2NpcInstance npc, final L2PcInstance player, final boolean repeating)
	{
		this.name = name;
		this.quest = quest;
		this.player = player;
		this.npc = npc;
		isRepeating = repeating;
		if (repeating)
		{
			schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time); // Prepare auto end task
		}
		else
		{
			schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
		}
	}
	
	public QuestTimer(final Quest quest, final String name, final long time, final L2NpcInstance npc, final L2PcInstance player)
	{
		this(quest, name, time, npc, player, false);
	}
	
	public QuestTimer(final QuestState qs, final String name, final long time)
	{
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}
	
	public void cancel()
	{
		cancel(true);
	}
	
	public void cancel(final boolean removeTimer)
	{
		isActive = false;
		
		if (schedular != null)
		{
			schedular.cancel(false);
		}
		
		if (removeTimer)
		{
			getQuest().removeQuestTimer(this);
		}
		
	}
	
	// public method to compare if this timer matches with the key attributes passed.
	// a quest and a name are required.
	// null npc or player act as wildcards for the match
	public boolean isMatch(final Quest quest, final String name, final L2NpcInstance npc, final L2PcInstance player)
	{
		
		if (quest == null || name == null)
		{
			return false;
		}
		
		if (quest != getQuest() || name.compareToIgnoreCase(getName()) != 0)
		{
			return false;
		}
		
		return npc == getNpc() && player == getPlayer();
	}
	
	public final boolean getIsActive()
	{
		return isActive;
	}
	
	public final boolean getIsRepeating()
	{
		return isRepeating;
	}
	
	public final Quest getQuest()
	{
		return quest;
	}
	
	public final String getName()
	{
		return name;
	}
	
	public final L2NpcInstance getNpc()
	{
		return npc;
	}
	
	public final L2PcInstance getPlayer()
	{
		return player;
	}
	
	@Override
	public final String toString()
	{
		return name;
	}
	
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsActive())
			{
				return;
			}
			
			try
			{
				if (!getIsRepeating())
				{
					cancel();
				}
				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
}
