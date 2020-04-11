/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.taskmanager.DecayTaskManager;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_stats",
		"admin_skill_test",
		"admin_known",
		"admin_dkmanager",
		"admin_movie",
		"admin_checkai",
		"admin_checkid",
		"admin_known",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_stats"))
		{
			for (String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if (command.startsWith("admin_known"))
		{
			activeChar.sendMessage("Your knownlist contains: " + activeChar.getKnownList().getKnownObjects().size());
		}
		else if (command.startsWith("admin_checkid"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			activeChar.sendMessage("Your id is free: " + IdFactory.getInstance().checkId(id));
		}
		else if (command.startsWith("admin_checkai"))
		{
			activeChar.sendMessage("Your Intention is: " + activeChar.getAI().getIntention().toString());
		}
		else if (command.startsWith("admin_dkmanager"))
		{
			activeChar.sendMessage("DecayTaksk: " + DecayTaskManager.getInstance().getTasks().size());
		}
		else if (command.startsWith("admin_skill_test"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				if (command.startsWith("admin_skill_test"))
				{
					adminTestSkill(activeChar, id, true);
				}
				else
				{
					adminTestSkill(activeChar, id, false);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_known"))
		{
			Config.CHECK_KNOWN = Config.CHECK_KNOWN ? false : true;
		}
		else if (command.startsWith("admin_movie"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				int id = Integer.parseInt(st.nextToken());
				activeChar.showQuestMovie(id);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param id
	 * @param msu
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id, boolean msu)
	{
		L2Character caster;
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2Character))
		{
			caster = activeChar;
		}
		else
		{
			caster = (L2Character) target;
		}
		
		L2Skill _skill = SkillData.getInstance().getInfo(id, 1);
		if (_skill != null)
		{
			caster.setTarget(activeChar);
			if (msu)
			{
				caster.broadcastPacket(new MagicSkillUse(caster, activeChar, id, 1, _skill.getHitTime(), _skill.getReuseDelay()));
			}
			else
			{
				caster.doCast(_skill);
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
