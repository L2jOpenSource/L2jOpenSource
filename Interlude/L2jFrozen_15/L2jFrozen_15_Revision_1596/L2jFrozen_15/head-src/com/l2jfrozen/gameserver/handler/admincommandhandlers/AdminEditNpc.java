package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoxInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author terry
 */
public class AdminEditNpc implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminEditNpc.class);
	private static final String DELETE_DROPLIST = "DELETE FROM droplist WHERE mobId=? AND itemId=? AND category=?";
	private static final String DELETE_CUSTOM_DROPLIST = "DELETE FROM custom_droplist WHERE mobId=? AND itemId=? AND category=?";
	private static final String SELECT_DROPLIST_BY_MOB_ID = "SELECT mobId, itemId, min, max, category, chance FROM droplist WHERE mobId=? ORDER BY category";
	private static final String SELECT_CUSTOM_DROPLIST_BY_MOB_ID = "SELECT mobId, itemId, min, max, category, chance FROM custom_droplist WHERE mobId=? ORDER BY category";
	private static final String SELECT_DROPLIST_DATA = "SELECT mobId, itemId, min, max, category, chance FROM droplist WHERE mobId=? AND itemId=? AND category=? ORDER BY category";
	private static final String SELECT_CUSTOM_DROPLIST_DATA = "SELECT mobId, itemId, min, max, category, chance FROM custom_droplist WHERE mobId=? AND itemId=? AND category=? ORDER BY category";
	private static final String SELECT_DROPLIST_MOB = "SELECT mobId FROM droplist WHERE mobId=? AND itemId=? AND category=?";
	private static final String SELECT_CUSTOM_DROPLIST_MOB = "SELECT mobId FROM custom_droplist WHERE mobId=? AND itemId=? AND category=?";
	private static final String UPDATE_DROPLIST = "UPDATE droplist SET min=?, max=?, chance=? WHERE mobId=? AND itemId=? AND category=?";
	private static final String UPDATE_CUSTOM_DROPLIST = "UPDATE custom_droplist SET min=?, max=?, chance=? WHERE mobId=? AND itemId=? AND category=?";
	private static final String INSERT_CUSTOM_DROPLIST_DATA = "INSERT INTO custom_droplist (mobId, itemId, min, max, category, chance) VALUES(?,?,?,?,?,?)";
	private static final String INSERT_NPC_SKILL = "INSERT INTO npcskills(npcid, skillid, level) values(?,?,?)";
	private static final String DELETE_NPC_SKILL = "DELETE FROM npcskills WHERE npcid=? AND skillid=?";
	private static final String SELECT_NPC_SKILL_LIST = "SELECT npcid, skillid, level FROM npcskills WHERE npcid=? AND (skillid NOT BETWEEN 4290 AND 4302)";
	private static final String UPDATE_NPC_SKILLS = "UPDATE npcskills SET level=? WHERE npcid=? AND skillid=?";
	private static final String SELECT_NPC_SKILL_BY_NPC_ID_AND_SKILL_ID = "SELECT npcid, skillid, level FROM npcskills WHERE npcid=? AND skillid=?";
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_npc",
		"admin_save_npc",
		"admin_show_droplist",
		"admin_edit_drop",
		"admin_add_drop",
		"admin_del_drop",
		"admin_box_access",
		"admin_close_window",
		"admin_show_skilllist_npc",
		"admin_add_skill_npc",
		"admin_edit_skill_npc",
		"admin_del_skill_npc",
		"admin_load_npc"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_edit_npc ") || command.equals("admin_edit_npc"))
		{
			if (command.startsWith("admin_edit_npc "))
			{
				try
				{
					String[] commandSplit = command.split(" ");
					
					final int npcId = Integer.valueOf(commandSplit[1]);
					
					L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
					showNpcProperties(activeChar, npc);
					
					commandSplit = null;
					npc = null;
				}
				catch (final Exception e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					activeChar.sendMessage("Wrong usage: //edit_npc <npcId>");
				}
			}
			else
			{
				if (activeChar.getTarget() instanceof L2NpcInstance)
				{
					
					final int npcId = Integer.valueOf(((L2NpcInstance) activeChar.getTarget()).getNpcId());
					
					L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
					showNpcProperties(activeChar, npc);
					
					npc = null;
					
				}
			}
			
		}
		else if (command.startsWith("admin_load_npc"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			int id = 0;
			try
			{
				id = Integer.parseInt(st.nextToken());
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //load_npc <id>");
			}
			if (id > 0)
			{
				NpcTable.getInstance().reloadNpc(id);
			}
		}
		else if (command.startsWith("admin_show_droplist "))
		{
			int npcId = 0;
			
			try
			{
				npcId = Integer.parseInt(command.substring(20).trim());
			}
			catch (Exception e)
			{
				LOGGER.error("AdminEditNpc.useAdminCommand : Invalid number format", e);
			}
			
			if (npcId > 0)
			{
				showNpcDropList(activeChar, npcId);
			}
			else
			{
				activeChar.sendMessage("Usage: //show_droplist <npc_id>");
			}
		}
		else if (command.startsWith("admin_save_npc "))
		{
			final String[] commandSplit = command.split(" ");
			if (commandSplit.length >= 4)
			{
				saveNpcProperties(activeChar, commandSplit);
			}
			else
			{
				activeChar.sendMessage("Usage: //save_npc <npc_id> <npc_stat> <npc_stat_value>");
			}
		}
		else if (command.startsWith("admin_show_skilllist_npc "))
		{
			final StringTokenizer st = new StringTokenizer(command.substring(25), " ");
			try
			{
				int npcId = -1;
				int page = 0;
				if (st.countTokens() <= 2)
				{
					if (st.hasMoreTokens())
					{
						npcId = Integer.parseInt(st.nextToken());
					}
					if (st.hasMoreTokens())
					{
						page = Integer.parseInt(st.nextToken());
					}
				}
				
				if (npcId > 0)
				{
					showNpcSkillList(activeChar, npcId, page);
				}
				else
				{
					activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
			}
		}
		else if (command.startsWith("admin_edit_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				final StringTokenizer st = new StringTokenizer(command.substring(21).trim(), " ");
				if (st.countTokens() == 2)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						showNpcSkillEdit(activeChar, npcId, skillId);
					}
					catch (final Exception e)
					{
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						final int level = Integer.parseInt(st.nextToken());
						
						updateNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (final Exception e)
					{
						LOGGER.warn("admin_edit_skill_npc parements error: " + command);
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		}
		else if (command.startsWith("admin_add_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				final StringTokenizer st = new StringTokenizer(command.substring(20).trim(), " ");
				if (st.countTokens() == 1)
				{
					try
					{
						final String[] input = command.substring(20).split(" ");
						if (input.length < 1)
						{
							return true;
						}
						npcId = Integer.parseInt(input[0]);
					}
					catch (final Exception e)
					{
					}
					
					if (npcId > 0)
					{
						final L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
						showNpcSkillAdd(activeChar, npcData);
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						final int level = Integer.parseInt(st.nextToken());
						
						addNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (final Exception e)
					{
						LOGGER.warn("admin_add_skill_npc parements error: " + command);
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<level>]");
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<level>]");
			}
		}
		else if (command.startsWith("admin_del_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				final String[] input = command.substring(20).split(" ");
				if (input.length >= 2)
				{
					npcId = Integer.parseInt(input[0]);
					skillId = Integer.parseInt(input[1]);
				}
			}
			catch (final Exception e)
			{
			}
			
			if (npcId > 0)
			{
				deleteNpcSkillData(activeChar, npcId, skillId);
			}
			else
			{
				activeChar.sendMessage("Usage: //del_skill_npc <npc_id> <skill_id>");
			}
		}
		else if (command.startsWith("admin_edit_drop "))
		{
			int npcId = -1;
			int itemId = 0;
			int category = -1000;
			
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(16).trim());
				
				if (st.countTokens() == 4)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						boolean isCustomDrop = Boolean.parseBoolean(st.nextToken());
						showEditDropData(activeChar, npcId, itemId, category, isCustomDrop);
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
					}
				}
				else if (st.countTokens() == 7)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());
						boolean isCustomDrop = Boolean.parseBoolean(st.nextToken());
						
						updateDropData(activeChar, npcId, itemId, min, max, category, chance, isCustomDrop);
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("admin_edit_drop parements error: " + command);
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
				}
				
				st = null;
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
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
						input = null;
					}
					catch (Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
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
						
						addCustomDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Invalid parements.");
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
			int npcId = -1;
			int itemId = -1;
			int category = -1000;
			boolean isCustomDrop = false;
			
			try
			{
				String[] input = command.substring(15).split(" ");
				if (input.length >= 4)
				{
					npcId = Integer.parseInt(input[0]);
					itemId = Integer.parseInt(input[1]);
					category = Integer.parseInt(input[2]);
					isCustomDrop = Boolean.parseBoolean(input[3]);
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
			
			if (npcId > 0)
			{
				deleteDropData(activeChar, npcId, itemId, category, isCustomDrop);
			}
			else
			{
				activeChar.sendMessage("Usage: //del_drop <npc_id> <item_id> <category> <isCustomDrop?>");
			}
		}
		else if (command.startsWith("admin_box_access"))
		{
			L2Object target = activeChar.getTarget();
			String[] players = command.split(" ");
			
			if (target instanceof L2BoxInstance)
			{
				L2BoxInstance box = (L2BoxInstance) target;
				
				if (players.length > 1)
				{
					boolean access = true;
					for (int i = 1; i < players.length; i++)
					{
						if (players[i].equals("no"))
						{
							access = false;
							continue;
						}
						box.grantAccess(players[i], access);
					}
				}
				else
				{
					try
					{
						String msg = "Access:";
						
						for (final Object p : box.getAccess())
						{
							msg += " " + (String) p;
						}
						
						activeChar.sendMessage(msg);
						msg = null;
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.info("box_access: " + e);
					}
				}
				
				box = null;
			}
			
			target = null;
			players = null;
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showNpcProperties(L2PcInstance activeChar, L2NpcTemplate npc)
	{
		if (npc.isCustom())
		{
			activeChar.sendMessage("You are going to modify Custom NPC");
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/editnpc.htm");
		
		adminReply.replace("%npcId%", String.valueOf(npc.npcId));
		adminReply.replace("%templateId%", String.valueOf(npc.idTemplate));
		adminReply.replace("%name%", npc.name);
		adminReply.replace("%serverSideName%", npc.serverSideName ? "1" : "0");
		adminReply.replace("%title%", npc.title);
		adminReply.replace("%serverSideTitle%", npc.serverSideTitle ? "1" : "0");
		adminReply.replace("%collisionRadius%", String.valueOf(npc.collisionRadius));
		adminReply.replace("%collisionHeight%", String.valueOf(npc.collisionHeight));
		adminReply.replace("%level%", String.valueOf(npc.level));
		adminReply.replace("%sex%", String.valueOf(npc.sex));
		adminReply.replace("%type%", String.valueOf(npc.type));
		adminReply.replace("%attackRange%", String.valueOf(npc.baseAtkRange));
		adminReply.replace("%hp%", String.valueOf(npc.baseHpMax));
		adminReply.replace("%mp%", String.valueOf(npc.baseMpMax));
		adminReply.replace("%hpRegen%", String.valueOf(npc.baseHpReg));
		adminReply.replace("%mpRegen%", String.valueOf(npc.baseMpReg));
		adminReply.replace("%str%", String.valueOf(npc.baseSTR));
		adminReply.replace("%con%", String.valueOf(npc.baseCON));
		adminReply.replace("%dex%", String.valueOf(npc.baseDEX));
		adminReply.replace("%int%", String.valueOf(npc.baseINT));
		adminReply.replace("%wit%", String.valueOf(npc.baseWIT));
		adminReply.replace("%men%", String.valueOf(npc.baseMEN));
		adminReply.replace("%exp%", String.valueOf(npc.rewardExp));
		adminReply.replace("%sp%", String.valueOf(npc.rewardSp));
		adminReply.replace("%pAtk%", String.valueOf(npc.basePAtk));
		adminReply.replace("%pDef%", String.valueOf(npc.basePDef));
		adminReply.replace("%mAtk%", String.valueOf(npc.baseMAtk));
		adminReply.replace("%mDef%", String.valueOf(npc.baseMDef));
		adminReply.replace("%pAtkSpd%", String.valueOf(npc.basePAtkSpd));
		adminReply.replace("%aggro%", String.valueOf(npc.aggroRange));
		adminReply.replace("%mAtkSpd%", String.valueOf(npc.baseMAtkSpd));
		adminReply.replace("%rHand%", String.valueOf(npc.rhand));
		adminReply.replace("%lHand%", String.valueOf(npc.lhand));
		adminReply.replace("%armor%", String.valueOf(npc.armor));
		adminReply.replace("%walkSpd%", String.valueOf(npc.baseWalkSpd));
		adminReply.replace("%runSpd%", String.valueOf(npc.baseRunSpd));
		adminReply.replace("%factionId%", npc.factionId == null ? "" : npc.factionId);
		adminReply.replace("%factionRange%", String.valueOf(npc.factionRange));
		adminReply.replace("%isUndead%", npc.isUndead ? "1" : "0");
		adminReply.replace("%absorbLevel%", String.valueOf(npc.absorbLevel));
		
		activeChar.sendPacket(adminReply);
	}
	
	private void saveNpcProperties(L2PcInstance activeChar, String[] commandSplit)
	{
		StatsSet newNpcData = new StatsSet();
		
		try
		{
			newNpcData.set("npcId", commandSplit[1]);
			String statToSet = commandSplit[2];
			String value = "";
			
			for (int i = 3; i < commandSplit.length; i++)
			{
				if (i == 3)
				{
					value += commandSplit[i];
				}
				else
				{
					value += " " + commandSplit[i];
				}
			}
			
			switch (statToSet)
			{
				case "templateId":
					newNpcData.set("idTemplate", Integer.valueOf(value));
					break;
				case "name":
					newNpcData.set("name", value);
					break;
				case "serverSideName":
					newNpcData.set("serverSideName", Integer.valueOf(value));
					break;
				case "title":
					newNpcData.set("title", value);
					break;
				case "serverSideTitle":
					newNpcData.set("serverSideTitle", Integer.valueOf(value) == 1 ? 1 : 0);
					break;
				case "collisionRadius":
					newNpcData.set("collision_radius", Integer.valueOf(value));
					break;
				case "collisionHeight":
					newNpcData.set("collision_height", Integer.valueOf(value));
					break;
				case "level":
					newNpcData.set("level", Integer.valueOf(value));
					break;
				case "sex":
					final int intValue = Integer.valueOf(value);
					newNpcData.set("sex", intValue == 0 ? "male" : intValue == 1 ? "female" : "etc");
					break;
				case "type":
					Class.forName("com.l2jfrozen.gameserver.model.actor.instance." + value + "Instance");
					newNpcData.set("type", value);
					break;
				case "attackRange":
					newNpcData.set("attackrange", Integer.valueOf(value));
					break;
				case "hp":
					newNpcData.set("hp", Integer.valueOf(value));
					break;
				case "mp":
					newNpcData.set("mp", Integer.valueOf(value));
					break;
				case "hpRegen":
					newNpcData.set("hpreg", Integer.valueOf(value));
					break;
				case "mpRegen":
					newNpcData.set("mpreg", Integer.valueOf(value));
					break;
				case "str":
					newNpcData.set("str", Integer.valueOf(value));
					break;
				case "con":
					newNpcData.set("con", Integer.valueOf(value));
					break;
				case "dex":
					newNpcData.set("dex", Integer.valueOf(value));
					break;
				case "int":
					newNpcData.set("int", Integer.valueOf(value));
					break;
				case "wit":
					newNpcData.set("wit", Integer.valueOf(value));
					break;
				case "men":
					newNpcData.set("men", Integer.valueOf(value));
					break;
				case "exp":
					newNpcData.set("exp", Integer.valueOf(value));
					break;
				case "sp":
					newNpcData.set("sp", Integer.valueOf(value));
					break;
				case "pAtk":
					newNpcData.set("patk", Integer.valueOf(value));
					break;
				case "pDef":
					newNpcData.set("pdef", Integer.valueOf(value));
					break;
				case "mAtk":
					newNpcData.set("matk", Integer.valueOf(value));
					break;
				case "mDef":
					newNpcData.set("mdef", Integer.valueOf(value));
					break;
				case "pAtkSpd":
					newNpcData.set("atkspd", Integer.valueOf(value));
					break;
				case "aggro":
					newNpcData.set("aggro", Integer.valueOf(value));
					break;
				case "mAtkSpd":
					newNpcData.set("matkspd", Integer.valueOf(value));
					break;
				case "rHand":
					newNpcData.set("rhand", Integer.valueOf(value));
					break;
				case "lHand":
					newNpcData.set("lhand", Integer.valueOf(value));
					break;
				case "armor":
					newNpcData.set("armor", Integer.valueOf(value));
					break;
				case "runSpd":
					newNpcData.set("runspd", Integer.valueOf(value));
					break;
				case "factionId":
					newNpcData.set("faction_id", value);
					break;
				case "factionRange":
					newNpcData.set("faction_range", Integer.valueOf(value));
					break;
				case "isUndead":
					newNpcData.set("isUndead", Integer.valueOf(value) == 1 ? 1 : 0);
					break;
				case "absorbLevel":
					final int intVal = Integer.valueOf(value);
					newNpcData.set("absorb_level", intVal < 0 ? 0 : intVal > 12 ? 0 : intVal);
					break;
			}
			
			statToSet = null;
			value = null;
		}
		catch (final Exception e)
		{
			LOGGER.error("AdminEditNpc.saveNpcProperties : Error saving new npc property", e);
		}
		
		final int npcId = newNpcData.getInteger("npcId");
		final L2NpcTemplate old = NpcTable.getInstance().getTemplate(npcId);
		
		if (old.isCustom())
		{
			activeChar.sendMessage("You are going to save Custom NPC");
		}
		
		NpcTable.getInstance().saveNpc(newNpcData);
		
		NpcTable.getInstance().reloadNpc(npcId);
		
		showNpcProperties(activeChar, NpcTable.getInstance().getTemplate(npcId));
		
		newNpcData = null;
	}
	
	private void showNpcDropList(L2PcInstance activeChar, int npcId)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
		
		if (npc == null)
		{
			activeChar.sendMessage("Unknown npc template id " + npcId);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder("<html><title>NPC: " + npc.name + "(" + npc.npcId + ") 's drop manage</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br><font color=LEVEL>Notes:</font> Clic on item name to show the detail of drop data, clic on 'X' to delete the drop data.");
		replyMSG.append("<table width=256>");
		L2Item itemTemplate;
		
		for (L2DropCategory cat : npc.getDropData())
		{
			for (L2DropData drop : cat.getAllDrops())
			{
				itemTemplate = ItemTable.getInstance().getTemplate(drop.getItemId());
				
				if (itemTemplate == null)
				{
					LOGGER.warn(getClass().getSimpleName() + ": Unkown item Id: " + drop.getItemId() + " for NPC: " + npc.npcId);
					continue;
				}
				
				String type = "Drop";
				
				if (drop.isQuestDrop())
				{
					type = "Quest";
				}
				else if (cat.isSweep())
				{
					type = "Spoil";
				}
				
				replyMSG.append("<tr><td>Category: " + cat.getCategoryType() + " <font color=LEVEL>[" + type + "]</font></td></tr>");
				replyMSG.append("<tr><td><a action=\"bypass -h admin_edit_drop " + npc.npcId + " " + drop.getItemId() + " " + cat.getCategoryType() + " " + drop.isCustomDrop() + "\">&#" + drop.getItemId() + ";</a></td><td><a action=\"bypass -h admin_del_drop " + npc.npcId + " " + drop.getItemId() + " " + cat.getCategoryType() + " " + drop.isCustomDrop() + "\">X</a></td></tr>");
			}
		}
		
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add DropData\" action=\"bypass -h admin_add_drop " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showEditDropData(L2PcInstance activeChar, int npcId, int itemId, int category, boolean isCustomDrop)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(isCustomDrop ? SELECT_CUSTOM_DROPLIST_DATA : SELECT_DROPLIST_DATA))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, itemId);
			statement.setInt(3, category);
			
			try (ResultSet dropData = statement.executeQuery())
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				
				StringBuilder replyMSG = new StringBuilder("<html><title>the detail of dropdata: (" + npcId + " " + itemId + " " + category + ")</title>");
				replyMSG.append("<body>");
				
				if (dropData.next())
				{
					replyMSG.append("<center>");
					replyMSG.append("<table width=256>");
					replyMSG.append("<tr><td><font color=LEVEL>");
					replyMSG.append(NpcTable.getInstance().getTemplate(dropData.getInt("mobId")).getName());
					replyMSG.append(" (").append(NpcTable.getInstance().getTemplate(dropData.getInt("mobId")).getNpcId()).append(")");
					replyMSG.append("</font></td></tr>");
					replyMSG.append("<tr><td><font color=LEVEL>");
					replyMSG.append("&#").append(dropData.getInt("itemId")).append(";");
					replyMSG.append("</font></td></tr>");
					replyMSG.append("</table>");
					replyMSG.append("</center>");
					replyMSG.append("<table width=256>");
					String type = category == -1 ? " [Spoil]" : " [Drop]";
					replyMSG.append("<tr><td>Category</td><td> " + category + type + " </td></tr>");
					replyMSG.append("<tr><td>MIN(" + dropData.getInt("min") + ")</td><td><edit var=\"min\" width=80></td></tr>");
					replyMSG.append("<tr><td>MAX(" + dropData.getInt("max") + ")</td><td><edit var=\"max\" width=80></td></tr>");
					replyMSG.append("<tr><td>CHANCE(" + dropData.getInt("chance") + ")</td><td><edit var=\"chance\" width=80></td></tr>");
					replyMSG.append("</table><br>");
					replyMSG.append("<center>");
					replyMSG.append("<button value=\"Save modify\" action=\"bypass -h admin_edit_drop " + npcId + " " + itemId + " " + category + " $min $max $chance " + isCustomDrop + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + dropData.getInt("mobId") + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("<br>");
					replyMSG.append("</center>");
					replyMSG.append("<font color=LEVEL>CHANCE VALUES:</font>");
					replyMSG.append("<br1>");
					replyMSG.append("1000000 (1kk) is 100%");
					replyMSG.append("<br1>");
					replyMSG.append("500000 (500k) is 50%");
					replyMSG.append("<br1>");
					replyMSG.append("100000 (100k) is 10% and so on");
				}
				
				replyMSG.append("</body></html>");
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.showEditDropData : Something went wrong", e);
		}
	}
	
	private void showAddDropData(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><title>Add dropdata to " + npcData.name + "(" + npcData.npcId + ")</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table witdh=100%>");
		replyMSG.append("<tr><td width=120>Item ID</td><td><edit var=\"itemId\" width=80></td></tr>");
		replyMSG.append("<tr><td width=120>Min</td><td><edit var=\"min\" width=80></td></tr>");
		replyMSG.append("<tr><td width=120>Max</td><td><edit var=\"max\" width=80></td></tr>");
		replyMSG.append("<tr><td width=120>Category(spoil=-1)</td><td><edit var=\"category\" width=80></td></tr>");
		replyMSG.append("<tr><td width=120>Chance(0-1000000)</td><td><edit var=\"chance\" width=80></td></tr>");
		replyMSG.append("<tr><td></td><td></td></tr>");
		replyMSG.append("<tr><td></td><td></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Save\" action=\"bypass -h admin_add_drop " + npcData.npcId + " $itemId $category $min $max $chance\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcData.npcId + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		
		activeChar.sendPacket(adminReply);
	}
	
	private void updateDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance, boolean customDrop)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement updatestmt = con.prepareStatement(customDrop ? UPDATE_CUSTOM_DROPLIST : UPDATE_DROPLIST);
			PreparedStatement selectstmt = con.prepareStatement(customDrop ? SELECT_CUSTOM_DROPLIST_MOB : SELECT_DROPLIST_MOB))
		{
			updatestmt.setInt(1, min);
			updatestmt.setInt(2, max);
			updatestmt.setInt(3, chance);
			updatestmt.setInt(4, npcId);
			updatestmt.setInt(5, itemId);
			updatestmt.setInt(6, category);
			updatestmt.executeUpdate();
			
			selectstmt.setInt(1, npcId);
			selectstmt.setInt(2, itemId);
			selectstmt.setInt(3, category);
			
			try (ResultSet npcIdRs = selectstmt.executeQuery())
			{
				if (npcIdRs.next())
				{
					npcId = npcIdRs.getInt("mobId");
				}
			}
			
			if (npcId > 0)
			{
				reloadNpcDropList(npcId);
				
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuilder replyMSG = new StringBuilder("<html><title>Drop data modify complete!</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			else
			{
				activeChar.sendMessage("npcId equals or lower than 0");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.updateEditDropData : Something went wrong", e);
		}
	}
	
	private void addCustomDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CUSTOM_DROPLIST_DATA))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, itemId);
			statement.setInt(3, min);
			statement.setInt(4, max);
			statement.setInt(5, category);
			statement.setInt(6, chance);
			statement.executeUpdate();
			
			reloadNpcDropList(npcId);
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><title>Add drop data complete!</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Continue add\" action=\"bypass -h admin_add_drop " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("<br><br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center></body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.addDropData : Something went wrong", e);
		}
	}
	
	private void deleteDropData(L2PcInstance activeChar, int npcId, int itemId, int category, boolean dropCustom)
	{
		if (npcId > 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement pst = con.prepareStatement(dropCustom ? DELETE_CUSTOM_DROPLIST : DELETE_DROPLIST))
			{
				pst.setInt(1, npcId);
				pst.setInt(2, itemId);
				pst.setInt(3, category);
				pst.executeUpdate();
				
				reloadNpcDropList(npcId);
				
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuilder replyMSG = new StringBuilder("<html><title>Delete drop data(" + npcId + ", " + itemId + ", " + category + ")complete</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			catch (Exception e)
			{
				LOGGER.error("AdminEditNpc.deleteDropData : Could not delete " + (dropCustom ? "customd drop" : "drop") + " data for npc id " + npcId, e);
			}
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
		npcData.getDropData().clear();
		
		// get the drops
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement selectDropList = con.prepareStatement(SELECT_DROPLIST_BY_MOB_ID);
			PreparedStatement selectCustomDropList = con.prepareStatement(SELECT_CUSTOM_DROPLIST_BY_MOB_ID))
		{
			selectDropList.setInt(1, npcId);
			
			try (ResultSet dropDataList = selectDropList.executeQuery())
			{
				while (dropDataList.next())
				{
					L2DropData dropData = new L2DropData();
					
					dropData.setItemId(dropDataList.getInt("itemId"));
					dropData.setMinDrop(dropDataList.getInt("min"));
					dropData.setMaxDrop(dropDataList.getInt("max"));
					dropData.setChance(dropDataList.getInt("chance"));
					
					int category = dropDataList.getInt("category");
					
					npcData.addDropData(dropData, category);
				}
			}
			
			selectCustomDropList.setInt(1, npcId);
			try (ResultSet customDropDataList = selectCustomDropList.executeQuery())
			{
				while (customDropDataList.next())
				{
					L2DropData customDropData = new L2DropData();
					customDropData = new L2DropData();
					
					customDropData.setItemId(customDropDataList.getInt("itemId"));
					customDropData.setMinDrop(customDropDataList.getInt("min"));
					customDropData.setMaxDrop(customDropDataList.getInt("max"));
					customDropData.setChance(customDropDataList.getInt("chance"));
					customDropData.setIsCustomDrop(true);
					
					int category = customDropDataList.getInt("category");
					
					npcData.addDropData(customDropData, category);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.reloadNpcDropList : Something went wrong", e);
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
		
		final Map<Integer, L2Skill> skills = npcData.getSkills();
		
		final int skillsize = Integer.valueOf(skills.size());
		
		final int MaxSkillsPerPage = 10;
		int MaxPages = skillsize / MaxSkillsPerPage;
		if (skillsize > MaxSkillsPerPage * MaxPages)
		{
			MaxPages++;
		}
		
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		final int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = skillsize;
		if (SkillsEnd - SkillsStart > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final StringBuffer replyMSG = new StringBuffer("");
		replyMSG.append("<html><title>" + npcData.getName() + " Skillist");
		replyMSG.append(" (ID:" + npcData.getNpcId() + "Skills " + Integer.valueOf(skillsize) + ")</title>");
		replyMSG.append("<body>");
		String pages = "<center><table width=270><tr>";
		for (int x = 0; x < MaxPages; x++)
		{
			final int pagenr = x + 1;
			if (page == x)
			{
				pages += "<td>Page " + pagenr + "</td>";
			}
			else
			{
				pages += "<td><a action=\"bypass -h admin_show_skilllist_npc " + npcData.getNpcId() + " " + x + "\">Page " + pagenr + "</a></td>";
			}
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		
		replyMSG.append("<table width=270>");
		
		final Set<Integer> skillset = skills.keySet();
		final Iterator<Integer> skillite = skillset.iterator();
		Object skillobj = null;
		
		for (int i = 0; i < SkillsStart; i++)
		{
			if (skillite.hasNext())
			{
				skillobj = skillite.next();
			}
		}
		
		int cnt = SkillsStart;
		while (skillite.hasNext())
		{
			cnt++;
			if (cnt > SkillsEnd)
			{
				break;
			}
			skillobj = skillite.next();
			replyMSG.append("<tr><td><a action=\"bypass -h admin_edit_skill_npc " + npcData.getNpcId() + " " + skills.get(skillobj).getId() + "\">" + skills.get(skillobj).getName() + " [" + skills.get(skillobj).getId() + "]" + "</a></td>" + "<td>" + skills.get(skillobj).getLevel() + "</td>" + "<td><a action=\"bypass -h admin_del_skill_npc " + npcData.getNpcId() + " " + skillobj
				+ "\">Delete</a></td></tr>");
			
		}
		replyMSG.append("</table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Droplist\" action=\"bypass -h admin_show_droplist " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showNpcSkillEdit(L2PcInstance activeChar, int npcId, int skillId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_NPC_SKILL_BY_NPC_ID_AND_SKILL_ID))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, skillId);
			final ResultSet skillData = statement.executeQuery();
			
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			final StringBuffer replyMSG = new StringBuffer("<html><title>(NPC:" + npcId + " SKILL:" + skillId + ")</title>");
			replyMSG.append("<body>");
			
			if (skillData.next())
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(skillData.getInt("skillid"), skillData.getInt("level"));
				
				replyMSG.append("<table>");
				replyMSG.append("<tr><td>NPC</td><td>" + NpcTable.getInstance().getTemplate(skillData.getInt("npcid")).getName() + "</td></tr>");
				replyMSG.append("<tr><td>SKILL</td><td>" + skill.getName() + "(" + skillData.getInt("skillid") + ")</td></tr>");
				replyMSG.append("<tr><td>Lv(" + skill.getLevel() + ")</td><td><edit var=\"level\" width=50></td></tr>");
				replyMSG.append("</table>");
				
				replyMSG.append("<center>");
				replyMSG.append("<button value=\"Edit Skill\" action=\"bypass -h admin_edit_skill_npc " + npcId + " " + skillId + " $level\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
				replyMSG.append("</center>");
			}
			
			skillData.close();
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			
			activeChar.sendPacket(adminReply);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void updateNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		final L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
		if (skillData == null)
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
			replyMSG.append("</body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_NPC_SKILLS))
		{
			statement.setInt(1, level);
			statement.setInt(2, npcId);
			statement.setInt(3, skillId);
			
			statement.executeUpdate();
			
			if (npcId > 0)
			{
				reLoadNpcSkillList(npcId);
				
				final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				final StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			else
			{
				activeChar.sendMessage("npcId equals or lower than 0");
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.updateNpcSkillData : Could not update npcskills into db", e);
		}
	}
	
	private void showNpcSkillAdd(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to " + npcData.getName() + "(ID:" + npcData.getNpcId() + ")</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>SkillId</td><td><edit var=\"skillId\" width=80></td></tr>");
		replyMSG.append("<tr><td>Level</td><td><edit var=\"level\" width=80></td></tr>");
		replyMSG.append("</table>");
		
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcData.getNpcId() + " $skillId $level\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcData.getNpcId() + "\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		
		activeChar.sendPacket(adminReply);
	}
	
	private void addNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		// skill check
		final L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
		if (skillData == null)
		{
			
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
			replyMSG.append("</body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_NPC_SKILL))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, skillId);
			statement.setInt(3, level);
			statement.executeUpdate();
			
			reLoadNpcSkillList(npcId);
			
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc (" + npcId + ", " + skillId + ", " + level + ")</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("<br><br><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center></body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.addNpcSkillData : Something went wrong", e);
		}
	}
	
	private void deleteNpcSkillData(L2PcInstance activeChar, int npcId, int skillId)
	{
		if (npcId > 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement2 = con.prepareStatement(DELETE_NPC_SKILL);)
			{
				statement2.setInt(1, npcId);
				statement2.setInt(2, skillId);
				statement2.executeUpdate();
				
				reLoadNpcSkillList(npcId);
				
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Delete Skill (" + npcId + ", " + skillId + ")</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass -h admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			catch (Exception e)
			{
				LOGGER.error("AdminEditNpc.deleteNpcSkillData : Could not delete npcskill from db", e);
			}
		}
	}
	
	private void reLoadNpcSkillList(int npcId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_NPC_SKILL_LIST);)
		{
			L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
			npcData.getSkills().clear();
			
			L2Skill skillData = null;
			
			statement.setInt(1, npcId);
			
			try (ResultSet skillDataList = statement.executeQuery())
			{
				while (skillDataList.next())
				{
					int idval = skillDataList.getInt("skillid");
					int levelval = skillDataList.getInt("level");
					skillData = SkillTable.getInstance().getInfo(idval, levelval);
					
					if (skillData != null)
					{
						npcData.addSkill(skillData);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("AdminEditNpc.reLoadNpcSkillList : Could not reload skills for npc", e);
		}
	}
}
