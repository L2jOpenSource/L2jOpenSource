package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - show_skills - remove_skills - skill_list - skill_index - add_skill - remove_skill - get_skills - reset_skills - give_all_skills - remove_all_skills - add_clan_skills
 * @version $Revision: 1.2.4.7 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminSkill implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminSkill.class);
	
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
		"admin_remove_all_skills",
		"admin_add_clan_skill"
	};
	
	private static L2Skill[] adminSkills;
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
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
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_skill_list"))
		{
			AdminHelpPage.showHelpPage(activeChar, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			try
			{
				String val = command.substring(18);
				AdminHelpPage.showHelpPage(activeChar, "skills/" + val + ".htm");
				val = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				String val = command.substring(15);
				
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel().isGm())
				{
					adminAddSkill(activeChar, val);
				}
				
				val = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //add_skill <skill_id> <level>");
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				String id = command.substring(19);
				
				final int idval = Integer.parseInt(id);
				
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel().isGm())
				{
					adminRemoveSkill(activeChar, idval);
				}
				
				id = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //remove_skill <skill_id>");
			}
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(activeChar);
		}
		else if (command.equals("admin_reset_skills"))
		{
			if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel().isGm())
			{
				adminResetSkills(activeChar);
			}
		}
		else if (command.equals("admin_give_all_skills"))
		{
			if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel().isGm())
			{
				adminGiveAllSkills(activeChar);
			}
		}
		
		else if (command.equals("admin_remove_all_skills"))
		{
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar.getTarget();
				
				for (final L2Skill skill : player.getAllSkills())
				{
					player.removeSkill(skill);
				}
				
				activeChar.sendMessage("You removed all skills from " + player.getName());
				player.sendMessage("Admin removed all skills from you.");
				player.sendSkillList();
				player = null;
			}
		}
		else if (command.startsWith("admin_add_clan_skill"))
		{
			try
			{
				String[] val = command.split(" ");
				
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel().isGm())
				{
					adminAddClanSkill(activeChar, Integer.parseInt(val[1]), Integer.parseInt(val[2]));
				}
				
				val = null;
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			}
		}
		return true;
	}
	
	/**
	 * This function will give all the skills that the target can learn at his/her level
	 * @param activeChar the gm char
	 */
	private void adminGiveAllSkills(final L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		player.giveAvailableSkills();
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void removeSkillsPage(final L2PcInstance activeChar, int page)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		L2Skill[] skills = player.getAllSkills();
		
		int maxSkillsPerPage = 10;
		int maxPages = skills.length / maxSkillsPerPage;
		
		if (skills.length > maxSkillsPerPage * maxPages)
		{
			maxPages++;
		}
		
		if (page > maxPages)
		{
			page = maxPages;
		}
		
		int skillsStart = maxSkillsPerPage * page;
		int skillsEnd = skills.length;
		
		if (skillsEnd - skillsStart > maxSkillsPerPage)
		{
			skillsEnd = skillsStart + maxSkillsPerPage;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charskillsdelete.htm");
		StringBuilder htm = new StringBuilder();
		
		// Paginator
		htm.append("<table width=240><tr>");
		
		int previousPage = page - 1;
		
		if (previousPage >= 0)
		{
			htm.append("<td width=\"80\"><a action=\"bypass -h admin_remove_skills ");
			htm.append(previousPage);
			htm.append("\">");
			htm.append("Previous page");
			htm.append("</a>");
			htm.append("</td>");
		}
		else
		{
			htm.append("<td width=\"80\"></td>");
		}
		
		if (page >= 1) // La pagina inicial es 0
		{
			htm.append("<td width=\"80\">");
			htm.append("Page ");
			htm.append(page);
			htm.append("</td>");
		}
		
		int nextPage = page + 1;
		
		if (nextPage < maxPages)
		{
			htm.append("<td width=\"80\"><a action=\"bypass -h admin_remove_skills ");
			htm.append(nextPage);
			htm.append("\">");
			htm.append("Next page");
			htm.append("</a>");
			htm.append("</td>");
		}
		else
		{
			htm.append("<td width=\"80\"></td>");
		}
		
		htm.append("</tr></table>");
		// End paginator
		
		htm.append("<br><table width=270>");
		htm.append("<tr><td width=80>Name:</td><td width=40>ID:</td><td width=60>Level:</td></tr>");
		
		for (int i = skillsStart; i < skillsEnd; i++)
		{
			htm.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill " + skills[i].getId() + "\">" + skills[i].getName() + "</a></td><td width=60>" + skills[i].getId() + "</td><td width=40>" + skills[i].getLevel() + "</td></tr>");
		}
		
		htm.append("</table><br>");
		
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%class%", player.getClassId().getName());
		adminReply.replace("%level%", player.getLevel());
		adminReply.replace("%total_skills%", skills.length);
		adminReply.replace("%body%", htm.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showMainPage(final L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charskills.htm");
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%class%", player.getTemplate().className);
		activeChar.sendPacket(adminReply);
		
		adminReply = null;
		player = null;
		target = null;
	}
	
	private void adminGetSkills(final L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			L2Skill[] skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			
			for (final L2Skill adminSkill : adminSkills)
			{
				activeChar.removeSkill(adminSkill);
			}
			
			for (final L2Skill skill : skills)
			{
				activeChar.addSkill(skill, true);
			}
			
			activeChar.sendMessage("You now have all the skills of " + player.getName() + ".");
			activeChar.sendSkillList();
			
			skills = null;
		}
		
		showMainPage(activeChar);
		
		target = null;
		player = null;
	}
	
	private void adminResetSkills(final L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (adminSkills == null)
		{
			activeChar.sendMessage("You must get the skills of someone in order to do this.");
		}
		else
		{
			L2Skill[] skills = player.getAllSkills();
			
			for (final L2Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			
			for (int i = 0; i < activeChar.getAllSkills().length; i++)
			{
				player.addSkill(activeChar.getAllSkills()[i], true);
			}
			
			for (final L2Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			
			for (final L2Skill adminSkill : adminSkills)
			{
				activeChar.addSkill(adminSkill, true);
			}
			
			player.sendMessage("[GM]" + activeChar.getName() + " updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
			activeChar.sendSkillList();
			
			skills = null;
		}
		
		showMainPage(activeChar);
		
		player = null;
		target = null;
	}
	
	private void adminAddSkill(final L2PcInstance activeChar, final String val)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			if (target == activeChar || (target != activeChar && activeChar.getAccessLevel().getLevel() < 3))
			{
				player = (L2PcInstance) target;
			}
			else
			{
				showMainPage(activeChar);
				activeChar.sendPacket(SystemMessage.sendString("You have not right to add skills to other players"));
				return;
			}
		}
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		StringTokenizer st = new StringTokenizer(val);
		
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
				
				skill = SkillTable.getInstance().getInfo(idval, levelval);
			}
			catch (Exception e)
			{
				LOGGER.error("AdminSkill.addminAddSkill : Something went wrong", e);
			}
			
			if (skill != null)
			{
				player.sendMessage("Admin gave you the skill " + skill.getName() + ".");
				player.addSkill(skill, true);
				// Admin information
				activeChar.sendMessage("You gave the skill " + skill.getName() + " to " + player.getName() + ".");
				
				if (Config.DEBUG)
				{
					LOGGER.debug("[GM]" + activeChar.getName() + " gave skill " + skill.getName() + " to " + player.getName() + ".");
				}
				
				player.sendSkillList();
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
			
			showMainPage(activeChar); // Back to start
		}
	}
	
	private void adminRemoveSkill(final L2PcInstance activeChar, final int idval)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		
		if (skill != null)
		{
			final String skillname = skill.getName();
			player.sendMessage("Admin removed the skill " + skillname + " from your skills list.");
			player.removeSkill(skill);
			// Admin information
			activeChar.sendMessage("You removed the skill " + skillname + " from " + player.getName() + ".");
			
			if (Config.DEBUG)
			{
				LOGGER.debug("[GM]" + activeChar.getName() + " removed skill " + skillname + " from " + player.getName() + ".");
			}
			
			activeChar.sendSkillList();
		}
		else
		{
			activeChar.sendMessage("Error: there is no such skill.");
		}
		
		// Back to previous page
		removeSkillsPage(activeChar, 0);
		
		skill = null;
		player = null;
		target = null;
	}
	
	private void adminAddClanSkill(final L2PcInstance activeChar, final int id, final int level)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			showMainPage(activeChar);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			
			return;
		}
		
		target = null;
		
		if (!player.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(player.getName()));
			showMainPage(activeChar);
			
			return;
		}
		
		if (id < 370 || id > 391 || level < 1 || level > 3)
		{
			activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>");
			showMainPage(activeChar);
			
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill != null)
		{
			String skillname = skill.getName();
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(id);
			player.sendPacket(sm);
			player.getClan().broadcastToOnlineMembers(sm);
			player.getClan().addNewSkill(skill);
			activeChar.sendMessage("You gave the Clan Skill: " + skillname + " to the clan " + player.getClan().getName() + ".");
			
			activeChar.getClan().broadcastToOnlineMembers(new PledgeSkillList(activeChar.getClan()));
			
			for (final L2PcInstance member : activeChar.getClan().getOnlineMembers(""))
			{
				member.sendSkillList();
			}
			
			showMainPage(activeChar);
			skillname = null;
			return;
		}
		activeChar.sendMessage("Error: there is no such skill.");
	}
}
