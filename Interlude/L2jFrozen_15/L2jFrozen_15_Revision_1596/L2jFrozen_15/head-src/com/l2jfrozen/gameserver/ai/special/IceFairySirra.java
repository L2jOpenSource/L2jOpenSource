package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.model.zone.type.L2BossZone;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * Ice Fairy Sirra AI
 * @author Kerberos
 */

public class IceFairySirra extends Quest implements Runnable
{
	private static final int STEWARD = 32029;
	private static final int SILVER_HEMOCYTE = 8057;
	private static L2BossZone freyasZone;
	private static L2PcInstance playerTarget = null;
	protected List<L2NpcInstance> allMobs = new ArrayList<>();
	protected Future<?> onDeadEventTask = null;
	
	public IceFairySirra(final int id, final String name, final String descr)
	{
		super(id, name, descr);
		final int[] mobs =
		{
			STEWARD,
			22100,
			22102,
			22104
		};
		
		for (final int mob : mobs)
		{
			// TODO:
			addEventId(mob, Quest.QuestEventType.QUEST_START);
			addEventId(mob, Quest.QuestEventType.QUEST_TALK);
			addEventId(mob, Quest.QuestEventType.NPC_FIRST_TALK);
		}
		
		init();
	}
	
	@Override
	public String onFirstTalk(final L2NpcInstance npc, final L2PcInstance player)
	{
		if (player.getQuestState("IceFairySirra") == null)
		{
			newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		String filename = "";
		if (npc.isBusy())
		{
			filename = getHtmlPath(10);
		}
		else
		{
			filename = getHtmlPath(0);
		}
		sendHtml(npc, player, filename);
		return null;
	}
	
	@Override
	public String onAdvEvent(final String event, final L2NpcInstance npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("check_condition"))
		{
			if (npc.isBusy())
			{
				return super.onAdvEvent(event, npc, player);
			}
			
			String filename = "";
			if (player.isInParty() && player.getParty().getPartyLeaderOID() == player.getObjectId())
			{
				if (checkItems(player))
				{
					startQuestTimer("start", 100000, null, player);
					playerTarget = player;
					destroyItems(player);
					player.getInventory().addItem("Scroll", 8379, 3, player, null);
					npc.setBusy(true);
					screenMessage(player, "Steward: Please wait a moment.", 100000);
					filename = getHtmlPath(3);
				}
				else
				{
					filename = getHtmlPath(2);
				}
			}
			else
			{
				filename = getHtmlPath(1);
			}
			sendHtml(npc, player, filename);
		}
		else if (event.equalsIgnoreCase("start"))
		{
			if (freyasZone == null)
			{
				LOGGER.warn("IceFairySirraManager: Failed to load zone");
				cleanUp();
				return super.onAdvEvent(event, npc, player);
			}
			freyasZone.setZoneEnabled(true);
			closeGates();
			doSpawns();
			startQuestTimer("Party_Port", 2000, null, player);
			startQuestTimer("End", 1802000, null, player);
		}
		else if (event.equalsIgnoreCase("Party_Port"))
		{
			teleportInside(player);
			screenMessage(player, "Steward: Please restore the Queen's appearance!", 10000);
			startQuestTimer("30MinutesRemaining", 300000, null, player);
		}
		else if (event.equalsIgnoreCase("30MinutesRemaining"))
		{
			screenMessage(player, "30 minute(s) are remaining.", 10000);
			startQuestTimer("20minutesremaining", 600000, null, player);
		}
		else if (event.equalsIgnoreCase("20MinutesRemaining"))
		{
			screenMessage(player, "20 minute(s) are remaining.", 10000);
			startQuestTimer("10minutesremaining", 600000, null, player);
		}
		else if (event.equalsIgnoreCase("10MinutesRemaining"))
		{
			screenMessage(player, "Steward: Waste no time! Please hurry!", 10000);
		}
		else if (event.equalsIgnoreCase("End"))
		{
			screenMessage(player, "Steward: Was it indeed too much to ask.", 10000);
			cleanUp();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void init()
	{
		freyasZone = GrandBossManager.getInstance().getZone(105546, -127892, -2768);
		if (freyasZone == null)
		{
			LOGGER.warn("IceFairySirraManager: Failed to load zone");
			return;
		}
		freyasZone.setZoneEnabled(false);
		final L2NpcInstance steward = findTemplate(STEWARD);
		if (steward != null)
		{
			steward.setBusy(false);
		}
		openGates();
	}
	
	public void cleanUp()
	{
		init();
		cancelQuestTimer("30MinutesRemaining", null, playerTarget);
		cancelQuestTimer("20MinutesRemaining", null, playerTarget);
		cancelQuestTimer("10MinutesRemaining", null, playerTarget);
		cancelQuestTimer("End", null, playerTarget);
		for (final L2NpcInstance mob : allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.error("IceFairySirraManager: Failed deleting mob.", e);
			}
		}
		allMobs.clear();
	}
	
	public L2NpcInstance findTemplate(final int npcId)
	{
		L2NpcInstance npc = null;
		for (final L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if (spawn != null && spawn.getNpcid() == npcId)
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	protected void openGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final L2DoorInstance door = DoorTable.getInstance().getDoor(i);
				if (door != null)
				{
					door.openMe();
				}
				else
				{
					LOGGER.warn("IceFairySirraManager: Attempted to open undefined door. doorId: " + i);
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.error("IceFairySirraManager: Failed closing door", e);
			}
		}
	}
	
	protected void closeGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final L2DoorInstance door = DoorTable.getInstance().getDoor(i);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					LOGGER.warn("IceFairySirraManager: Attempted to close undefined door. doorId: " + i);
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				LOGGER.error("IceFairySirraManager: Failed closing door", e);
			}
		}
	}
	
	public boolean checkItems(final L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (final L2PcInstance pc : player.getParty().getPartyMembers())
			{
				final L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				if (i == null || i.getCount() < 10)
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public void destroyItems(final L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (final L2PcInstance pc : player.getParty().getPartyMembers())
			{
				final L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				pc.destroyItem("Hemocytes", i.getObjectId(), 10, null, false);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void teleportInside(final L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (final L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.teleToLocation(113533, -126159, -3488, false);
				if (freyasZone == null)
				{
					LOGGER.warn("IceFairySirraManager: Failed to load zone");
					cleanUp();
					return;
				}
				freyasZone.allowPlayerEntry(pc, 2103);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void screenMessage(final L2PcInstance player, final String text, final int time)
	{
		if (player.getParty() != null)
		{
			for (final L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.sendPacket(new ExShowScreenMessage(text, time));
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void doSpawns()
	{
		final int[][] mobs =
		{
			{
				29060,
				105546,
				-127892,
				-2768
			},
			{
				29056,
				102779,
				-125920,
				-2840
			},
			{
				22100,
				111719,
				-126646,
				-2992
			},
			{
				22102,
				109509,
				-128946,
				-3216
			},
			{
				22104,
				109680,
				-125756,
				-3136
			}
		};
		L2Spawn spawnDat;
		L2NpcTemplate template;
		try
		{
			for (int i = 0; i < 5; i++)
			{
				template = NpcTable.getInstance().getTemplate(mobs[i][0]);
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(mobs[i][1]);
					spawnDat.setLocy(mobs[i][2]);
					spawnDat.setLocz(mobs[i][3]);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(60);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					allMobs.add(spawnDat.doSpawn());
					spawnDat.stopRespawn();
				}
				else
				{
					LOGGER.warn("IceFairySirraManager: Data missing in NPC table for ID: " + mobs[i][0]);
				}
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("IceFairySirraManager: Spawns could not be initialized: " + e);
		}
	}
	
	public String getHtmlPath(final int val)
	{
		String pom = "";
		
		pom = "32029-" + val;
		if (val == 0)
		{
			pom = "32029";
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else
		{
			if (HtmCache.getInstance().isLoadable(temp))
			{
				return temp;
			}
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	public void sendHtml(final L2NpcInstance npc, final L2PcInstance player, final String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void run()
	{
	}
}
