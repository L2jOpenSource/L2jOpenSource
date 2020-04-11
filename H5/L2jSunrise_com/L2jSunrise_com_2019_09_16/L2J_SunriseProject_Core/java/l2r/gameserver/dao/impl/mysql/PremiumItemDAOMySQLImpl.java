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

import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.PremiumItemDAO;
import l2r.gameserver.model.L2PremiumItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Premium Item DAO MySQL implementation.
 * @author vGodFather
 */
public class PremiumItemDAOMySQLImpl implements PremiumItemDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String GET_PREMIUM_ITEMS = "SELECT itemNum, itemId, itemCount, itemSender FROM character_premium_items WHERE charId=?";
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_PREMIUM_ITEMS))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int itemNum = rset.getInt("itemNum");
					int itemId = rset.getInt("itemId");
					long itemCount = rset.getLong("itemCount");
					String itemSender = rset.getString("itemSender");
					player.getPremiumItemList().put(itemNum, new L2PremiumItem(itemId, itemCount, itemSender));
				}
			}
		}
		catch (Exception e)
		{
			Log.error("Could not restore premium items: {}", e);
		}
	}
	
	@Override
	public void update(L2PcInstance player, int itemNum, long newcount)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? "))
		{
			ps.setLong(1, newcount);
			ps.setInt(2, player.getObjectId());
			ps.setInt(3, itemNum);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.error("Could not update premium items: {}", e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, int itemNum)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? "))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, itemNum);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.error("Could not delete premium item: {}" + e);
		}
	}
}
