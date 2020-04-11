/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.ItemReuseDAO;
import l2r.gameserver.model.TimeStamp;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.ExUseSharedGroupItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item Reuse DAO MySQL implementation.
 * @author vGodFather
 */
public class ItemReuseDAOMySQLImpl implements ItemReuseDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String INSERT = "INSERT INTO character_item_reuse_save (charId,itemId,itemObjId,reuseDelay,systime) VALUES (?,?,?,?,?)";
	private static final String SELECT = "SELECT charId,itemId,itemObjId,reuseDelay,systime FROM character_item_reuse_save WHERE charId=?";
	private static final String DELETE = "DELETE FROM character_item_reuse_save WHERE charId=?";
	
	@Override
	public void delete(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE))
		{
			ps.setInt(1, player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Could not delete player's " + player + " item reuse data!", e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player)
	{
		delete(player);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT))
		{
			final Map<Integer, TimeStamp> itemReuseTimeStamps = player.getItemReuseTimeStamps();
			if (itemReuseTimeStamps != null)
			{
				for (TimeStamp ts : itemReuseTimeStamps.values())
				{
					if ((ts != null) && ts.hasNotPassed())
					{
						ps.setInt(1, player.getObjectId());
						ps.setInt(2, ts.getItemId());
						ps.setInt(3, ts.getItemObjectId());
						ps.setLong(4, ts.getReuse());
						ps.setLong(5, ts.getStamp());
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			Log.warn("Could not store player's " + player + " item reuse data!", e);
		}
	}
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int itemId = rs.getInt("itemId");
					@SuppressWarnings("unused")
					int itemObjId = rs.getInt("itemObjId");
					long reuseDelay = rs.getLong("reuseDelay");
					long systime = rs.getLong("systime");
					boolean isInInventory = true;
					
					// Using item Id
					L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
					if (item == null)
					{
						item = player.getWarehouse().getItemByItemId(itemId);
						isInInventory = false;
					}
					
					if ((item != null) && (item.getId() == itemId) && (item.getReuseDelay() > 0))
					{
						long remainingTime = systime - System.currentTimeMillis();
						// Hardcoded to 10 seconds.
						if (remainingTime > 10)
						{
							player.addTimeStampItem(item, reuseDelay, systime);
							
							if (isInInventory && item.isEtcItem())
							{
								final int group = item.getSharedReuseGroup();
								if (group > 0)
								{
									player.sendPacket(new ExUseSharedGroupItem(itemId, group, (int) remainingTime, (int) reuseDelay));
								}
							}
						}
					}
				}
			}
			
			// Deletes item reuse.
			delete(player);
		}
		catch (Exception e)
		{
			Log.error("Could not restore " + player + " Item Reuse data:", e);
		}
	}
}
