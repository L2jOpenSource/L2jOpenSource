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
package l2r.gameserver.model.actor.tasks.attackable;

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xban1x
 */
public final class OnKillNotifyTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(OnKillNotifyTask.class);
	
	private final L2Attackable _attackable;
	private final Quest _quest;
	private final L2PcInstance _killer;
	private final boolean _isSummon;
	
	public OnKillNotifyTask(L2Attackable attackable, Quest quest, L2PcInstance killer, boolean isSummon)
	{
		_attackable = attackable;
		_quest = quest;
		_killer = killer;
		_isSummon = isSummon;
	}
	
	@Override
	public void run()
	{
		if ((_quest != null) && (_attackable != null) && (_killer != null))
		{
			try
			{
				_quest.notifyKill(_attackable, _killer, _isSummon);
			}
			catch (Exception e)
			{
				if (_quest == null)
				{
					_log.error("Quest[notifyKill] getName() is NULL");
				}
				else
				{
					_log.error("Quest[notifyKill] getQuest() name is: " + _quest.getName());
				}
				
				if (_attackable == null)
				{
					_log.error("Quest[notifyKill] _attackable is NULL");
				}
				else
				{
					_log.error("Quest[notifyKill] _attackable name is: " + _attackable.getName());
					_log.error("Quest[notifyKill] _attackable Id is: " + _attackable.getId());
				}
				
				if (_killer == null)
				{
					_log.error("Quest[notifyKill] _killer is NULL");
				}
				else
				{
					_log.error("Quest[notifyKill] killer is: " + _killer.getName());
				}
			}
		}
	}
}
