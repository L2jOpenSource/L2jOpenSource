package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.spawn.AutoSpawn;
import com.l2jfrozen.gameserver.model.spawn.AutoSpawn.AutoSpawnInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * Admin Command Handler for Mammon NPCs
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mammon_find",
		"admin_mammon_respawn",
		"admin_list_spawns",
		"admin_msg"
	};
	
	private final boolean isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		int npcId = 0;
		int teleportIndex = -1;
		int spawnId = 0;
		
		AutoSpawnInstance blackSpawnInst = AutoSpawn.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		AutoSpawnInstance merchSpawnInst = AutoSpawn.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);
		
		if (command.startsWith("admin_mammon_find"))
		{
			try
			{
				if (command.length() > 17)
				{
					teleportIndex = Integer.parseInt(command.substring(18));
				}
			}
			catch (final Exception NumberFormatException)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					NumberFormatException.printStackTrace();
				}
				
				activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
			}
			
			if (!isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			if (blackSpawnInst != null)
			{
				L2NpcInstance[] blackInst = blackSpawnInst.getNPCInstanceList();
				if (blackInst.length > 0)
				{
					final int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
					activeChar.sendMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
					
					if (teleportIndex == 1)
					{
						activeChar.teleToLocation(x1, y1, z1, true);
					}
				}
				
				blackInst = null;
			}
			else
			{
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
			}
			
			if (merchSpawnInst != null)
			{
				L2NpcInstance[] merchInst = merchSpawnInst.getNPCInstanceList();
				
				if (merchInst.length > 0)
				{
					final int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();
					
					activeChar.sendMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
					
					if (teleportIndex == 2)
					{
						activeChar.teleToLocation(x2, y2, z2, true);
					}
				}
				
				merchInst = null;
			}
			else
			{
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
			}
		}
		
		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			
			if (merchSpawnInst != null)
			{
				final long merchRespawn = AutoSpawn.getInstance().getTimeToNextSpawn(merchSpawnInst);
				activeChar.sendMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000 + " minute(s).");
			}
			else
			{
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
			}
			
			if (blackSpawnInst != null)
			{
				final long blackRespawn = AutoSpawn.getInstance().getTimeToNextSpawn(blackSpawnInst);
				activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000 + " minute(s).");
			}
			else
			{
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
			}
		}
		else if (command.startsWith("admin_list_spawns"))
		{
			try
			{
				L2NpcTemplate npc;
				String[] params = command.split(" ");
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher regexp = pattern.matcher(params[1]);
				
				if (regexp.matches())
				{
					npcId = Integer.parseInt(params[1]);
				}
				else
				{
					params[1] = params[1].replace('_', ' ');
					String npcName = params[1];
					npc = NpcTable.getInstance().getTemplateByName(npcName);
					
					if (npc == null)
					{
						activeChar.sendMessage("No NPC found with name: " + npcName);
						return false;
					}
					
					npcId = NpcTable.getInstance().getTemplateByName(params[1]).npcId;
				}
				
				if (params.length > 2)
				{
					teleportIndex = Integer.parseInt(params[2]);
				}
				
				if (params.length > 3)
				{
					spawnId = Integer.parseInt(params[3]);
				}
				
				SpawnTable.getInstance().showSpawnList(activeChar, npcId, teleportIndex, spawnId);
			}
			catch (Exception e)
			{
				activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <npcId|npc_name> [tele_index]"));
			}
		}
		
		// Used for testing SystemMessage IDs - Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			int msgId = -1;
			
			try
			{
				msgId = Integer.parseInt(command.substring(10).trim());
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			activeChar.sendPacket(new SystemMessage(msgId));
		}
		
		merchSpawnInst = null;
		blackSpawnInst = null;
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
