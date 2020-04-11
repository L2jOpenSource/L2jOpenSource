/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.TowerSpawn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Siege;
import com.l2jserver.gameserver.model.interfaces.ILocational;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.util.PropertiesParser;

public final class SiegeManager
{
	private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());
	
	private final Map<Integer, List<TowerSpawn>> _controlTowers = new HashMap<>();
	private final Map<Integer, List<TowerSpawn>> _flameTowers = new HashMap<>();
	
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 0; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 5; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	private int _bloodAllianceReward = 0; // Number of Blood Alliance items reward for successful castle defending
	
	protected SiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.addSkill(sk, false);
		}
	}
	
	/**
	 * @param clan The L2Clan of the player
	 * @param castleid
	 * @return true if the clan is registered or owner of a castle
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getCastleId() > 0)
		{
			return true;
		}
		
		boolean register = false;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?"))
		{
			ps.setInt(1, clan.getId());
			ps.setInt(2, castleid);
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
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception: checkIsRegistered(): " + e.getMessage(), e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character)
	{
		for (Skill sk : SkillData.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.removeSkill(sk);
		}
	}
	
	private final void load()
	{
		final PropertiesParser siegeSettings = new PropertiesParser(Config.SIEGE_CONFIGURATION_FILE);
		
		// Siege setting
		_attackerMaxClans = siegeSettings.getInt("AttackerMaxClans", 500);
		_attackerRespawnDelay = siegeSettings.getInt("AttackerRespawn", 0);
		_defenderMaxClans = siegeSettings.getInt("DefenderMaxClans", 500);
		_flagMaxCount = siegeSettings.getInt("MaxFlags", 1);
		_siegeClanMinLevel = siegeSettings.getInt("SiegeClanMinLevel", 5);
		_siegeLength = siegeSettings.getInt("SiegeLength", 120);
		_bloodAllianceReward = siegeSettings.getInt("BloodAllianceReward", 1);
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			final List<TowerSpawn> controlTowers = new ArrayList<>();
			for (int i = 1; i < 0xFF; i++)
			{
				final String settingsKeyName = castle.getName() + "ControlTower" + i;
				if (!siegeSettings.containskey(settingsKeyName))
				{
					break;
				}
				
				final StringTokenizer st = new StringTokenizer(siegeSettings.getString(settingsKeyName, ""), ",");
				try
				{
					final int x = Integer.parseInt(st.nextToken());
					final int y = Integer.parseInt(st.nextToken());
					final int z = Integer.parseInt(st.nextToken());
					final int npcId = Integer.parseInt(st.nextToken());
					
					controlTowers.add(new TowerSpawn(npcId, new Location(x, y, z)));
				}
				catch (Exception e)
				{
					_log.warning(getClass().getSimpleName() + ": Error while loading control tower(s) for " + castle.getName() + " castle.");
				}
			}
			
			final List<TowerSpawn> flameTowers = new ArrayList<>();
			for (int i = 1; i < 0xFF; i++)
			{
				final String settingsKeyName = castle.getName() + "FlameTower" + i;
				if (!siegeSettings.containskey(settingsKeyName))
				{
					break;
				}
				
				final StringTokenizer st = new StringTokenizer(siegeSettings.getString(settingsKeyName, ""), ",");
				try
				{
					final int x = Integer.parseInt(st.nextToken());
					final int y = Integer.parseInt(st.nextToken());
					final int z = Integer.parseInt(st.nextToken());
					final int npcId = Integer.parseInt(st.nextToken());
					final List<Integer> zoneList = new ArrayList<>();
					
					while (st.hasMoreTokens())
					{
						zoneList.add(Integer.parseInt(st.nextToken()));
					}
					
					flameTowers.add(new TowerSpawn(npcId, new Location(x, y, z), zoneList));
				}
				catch (Exception e)
				{
					_log.warning(getClass().getSimpleName() + ": Error while loading flame tower(s) for " + castle.getName() + " castle.");
				}
			}
			_controlTowers.put(castle.getResidenceId(), controlTowers);
			_flameTowers.put(castle.getResidenceId(), flameTowers);
			MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getResidenceId() - 1] = siegeSettings.getInt(castle.getName() + "MaxMercenaries", MercTicketManager.MERCS_MAX_PER_CASTLE[castle.getResidenceId() - 1]);
			
			if (castle.getOwnerId() != 0)
			{
				loadTrapUpgrade(castle.getResidenceId());
			}
		}
	}
	
	public final List<TowerSpawn> getControlTowers(int castleId)
	{
		return _controlTowers.get(castleId);
	}
	
	public final List<TowerSpawn> getFlameTowers(int castleId)
	{
		return _flameTowers.get(castleId);
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}
	
	public final int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}
	
	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public final Siege getSiege(ILocational loc)
	{
		return getSiege(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
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
	
	public final int getBloodAllianceReward()
	{
		return _bloodAllianceReward;
	}
	
	public final List<Siege> getSieges()
	{
		List<Siege> sieges = new ArrayList<>();
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	private final void loadTrapUpgrade(int castleId)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_trapupgrade WHERE castleId=?"))
		{
			ps.setInt(1, castleId);
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					_flameTowers.get(castleId).get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: loadTrapUpgrade(): " + e.getMessage(), e);
		}
	}
	
	public static final SiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeManager _instance = new SiegeManager();
	}
}