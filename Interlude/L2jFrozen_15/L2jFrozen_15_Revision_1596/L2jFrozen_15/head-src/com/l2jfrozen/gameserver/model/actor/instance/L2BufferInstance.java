package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.xml.L2BufferSkillsData;
import com.l2jfrozen.gameserver.datatables.xml.L2BufferSkillsData.BuffData;
import com.l2jfrozen.gameserver.managers.SchemeBufferManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.util.StringUtil;

/**
 * @author ReynalDev
 */
public class L2BufferInstance extends L2NpcInstance
{
	private static final Logger LOGGER = Logger.getLogger(L2BufferInstance.class);
	private static final int PAGE_LIMIT = 6;
	
	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/buffer/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.isInFunEvent())
		{
			player.sendMessage("It is not allowed to use the Buffer in events.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can not buff while registered in Olimpiad.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (command.equalsIgnoreCase("heal_all"))
		{
			player.restoreCP();
			player.restoreHPMP();
		}
		else if (command.equalsIgnoreCase("cancel"))
		{
			player.stopAllEffects();
		}
		else if (command.startsWith("buff "))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			if (st.countTokens() == 2) // command should be ---> buff BUFF_KEY
			{
				st.nextToken();
				
				try
				{
					int buffKey = Integer.parseInt(st.nextToken());
					BuffData buff = L2BufferSkillsData.getInstance().getBuffTable().get(buffKey);
					
					if (buff != null)
					{
						if (buff.getSkillTime() > 0)
						{
							giveBuff(player, buff.getSkillId(), buff.getSkillLevel(), buff.getSkillTime());
						}
						else
						{
							giveBuff(player, buff.getSkillId(), buff.getSkillLevel());
						}
					}
					else
					{
						LOGGER.warn("L2BufferInstance.onBypassFeedback: bypass buff, buff key " + buffKey + " do not exist in gameserver/data/xml/bufferSkillData.xml file.");
					}
				}
				catch (Exception e)
				{
					LOGGER.error("L2BufferInstance.onBypassFeedback : invalid bypass buff", e);
				}
			}
		}
		else if (command.startsWith("support"))
		{
			showGiveBuffsWindow(player);
		}
		else if (command.startsWith("createscheme"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			try
			{
				final String schemeName = st.nextToken();
				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
					return;
				}
				
				final Map<String, ArrayList<Integer>> schemes = SchemeBufferManager.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == Config.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}
					
					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}
				
				SchemeBufferManager.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<Integer>());
				showGiveBuffsWindow(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
			}
		}
		else if (command.startsWith("editschemes"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		}
		else if (command.startsWith("skill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String currentCommand = st.nextToken();
			
			final String groupType = st.nextToken();
			final String schemeName = st.nextToken();
			
			final int skillId = Integer.parseInt(st.nextToken());
			final int pageScheme = Integer.parseInt(st.nextToken());
			
			final List<Integer> skills = SchemeBufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
			
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
			{
				if (skills.size() < player.getMaxBuffCount())
				{
					skills.add(skillId);
				}
				else
				{
					player.sendMessage("This scheme has reached the maximum amount of buffs.");
				}
			}
			else if (currentCommand.startsWith("skillunselect"))
			{
				skills.remove(Integer.valueOf(skillId));
			}
			
			showEditSchemeWindow(player, groupType, schemeName, pageScheme);
		}
		else if (command.startsWith("givebuffs"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			final String schemeName = st.nextToken();
			final int cost = Integer.parseInt(st.nextToken());
			
			L2Character target = null;
			if (st.hasMoreTokens())
			{
				final String targetType = st.nextToken();
				if (targetType != null && targetType.equalsIgnoreCase("pet"))
				{
					target = player.getPet();
				}
			}
			else
			{
				target = player;
			}
			
			if (target == null)
			{
				player.sendMessage("You don't have a pet.");
			}
			else if (cost == 0 || player.reduceAdena("NPC Buffer", cost, this, true))
			{
				for (int skillId : SchemeBufferManager.getInstance().getScheme(player.getObjectId(), schemeName))
				{
					SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId, 1)).getEffects(this, target);
				}
			}
		}
		else if (command.startsWith("deletescheme"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			try
			{
				final String schemeName = st.nextToken();
				final Map<String, ArrayList<Integer>> schemes = SchemeBufferManager.getInstance().getPlayerSchemes(player.getObjectId());
				
				if (schemes != null && schemes.containsKey(schemeName))
				{
					schemes.remove(schemeName);
				}
			}
			catch (Exception e)
			{
				player.sendMessage("This scheme name is invalid.");
			}
			showGiveBuffsWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public void giveBuff(L2PcInstance player, int skill_id, int skill_level)
	{
		giveBuff(player, skill_id, skill_level, 0);
	}
	
	public void giveBuff(L2PcInstance player, int skill_id, int skill_level, int skill_time)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(skill_id, skill_level);
		
		if (skill != null)
		{
			if (skill_time >= 1)
			{
				skill.getEffects(null, player, skill_time);
			}
			else
			{
				skill.getEffects(null, player);
			}
		}
	}
	
	/**
	 * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
	 * @param player : The player to make checks on.
	 */
	private void showGiveBuffsWindow(L2PcInstance player)
	{
		final StringBuilder sb = new StringBuilder(200);
		
		final Map<String, ArrayList<Integer>> schemes = SchemeBufferManager.getInstance().getPlayerSchemes(player.getObjectId());
		if (schemes == null || schemes.isEmpty())
		{
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		}
		else
		{
			for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet())
			{
				final int cost = getFee(scheme.getValue());
				StringUtil.append(sb, "<font color=\"LEVEL\">", scheme.getKey(), " [", scheme.getValue().size(), " / ", player.getMaxBuffCount(), "]", ((cost > 0) ? " - cost: " + StringUtil.formatNumber(cost) : ""), "</font><br1>");
				StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, "\">Use on Me</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, " pet\">Use on Pet</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_editschemes Buffs ", scheme.getKey(), " 1\">Edit</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_deletescheme ", scheme.getKey(), "\">Delete</a><br>");
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(getHtmlPath(getNpcId(), 8));
		html.replace("%schemes%", sb.toString());
		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	/**
	 * @param  list : A list of skill ids.
	 * @return      a global fee for all skills contained in list.
	 */
	private static int getFee(ArrayList<Integer> list)
	{
		if (Config.BUFFER_STATIC_BUFF_COST > 0)
		{
			return list.size() * Config.BUFFER_STATIC_BUFF_COST;
		}
		
		int fee = 0;
		for (int sk : list)
		{
			fee += SchemeBufferManager.getInstance().getAvailableBuff(sk).getValue();
		}
		
		return fee;
	}
	
	/**
	 * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
	 * @param player     : The player to make checks on.
	 * @param groupType  : The group of skills to select.
	 * @param schemeName : The scheme to make check.
	 * @param page       The page.
	 */
	private void showEditSchemeWindow(L2PcInstance player, String groupType, String schemeName, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		final List<Integer> schemeSkills = SchemeBufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
		
		html.setFile(getHtmlPath(getNpcId(), 9));
		html.replace("%schemename%", schemeName);
		html.replace("%count%", schemeSkills.size() + " / " + player.getMaxBuffCount());
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	/**
	 * @param  groupType  : The group of skills to select.
	 * @param  schemeName : The scheme to make check.
	 * @return            a string representing all groupTypes available. The group currently on selection isn't linkable.
	 */
	private static String getTypesFrame(String groupType, String schemeName)
	{
		final StringBuilder sb = new StringBuilder(500);
		sb.append("<table>");
		
		int count = 0;
		for (String type : SchemeBufferManager.getInstance().getSkillTypes())
		{
			if (count == 0)
			{
				sb.append("<tr>");
			}
			
			if (groupType.equalsIgnoreCase(type))
			{
				StringUtil.append(sb, "<td width=65>", type, "</td>");
			}
			else
			{
				StringUtil.append(sb, "<td width=65><a action=\"bypass -h npc_%objectId%_editschemes ", type, " ", schemeName, " 1\">", type, "</a></td>");
			}
			
			count++;
			if (count == 4)
			{
				sb.append("</tr>");
				count = 0;
			}
		}
		
		if (!sb.toString().endsWith("</tr>"))
		{
			sb.append("</tr>");
		}
		
		sb.append("</table>");
		
		return sb.toString();
	}
	
	/**
	 * @param  player     : The player to make checks on.
	 * @param  groupType  : The group of skills to select.
	 * @param  schemeName : The scheme to make check.
	 * @param  page       The page.
	 * @return            a String representing skills available to selection for a given groupType.
	 */
	private String getGroupSkillList(L2PcInstance player, String groupType, String schemeName, int page)
	{
		// Retrieve the entire skills list based on group type.
		List<Integer> skills = SchemeBufferManager.getInstance().getSkillsIdsByType(groupType);
		if (skills.isEmpty())
		{
			return "That group doesn't contain any skills.";
		}
		
		// Calculate page number.
		final int max = countPagesNumber(skills.size(), PAGE_LIMIT);
		if (page > max)
		{
			page = max;
		}
		
		// Cut skills list up to page number.
		skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));
		
		final List<Integer> schemeSkills = SchemeBufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
		final StringBuilder sb = new StringBuilder(skills.size() * 150);
		
		int row = 0;
		for (int skillId : skills)
		{
			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));
			
			if (skillId < 100)
			{
				if (schemeSkills.contains(skillId))
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				}
				else
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
				}
			}
			else if (skillId < 1000)
			{
				if (schemeSkills.contains(skillId))
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				}
				else
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
				}
			}
			else
			{
				if (schemeSkills.contains(skillId))
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
				}
				else
				{
					StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", SchemeBufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h npc_%objectId%_skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
				}
			}
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}
		
		// Build page footer.
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		
		if (page > 1)
		{
			StringUtil.append(sb, "<td align=left width=70><a action=\"bypass -h npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page - 1, "\">Previous</a></td>");
		}
		else
		{
			StringUtil.append(sb, "<td align=left width=70>Previous</td>");
		}
		
		StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>");
		
		if (page < max)
		{
			StringUtil.append(sb, "<td align=right width=70><a action=\"bypass -h npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page + 1, "\">Next</a></td>");
		}
		else
		{
			StringUtil.append(sb, "<td align=right width=70>Next</td>");
		}
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		
		return sb.toString();
	}
	
	/**
	 * @param  objectsSize : The overall elements size.
	 * @param  pageSize    : The number of elements per page.
	 * @return             The number of pages, based on the number of elements and the number of elements we want per page.
	 */
	public static int countPagesNumber(int objectsSize, int pageSize)
	{
		return objectsSize / pageSize + (objectsSize % pageSize == 0 ? 0 : 1);
	}
}
