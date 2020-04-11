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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2DropCategory;
import l2r.gameserver.model.L2DropData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.MoveType;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author terry con.close() change by Zoey76 24/02/2011
 */
public class AdminEditNpc implements IAdminCommandHandler
{
	private static Logger _log = LoggerFactory.getLogger(AdminEditNpc.class);
	private final static int PAGE_LIMIT = 20;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_npc",
		"admin_save_npc",
		"admin_show_droplist",
		"admin_edit_drop",
		"admin_add_drop",
		"admin_del_drop",
		"admin_close_window",
		"admin_show_skilllist_npc",
		"admin_add_skill_npc",
		"admin_edit_skill_npc",
		"admin_del_skill_npc",
		"admin_log_npc_spawn"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		// TODO: Tokenize and protect arguments parsing. Externalize HTML.
		if (command.startsWith("admin_log_npc_spawn"))
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) target;
				_log.info("('',1," + npc.getId() + "," + npc.getX() + "," + npc.getY() + "," + npc.getZ() + ",0,0," + npc.getHeading() + ",60,0,0),");
			}
		}
		else if (command.startsWith("admin_edit_npc "))
		{
			try
			{
				String[] commandSplit = command.split(" ");
				int npcId = Integer.parseInt(commandSplit[1]);
				L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
				showNpcProperty(activeChar, npc);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Wrong usage: //edit_npc <npcId>");
			}
		}
		else if (command.startsWith("admin_show_droplist "))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int page = 1;
				if (st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				showNpcDropList(activeChar, npcId, page);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_droplist <npc_id> [<page>]");
			}
		}
		else if (command.startsWith("admin_save_npc "))
		{
			try
			{
				saveNpcProperty(activeChar, command);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_edit_drop "))
		{
			int npcId = -1, itemId = 0, category = -1000;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(16).trim());
				if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						showEditDropData(activeChar, npcId, itemId, category);
					}
					catch (Exception e)
					{
						_log.warn("", e);
					}
				}
				else if (st.countTokens() == 6)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());
						
						updateDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						_log.error("admin_edit_drop parameters error: " + command);
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
			}
		}
		else if (command.startsWith("admin_add_drop "))
		{
			int npcId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(15).trim());
				if (st.countTokens() == 1)
				{
					try
					{
						String[] input = command.substring(15).split(" ");
						if (input.length < 1)
						{
							return true;
						}
						npcId = Integer.parseInt(input[0]);
					}
					catch (Exception e)
					{
					}
					
					if (npcId > 0)
					{
						L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
						showAddDropData(activeChar, npcData);
					}
				}
				else if (st.countTokens() == 6)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						int itemId = Integer.parseInt(st.nextToken());
						int category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());
						
						addDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						_log.error("admin_add_drop parameters error: " + command);
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
			}
		}
		else if (command.startsWith("admin_del_drop "))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int itemId = Integer.parseInt(st.nextToken());
				int category = Integer.parseInt(st.nextToken());
				boolean confirmed = false;
				if (st.hasMoreTokens())
				{
					confirmed = true;
				}
				deleteDropData(activeChar, npcId, itemId, category, confirmed);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //del_drop <npc_id> <item_id> <category>");
			}
		}
		else if (command.startsWith("admin_show_skilllist_npc "))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				int npcId = Integer.parseInt(st.nextToken());
				int page = 0;
				if (st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				showNpcSkillList(activeChar, npcId, page);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
			}
		}
		else if (command.startsWith("admin_edit_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				if (!st.hasMoreTokens())
				{
					showNpcSkillEdit(activeChar, npcId, skillId);
				}
				else
				{
					int level = Integer.parseInt(st.nextToken());
					updateNpcSkillData(activeChar, npcId, skillId, level);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		}
		else if (command.startsWith("admin_add_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				if (!st.hasMoreTokens())
				{
					showNpcSkillAdd(activeChar, npcId);
				}
				else
				{
					int skillId = Integer.parseInt(st.nextToken());
					int level = Integer.parseInt(st.nextToken());
					addNpcSkillData(activeChar, npcId, skillId, level);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<skill_id> <level>]");
			}
		}
		else if (command.startsWith("admin_del_skill_npc "))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				deleteNpcSkillData(activeChar, npcId, skillId);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //del_skill_npc <npc_id> <skill_id>");
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showNpcProperty(L2PcInstance activeChar, L2NpcTemplate npc)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/admin/editnpc.htm");
		
		if (content != null)
		{
			adminReply.setHtml(content);
			adminReply.replace("%npcId%", String.valueOf(npc.getId()));
			adminReply.replace("%templateId%", String.valueOf(npc.getDisplayId()));
			adminReply.replace("%name%", npc.getName());
			adminReply.replace("%serverSideName%", npc.isUsingServerSideName() == true ? "1" : "0");
			adminReply.replace("%title%", npc.getTitle());
			adminReply.replace("%serverSideTitle%", npc.isUsingServerSideTitle() == true ? "1" : "0");
			adminReply.replace("%collisionRadius%", String.valueOf(npc.getfCollisionRadius()));
			adminReply.replace("%collisionHeight%", String.valueOf(npc.getfCollisionHeight()));
			adminReply.replace("%level%", String.valueOf(npc.getLevel()));
			adminReply.replace("%sex%", String.valueOf(npc.getSex()));
			adminReply.replace("%type%", String.valueOf(npc.getType()));
			adminReply.replace("%attackRange%", String.valueOf(npc.getBaseAttackRange()));
			adminReply.replace("%hp%", String.valueOf(npc.getBaseHpMax()));
			adminReply.replace("%mp%", String.valueOf(npc.getBaseMpMax()));
			adminReply.replace("%hpRegen%", String.valueOf(npc.getBaseHpReg()));
			adminReply.replace("%mpRegen%", String.valueOf(npc.getBaseMpReg()));
			adminReply.replace("%str%", String.valueOf(npc.getBaseSTR()));
			adminReply.replace("%con%", String.valueOf(npc.getBaseCON()));
			adminReply.replace("%dex%", String.valueOf(npc.getBaseDEX()));
			adminReply.replace("%int%", String.valueOf(npc.getBaseINT()));
			adminReply.replace("%wit%", String.valueOf(npc.getBaseWIT()));
			adminReply.replace("%men%", String.valueOf(npc.getBaseMEN()));
			adminReply.replace("%exp%", String.valueOf(npc.getRewardExp()));
			adminReply.replace("%sp%", String.valueOf(npc.getRewardSp()));
			adminReply.replace("%pAtk%", String.valueOf(npc.getBasePAtk()));
			adminReply.replace("%pDef%", String.valueOf(npc.getBasePDef()));
			adminReply.replace("%mAtk%", String.valueOf(npc.getBaseMAtk()));
			adminReply.replace("%mDef%", String.valueOf(npc.getBaseMDef()));
			adminReply.replace("%pAtkSpd%", String.valueOf(npc.getBasePAtkSpd()));
			adminReply.replace("%aggro%", String.valueOf(npc.getAIDataStatic().getAggroRange()));
			adminReply.replace("%mAtkSpd%", String.valueOf(npc.getBaseMAtkSpd()));
			adminReply.replace("%rHand%", String.valueOf(npc.getRHandId()));
			adminReply.replace("%lHand%", String.valueOf(npc.getLHandId()));
			adminReply.replace("%enchant%", String.valueOf(npc.getWeaponEnchant()));
			adminReply.replace("%walkspd%", String.valueOf(npc.getBaseMoveSpeed(MoveType.WALK)));
			adminReply.replace("%runspd%", String.valueOf(npc.getBaseMoveSpeed(MoveType.RUN)));
			adminReply.replace("%factionId%", npc.getAIDataStatic().getClan() == null ? "" : npc.getAIDataStatic().getClan());
			adminReply.replace("%factionRange%", String.valueOf(npc.getAIDataStatic().getClanRange()));
		}
		else
		{
			adminReply.setHtml("<html><head><body>File not found: data/html/admin/editnpc.htm</body></html>");
		}
		activeChar.sendPacket(adminReply);
	}
	
	private void saveNpcProperty(L2PcInstance activeChar, String command)
	{
		String[] commandSplit = command.split(" ");
		
		if (commandSplit.length < 4)
		{
			return;
		}
		
		StatsSet newNpcData = new StatsSet();
		try
		{
			newNpcData.set("npcId", commandSplit[1]);
			
			String statToSet = commandSplit[2];
			String value = commandSplit[3];
			
			if (commandSplit.length > 4)
			{
				for (int i = 0; i < (commandSplit.length - 3); i++)
				{
					value += " " + commandSplit[i + 4];
				}
			}
			
			if (statToSet.equals("templateId"))
			{
				newNpcData.set("idTemplate", Integer.parseInt(value));
			}
			else if (statToSet.equals("name"))
			{
				newNpcData.set("name", value);
			}
			else if (statToSet.equals("serverSideName"))
			{
				newNpcData.set("serverSideName", Integer.parseInt(value));
			}
			else if (statToSet.equals("title"))
			{
				newNpcData.set("title", value);
			}
			else if (statToSet.equals("serverSideTitle"))
			{
				newNpcData.set("serverSideTitle", Integer.parseInt(value) == 1 ? 1 : 0);
			}
			else if (statToSet.equals("collisionRadius"))
			{
				newNpcData.set("collision_radius", Integer.parseInt(value));
			}
			else if (statToSet.equals("collisionHeight"))
			{
				newNpcData.set("collision_height", Integer.parseInt(value));
			}
			else if (statToSet.equals("level"))
			{
				newNpcData.set("level", Integer.parseInt(value));
			}
			else if (statToSet.equals("sex"))
			{
				int intValue = Integer.parseInt(value);
				newNpcData.set("sex", intValue == 0 ? "male" : intValue == 1 ? "female" : "etc");
			}
			else if (statToSet.equals("type"))
			{
				Class.forName("l2r.gameserver.model.actor.instance." + value + "Instance");
				newNpcData.set("type", value);
			}
			else if (statToSet.equals("attackRange"))
			{
				newNpcData.set("attackrange", Integer.parseInt(value));
			}
			else if (statToSet.equals("hp"))
			{
				newNpcData.set("hp", Integer.parseInt(value));
			}
			else if (statToSet.equals("mp"))
			{
				newNpcData.set("mp", Integer.parseInt(value));
			}
			else if (statToSet.equals("hpRegen"))
			{
				newNpcData.set("hpreg", Integer.parseInt(value));
			}
			else if (statToSet.equals("mpRegen"))
			{
				newNpcData.set("mpreg", Integer.parseInt(value));
			}
			else if (statToSet.equals("str"))
			{
				newNpcData.set("str", Integer.parseInt(value));
			}
			else if (statToSet.equals("con"))
			{
				newNpcData.set("con", Integer.parseInt(value));
			}
			else if (statToSet.equals("dex"))
			{
				newNpcData.set("dex", Integer.parseInt(value));
			}
			else if (statToSet.equals("int"))
			{
				newNpcData.set("int", Integer.parseInt(value));
			}
			else if (statToSet.equals("wit"))
			{
				newNpcData.set("wit", Integer.parseInt(value));
			}
			else if (statToSet.equals("men"))
			{
				newNpcData.set("men", Integer.parseInt(value));
			}
			else if (statToSet.equals("exp"))
			{
				newNpcData.set("exp", Integer.parseInt(value));
			}
			else if (statToSet.equals("sp"))
			{
				newNpcData.set("sp", Integer.parseInt(value));
			}
			else if (statToSet.equals("pAtk"))
			{
				newNpcData.set("patk", Integer.parseInt(value));
			}
			else if (statToSet.equals("pDef"))
			{
				newNpcData.set("pdef", Integer.parseInt(value));
			}
			else if (statToSet.equals("mAtk"))
			{
				newNpcData.set("matk", Integer.parseInt(value));
			}
			else if (statToSet.equals("mDef"))
			{
				newNpcData.set("mdef", Integer.parseInt(value));
			}
			else if (statToSet.equals("pAtkSpd"))
			{
				newNpcData.set("atkspd", Integer.parseInt(value));
			}
			else if (statToSet.equals("aggro"))
			{
				newNpcData.set("aggro", Integer.parseInt(value));
			}
			else if (statToSet.equals("mAtkSpd"))
			{
				newNpcData.set("matkspd", Integer.parseInt(value));
			}
			else if (statToSet.equals("rHand"))
			{
				newNpcData.set("rhand", Integer.parseInt(value));
			}
			else if (statToSet.equals("lHand"))
			{
				newNpcData.set("lhand", Integer.parseInt(value));
			}
			else if (statToSet.equals("armor"))
			{
				newNpcData.set("armor", Integer.parseInt(value));
			}
			else if (statToSet.equals("enchant"))
			{
				newNpcData.set("enchant", Integer.parseInt(value));
			}
			else if (statToSet.equals("runSpd"))
			{
				newNpcData.set("runspd", Integer.parseInt(value));
			}
			else if (statToSet.equals("isUndead"))
			{
				newNpcData.set("isUndead", Integer.parseInt(value) == 1 ? 1 : 0);
			}
			else if (statToSet.equals("absorbLevel"))
			{
				int intVal = Integer.parseInt(value);
				newNpcData.set("absorb_level", intVal < 0 ? 0 : intVal > 16 ? 0 : intVal);
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not save npc property!");
			_log.warn("Error saving new npc value (" + command + "): " + e);
		}
		
		NpcTable.getInstance().saveNpc(newNpcData);
		
		int npcId = newNpcData.getInt("npcId");
		
		NpcTable.getInstance().reloadNpc(npcId);
		showNpcProperty(activeChar, NpcTable.getInstance().getTemplate(npcId));
	}
	
	private void showNpcDropList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Unknown npc template id " + npcId);
			return;
		}
		
		final StringBuilder replyMSG = new StringBuilder(2900);
		replyMSG.append("<html><title>Show droplist page ");
		replyMSG.append(page);
		replyMSG.append("</title><body><br1><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcId);
		replyMSG.append(")</font></center><br1><table width=\"100%\" border=0><tr><td width=35>cat.</td><td width=210>item</td><td width=30>type</td><td width=25>del</td></tr>");
		
		int myPage = 1;
		int i = 0;
		int shown = 0;
		boolean hasMore = false;
		if (npcData.getDropData() != null)
		{
			for (L2DropCategory cat : npcData.getDropData())
			{
				if (shown == PAGE_LIMIT)
				{
					hasMore = true;
					break;
				}
				for (L2DropData drop : cat.getAllDrops())
				{
					if (myPage != page)
					{
						i++;
						if (i == PAGE_LIMIT)
						{
							myPage++;
							i = 0;
						}
						continue;
					}
					if (shown == PAGE_LIMIT)
					{
						hasMore = true;
						break;
					}
					
					replyMSG.append("<tr><td>");
					replyMSG.append(cat.getCategoryType());
					replyMSG.append("</td><td><a action=\"bypass -h admin_edit_drop ");
					replyMSG.append(npcId);
					replyMSG.append(" ");
					replyMSG.append(drop.getId());
					replyMSG.append(" ");
					replyMSG.append(cat.getCategoryType());
					replyMSG.append("\">");
					replyMSG.append(ItemData.getInstance().getTemplate(drop.getId()).getName());
					replyMSG.append(" (");
					replyMSG.append(drop.getId());
					replyMSG.append(")</a></td><td>");
					replyMSG.append((drop.isQuestDrop() ? "Q" : (cat.isSweep() ? "S" : "D")));
					replyMSG.append("</td><td><a action=\"bypass -h admin_del_drop ");
					replyMSG.append(npcId);
					replyMSG.append(" ");
					replyMSG.append(drop.getId());
					replyMSG.append(" ");
					replyMSG.append(cat.getCategoryType());
					replyMSG.append("\">del</a></td></tr>");
					shown++;
				}
			}
		}
		
		replyMSG.append("</table>");
		
		if (page > 1)
		{
			replyMSG.append("<table width=300 bgcolor=666666 border=0><tr>");
			
			replyMSG.append("<td width=120><a action=\"bypass -h admin_show_droplist ");
			replyMSG.append(npcId);
			replyMSG.append(" ");
			replyMSG.append(page - 1);
			replyMSG.append("\">Prev Page</a></td>");
			if (!hasMore)
			{
				replyMSG.append("<td width=100>Page ");
				replyMSG.append(page);
				replyMSG.append("</td><td width=70></td></tr>");
			}
			
			replyMSG.append("</table>");
		}
		if (hasMore)
		{
			replyMSG.append("<table width=300 bgcolor=666666 border=0><tr>");
			
			if (page <= 1)
			{
				replyMSG.append("<td width=120></td>");
			}
			replyMSG.append("<td width=100>Page ");
			replyMSG.append(page);
			replyMSG.append("</td><td width=70><a action=\"bypass -h admin_show_droplist ");
			replyMSG.append(npcId);
			replyMSG.append(" ");
			replyMSG.append(page + 1);
			replyMSG.append("\">Next Page</a></td></tr>");
			
			replyMSG.append("</table>");
		}
		
		replyMSG.append("<center><br><button value=\"Add Drop Data\" action=\"bypass -h admin_add_drop ");
		replyMSG.append(npcId);
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showEditDropData(L2PcInstance activeChar, int npcId, int itemId, int category)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Unknown npc template id " + npcId);
			return;
		}
		
		L2Item itemData = ItemData.getInstance().getTemplate(itemId);
		if (itemData == null)
		{
			activeChar.sendMessage("Unknown item template id " + itemId);
			return;
		}
		
		final StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Edit drop data</title><body>");
		
		List<L2DropData> dropDatas = null;
		if (npcData.getDropData() != null)
		{
			for (L2DropCategory dropCat : npcData.getDropData())
			{
				if (dropCat.getCategoryType() == category)
				{
					dropDatas = dropCat.getAllDrops();
					break;
				}
			}
		}
		
		L2DropData dropData = null;
		if (dropDatas != null)
		{
			for (L2DropData drop : dropDatas)
			{
				if (drop.getId() == itemId)
				{
					dropData = drop;
					break;
				}
			}
		}
		
		if (dropData != null)
		{
			replyMSG.append("<table width=\"100%\"><tr><td>Npc</td><td>");
			replyMSG.append(npcData.getName());
			replyMSG.append(" (");
			replyMSG.append(npcId);
			replyMSG.append(")</td></tr><tr><td>Item</td><td>");
			replyMSG.append(itemData.getName());
			replyMSG.append(" (");
			replyMSG.append(itemId);
			replyMSG.append(")</td></tr><tr><td>Category</td><td>");
			replyMSG.append(((category == -1) ? "-1 (sweep)" : Integer.toString(category)));
			replyMSG.append("</td></tr>");
			replyMSG.append("<tr><td>Min count (");
			replyMSG.append(dropData.getMinDrop());
			replyMSG.append(")</td><td><edit var=\"min\" width=80></td></tr><tr><td>Max count (");
			replyMSG.append(dropData.getMaxDrop());
			replyMSG.append(")</td><td><edit var=\"max\" width=80></td></tr><tr><td>Chance (");
			replyMSG.append(dropData.getChance());
			replyMSG.append(")</td><td><edit var=\"chance\" width=80></td></tr></table><br>");
			
			replyMSG.append("<center><br><button value=\"Save\" action=\"bypass -h admin_edit_drop ");
			replyMSG.append(npcId);
			replyMSG.append(" ");
			replyMSG.append(itemId);
			replyMSG.append(" ");
			replyMSG.append(category);
			replyMSG.append(" $min $max $chance\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			replyMSG.append("No drop data detail found.<center><br>");
		}
		replyMSG.append("<button value=\"Back to Droplist\" action=\"bypass -h admin_show_droplist ");
		replyMSG.append(npcId);
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		replyMSG.append("</body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showAddDropData(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		final String replyMSG = StringUtil.concat("<html><title>Add drop data</title><body><table width=\"100%\"><tr><td>Npc</td><td>", npcData.getName(), " (", Integer.toString(npcData.getId()), ")", "</td></tr><tr><td>Item Id</td><td><edit var=\"itemId\" width=80></td></tr><tr><td>Min count</td><td><edit var=\"min\" width=80></td></tr><tr><td>Max count</td><td><edit var=\"max\" width=80></td></tr><tr><td>Category (sweep=-1)</td><td><edit var=\"category\" width=80></td></tr><tr><td>Chance (0-1000000)</td><td><edit var=\"chance\" width=80></td></tr></table><center><br><button value=\"Add\" action=\"bypass -h admin_add_drop ", Integer.toString(npcData.getId()), " $itemId $category $min $max $chance\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back to Droplist\" action=\"bypass -h admin_show_droplist ", Integer.toString(npcData.getId()), "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}
	
	private void updateDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int updated = 0;
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE `custom_droplist` SET `min`=?, `max`=?, `chance`=? WHERE `mobId`=? AND `itemId`=? AND `category`=?"))
				{
					statement.setInt(1, min);
					statement.setInt(2, max);
					statement.setInt(3, chance);
					statement.setInt(4, npcId);
					statement.setInt(5, itemId);
					statement.setInt(6, category);
					
					updated = statement.executeUpdate();
				}
			}
			if (updated == 0)
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE `droplist` SET `min`=?, `max`=?, `chance`=? WHERE `mobId`=? AND `itemId`=? AND `category`=?"))
				{
					statement.setInt(1, min);
					statement.setInt(2, max);
					statement.setInt(3, chance);
					statement.setInt(4, npcId);
					statement.setInt(5, itemId);
					statement.setInt(6, category);
					
					updated = statement.executeUpdate();
				}
			}
			
			reloadNpcDropList(npcId);
			
			showNpcDropList(activeChar, npcId, 1);
			activeChar.sendMessage("Updated drop data for npc id " + npcId + " and item id " + itemId + " in category " + category + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not update drop data!");
			_log.warn("Error while updating drop data (" + npcId + ", " + itemId + ", " + min + ", " + max + ", " + category + ", " + chance + "): " + e);
		}
	}
	
	private void addDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		String table = "droplist";
		if (Config.CUSTOM_DROPLIST_TABLE)
		{
			table = "custom_droplist";
		}
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `" + table + "`(`mobId`, `itemId`, `min`, `max`, `category`, `chance`) VALUES(?,?,?,?,?,?)"))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, itemId);
			statement.setInt(3, min);
			statement.setInt(4, max);
			statement.setInt(5, category);
			statement.setInt(6, chance);
			statement.execute();
			statement.close();
			
			reloadNpcDropList(npcId);
			
			showNpcDropList(activeChar, npcId, 1);
			activeChar.sendMessage("Added drop data for npc id " + npcId + " with item id " + itemId + " in category " + category + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not add drop data!");
			_log.warn("Error while adding drop data (" + npcId + ", " + itemId + ", " + min + ", " + max + ", " + category + ", " + chance + "): " + e);
		}
	}
	
	private void deleteDropData(L2PcInstance activeChar, int npcId, int itemId, int category, boolean confirmed)
	{
		if (!confirmed)
		{
			final String replyMSG = StringUtil.concat("<html><title>Drop Data Delete</title><body><br>Delete drop data.", "<table width=\"100%\"><tr><td>NPC</td><td>", NpcTable.getInstance().getTemplate(npcId).getName(), " (", Integer.toString(npcId), ")</td></tr><tr><td>Item ID</td><td>", Integer.toString(itemId), "</td></tr><tr><td>Category</td><td>", Integer.toString(category), "</td></tr></table><center><br><button value=\"Delete\" action=\"bypass -h admin_del_drop ", Integer.toString(npcId), " ", Integer.toString(itemId), " ", Integer.toString(category), " 1\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back to Droplist\" action=\"bypass -h admin_show_droplist ", Integer.toString(npcId), "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage();
			adminReply.setHtml(replyMSG);
			activeChar.sendPacket(adminReply);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int updated = 0;
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				try (PreparedStatement statement = con.prepareStatement("DELETE FROM `custom_droplist` WHERE `mobId`=? AND `itemId`=? AND `category`=?"))
				{
					statement.setInt(1, npcId);
					statement.setInt(2, itemId);
					statement.setInt(3, category);
					updated = statement.executeUpdate();
				}
			}
			if (updated == 0)
			{
				try (PreparedStatement statement = con.prepareStatement("DELETE FROM `droplist` WHERE `mobId`=? AND `itemId`=? AND `category`=?"))
				{
					statement.setInt(1, npcId);
					statement.setInt(2, itemId);
					statement.setInt(3, category);
					updated = statement.executeUpdate();
				}
			}
			
			reloadNpcDropList(npcId);
			
			showNpcDropList(activeChar, npcId, 1);
			activeChar.sendMessage("Deleted drop data for npc id " + npcId + " and item id " + itemId + " in category " + category + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not delete drop data!");
			_log.warn("Error while deleting drop data (" + npcId + ", " + itemId + ", " + category + "): " + e);
		}
	}
	
	private void reloadNpcDropList(int npcId)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			return;
		}
		
		// reset the drop lists
		npcData.clearAllDropData();
		
		// get the drops
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2DropData dropData = null;
			try (PreparedStatement statement = con.prepareStatement("SELECT `mobId`, `itemId`, `min`, `max`, `category`, `chance` FROM `droplist` WHERE `mobId`=?"))
			{
				statement.setInt(1, npcId);
				try (ResultSet dropDataList = statement.executeQuery())
				{
					while (dropDataList.next())
					{
						dropData = new L2DropData();
						
						dropData.setItemId(dropDataList.getInt("itemId"));
						dropData.setMinDrop(dropDataList.getInt("min"));
						dropData.setMaxDrop(dropDataList.getInt("max"));
						dropData.setChance(dropDataList.getInt("chance"));
						
						int category = dropDataList.getInt("category");
						npcData.addDropData(dropData, category);
					}
				}
			}
			
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				try (PreparedStatement statement2 = con.prepareStatement("SELECT `mobId`, `itemId`, `min`, `max`, `category`, `chance` FROM `custom_droplist` WHERE `mobId`=?"))
				{
					statement2.setInt(1, npcId);
					try (ResultSet dropDataList2 = statement2.executeQuery())
					{
						while (dropDataList2.next())
						{
							dropData = new L2DropData();
							
							dropData.setItemId(dropDataList2.getInt("itemId"));
							dropData.setMinDrop(dropDataList2.getInt("min"));
							dropData.setMaxDrop(dropDataList2.getInt("max"));
							dropData.setChance(dropDataList2.getInt("chance"));
							
							int category = dropDataList2.getInt("category");
							npcData.addDropData(dropData, category);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error while reloading npc droplist (" + npcId + "): " + e);
		}
	}
	
	private void showNpcSkillList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Template id unknown: " + npcId);
			return;
		}
		
		Map<Integer, L2Skill> skills = new HashMap<>(npcData.getSkills());
		int _skillsize = skills.size();
		
		int MaxSkillsPerPage = PAGE_LIMIT;
		int MaxPages = _skillsize / MaxSkillsPerPage;
		if (_skillsize > (MaxSkillsPerPage * MaxPages))
		{
			MaxPages++;
		}
		
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = _skillsize;
		if ((SkillsEnd - SkillsStart) > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}
		
		StringBuffer replyMSG = new StringBuffer("<html><title>Show NPC Skill List</title><body><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.getId());
		replyMSG.append("): ");
		replyMSG.append(_skillsize);
		replyMSG.append(" skills</font></center><table width=300 bgcolor=666666><tr>");
		
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			if (page == x)
			{
				replyMSG.append("<td>Page ");
				replyMSG.append(pagenr);
				replyMSG.append("</td>");
			}
			else
			{
				replyMSG.append("<td><a action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcData.getId());
				replyMSG.append(" ");
				replyMSG.append(x);
				replyMSG.append("\"> Page ");
				replyMSG.append(pagenr);
				replyMSG.append(" </a></td>");
			}
		}
		replyMSG.append("</tr></table><table width=\"100%\" border=0><tr><td>Skill name [skill id-skill lvl]</td><td>Delete</td></tr>");
		Iterator<L2Skill> skillite = skills.values().iterator();
		
		for (int i = 0; i < SkillsStart; i++)
		{
			if (skillite.hasNext())
			{
				skillite.next();
			}
		}
		
		int cnt = SkillsStart;
		L2Skill sk;
		while (skillite.hasNext())
		{
			cnt++;
			if (cnt > SkillsEnd)
			{
				break;
			}
			
			sk = skillite.next();
			replyMSG.append("<tr><td width=240><a action=\"bypass -h admin_edit_skill_npc ");
			replyMSG.append(npcData.getId());
			replyMSG.append(" ");
			replyMSG.append(sk.getId());
			replyMSG.append("\">");
			if (sk.getSkillType() == L2SkillType.NOTDONE)
			{
				replyMSG.append("<font color=\"777777\">" + sk.getName() + "</font>");
			}
			else
			{
				replyMSG.append(sk.getName());
			}
			replyMSG.append(" [");
			replyMSG.append(sk.getId());
			replyMSG.append("-");
			replyMSG.append(sk.getLevel());
			replyMSG.append("]</a></td><td width=60><a action=\"bypass -h admin_del_skill_npc ");
			replyMSG.append(npcData.getId());
			replyMSG.append(" ");
			replyMSG.append(sk.getId());
			replyMSG.append("\">Delete</a></td></tr>");
		}
		replyMSG.append("</table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcId);
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showNpcSkillEdit(L2PcInstance activeChar, int npcId, int skillId)
	{
		try
		{
			StringBuffer replyMSG = new StringBuffer("<html><title>NPC Skill Edit</title><body>");
			
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
			if (npcData == null)
			{
				activeChar.sendMessage("Template id unknown: " + npcId);
				return;
			}
			if (npcData.getSkills() == null)
			{
				return;
			}
			
			L2Skill npcSkill = npcData.getSkills().get(skillId);
			
			if (npcSkill != null)
			{
				replyMSG.append("<table width=\"100%\"><tr><td>NPC: </td><td>");
				replyMSG.append(NpcTable.getInstance().getTemplate(npcId).getName());
				replyMSG.append(" (");
				replyMSG.append(npcId);
				replyMSG.append(")</td></tr><tr><td>Skill: </td><td>");
				replyMSG.append(npcSkill.getName());
				replyMSG.append(" (");
				replyMSG.append(skillId);
				replyMSG.append(")</td></tr><tr><td>Skill Lvl: (");
				replyMSG.append(npcSkill.getLevel());
				replyMSG.append(") </td><td><edit var=\"level\" width=50></td></tr></table><br><center><button value=\"Save\" action=\"bypass -h admin_edit_skill_npc ");
				replyMSG.append(npcId);
				replyMSG.append(" ");
				replyMSG.append(skillId);
				replyMSG.append(" $level\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcId);
				replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
			}
			
			replyMSG.append("</body></html>");
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage();
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not edit npc skills!");
			_log.warn("Error while editing npc skills (" + npcId + ", " + skillId + "): " + e);
		}
	}
	
	private void updateNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2Skill skillData = SkillData.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{
				activeChar.sendMessage("Could not update npc skill: not existing skill id with that level!");
				showNpcSkillEdit(activeChar, npcId, skillId);
				return;
			}
			
			if (skillData.getLevel() != level)
			{
				activeChar.sendMessage("Skill id with requested level doesn't exist! Skill level not changed.");
				showNpcSkillEdit(activeChar, npcId, skillId);
				return;
			}
			
			int updated = 0;
			if (Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				try (PreparedStatement statement2 = con.prepareStatement("UPDATE `custom_npcskills` SET `level`=? WHERE `npcid`=? AND `skillid`=?"))
				{
					statement2.setInt(1, level);
					statement2.setInt(2, npcId);
					statement2.setInt(3, skillId);
					
					updated = statement2.executeUpdate();
				}
			}
			if (updated == 0)
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE `npcskills` SET `level`=? WHERE `npcid`=? AND `skillid`=?"))
				{
					statement.setInt(1, level);
					statement.setInt(2, npcId);
					statement.setInt(3, skillId);
					
					statement.execute();
				}
			}
			reloadNpcSkillList(npcId);
			
			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Updated skill id " + skillId + " for npc id " + npcId + " to level " + level + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not update npc skill!");
			_log.warn("Error while updating npc skill (" + npcId + ", " + skillId + ", " + level + "): " + e);
		}
	}
	
	private void showNpcSkillAdd(L2PcInstance activeChar, int npcId)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		
		StringBuffer replyMSG = new StringBuffer("<html><title>NPC Skill Add</title><body><table width=\"100%\"><tr><td>NPC: </td><td>");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.getId());
		replyMSG.append(")</td></tr><tr><td>SkillId: </td><td><edit var=\"skillId\" width=80></td></tr><tr><td>Level: </td><td><edit var=\"level\" width=80></td></tr></table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcData.getId());
		replyMSG.append(" $skillId $level\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
		replyMSG.append(npcData.getId());
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void addNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// skill check
			L2Skill skillData = SkillData.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{
				activeChar.sendMessage("Could not add npc skill: not existing skill id with that level!");
				showNpcSkillAdd(activeChar, npcId);
				return;
			}
			
			if (Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO `custom_npcskills`(`npcid`, `skillid`, `level`) VALUES(?,?,?)"))
				{
					statement.setInt(1, npcId);
					statement.setInt(2, skillId);
					statement.setInt(3, level);
					statement.execute();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO `npcskills`(`npcid`, `skillid`, `level`) VALUES(?,?,?)"))
				{
					statement.setInt(1, npcId);
					statement.setInt(2, skillId);
					statement.setInt(3, level);
					statement.execute();
				}
			}
			
			reloadNpcSkillList(npcId);
			
			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Added skill " + skillId + "-" + level + " to npc id " + npcId + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not add npc skill!");
			_log.warn("Error while adding a npc skill (" + npcId + ", " + skillId + ", " + level + "): " + e);
		}
	}
	
	private void deleteNpcSkillData(L2PcInstance activeChar, int npcId, int skillId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (npcId > 0)
			{
				int updated = 0;
				if (Config.CUSTOM_NPC_SKILLS_TABLE)
				{
					try (PreparedStatement statement = con.prepareStatement("DELETE FROM `custom_npcskills` WHERE `npcid`=? AND `skillid`=?"))
					{
						statement.setInt(1, npcId);
						statement.setInt(2, skillId);
						updated = statement.executeUpdate();
					}
				}
				if (updated == 0)
				{
					try (PreparedStatement statement2 = con.prepareStatement("DELETE FROM `npcskills` WHERE `npcid`=? AND `skillid`=?"))
					{
						statement2.setInt(1, npcId);
						statement2.setInt(2, skillId);
						statement2.execute();
					}
				}
				
				reloadNpcSkillList(npcId);
				
				showNpcSkillList(activeChar, npcId, 0);
				activeChar.sendMessage("Deleted skill id " + skillId + " from npc id " + npcId + ".");
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Could not delete npc skill!");
			_log.warn("Error while deleting npc skill (" + npcId + ", " + skillId + "): " + e);
		}
	}
	
	private void reloadNpcSkillList(int npcId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement("SELECT `skillid`, `level` FROM `npcskills` WHERE `npcid`=? AND `skillid` <> 4416");
			PreparedStatement statement2 = con.prepareStatement("SELECT `skillid`, `level` FROM `npcskills` WHERE `npcid`=? AND `skillid` <> 4416"))
		{
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
			
			L2Skill skillData = null;
			if (npcData.getSkills() != null)
			{
				npcData.getSkills().clear();
			}
			
			// without race
			
			statement1.setInt(1, npcId);
			try (ResultSet skillDataList = statement1.executeQuery())
			{
				while (skillDataList.next())
				{
					int idval = skillDataList.getInt("skillid");
					int levelval = skillDataList.getInt("level");
					skillData = SkillData.getInstance().getInfo(idval, levelval);
					if (skillData != null)
					{
						npcData.addSkill(skillData);
					}
				}
			}
			
			if (Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				statement2.setInt(1, npcId);
				try (ResultSet skillDataList2 = statement2.executeQuery())
				{
					while (skillDataList2.next())
					{
						int idval = skillDataList2.getInt("skillid");
						int levelval = skillDataList2.getInt("level");
						skillData = SkillData.getInstance().getInfo(idval, levelval);
						if (skillData != null)
						{
							npcData.addSkill(skillData);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Error while reloading npc skill list (" + npcId + "): " + e);
		}
	}
}
