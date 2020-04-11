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

import java.util.Map;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.data.xml.impl.ClassListData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.PledgeSkillList;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>show_skills</li>
 * <li>remove_skills</li>
 * <li>skill_list</li>
 * <li>skill_index</li>
 * <li>add_skill</li>
 * <li>remove_skill</li>
 * <li>get_skills</li>
 * <li>reset_skills</li>
 * <li>give_all_skills</li>
 * <li>give_all_skills_fs</li>
 * <li>admin_give_all_clan_skills</li>
 * <li>remove_all_skills</li>
 * <li>add_clan_skills</li>
 * <li>admin_setskill</li>
 * </ul>
 * @version 2012/02/26 Small fixes by Zoey76 05/03/2011
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static Logger _log = LoggerFactory.getLogger(AdminSkill.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_skills",
		"admin_remove_skills",
		"admin_skill_list",
		"admin_skill_index",
		"admin_add_skill",
		"admin_remove_skill",
		"admin_get_skills",
		"admin_reset_skills",
		"admin_give_all_skills",
		"admin_give_all_skills_fs",
		"admin_give_clan_skills",
		"admin_give_all_clan_skills",
		"admin_remove_all_skills",
		"admin_add_clan_skill",
		"admin_setskill"
	};
	
	private static L2Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_show_skills"))
		{
			showMainPage(activeChar);
		}
		else if (command.startsWith("admin_remove_skills"))
		{
			try
			{
				String val = command.substring(20);
				removeSkillsPage(activeChar, Integer.parseInt(val));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHtml.showAdminHtml(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHtml.showAdminHtml(activeChar, "skills/" + val + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				adminAddSkill(activeChar, val);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String[] idAndPage = command.substring(19).split(" ");
				
				int idval = Integer.parseInt(idAndPage[0]);
				adminRemoveSkill(activeChar, idval);
				removeSkillsPage(activeChar, Integer.parseInt(idAndPage[1]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			adminResetSkills(activeChar);
		}
		else if (command.equals("admin_give_all_skills"))
		{
			adminGiveAllSkills(activeChar, false);
		}
		else if (command.equals("admin_give_all_skills_fs"))
		{
			adminGiveAllSkills(activeChar, true);
		}
		else if (command.equals("admin_give_clan_skills"))
		{
			adminGiveClanSkills(activeChar, false);
		}
		else if (command.equals("admin_give_all_clan_skills"))
		{
			adminGiveClanSkills(activeChar, true);
		}
		else if (command.equals("admin_remove_all_skills"))
		{
			final L2Object target = activeChar.getTarget();
			if ((target == null) || !target.isPlayer())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			final L2PcInstance player = target.getActingPlayer();
			for (L2Skill skill : player.getAllSkills())
			{
				player.removeSkill(skill);
			}
			activeChar.sendMessage("You have removed all skills from " + player.getName() + ".");
			player.sendMessage("Admin removed all skills from you.");
			player.sendSkillList();
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_setskill"))
		{
			String[] split = command.split(" ");
			int id = Integer.parseInt(split[1]);
			int lvl = Integer.parseInt(split[2]);
			L2Skill skill = SkillData.getInstance().getInfo(id, lvl);
			activeChar.addSkill(skill);
			activeChar.sendSkillList();
			activeChar.sendMessage("You added yourself skill " + skill.getName() + "(" + id + ") level " + lvl);
		}
		return true;
	}
	
	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar the active char
	 * @param includedByFs if {@code true} Forgotten Scroll skills will be delivered.
	 */
	private void adminGiveAllSkills(L2PcInstance activeChar, boolean includedByFs)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		// Notify player and admin
		activeChar.sendMessage("You gave " + player.giveAvailableSkills(includedByFs, true) + " skills to " + player.getName());
		player.sendSkillList();
	}
	
	/**
	 * This function will give all the skills that the target's clan can learn at it's level.<br>
	 * If the target is not the clan leader, a system message will be sent to the Game Master.
	 * @param activeChar the active char, probably a Game Master.
	 * @param includeSquad if Squad skills is included
	 */
	private void adminGiveClanSkills(L2PcInstance activeChar, boolean includeSquad)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2PcInstance player = target.getActingPlayer();
		final L2Clan clan = player.getClan();
		if (clan == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return;
		}
		
		if (!player.isClanLeader())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(player.getName());
			activeChar.sendPacket(sm);
		}
		
		final Map<Integer, L2SkillLearn> skills = SkillTreesData.getInstance().getMaxPledgeSkills(clan, includeSquad);
		for (L2SkillLearn s : skills.values())
		{
			clan.addNewSkill(SkillData.getInstance().getInfo(s.getSkillId(), s.getSkillLevel()));
		}
		
		// Notify target and active char
		clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.sendSkillList();
		}
		
		activeChar.sendMessage("You gave " + skills.size() + " skills to " + player.getName() + "'s clan " + clan.getName() + ".");
		player.sendMessage("Your clan received " + skills.size() + " skills.");
	}
	
	/**
	 * TODO: Externalize HTML
	 * @param activeChar the active Game Master.
	 * @param page
	 */
	private void removeSkillsPage(L2PcInstance activeChar, int page)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		StringBuilder sb = new StringBuilder();
		
		final L2PcInstance player = target.getActingPlayer();
		final L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
		
		page = page < 1 ? 1 : page;
		int size = skills.length;
		int maxItemsPerPage = 11;
		int startIndex = (page - 1) * maxItemsPerPage;
		startIndex = ((startIndex > (size - 1)) ? (size - 1) : startIndex);
		int endIndex = startIndex + maxItemsPerPage;
		endIndex = ((endIndex > (size - 1)) ? (size - 1) : endIndex);
		int filled = 0;
		
		sb.append("<html><body>");
		
		sb.append("<table width=260>");
		sb.append("<tr><td width=40>");
		sb.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>Character Selection Menu</center></td><td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		sb.append("</td></tr></table>");
		sb.append("<br>");
		sb.append("<table width=270><tr><td>Editing <font color=\"LEVEL\">" + player.getName() + "</font></td></tr></table>");
		sb.append("<table width=270><tr><td>Lv: " + String.valueOf(player.getLevel()) + " " + ClassListData.getInstance().getClass(player.getClassId()).getClientCode() + "</td></tr></table>");
		sb.append("<table width=270><tr><td>Note: Modifying skills can ruin the game</td></tr></table>");
		sb.append("<table width=270><tr><td>Click on the skill you wish to remove:</td></tr></table>");
		
		String prevButton = startIndex > 0 ? "<a action=\"bypass -h admin_remove_skills " + (page - 1) + "\">PrevPage</a>" : "";
		String nextButton = endIndex < (size - 1) ? "<a action=\"bypass -h admin_remove_skills " + (page + 1) + "\">NextPage</a>" : "";
		sb.append("<table width=270><tr>");
		sb.append("<td width=120 align=left>" + prevButton + "</td><td width=55><font color=\"LEVEL\">Page: " + String.valueOf(page) + "</font></td><td width=120 align=right>" + nextButton + "</td>");
		sb.append("</tr></table>");
		
		sb.append("<table width=270><tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
		
		if (size > 0)
		{
			for (int index = startIndex; index <= endIndex; ++index)
			{
				sb.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + String.valueOf(skills[index].getId()) + " " + page + "\">" + skills[index].getName() + "</a></td><td width=60>" + String.valueOf(skills[index].getLevel()) + "</td><td width=40>" + String.valueOf(skills[index].getId()) + "</td></tr>");
				filled++;
			}
		}
		
		while (filled < maxItemsPerPage)
		{
			sb.append("<tr><td width=80>-----</td><td width=60>-----</td><td width=40>-----</td></tr>");
			filled++;
		}
		
		sb.append("</table width=260>");
		sb.append("<br><center>");
		sb.append("<table><tr><td>Remove skill by ID: <edit var=\"id_to_remove\" width=110>");
		sb.append("</td></tr></table>");
		sb.append("<br></center><center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" + "<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" + "</body></html>");
		adminReply.setHtml(sb.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void showMainPage(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", ClassListData.getInstance().getClass(player.getClassId()).getClientCode());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminGetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			adminSkills = activeChar.getAllSkills().toArray(new L2Skill[activeChar.getAllSkills().size()]);
			for (L2Skill skill : adminSkills)
			{
				activeChar.removeSkill(skill);
			}
			for (L2Skill skill : skills)
			{
				activeChar.addSkill(skill, true);
			}
			activeChar.sendMessage("You now have all the skills of " + player.getName() + ".");
			activeChar.sendSkillList();
		}
		showMainPage(activeChar);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 */
	private void adminResetSkills(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (adminSkills == null)
		{
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		}
		else
		{
			L2Skill[] skills = player.getAllSkills().toArray(new L2Skill[player.getAllSkills().size()]);
			for (L2Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			for (L2Skill skill : activeChar.getAllSkills())
			{
				player.addSkill(skill, true);
			}
			for (L2Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			for (L2Skill skill : adminSkills)
			{
				activeChar.addSkill(skill, true);
			}
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
			activeChar.sendSkillList();
			player.sendSkillList();
		}
		showMainPage(activeChar);
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param val
	 */
	private void adminAddSkill(L2PcInstance activeChar, String val)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
		{
			showMainPage(activeChar);
		}
		else
		{
			L2Skill skill = null;
			try
			{
				String id = st.nextToken();
				String level = st.nextToken();
				int idval = Integer.parseInt(id);
				int levelval = Integer.parseInt(level);
				skill = SkillData.getInstance().getInfo(idval, levelval);
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
			if (skill != null)
			{
				String name = skill.getName();
				// Player's info.
				player.sendMessage("Admin gave you the skill " + name + ".");
				player.addSkill(skill, true);
				player.sendSkillList();
				// Admin info.
				activeChar.sendMessage("You gave the skill " + name + " to " + player.getName() + ".");
				if (Config.DEBUG)
				{
					_log.info("[GM]" + activeChar.getName() + " gave skill " + name + " to " + player.getName() + ".");
				}
				activeChar.sendSkillList();
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
			showMainPage(activeChar); // Back to start
		}
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param idval
	 */
	private void adminRemoveSkill(L2PcInstance activeChar, int idval)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		L2Skill skill = SkillData.getInstance().getInfo(idval, player.getSkillLevel(idval));
		if (skill != null)
		{
			String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			// Admin information
			activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
			if (Config.DEBUG)
			{
				_log.info("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
			}
			activeChar.sendSkillList();
		}
		else
		{
			activeChar.sendMessage("Error: there is no such skill.");
		}
		removeSkillsPage(activeChar, 0); // Back to previous page
	}
	
	/**
	 * @param activeChar the active Game Master.
	 * @param id
	 * @param level
	 */
	private void adminAddClanSkill(L2PcInstance activeChar, int id, int level)
	{
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return;
		}
		final L2PcInstance player = target.getActingPlayer();
		if (!player.isClanLeader())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(player.getName());
			activeChar.sendPacket(sm);
			showMainPage(activeChar);
			return;
		}
		if ((id < 370) || (id > 391) || (level < 1) || (level > 3))
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			return;
		}
		
		final L2Skill skill = SkillData.getInstance().getInfo(id, level);
		if (skill == null)
		{
			activeChar.sendMessage("Error: there is no such skill.");
			return;
		}
		
		String skillname = skill.getName();
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
		sm.addSkillName(skill);
		player.sendPacket(sm);
		final L2Clan clan = player.getClan();
		clan.broadcastToOnlineMembers(sm);
		clan.addNewSkill(skill);
		activeChar.sendMessage("You gave the Clan Skill: " + skillname + " to the clan " + clan.getName() + ".");
		
		clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.sendSkillList();
		}
		
		showMainPage(activeChar);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
