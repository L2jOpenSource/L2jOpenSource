package com.l2jfrozen.gameserver.model.zone.type;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author DaRkRaGe
 * @author ReynalDev
 */
public class L2BossZone extends L2ZoneType
{
	private static final Logger LOGGER = Logger.getLogger(L2BossZone.class);
	private static final String SELECT_ALLOWED_PLAYERS_OBJ_ID = "SELECT player_id FROM grandboss_list WHERE zone=?";
	private boolean enabled = true; // default value, unless overridden by xml...
	private boolean isFlyingEnable = true; // default value, unless overridden by xml...
	private int bossId;
	
	/**
	 * Boss zones have special behaviors for player characters. Players are automatically teleported out when the attempt to enter these zones, except if the time at which they enter the zone is prior to the entry expiration time set for that player. Entry expiration times are set by any one of the
	 * following: 1) A player logs out while in a zone (Expiration gets set to logoutTime + timeInvade) 2) An external source (such as a quest or AI of NPC) set up the player for entry. There exists one more case in which the player will be allowed to enter. That is if the server recently rebooted AND
	 * the player was in the zone prior to reboot.
	 */
	private long timeInvade = 0; // Time in miliseconds, time allowed to RELOG INSIDE the zone
	private Map<Integer, Long> playerAllowedReEntryTimes = new ConcurrentHashMap<>();
	private List<Integer> playersAllowed = new ArrayList<>();
	
	public L2BossZone(int id)
	{
		super(id);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement pst = con.prepareStatement(SELECT_ALLOWED_PLAYERS_OBJ_ID))
		{
			pst.setInt(1, getZoneId());
			
			try (ResultSet rs = pst.executeQuery())
			{
				while (rs.next())
				{
					playersAllowed.add(rs.getInt("player_id"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("L2BossZone.L2BossZone: Problem while reading data from grandboss_list table ", e);
		}
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("bossId"))
		{
			bossId = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("InvadeTime"))
		{
			timeInvade = Long.parseLong(value);
		}
		else if (name.equalsIgnoreCase("EnabledByDefault"))
		{
			enabled = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("flying"))
		{
			isFlyingEnable = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (!enabled)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) character;
			
			if (player.isGM())
			{
				player.sendMessage("You entered " + getZoneName());
			}
			
			// Ignore the check for Van Halter zone id 12014 if player got marks
			if (getZoneId() == 12014)
			{
				L2ItemInstance visitorsMark = player.getInventory().getItemByItemId(8064);
				L2ItemInstance fadedVisitorsMark = player.getInventory().getItemByItemId(8065);
				L2ItemInstance pagansMark = player.getInventory().getItemByItemId(8067);
				
				long mark1 = visitorsMark == null ? 0 : visitorsMark.getCount();
				long mark2 = fadedVisitorsMark == null ? 0 : fadedVisitorsMark.getCount();
				long mark3 = pagansMark == null ? 0 : pagansMark.getCount();
				
				if (mark1 != 0 || mark2 != 0 || mark3 != 0)
				{
					return;
				}
			}
			
			if (!player.isGM() && player.isFlying() && !isFlyingEnable)
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				return;
			}
			
			if (!isPlayerAllowed(player))
			{
				return;
			}
			
			if (timeInvade > 0)
			{
				// Last time player was disconnected INSIDE BossZone
				long endTimeAllowed = playerAllowedReEntryTimes.getOrDefault(player.getObjectId(), 0L);
				
				if (endTimeAllowed > 0)
				{
					long now = System.currentTimeMillis();
					if (endTimeAllowed < now)
					{
						String text = "BossZone: Player " + player.getName() + " exceeded the allowed time to RELOG INSIDE BossZone ID " + getZoneId() + ", sending to Town. ";
						LOGGER.info(text);
						Log.add(text + "|InvadeTime=" + timeInvade, "bossZone_relog");
						kickFromZone(player);
						return;
					}
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (!enabled)
		{
			return;
		}
		
		if (character == null)
		{
			return;
		}
		
		if (character.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) character;
			
			if (player.isGM())
			{
				player.sendMessage("You left " + getZoneName());
				return;
			}
			
			// if the player just got disconnected/logged out, store the dc time so that
			// decisions can be made later about allowing or not the player to LOG IN into the zone
			// mark the time that the player left the zone + time allowed to reentry
			if (timeInvade > 0)
			{
				playerAllowedReEntryTimes.put(character.getObjectId(), System.currentTimeMillis() + timeInvade);
			}
		}
	}
	
	/**
	 * Some GrandBosses send all players in zone to a specific part of the zone, rather than just removing them all. If this is the case, this command should be used. If this is no the case, then use oustAllPlayers().
	 * @param x
	 * @param y
	 * @param z
	 */
	public void movePlayersTo(int x, int y, int z)
	{
		if (characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : characterList.values())
		{
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline())
				{
					player.teleToLocation(x, y, z);
				}
			}
		}
	}
	
