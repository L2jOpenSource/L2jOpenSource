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
package l2r.gameserver.model.quest.AITasks;

import l2r.Config;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggro Range Enter AI task.
 * @author Zoey76
 */
public final class AggroRangeEnter implements Runnable
{
	private final Logger _log = LoggerFactory.getLogger(AggroRangeEnter.class);
	
	private final Quest _quest;
	private final L2Npc _npc;
	private final L2PcInstance _pc;
	private final boolean _isSummon;
	
	public AggroRangeEnter(Quest quest, L2Npc npc, L2PcInstance pc, boolean isSummon)
	{
		_quest = quest;
		_npc = npc;
		_pc = pc;
		_isSummon = isSummon;
	}
	
	@Override
	public void run()
	{
		String res = null;
		try
		{
			try
			{
				res = _quest.onAggroRangeEnter(_npc, _pc, _isSummon);
			}
			catch (Exception e)
			{
				if (Config.DEBUG_SCRIPT_NOTIFIES)
				{
					if (_quest != null)
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] quest name is: " + _quest.getName());
					}
					else
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] quest is: NULL");
					}
					
					if (_pc != null)
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] Player name is: " + _pc.getName());
					}
					else
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] Player is: NULL");
					}
					
					if (_npc != null)
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] NpcId is: " + String.valueOf(_npc.getId()));
					}
					else
					{
						_log.error("AggroRangeEnter[onAggroRangeEnter] NPC is: NULL");
					}
				}
			}
		}
		catch (Exception e)
		{
			_quest.showError(_pc, e);
		}
		_quest.showResult(_pc, res);
	}
}
