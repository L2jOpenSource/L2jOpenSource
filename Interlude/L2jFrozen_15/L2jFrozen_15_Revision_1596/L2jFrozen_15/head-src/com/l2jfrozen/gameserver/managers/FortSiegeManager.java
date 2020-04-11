package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import com.l2jfrozen.Config;
import com.l2jfrozen.FService;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.model.entity.siege.FortSiege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class FortSiegeManager
{
	private static final Logger LOGGER = Logger.getLogger(FortSiegeManager.class);
	private static final String SELECT_CLAN_ID_BY_CLAN_ID_AND_FORTRESS_ID = "SELECT clan_id FROM fortsiege_clans WHERE clan_id=? AND fort_id=?";
	
	public static final FortSiegeManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public FortSiegeManager()
	{
		load();
	}
	
	private int attackerMaxClans = 500; // Max number of clans
	private int attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int defenderMaxClans = 500; // Max number of clans
	private int defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config
	
	private Map<Integer, List<SiegeSpawn>> commanderSpawnList;
	private Map<Integer, List<SiegeSpawn>> flagList;
	
	private int controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int flagMaxCount = 1; // Changeable in siege.config
	private int siegeClanMinLevel = 4; // Changeable in siege.config
	private int siegeLength = 120; // Time in minute. Changeable in siege.config
	private List<FortSiege> sieges;
	
	public final void addSiegeSkills(final L2PcInstance character)
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
		Fort fort = FortManager.getInstance().getFort(player);
		
		if (fort == null || fort.getFortId() <= 0)
		{
			sm.addString("You must be on fort ground to summon this");
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			sm.addString("You can only summon this during a siege.");
		}
		else if (player.getClanId() != 0 && fort.getSiege().getAttackerClan(player.getClanId()) == null)
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
		fort = null;
		
		return false;
	}
	
	/**
	 * Return true if the clan is registered or owner of a fort<BR>
	 * <BR>
	 * @param  clan   The L2Clan of the player
	 * @param  fortid
	 * @return
	 */
	public final boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getHasFort() > 0)
		{
			return true;
		}
		
		boolean register = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_ID_BY_CLAN_ID_AND_FORTRESS_ID))
		{
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, fortid);
			
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
			LOGGER.error("FortSiegeManager.checkIsRegistered : Could not select from fortsiege_clans table", e);
		}
		return register;
	}
	
	public final void removeSiegeSkills(final L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
	
	private final void load()
	{
		LOGGER.info("Initializing FortSiegeManager");
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(FService.FORTSIEGE_CONFIGURATION_FILE));
			final Properties siegeSettings = new Properties();
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
			
			// Siege spawns settings
			commanderSpawnList = new HashMap<>();
			flagList = new HashMap<>();
			
			for (final Fort fort : FortManager.getInstance().getForts())
			{
				final List<SiegeSpawn> commanderSpawns = new ArrayList<>();
				final List<SiegeSpawn> flagSpawns = new ArrayList<>();
				
				for (int i = 1; i < 5; i++)
				{
					final String spawnParams = siegeSettings.getProperty(fort.getName() + "Commander" + Integer.toString(i), "");
					
					if (spawnParams.length() == 0)
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int heading = Integer.parseInt(st.nextToken());
						final int npc_id = Integer.parseInt(st.nextToken());
						
						commanderSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, heading, npc_id));
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("Error while loading commander(s) for " + fort.getName() + " fort.");
					}
				}
				
				commanderSpawnList.put(fort.getFortId(), commanderSpawns);
				
				for (int i = 1; i < 4; i++)
				{
					final String spawnParams = siegeSettings.getProperty(fort.getName() + "Flag" + Integer.toString(i), "");
					
					if (spawnParams.length() == 0)
					{
						break;
					}
					
					final StringTokenizer st = new StringTokenizer(spawnParams.trim(), ",");
					
					try
					{
						final int x = Integer.parseInt(st.nextToken());
						final int y = Integer.parseInt(st.nextToken());
						final int z = Integer.parseInt(st.nextToken());
						final int flag_id = Integer.parseInt(st.nextToken());
						
						flagSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, 0, flag_id));
					}
					catch (final Exception e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						LOGGER.warn("Error while loading flag(s) for " + fort.getName() + " fort.");
					}
				}
				flagList.put(fort.getFortId(), flagSpawns);
			}
			
		}
		catch (final Exception e)
		{
			LOGGER.error("Error while loading fortsiege data.");
			e.printStackTrace();
			
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public final List<SiegeSpawn> getCommanderSpawnList(final int fortId)
	{
		if (commanderSpawnList.containsKey(fortId))
		{
			return commanderSpawnList.get(fortId);
		}
		return null;
	}
	
	public final List<SiegeSpawn> getFlagList(final int fortId)
	{
		if (flagList.containsKey(fortId))
		{
			return flagList.get(fortId);
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
	
	public final FortSiege getSiege(final L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final FortSiege getSiege(final int x, final int y, final int z)
	{
		for (final Fort fort : FortManager.getInstance().getForts())
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
		return siegeClanMinLevel;
	}
	
	public final int getSiegeLength()
	{
		return siegeLength;
	}
	
	public final List<FortSiege> getSieges()
	{
		if (sieges == null)
		{
			sieges = new ArrayList<>();
		}
		return sieges;
	}
	
	public final void addSiege(final FortSiege fortSiege)
	{
		if (sieges == null)
		{
			sieges = new ArrayList<>();
		}
		sieges.add(fortSiege);
	}
	
	public final void removeSiege(final FortSiege fortSiege)
	{
		if (sieges == null)
		{
			sieges = new ArrayList<>();
		}
		sieges.remove(fortSiege);
	}
	
	public boolean isCombat(final int itemId)
	{
		return itemId == 9819;
	}
	
	public class SiegeSpawn
	{
		Location location;
		private final int npcId;
		private final int heading;
		private final int fortId;
		private int hp;
		
		public SiegeSpawn(final int fort_id, final int x, final int y, final int z, final int heading, final int npc_id)
		{
			fortId = fort_id;
			location = new Location(x, y, z, heading);
			this.heading = heading;
			npcId = npc_id;
		}
		
		public SiegeSpawn(final int fort_id, final int x, final int y, final int z, final int heading, final int npc_id, final int hp)
		{
			fortId = fort_id;
			location = new Location(x, y, z, heading);
			this.heading = heading;
			npcId = npc_id;
			this.hp = hp;
		}
		
		public int getFortId()
		{
			return fortId;
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
	
	public final boolean checkIsRegisteredInSiege(final L2Clan clan)
	{
		for (final Fort fort : FortManager.getInstance().getForts())
		{
			if (checkIsRegistered(clan, fort.getFortId()) && fort.getSiege() != null && fort.getSiege().getIsInProgress())
			{
				return true;
			}
		}
		
		return false;
		
	}
	
	private static class SingletonHolder
	{
		protected static final FortSiegeManager instance = new FortSiegeManager();
	}
}
