package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.DayNightSpawnManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.data.memory.ObjectData;

/**
 * This class ...
 * @author  Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
	private final static Logger LOGGER = Logger.getLogger(SpawnTable.class);
	private static final String SELECT_SPAWNLIST = "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id";
	private static final String DELETE_SPAWN_BY_ID = "DELETE FROM spawnlist WHERE id=?";
	private static final String SELECT_CUSTOM_SPAWNLIST = "SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM custom_spawnlist ORDER BY id";
	private static final String INSERT_CUSTOM_SPAWNLIST = "INSERT INTO custom_spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_CUSTOM_SPAWN_BY_ID = "DELETE FROM custom_spawnlist WHERE id=?";
	
	private static final SpawnTable instance = new SpawnTable();
	
	private final Map<Integer, L2Spawn> spawntable = new ConcurrentHashMap<>();
	private int npcSpawnCount;
	private int customSpawnCount;
	
	private int highestId;
	
	public static SpawnTable getInstance()
	{
		return instance;
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			fillSpawnTable();
		}
	}
	
	public Map<Integer, L2Spawn> getSpawnTable()
	{
		return spawntable;
	}
	
	private void fillSpawnTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_SPAWNLIST);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template != null)
				{
					if (ignoreTemplate(template))
					{
						// Read the method
					}
					else
					{
						L2Spawn spawnDat = new L2Spawn(template);
						spawnDat.setId(rset.getInt("id"));
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						
						int loc_id = rset.getInt("loc_id");
						
						spawnDat.setLocation(loc_id);
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0: // default
								npcSpawnCount += spawnDat.init();
								break;
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								npcSpawnCount++;
								break;
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								npcSpawnCount++;
								break;
						}
						
						spawntable.put(spawnDat.getId(), spawnDat);
						
						if (spawnDat.getId() > highestId)
						{
							highestId = spawnDat.getId();
						}
						
						if (spawnDat.getTemplate().getNpcId() == Olympiad.OLY_MANAGER)
						{
							Olympiad.olymanagers.add(spawnDat);
						}
					}
				}
				else
				{
					LOGGER.warn("SpawnTable.fillSpawnTable : Data missing in NPC table for ID: " + rset.getInt("npc_templateid"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("SpawnTable.fillSpawnTable : Spawn could not be initialized ", e);
		}
		
		LOGGER.info("SpawnTable: Loaded " + spawntable.size() + " NPC spawn locations. ");
		
		// -------------------------------Custom Spawnlist----------------------------//
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CUSTOM_SPAWNLIST);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				
				if (template != null)
				{
					if (ignoreTemplate(template))
					{
						// Read the method
					}
					else
					{
						L2Spawn spawnDat = new L2Spawn(template);
						spawnDat.setIsCustomSpawn(true);
						spawnDat.setId(rset.getInt("id"));
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						
						int loc_id = rset.getInt("loc_id");
						
						spawnDat.setLocation(loc_id);
						
						switch (rset.getInt("periodOfDay"))
						{
							case 0: // default
								customSpawnCount += spawnDat.init();
								break;
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								customSpawnCount++;
								break;
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								customSpawnCount++;
								break;
						}
						
						spawntable.put(spawnDat.getId(), spawnDat);
						
						if (spawnDat.getId() > highestId)
						{
							highestId = spawnDat.getId();
						}
					}
				}
				else
				{
					LOGGER.warn("SpawnTable.fillSpawnTable : Data missing in Custom NPC table for ID: " + rset.getInt("npc_templateid"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("SpawnTable.fillSpawnTable: Custom spawn could not be initialized ", e);
		}
		
		if (customSpawnCount > 0)
		{
			LOGGER.info("CustomSpawnTable: Loaded " + customSpawnCount + " Npc Spawn Locations. ");
		}
		
		LOGGER.info("SpawnTable: Spawning completed, total number of NPCs in the world: " + (npcSpawnCount + customSpawnCount));
	}
	
	public L2Spawn getTemplate(final int id)
	{
		return spawntable.get(id);
	}
	
	/**
	 * All new NPCs spawns will be stored in custom_spawn_list.
	 * @param spawn
	 * @param storeInDb : <B>true</B> --> Wil be stored in db, <B>false</B> --> Will not store in db
	 */
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		highestId++;
		spawn.setId(highestId);
		spawntable.put(highestId, spawn);
		
		if (storeInDb)
		{
			// OLD SQL ---> "INSERT INTO " + (spawn.isCustom() ? "custom_spawnlist" : "spawnlist") + "(id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) VALUES (?,?,?,?,?,?,?,?,?)"
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(INSERT_CUSTOM_SPAWNLIST))
			{
				statement.setInt(1, spawn.getId());
				statement.setInt(2, spawn.getAmount());
				statement.setInt(3, spawn.getNpcid());
				statement.setInt(4, spawn.getLocx());
				statement.setInt(5, spawn.getLocy());
				statement.setInt(6, spawn.getLocz());
				statement.setInt(7, spawn.getHeading());
				statement.setInt(8, spawn.getRespawnDelay() / 1000);
				statement.setInt(9, spawn.getLocation());
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("SpawnTable: Could not insert NPC spawn in custom_spawnlist table", e);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (spawntable.remove(spawn.getId()) == null)
		{
			return;
		}
		
		ObjectData.removeObject(spawn.getLastSpawn());
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(spawn.isCustomSpawn() ? DELETE_CUSTOM_SPAWN_BY_ID : DELETE_SPAWN_BY_ID))
			{
				statement.setInt(1, spawn.getId());
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("SpawnTable: Spawn {} could not be removed from DB " + spawn.getId(), e);
			}
		}
	}
	
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	/**
	 * Send html dialog to player with all the spawns of a NPC<BR>
	 * <BR>
	 * @param activeChar    : Player to send htm dialog
	 * @param npcId         : ID of the NPC to find.
	 * @param teleportIndex : If there is 2 spawns and this parameter is equals to 2, player will be teleported
	 * @param spawnId       : In the list showed of spawns, the same spawn of the NPC will be ignored
	 */
	public void showSpawnList(L2PcInstance activeChar, int npcId, int teleportIndex, int spawnId)
	{
		if (activeChar == null)
		{
			return;
		}
		
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
		
		if (npc == null)
		{
			activeChar.sendMessage("No spawn found with NPC ID: " + npcId);
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		int index = 0;
		
		sb.append("<html><title>").append(npc.getName()).append(" (").append(npc.getNpcId()).append(")").append("</title>");
		sb.append("<body>");
		
		for (L2Spawn spawn : spawntable.values())
		{
			if (npcId == spawn.getNpcid())
			{
				index++;
				
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
						return;
					}
				}
				else
				{
					if (spawn.getId() == spawnId)
					{
						sb.append("<br1>");
						sb.append(index).append(" [").append(spawn.getLocx()).append(" ").append(spawn.getLocy()).append(" ").append(spawn.getLocz()).append("] -- Current");
						sb.append("<br1>");
					}
					else
					{
						sb.append("<a action=\"bypass -h admin_move_to ").append(spawn.getLocx()).append(" ").append(spawn.getLocy()).append(" ").append(spawn.getLocz()).append("\">");
						sb.append(index).append(" [").append(spawn.getLocx()).append(" ").append(spawn.getLocy()).append(" ").append(spawn.getLocz()).append("]</a><br1>");
					}
				}
			}
		}
		sb.append("</body></html>");
		
		NpcHtmlMessage dialog = new NpcHtmlMessage(5);
		dialog.setHtml(sb.toString());
		activeChar.sendPacket(dialog);
	}
	
	public Map<Integer, L2Spawn> getAllSpawns()
	{
		return spawntable;
	}
	
	/**
	 * @param  template This NPC SHOULD NOT be SPAWNED when loading database spawnlist or custom_spawnlist
	 * @return          For example NPCs with TYPE <b>L2SiegeGuard</b> Should NOT spawn when server is loading, cause <b>L2SiegeGuard</b> will spawn ONLY in SIEGE.
	 */
	public boolean ignoreTemplate(L2NpcTemplate template)
	{
		if (template.type.equalsIgnoreCase("L2SiegeGuard"))
		{
			// Don't spawn
			return true;
		}
		else if (template.type.equalsIgnoreCase("L2RaidBoss"))
		{
			// Don't spawn raidboss
			return true;
		}
		else if (template.type.equalsIgnoreCase("L2GrandBoss"))
		{
			// Don't spawn grandboss
			return true;
		}
		else if (!Config.ALLOW_CLASS_MASTERS && template.type.equals("L2ClassMaster"))
		{
			// Dont' spawn class masters
			return true;
		}
		else if (!Config.SPAWN_CHRISTMAS_TREE && template.type.equals("L2XmasTree"))
		{
			// Dont' spawn Christ mass Tree
			return true;
		}
		
		return false;
	}
}
