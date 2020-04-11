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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.CursedWeapon;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.actor.instance.L2FeedableBeastInstance;
import l2r.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import l2r.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * UnAfraid: TODO: Rewrite with DocumentParser
 * @author Micht
 */
public class CursedWeaponsManager
{
	private static final Logger _log = LoggerFactory.getLogger(CursedWeaponsManager.class);
	
	private Map<Integer, CursedWeapon> _cursedWeapons;
	
	protected CursedWeaponsManager()
	{
		init();
	}
	
	private void init()
	{
		_cursedWeapons = new HashMap<>();
		
		if (!Config.ALLOW_CURSED_WEAPONS)
		{
			return;
		}
		
		load();
		restore();
		controlPlayers();
		_log.info(getClass().getSimpleName() + ": Loaded : " + _cursedWeapons.size() + " cursed weapon(s).");
	}
	
	public final void reload()
	{
		init();
	}
	
	private final void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(Config.DATAPACK_ROOT + "/data/xml/cursedWeapons.xml");
			if (!file.exists())
			{
				_log.warn(getClass().getSimpleName() + ": Couldn't find data/" + file.getName());
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();
							
							CursedWeapon cw = new CursedWeapon(id, skillId, name);
							
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
							_cursedWeapons.put(id, cw);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error parsing cursed weapons file.", e);
			
			return;
		}
	}
	
	private final void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT itemId, charId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons"))
		{
			// Retrieve the L2PcInstance from the characters table of the database
			CursedWeapon cw;
			while (rs.next())
			{
				cw = _cursedWeapons.get(rs.getInt("itemId"));
				cw.setPlayerId(rs.getInt("charId"));
				cw.setPlayerKarma(rs.getInt("playerKarma"));
				cw.setPlayerPkKills(rs.getInt("playerPkKills"));
				cw.setNbKills(rs.getInt("nbKills"));
				cw.setEndTime(rs.getLong("endTime"));
				cw.reActivate();
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore CursedWeapons data: " + e.getMessage(), e);
		}
	}
	
	private final void controlPlayers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// TODO: See comments below...
			// This entire for loop should NOT be necessary, since it is already handled by
			// CursedWeapon.endOfLife(). However, if we indeed *need* to duplicate it for safety,
			// then we'd better make sure that it FULLY cleans up inactive cursed weapons!
			// Undesired effects result otherwise, such as player with no zariche but with karma
			// or a lost-child entry in the cursed weapons table, without a corresponding one in items...
			
			// Retrieve the L2PcInstance from the characters table of the database
			try (PreparedStatement ps = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?"))
			{
				for (CursedWeapon cw : _cursedWeapons.values())
				{
					if (cw.isActivated())
					{
						continue;
					}
					
					// Do an item check to be sure that the cursed weapon isn't hold by someone
					int itemId = cw.getId();
					ps.setInt(1, itemId);
					try (ResultSet rset = ps.executeQuery())
					{
						if (rset.next())
						{
							// A player has the cursed weapon in his inventory ...
							int playerId = rset.getInt("owner_id");
							_log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
							
							// Delete the item
							try (PreparedStatement delete = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?"))
							{
								delete.setInt(1, playerId);
								delete.setInt(2, itemId);
								if (delete.executeUpdate() != 1)
								{
									_log.warn("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
								}
							}
							
							// Restore the player's old karma and pk count
							try (PreparedStatement update = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?"))
							{
								update.setInt(1, cw.getPlayerKarma());
								update.setInt(2, cw.getPlayerPkKills());
								update.setInt(3, playerId);
								if (update.executeUpdate() != 1)
								{
									_log.warn("Error while updating karma & pkkills for userId " + cw.getPlayerId());
								}
							}
							// clean up the cursed weapons table.
							removeFromDb(itemId);
						}
					}
					ps.clearParameters();
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not check CursedWeapons data: " + e.getMessage(), e);
		}
	}
	
	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if ((attackable instanceof L2DefenderInstance) || (attackable instanceof L2RiftInvaderInstance) || (attackable instanceof L2FestivalMonsterInstance) || (attackable instanceof L2GuardInstance) || (attackable instanceof L2GrandBossInstance) || (attackable instanceof L2FeedableBeastInstance) || (attackable instanceof L2FortCommanderInstance))
		{
			return;
		}
		
		for (CursedWeapon cw : _cursedWeapons.values())
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
	
	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		CursedWeapon cw = _cursedWeapons.get(item.getId());
		if (player.isCursedWeaponEquipped()) // cannot own 2 cursed swords
		{
			CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());
			// TODO: give the bonus level in a more appropriate manner.
			// The following code adds "_stageKills" levels. This will also show in the char status.
			// I do not have enough info to know if the bonus should be shown in the pk count, or if it
			// should be a full "_stageKills" bonus or just the remaining from the current count till the of the current stage...
			// This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as little change in the code as possible, until proper info arises.
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
	}
	
	public void drop(int itemId, L2Character killer)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		
		cw.dropIt(killer);
	}
	
	public void increaseKills(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		
		cw.increaseKills();
	}
	
	public int getLevel(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		
		return cw.getLevel();
	}
	
	public static void announce(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}
	
	public void checkPlayer(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && (player.getObjectId() == cw.getPlayerId()))
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getId()));
				cw.giveSkill();
				player.setCursedWeaponEquippedId(cw.getId());
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
				sm.addString(cw.getName());
				// sm.addItemName(cw.getId());
				sm.addInt((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
			}
		}
	}
	
	public int checkOwnsWeaponId(int ownerId)
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && (ownerId == cw.getPlayerId()))
			{
				return cw.getId();
			}
		}
		return -1;
	}
	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?"))
		{
			ps.setInt(1, itemId);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.error("CursedWeaponsManager: Failed to remove data: " + e.getMessage(), e);
		}
	}
	
	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			cw.saveData();
		}
	}
	
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}
	
	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception e)
		{
			/***/
		}
	}
	
	public static final CursedWeaponsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
	}
}
