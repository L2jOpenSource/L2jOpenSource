/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestTimer
{
	protected static final Logger _log = LoggerFactory.getLogger(QuestTimer.class);
	
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
					cancelAndRemove();
				}
				try
				{
					getQuest().notifyEvent(getName(), getNpc(), getPlayer());
				}
				catch (Exception e)
				{
					if (Config.DEBUG_SCRIPT_NOTIFIES)
					{
						if (getName() == null)
						{
							_log.error("QuestTimer[notifyEvent] getName() is NULL");
						}
						else
						{
							_log.error("QuestTimer[notifyEvent] getQuest() name is: " + getQuest().getName());
						}
						
						if (getNpc() == null)
						{
							_log.error("QuestTimer[notifyEvent] getNpc() is NULL");
						}
						else
						{
							_log.error("QuestTimer[notifyEvent] getNpc() ID is: " + String.valueOf(getNpc().getId()));
						}
						
						if (getPlayer() == null)
						{
							_log.error("QuestTimer[notifyEvent] getPlayer() is NULL");
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	private boolean _isActive = true;
	private final String _name;
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _player;
	private final boolean _isRepeating;
	private ScheduledFuture<?> _schedular;
	
	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		_name = name;
		_quest = quest;
		_player = player;
		_npc = npc;
		_isRepeating = repeating;
		if (repeating)
		{
			_schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time); // Prepare auto end task
		}
		else
		{
			_schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
		}
	}
	
	public QuestTimer(Quest quest, String name, long time, L2Npc npc, L2PcInstance player)
	{
		this(quest, name, time, npc, player, false);
	}
	
	public QuestTimer(QuestState qs, String name, long time)
	{
		this(qs.getQuest(), name, time, null, qs.getPlayer(), false);
	}
	
	/**
	 * Cancel this quest timer.
	 */
	public void cancel()
	{
		_isActive = false;
		if (_schedular != null)
		{
			_schedular.cancel(false);
		}
	}
	
	/**
	 * Cancel this quest timer and remove it from the associated quest.
	 */
	public void cancelAndRemove()
	{
		cancel();
		_quest.removeQuestTimer(this);
	}
	
	/**
	 * public method to compare if this timer matches with the key attributes passed.
	 * @param quest : Quest instance to which the timer is attached
	 * @param name : Name of the timer
	 * @param npc : Npc instance attached to the desired timer (null if no npc attached)
	 * @param player : Player instance attached to the desired timer (null if no player attached)
	 * @return
	 */
	public boolean isMatch(Quest quest, String name, L2Npc npc, L2PcInstance player)
	{
		if ((quest == null) || (name == null))
		{
			return false;
		}
		if ((quest != _quest) || !name.equalsIgnoreCase(getName()))
		{
			return false;
		}
		return ((npc == _npc) && (player == _player));
	}
	
	public final boolean getIsActive()
	{
		return _isActive;
	}
	
	public final boolean getIsRepeating()
	{
		return _isRepeating;
	}
	
	public final Quest getQuest()
	{
		return _quest;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final L2Npc getNpc()
	{
		return _npc;
	}
	
	public final L2PcInstance getPlayer()
	{
		return _player;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
}
