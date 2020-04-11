package com.l2jfrozen.gameserver.model.quest;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class QuestStateManager
{
	private static QuestStateManager instance;
	private List<QuestState> questStates = new ArrayList<>();
	
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				cleanUp();
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	public QuestStateManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
	}
	
	public static final QuestStateManager getInstance()
	{
		if (instance == null)
		{
			instance = new QuestStateManager();
		}
		
		return instance;
	}
	
	public void addQuestState(final Quest quest, final L2PcInstance player, final State state, final boolean completed)
	{
		QuestState qs = getQuestState(player);
		if (qs == null)
		{
			qs = new QuestState(quest, player, state, completed);
		}
		
		// Save the state of the quest for the player in the player's list of quest onwed
		player.setQuestState(qs);
		
	}
	
	/**
	 * Remove all QuestState for all player instance that does not exist
	 */
	public void cleanUp()
	{
		for (int i = getQuestStates().size() - 1; i >= 0; i--)
		{
			if (getQuestStates().get(i).getPlayer() == null)
			{
				getQuestStates().remove(i);
			}
		}
	}
	
	/**
	 * @param  player
	 * @return        QuestState for specified player instance
	 */
	public QuestState getQuestState(final L2PcInstance player)
	{
		for (int i = 0; i < getQuestStates().size(); i++)
		{
			if (getQuestStates().get(i).getPlayer() != null && getQuestStates().get(i).getPlayer().getObjectId() == player.getObjectId())
			{
				return getQuestStates().get(i);
			}
			
		}
		
		return null;
	}
	
	/**
	 * @return all QuestState
	 */
	public List<QuestState> getQuestStates()
	{
		if (questStates == null)
		{
			questStates = new ArrayList<>();
		}
		
		return questStates;
	}
}
