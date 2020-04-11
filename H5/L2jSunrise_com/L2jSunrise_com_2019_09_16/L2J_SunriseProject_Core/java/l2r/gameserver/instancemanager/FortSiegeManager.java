/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.CombatFlag;
import l2r.gameserver.model.FortSiegeSpawn;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.CommonSkill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FortSiegeManager
{
	private static final Logger _log = LoggerFactory.getLogger(FortSiegeManager.class);
	
	public static final FortSiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private int _attackerMaxClans = 500; // Max number of clans
	
	// Fort Siege settings
	private Map<Integer, List<FortSiegeSpawn>> _commanderSpawnList;
	private Map<Integer, List<CombatFlag>> _flagList;
	private boolean _justToTerritory = true; // Changeable in fortsiege.properties
	private int _flagMaxCount = 1; // Changeable in fortsiege.properties
	private int _siegeClanMinLevel = 4; // Changeable in fortsiege.properties
	private int _siegeLength = 60; // Time in minute. Changeable in fortsiege.properties
	private int _countDownLength = 10; // Time in minute. Changeable in fortsiege.properties
	private int _suspiciousMerchantRespawnDelay = 180; // Time in minute. Changeable in fortsiege.properties
	private final List<FortSiege> _sieges = new ArrayList<>();
	
	protected FortSiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(CommonSkill.SEAL_OF_RULER.getSkill(), false);
		character.addSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill(), false);
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @param fortid
	 * @return true if the clan is registered or owner of a fort
	 */
	public final boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if (clan == null)
		{
			return false;
		}
		
		boolean register = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?"))
		{
			ps.setInt(1, clan.getId());
			ps.setInt(2, fortid);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					register = true;
					break;
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(CommonSkill.SEAL_OF_RULER.getSkill());
		character.removeSkill(CommonSkill.BUILD_HEADQUARTERS.getSkill());
	}
	
	private final void load()
	{
		Properties siegeSettings = new Properties();
		final File file = new File(Config.FORTSIEGE_CONFIGURATION_FILE);
		try (InputStream is = new FileInputStream(file))
		{
			siegeSettings.load(is);
		}
		catch (Exception e)
		{
			_log.warn("Error while loading Fort Siege Manager settings!", e);
		}
		
		// Siege setting
		_justToTerritory = Boolean.parseBoolean(siegeSettings.getProperty("JustToTerritory", "true"));
		_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
		_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
		_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
		_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "60"));
		_countDownLength = Integer.decode(siegeSettings.getProperty("CountDownLength", "10"));
		_suspiciousMerchantRespawnDelay = Integer.decode(siegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));
		
		// Siege spawns settings
		_commanderSpawnList = new ConcurrentHashMap<>();
		_flagList = new ConcurrentHashMap<>();
		
		for (Fort fort : FortManager.getInstance().getForts())
		{
			List<FortSiegeSpawn> commanderSpawns = new ArrayList<>();
			List<CombatFlag> flagSpawns = new ArrayList<>();
			for (int i = 1; i < 5; i++)
			{
				String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}
				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npc_id = Integer.parseInt(st.nextToken());
					
					commanderSpawns.add(new FortSiegeSpawn(fort.getResidenceId(), x, y, z, heading, npc_id, i));
				}
				catch (Exception e)
				{
					_log.warn("Error while loading commander(s) for " + fort.getName() + " fort.");
				}
			}
			
			_commanderSpawnList.put(fort.getResidenceId(), commanderSpawns);
			
			for (int i = 1; i < 4; i++)
			{
				String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Flag" + i, "");
				if (_spawnParams.isEmpty())
				{
					break;
				}
				StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int flag_id = Integer.parseInt(st.nextToken());
					
					flagSpawns.add(new CombatFlag(fort.getResidenceId(), x, y, z, 0, flag_id));
				}
				catch (Exception e)
				{
					_log.warn("Error while loading flag(s) for " + fort.getName() + " fort.");
				}
			}
			_flagList.put(fort.getResidenceId(), flagSpawns);
		}
	}
	
	public final List<FortSiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		return _commanderSpawnList.get(_fortId);
	}
	
	public final List<CombatFlag> getFlagList(int _fortId)
	{
		return _flagList.get(_fortId);
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public final boolean canRegisterJustTerritory()
	{
		return _justToTerritory;
	}
	
	public final int getSuspiciousMerchantRespawnDelay()
	{
		return _suspiciousMerchantRespawnDelay;
	}
	
	public final FortSiege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final FortSiege getSiege(int x, int y, int z)
	{
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (fort.getSiege().checkIfInZone(x, y, z))
			{
				return fort.getSiege();
			}
		}
		return null;
	}
	
	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}
	
	public final int getSiegeLength()
	{
		return _siegeLength;
	}
	
	public final int getCountDownLength()
	{
		return _countDownLength;
	}
	
	public final List<FortSiege> getSieges()
	{
		return _sieges;
	}
	
	public final void addSiege(FortSiege fortSiege)
	{
		_sieges.add(fortSiege);
	}
	
	public boolean isCombat(int itemId)
	{
		return (itemId == 9819);
	}
	
	public boolean activateCombatFlag(L2PcInstance player, L2ItemInstance item)
	{
		if (!checkIfCanPickup(player))
		{
			return false;
		}
		
		Fort fort = FortManager.getInstance().getFort(player);
		
		final List<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
		for (CombatFlag cf : fcf)
		{
			if (cf.getCombatFlagInstance() == item)
			{
				cf.activate(player, item);
			}
		}
		return true;
	}
	
	public boolean checkIfCanPickup(L2PcInstance player)
	{
		SystemMessage sm;
		sm = SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED);
		sm.addItemName(9819);
		// Cannot own 2 combat flag
		if (player.isCombatFlagEquipped())
		{
			player.sendPacket(sm);
			return false;
		}
		
		// here check if is siege is in progress
		// here check if is siege is attacker
		Fort fort = FortManager.getInstance().getFort(player);
		
		if ((fort == null) || (fort.getResidenceId() <= 0))
		{
			player.sendPacket(sm);
			return false;
		}
		else if (!fort.getSiege().isInProgress())
		{
			player.sendPacket(sm);
			return false;
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			player.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	public void dropCombatFlag(L2PcInstance player, int fortId)
	{
		Fort fort = FortManager.getInstance().getFortById(fortId);
		
		final List<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
		for (CombatFlag cf : fcf)
		{
			if (cf.getPlayerObjectId() == player.getObjectId())
			{
				cf.dropIt();
				if (fort.getSiege().isInProgress())
				{
					cf.spawnMe();
				}
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final FortSiegeManager _instance = new FortSiegeManager();
	}
}
