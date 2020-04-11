package com.l2jfrozen.gameserver.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.CursedWeapon;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2CommanderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Micht
 */
public class CursedWeaponsManager
{
	private static final Logger LOGGER = Logger.getLogger(CursedWeaponsManager.class);
	private static final String SELECT_CURSED_WEAPONS = "SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons";
	private static final String DELETE_CURSED_WEAPON_BY_ITEM_ID = "DELETE FROM cursed_weapons WHERE itemId = ?";
	
	private static final Map<Integer, CursedWeapon> cursedWeapons = new HashMap<>();
	
	public static final CursedWeaponsManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public CursedWeaponsManager()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}
		
		load();
		restore();
		controlPlayers();
		
		LOGGER.info("Loaded: " + cursedWeapons.size() + " cursed weapon(s).");
	}
	
	public final void reload()
	{
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}
		
		cursedWeapons.clear();
		
		load();
		restore();
		controlPlayers();
		
		LOGGER.info("Reloaded: " + cursedWeapons.size() + " cursed weapon(s).");
	}
	
	private final void load()
	{
		LOGGER.info("Initializing CursedWeaponsManager");
		if (Config.DEBUG)
		{
			LOGGER.info("Loading data: ");
		}
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			final File file = new File(Config.DATAPACK_ROOT + "/data/xml/cursedWeapons.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
				{
					LOGGER.info("NO FILE");
				}
				return;
			}
			
			final Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							final int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();
							
							CursedWeapon cw = new CursedWeapon(id, skillId, name);
							name = null;
							
							int val;
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDropRate(val);
								}
								else if ("duration".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDuration(val);
								}
								else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDurationLost(val);
								}
								else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDisapearChance(val);
								}
								else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setStageKills(val);
								}
							}
							
							// Store cursed weapon
							cursedWeapons.put(id, cw);
							
							attrs = null;
							cw = null;
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("Error parsing cursed weapons file.", e);
			
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	private final void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CURSED_WEAPONS);
			ResultSet rset = statement.executeQuery())
		{
			if (rset.next())
			{
				int itemId = rset.getInt("itemId");
				int playerId = rset.getInt("playerId");
				int playerKarma = rset.getInt("playerKarma");
				int playerPkKills = rset.getInt("playerPkKills");
				int nbKills = rset.getInt("nbKills");
				long endTime = rset.getLong("endTime");
				
				CursedWeapon cw = cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate();
				
				cw = null;
				
				// clean up the cursed weapons table.
				removeFromDb(itemId);
			}
			
		}
		catch (Exception e)
		{
			LOGGER.error("CursedWeaponsManager.restore : Could not select from cursed_weapons table", e);
		}
		
	}
	
	private final void controlPlayers()
	{
		
		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;
			ResultSet rset = null;
			
			// TODO: See comments below...
			// This entire for loop should NOT be necessary, since it is already handled by
			// CursedWeapon.endOfLife(). However, if we indeed *need* to duplicate it for safety,
			// then we'd better make sure that it FULLY cleans up inactive cursed weapons!
			// Undesired effects result otherwise, such as player with no zariche but with karma
			// or a lost-child entry in the cursed weapons table, without a corresponding one in items...
			for (final CursedWeapon cw : cursedWeapons.values())
			{
				if (cw.isActivated())
				{
					continue;
				}
				
				// Do an item check to be sure that the cursed weapon isn't hold by someone
				final int itemId = cw.getItemId();
				try
				{
					statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					rset = statement.executeQuery();
					
					if (rset.next())
					{
						// A player has the cursed weapon in his inventory ...
						final int playerId = rset.getInt("owner_id");
						LOGGER.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
						
						// Delete the item
						statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warn("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}
						DatabaseUtils.close(statement);
						
						// Restore the player's old karma and pk count
						statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
						statement.setInt(1, cw.getPlayerKarma());
						statement.setInt(2, cw.getPlayerPkKills());
						statement.setInt(3, playerId);
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warn("Error while updating karma & pkkills for userId " + cw.getPlayerId());
						}
						
					}
					DatabaseUtils.close(rset);
					DatabaseUtils.close(statement);
					rset = null;
					statement = null;
					
				}
				catch (final SQLException sqlE)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						sqlE.printStackTrace();
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not check CursedWeapons data: ");
			e.printStackTrace();
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
	}
	
	// =========================================================
	// Properties - Public
	public synchronized void checkDrop(final L2Attackable attackable, final L2PcInstance player)
	{
		if (attackable instanceof L2SiegeGuardInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2FestivalMonsterInstance || attackable instanceof L2GrandBossInstance || attackable instanceof L2FortSiegeGuardInstance || attackable instanceof L2CommanderInstance)
		{
			return;
		}
		
		if (player.isCursedWeaponEquiped())
		{
			return;
		}
		
		for (final CursedWeapon cw : cursedWeapons.values())
		{
			if (cw.isActive())
			{
				continue;
			}
			
			if (cw.checkDrop(attackable, player))
			{
				break;
			}
		}
	}
	
	public void activate(final L2PcInstance player, final L2ItemInstance item)
	{
		CursedWeapon cw = cursedWeapons.get(item.getItemId());
		if (player.isCursedWeaponEquiped()) // cannot own 2 cursed swords
		{
			final CursedWeapon cw2 = cursedWeapons.get(player.getCursedWeaponEquipedId());
			/*
			 * TODO: give the bonus level in a more appropriate manner. The following code adds "_stageKills" levels. This will also show in the char status. I do not have enough info to know if the bonus should be shown in the pk count, or if it should be a full "_stageKills" bonus or just the remaining from
			 * the current count till the of the current stage... This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as little change in the code as possible, until proper info arises.
			 */
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();
			
			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
			
		}
		else
		{
			cw.activate(player, item);
		}
		
		cw = null;
	}
	
	public void drop(final int itemId, final L2Character killer)
	{
		CursedWeapon cw = cursedWeapons.get(itemId);
		
		cw.dropIt(killer);
		cw = null;
	}
	
	public void increaseKills(final int itemId)
	{
		CursedWeapon cw = cursedWeapons.get(itemId);
		
		cw.increaseKills();
		cw = null;
	}
	
	public int getLevel(final int itemId)
	{
		final CursedWeapon cw = cursedWeapons.get(itemId);
		
		return cw.getLevel();
	}
	
	public static void announce(final SystemMessage sm)
	{
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(sm);
		}
		
	}
	
	public void checkPlayer(final L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		for (final CursedWeapon cw : cursedWeapons.values())
		{
			if (cw.isActive() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquipedId(cw.getItemId());
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
				sm.addString(cw.getName());
				// sm.addItemName(cw.getItemId());
				sm.addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
				
				sm = new SystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
				sm.addZoneName(player.getX(), player.getY(), player.getZ()); // Region Name
				sm.addItemName(cw.getItemId());
				CursedWeaponsManager.announce(sm);
				sm = null;
				
			}
		}
	}
	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CURSED_WEAPON_BY_ITEM_ID))
		{
			statement.setInt(1, itemId);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.error("CursedWeaponsManager.removeFromDb : Failed to remove data", e);
		}
	}
	
	public void saveData()
	{
		for (CursedWeapon cw : cursedWeapons.values())
		{
			cw.saveData();
		}
	}
	
	public boolean isCursed(final int itemId)
	{
		return cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(final int itemId)
	{
		return cursedWeapons.get(itemId);
	}
	
	public void givePassive(final int itemId)
	{
		try
		{
			cursedWeapons.get(itemId).giveSkill();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager instance = new CursedWeaponsManager();
	}
}
