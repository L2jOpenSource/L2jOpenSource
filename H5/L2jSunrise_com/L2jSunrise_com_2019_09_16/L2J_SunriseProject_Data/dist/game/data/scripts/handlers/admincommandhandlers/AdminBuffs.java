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

import java.util.Collection;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SkillCoolTime;
import l2r.gameserver.util.GMAudit;
import l2r.util.StringUtil;

public class AdminBuffs implements IAdminCommandHandler
{
	private final static int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_getbuffs",
		"admin_stopbuff",
		"admin_stopallbuffs",
		"admin_areacancel",
		"admin_removereuse",
		"admin_switch_gm_buffs"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		
		if (command.startsWith("admin_getbuffs"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			if (st.hasMoreTokens())
			{
				final String playername = st.nextToken();
				L2PcInstance player = L2World.getInstance().getPlayer(playername);
				if (player != null)
				{
					int page = 1;
					if (st.hasMoreTokens())
					{
						page = Integer.parseInt(st.nextToken());
					}
					showBuffs(activeChar, player, page, command.endsWith("_ps"));
					return true;
				}
				activeChar.sendMessage("The player " + playername + " is not online.");
				return false;
			}
			else if ((activeChar.getTarget() != null) && activeChar.getTarget().isCharacter())
			{
				showBuffs(activeChar, (L2Character) activeChar.getTarget(), 1, command.endsWith("_ps"));
				return true;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
		}
		else if (command.startsWith("admin_stopbuff"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				
				removeBuff(activeChar, objectId, skillId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed removing effect: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>");
				return false;
			}
		}
		else if (command.startsWith("admin_stopallbuffs"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int objectId = Integer.parseInt(st.nextToken());
				removeAllBuffs(activeChar, objectId);
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Failed removing all effects: " + e.getMessage());
				activeChar.sendMessage("Usage: //stopallbuffs <objectId>");
				return false;
			}
		}
		else if (command.startsWith("admin_areacancel"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			String val = st.nextToken();
			try
			{
				int radius = Integer.parseInt(val);
				
				for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
				{
					if (knownChar.isPlayer() && !knownChar.equals(activeChar))
					{
						knownChar.stopAllEffects();
					}
				}
				
				activeChar.sendMessage("All effects canceled within radius " + radius);
				return true;
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //areacancel <radius>");
				return false;
			}
		}
		else if (command.startsWith("admin_removereuse"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			
			L2Character creature = null;
			if (st.hasMoreTokens())
			{
				creature = L2World.getInstance().getPlayer(st.nextToken());
				if (creature == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
					return false;
				}
			}
			else
			{
				final L2Object target = activeChar.getTarget();
				if ((target != null) && target.isCharacter())
				{
					creature = (L2Character) target;
				}
				
				if (creature == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return false;
				}
			}
			
			creature.resetTimeStamps();
			creature.resetDisabledSkills();
			if (creature.isPlayer())
			{
				creature.sendPacket(new SkillCoolTime(creature.getActingPlayer()));
			}
			activeChar.sendMessage("Skill reuse was removed from " + creature.getName() + ".");
			return true;
		}
		else if (command.startsWith("admin_switch_gm_buffs"))
		{
			if (Config.GM_GIVE_SPECIAL_SKILLS != Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				final boolean toAuraSkills = activeChar.getKnownSkill(7041) != null;
				switchSkills(activeChar, toAuraSkills);
				activeChar.sendSkillList();
				activeChar.sendMessage("You have succefully changed to target " + (toAuraSkills ? "aura" : "one") + " special skills.");
				return true;
			}
			activeChar.sendMessage("There is nothing to switch.");
			return false;
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * @param gmchar the player to switch the Game Master skills.
	 * @param toAuraSkills if {@code true} it will remove "GM Aura" skills and add "GM regular" skills, vice versa if {@code false}.
	 */
	public static void switchSkills(L2PcInstance gmchar, boolean toAuraSkills)
	{
		final Collection<L2Skill> skills = toAuraSkills ? SkillTreesData.getInstance().getGMSkillTree().values() : SkillTreesData.getInstance().getGMAuraSkillTree().values();
		for (L2Skill skill : skills)
		{
			gmchar.removeSkill(skill, false); // Don't Save GM skills to database
		}
		SkillTreesData.getInstance().addSkills(gmchar, toAuraSkills);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	public static void showBuffs(L2PcInstance activeChar, L2Character target, int page, boolean passive)
	{
		final L2Effect[] effects = target.getAllEffects();
		if ((page > ((effects.length / PAGE_LIMIT) + 1)) || (page < 1))
		{
			return;
		}
		
		int max = effects.length / PAGE_LIMIT;
		if (effects.length > (PAGE_LIMIT * max))
		{
			max++;
		}
		
		final StringBuilder html = StringUtil.startAppend(500 + (effects.length * 200), "<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center><font color=\"LEVEL\">Effects of ", target.getName(), "</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"100%\"><tr><td width=200>Skill</td><td width=30>Rem. Time</td><td width=70>Action</td><td>In use</td></tr>");
		int start = ((page - 1) * PAGE_LIMIT);
		int end = Math.min(((page - 1) * PAGE_LIMIT) + PAGE_LIMIT, effects.length);
		L2Skill skill;
		for (int i = start; i < end; i++)
		{
			L2Effect e = effects[i];
			if (e != null)
			{
				skill = e.getSkill();
				StringUtil.append(html, "<tr><td>", skill.getName(), "(", e.getClass().getSimpleName(), "-", e.getState().toString(), ")", "</td><td>", skill.isToggle() ? "toggle" : skill.isPassive() ? "passive" : (e.getAbnormalTime() - e.getTime()) + "s", "</td><td><button value=\"X\" action=\"bypass -h admin_stopbuff ", Integer.toString(target.getObjectId()), " ", String.valueOf(skill.getId()), "\" width=30 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>" + e.getInUse() + "</td></tr>");
			}
		}
		
		html.append("</table><table width=300 bgcolor=444444><tr>");
		for (int x = 0; x < max; x++)
		{
			int pagenr = x + 1;
			if (page == pagenr)
			{
				html.append("<td>Page ");
				html.append(pagenr);
				html.append("</td>");
			}
			else
			{
				html.append("<td><a action=\"bypass -h admin_getbuffs ");
				html.append(target.getName());
				html.append(" ");
				html.append(x + 1);
				html.append("\"> Page ");
				html.append(pagenr);
				html.append(" </a></td>");
			}
		}
		
		html.append("</tr></table>");
		
		StringUtil.append(html, "<br><center><button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ", Integer.toString(target.getObjectId()), "\" width=80 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></html>");
		
		NpcHtmlMessage ms = new NpcHtmlMessage();
		ms.setHtml(html.toString());
		activeChar.sendPacket(ms);
		
		if (Config.GMAUDIT)
		{
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "getbuffs", target.getName() + " (" + Integer.toString(target.getObjectId()) + ")", "");
		}
	}
	
	private static void removeBuff(L2PcInstance activeChar, int objId, int skillId)
	{
		L2Character target = null;
		try
		{
			target = (L2Character) L2World.getInstance().findObject(objId);
		}
		catch (Exception e)
		{
		}
		
		if ((target != null) && (skillId > 0))
		{
			if (target.isAffectedBySkill(skillId))
			{
				target.stopSkillEffects(skillId);
				activeChar.sendMessage("Removed skill ID: " + skillId + " effects from " + target.getName() + " (" + objId + ").");
			}
			
			showBuffs(activeChar, target, 1, false);
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopbuff", target.getName() + " (" + objId + ")", Integer.toString(skillId));
			}
		}
	}
	
	private static void removeAllBuffs(L2PcInstance activeChar, int objId)
	{
		L2Character target = null;
		try
		{
			target = (L2Character) L2World.getInstance().findObject(objId);
		}
		catch (Exception e)
		{
		}
		
		if (target != null)
		{
			target.stopAllEffects();
			activeChar.sendMessage("Removed all effects from " + target.getName() + " (" + objId + ")");
			showBuffs(activeChar, target, 1, false);
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "stopallbuffs", target.getName() + " (" + objId + ")", "");
			}
		}
	}
}