	public void setZoneEnabled(boolean flag)
	{
		if (enabled != flag)
		{
			oustAllPlayers();
		}
		
		enabled = flag;
	}
	
	public long getTimeInvade()
	{
		return timeInvade;
	}
	
	public void setAllowedPlayers(List<Integer> list)
	{
		if (list != null)
		{
			playersAllowed = list;
		}
	}
	
	public List<Integer> getAllowedPlayers()
	{
		return playersAllowed;
	}
	
	public boolean isPlayerAllowed(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		else if (Config.ALLOW_DIRECT_TP_TO_BOSS_ROOM)
		{
			return true;
		}
		else if (timeInvade <= 0)
		{
			return true;
		}
		else if (playersAllowed.contains(player.getObjectId()))
		{
			return true;
		}
		else
		{
			String text = "BossZone: Player " + player.getName() + " is NOT ALLOWED to enter in BossZone ID " + getZoneId() + ", sending to Town. ";
			LOGGER.info(text);
			Log.add(text, "bossZone_relog");
			kickFromZone(player);
			return false;
		}
	}
	
	/**
	 * Occasionally, all players need to be sent out of the zone (for example, if the players are just running around without fighting for too long, or if all players die, etc). This call sends all online players to town and marks offline players to be teleported (by clearing their relog
	 * expirationtimes) when they LOG IN back in (no real need for off-line teleport).
	 */
	public void oustAllPlayers()
	{
		if (characterList == null)
		{
			return;
		}
		
		if (characterList.isEmpty())
		{
			return;
		}
		
		for (L2Character character : characterList.values())
		{
			if (character == null)
			{
				continue;
			}
			
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				
				if (player.isOnline())
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
				
			}
		}
		
		playerAllowedReEntryTimes.clear();
		playersAllowed.clear();
	}
	
	/**
	 * This function is to be used by external sources, such as quests and AI in order to allow a player for entry into the zone for some time. Naturally if the player does not enter within the allowed time, he/she will be teleported out again...
	 * @param player        reference to the player we wish to allow
	 * @param durationInSec amount of time in seconds during which entry is valid.
	 */
	public void allowPlayerEntry(L2PcInstance player, int durationInSec)
	{
		if (!player.isGM())
		{
			if (!playersAllowed.contains(player.getObjectId()))
			{
				playersAllowed.add(player.getObjectId());
			}
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
	}
	
	public void updateKnownList(L2NpcInstance npc)
	{
		if (characterList == null || characterList.isEmpty())
		{
			return;
		}
		
		Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
		
		for (L2Character character : characterList.values())
		{
			if (character == null)
			{
				continue;
			}
			
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline() || player.isInOfflineMode())
				{
					npcKnownPlayers.put(player.getObjectId(), player);
				}
			}
		}
	}
	
	public int getBossId()
	{
		return bossId;
	}
	
	public void deleteAllowedInfo(L2PcInstance player)
	{
		playerAllowedReEntryTimes.remove(player.getObjectId());
		
		if (playersAllowed.contains(player.getObjectId()))
		{
			playersAllowed.remove(Integer.valueOf(player.getObjectId()));
		}
	}
	
	public void kickFromZone(L2PcInstance player)
	{
		// teleport out all players who attempt "illegal" (re-)entry
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			deleteAllowedInfo(player);
		}, 500);
	}
}
