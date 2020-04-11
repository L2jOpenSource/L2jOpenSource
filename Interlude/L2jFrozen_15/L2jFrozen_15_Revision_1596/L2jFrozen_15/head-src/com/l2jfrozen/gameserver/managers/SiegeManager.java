package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.FService;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class SiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(SiegeManager.class);
	
	private static final String SELECT_CLAN_ID_FROM_SIEGE_CLANS = "SELECT clan_id FROM siege_clans WHERE clan_id=? AND castle_id=?";
	
	public static final SiegeManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private int attackerMaxClans = 500; // Max number of clans
	private int attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int defenderMaxClans = 500; // Max number of clans
	private int defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config
	
	// Siege settings
	private Map<Integer, List<SiegeSpawn>> artefactSpawnList;
	private Map<Integer, List<SiegeSpawn>> controlTowerSpawnList;
	
	private int controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int flagMaxCount = 1; // Changeable in siege.config
	private int siegeClanMinLevel = 4; // Changeable in siege.config
	private int siegeLength = 120; // Time in minute. Changeable in siege.config
	
	private boolean teleport_to_siege = false;
	private boolean teleport_to_siege_town = false;
	
	public SiegeManager()
	{
		load();
	}
	
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}
	
	/**
	 * Return true if character summon<BR>
	 * <BR>
	 * @param  activeChar  The L2Character of the character can summon
	 * @param  isCheckOnly
	 * @return
	 */
	public final boolean checkIfOkToSummon(final L2Character activeChar, final boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		Castle castle = CastleManager.getInstance().getCastle(player);
		
		if (castle == null || castle.getCastleId() <= 0)
		{
			sm.addString("You must be on castle ground to summon this");
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm.addString("You can only summon this during a siege.");
		}
		else if (player.getClanId() != 0 && castle.getSiege().getAttackerClan(player.getClanId()) == null)
		{
			sm.addString("You can only summon this as a registered attacker.");
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		sm = null;
		player = null;
		castle = null;
		
		return false;
	}
	
	public final boolean checkIsRegisteredInSiege(final L2Clan clan)
	{
		
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (checkIsRegistered(clan, castle.getCastleId()) && castle.getSiege() != null && castle.getSiege().getIsInProgress())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return true if the clan is registered or owner of a castle<BR>
	 * <BR>
	 * @param  clan     The L2Clan of the player
	 * @param  castleid
	 * @return
	 */
	public final boolean checkIsRegistered(final L2Clan clan, final int castleid)
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
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_ID_FROM_SIEGE_CLANS))
		{
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
			
			try (ResultSet rs = statement.executeQuery())
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
			LOGGER.error("SiegeManager.checkIsRegistered : Could not select from siege_clans table", e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(final L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
	
	private void load()
	{
		LOGGER.info("Initializing SiegeManager");
		
		try (InputStream is = new FileInputStream(new File(FService.SIEGE_CONFIGURATION_FILE)))
		{
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			
			// Siege setting
			attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000"));
			controlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000"));
			defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			defenderRespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000"));
			flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
			siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
			
			// Siege Teleports
			teleport_to_siege = Boolean.parseBoolean(siegeSettings.getProperty("AllowTeleportToSiege", "false"));
			teleport_to_siege_town = Boolean.parseBoolean(siegeSettings.getProperty("AllowTeleportToSiegeTown", "false"));
			
			// Siege spawns settings
			controlTowerSpawnList = new HashMap<>();
			artefactSpawnList = new HashMap<>();
			
			for (final Castle castle : CastleManager.getInstance().getCastles())
			{
				List<SiegeSpawn> controlTowersSpawns = new ArrayList<>();
				
				for (int i = 1; i < 20; i++)
				{
					String spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + i, "");
					
					if (spawnParams.length() == 0)
					{
						break;
					}
					
					StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int hp = Integer.parseInt(st.nextToken());
						
						// NPC ID 13002 is Life Control Tower
						controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, 13002, hp));
					}
					catch (Exception e)
					{
						LOGGER.error("SiegeManager.load : Error while loading control tower(s) for " + castle.getName() + " castle from siege.properties", e);
					}
				}
				
				List<SiegeSpawn> artefactSpawns = new ArrayList<>();
				
				for (int i = 1; i < 20; i++)
				{
					String spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + i, "");
					
					if (spawnParams.length() == 0)
					{
						break;
					}
					
					StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						
						artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						LOGGER.warn("SiegeManager.load : Error while loading artefact(s) for " + castle.getName() + " castle.", e);
					}
				}
				
				controlTowerSpawnList.put(castle.getCastleId(), controlTowersSpawns);
				artefactSpawnList.put(castle.getCastleId(), artefactSpawns);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("SiegeManager.load : Error while loading siege data.", e);
		}
	}
	
	public final List<SiegeSpawn> getArtefactSpawnList(final int castleId)
	{
		if (artefactSpawnList.containsKey(castleId))
		{
			return artefactSpawnList.get(castleId);
		}
		return null;
	}
	
	public final List<SiegeSpawn> getControlTowerSpawnList(final int castleId)
	{
		if (controlTowerSpawnList.containsKey(castleId))
		{
			return controlTowerSpawnList.get(castleId);
		}
		return null;
	}
	
	public final int getAttackerMaxClans()
	{
		return attackerMaxClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return attackerRespawnDelay;
	}
	
	public final int getControlTowerLosePenalty()
	{
		return controlTowerLosePenalty;
	}
	
	public final int getDefenderMaxClans()
	{
		return defenderMaxClans;
	}
	
	public final int getDefenderRespawnDelay()
	{
		return defenderRespawnDelay;
	}
	
	public final int getFlagMaxCount()
	{
		return flagMaxCount;
	}
	
	public final Siege getSiege(final L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final Siege getSiege(final int x, final int y, final int z)
	{
		for (final Castle castle : CastleManager.getInstance().getCastles())
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
		return siegeClanMinLevel;
	}
	
	public final int getSiegeLength()
	{
		return siegeLength;
	}
	
	public final List<Siege> getSieges()
	{
		final List<Siege> sieges = new ArrayList<>();
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	/**
	 * @return the teleport_to_siege
	 */
	public boolean is_teleport_to_siege_allowed()
	{
		return teleport_to_siege;
	}
	
	/**
	 * @return the teleport_to_siege_town
	 */
	public boolean is_teleport_to_siege_town_allowed()
	{
		return teleport_to_siege_town;
	}
	
	public class SiegeSpawn
	{
		Location location;
		private final int npcId;
		private final int heading;
		private final int castleId;
		private int hp;
		
		public SiegeSpawn(final int castle_id, final int x, final int y, final int z, final int heading, final int npc_id)
		{
			castleId = castle_id;
			location = new Location(x, y, z, heading);
			this.heading = heading;
			npcId = npc_id;
		}
		
		public SiegeSpawn(final int castle_id, final int x, final int y, final int z, final int heading, final int npc_id, final int hp)
		{
			castleId = castle_id;
			location = new Location(x, y, z, heading);
			this.heading = heading;
			npcId = npc_id;
			this.hp = hp;
		}
		
		public int getCastleId()
		{
			return castleId;
		}
		
		public int getNpcId()
		{
			return npcId;
		}
		
		public int getHeading()
		{
			return heading;
		}
		
		public int getHp()
		{
			return hp;
		}
		
		public Location getLocation()
		{
			return location;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeManager instance = new SiegeManager();
	}
}
