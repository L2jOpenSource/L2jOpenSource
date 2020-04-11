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
package l2r.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2r.L2DatabaseFactory;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author DS
 */
public class Mail extends ItemContainer
{
	private final int _ownerId;
	private int _messageId;
	
	public Mail(int objectId, int messageId)
	{
		_ownerId = objectId;
		_messageId = messageId;
	}
	
	@Override
	public String getName()
	{
		return "Mail";
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return null;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.MAIL;
	}
	
	public int getMessageId()
	{
		return _messageId;
	}
	
	public void setNewMessageId(int messageId)
	{
		_messageId = messageId;
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			
			item.setItemLocation(getBaseLocation(), messageId);
		}
		
		updateDatabase();
	}
	
	public void returnToWh(ItemContainer wh)
	{
		for (L2ItemInstance item : _items)
		{
			if (item == null)
			{
				continue;
			}
			if (wh == null)
			{
				item.setItemLocation(ItemLocation.WAREHOUSE);
			}
			else
			{
				transferItem("Expire", item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}
	
	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		item.setItemLocation(getBaseLocation(), _messageId);
	}
	
	/*
	 * Allow saving of the items without owner
	 */
	@Override
	public void updateDatabase()
	{
		for (L2ItemInstance item : _items)
		{
			if (item != null)
			{
				item.updateDatabase(true);
			}
		}
	}
	
	@Override
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND loc=? AND loc_data=?"))
		{
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setInt(3, getMessageId());
			try (ResultSet inv = statement.executeQuery())
			{
				L2ItemInstance item;
				while (inv.next())
				{
					item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
					if (item == null)
					{
						continue;
					}
					
					L2World.getInstance().storeObject(item);
					
					// If stackable item is found just add to current quantity
					if (item.isStackable() && (getItemByItemId(item.getId()) != null))
					{
						addItem("Restore", item, null, null);
					}
					else
					{
						addItem(item);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("could not restore container:", e);
		}
	}
	
	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
}