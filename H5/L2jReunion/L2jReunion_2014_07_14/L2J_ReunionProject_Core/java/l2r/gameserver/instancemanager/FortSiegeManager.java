/*
 * Copyright (C) 2004-2014 L2J Server
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
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.CombatFlag;
import l2r.gameserver.model.FortSiegeSpawn;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.items.instance.L2ItemInstance;
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
	private FastMap<Integer, FastList<FortSiegeSpawn>> _commanderSpawnList;
	private FastMap<Integer, FastList<CombatFlag>> _flagList;
	private boolean _justToTerritory = true; // Changeable in fortsiege.properties
	private int _flagMaxCount = 1; // Changeable in fortsiege.properties
	private int _siegeClanMinLevel = 4; // Changeable in fortsiege.properties
	private int _siegeLength = 60; // Time in minute. Changeable in fortsiege.properties
	private int _countDownLength = 10; // Time in minute. Changeable in fortsiege.properties
	private int _suspiciousMerchantRespawnDelay = 180; // Time in minute. Changeable in fortsiege.properties
	private List<FortSiege> _sieges;
	
	protected FortSiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillData.FrequentSkill.SEAL_OF_RULER.getSkill(), false);
		character.addSkill(SkillData.FrequentSkill.BUILD_HEADQUARTERS.getSkill(), false);
	}
	
	/**
	 * @param activeChar The L2Character of the character can summon
	 * @param isCheckOnly
	 * @return true if character summon
	 */
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;
		Fort fort = FortManager.getInstance().getFort(player);
		
		if ((fort == null) || (fort.getResidenceId() <= 0))
		{
			text = "You must be on fort ground to summon this";
		}
		else if (!fort.getSiege().isInProgress())
		{
			text = "You can only summon this during a siege.";
		}
		else if ((player.getClanId() != 0) && (fort.getSiege().getAttackerClan(player.getClanId()) == null))
		{
			text = "You can only summon this as a registered attacker.";
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
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
		character.removeSkill(SkillData.FrequentSkill.SEAL_OF_RULER.getSkill());
		character.removeSkill(SkillData.FrequentSkill.BUILD_HEADQUARTERS.getSkill());
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
		_commanderSpawnList = new FastMap<>();
		_flagList = new FastMap<>();
		
		for (Fort fort : FortManager.getInstance().getForts())
		{
			FastList<FortSiegeSpawn> _commanderSpawns = new FastList<>();
			FastList<CombatFlag> _flagSpawns = new FastList<>();
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
					
					_commanderSpawns.add(new FortSiegeSpawn(fort.getResidenceId(), x, y, z, heading, npc_id, i));
				}
				catch (Exception e)
				{
					_log.warn("Error while loading commander(s) for " + fort.getName() + " fort.");
				}
			}
			
			_commanderSpawnList.put(fort.getResidenceId(), _commanderSpawns);
			
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
					
					_flagSpawns.add(new CombatFlag(fort.getResidenceId(), x, y, z, 0, flag_id));
				}
				catch (Exception e)
				{
					_log.warn("Error while loading flag(s) for " + fort.getName() + " fort.");
				}
			}
			_flagList.put(fort.getResidenceId(), _flagSpawns);
		}
	}
	
	public final FastList<FortSiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		if (_commanderSpawnList.containsKey(_fortId))
		{
			return _commanderSpawnList.get(_fortId);
		}
		return null;
	}
	
	public final FastList<CombatFlag> getFlagList(int _fortId)
	{
		if (_flagList.containsKey(_fortId))
		{
			return _flagList.get(_fortId);
		}
		return null;
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
		if (_sieges == null)
		{
			_sieges = new FastList<>();
		}
		return _sieges;
	}
	
	public final void addSiege(FortSiege fortSiege)
	{
		if (_sieges == null)
		{
			_sieges = new FastList<>();
		}
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
		
		FastList<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
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
		
		FastList<CombatFlag> fcf = _flagList.get(fort.getResidenceId());
		
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
