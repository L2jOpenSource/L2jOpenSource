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
package l2r.gameserver.network.clientpackets;

import static l2r.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static l2r.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import java.util.ArrayList;
import java.util.List;

import l2r.Config;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.CropProcure;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.util.Util;

/**
 * Format: (ch) dd [dddc] d - manor id d - size [ d - crop id d - sales d - price c - reward type ]
 * @author l3x
 */
public class RequestSetCrop extends L2GameClientPacket
{
	private static final String _C__D0_04_REQUESTSETCROP = "[C] D0:04 RequestSetCrop";
	
	private static final int BATCH_LENGTH = 21; // length of the one item
	
	private int _manorId;
	private Crop[] _items = null;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new Crop[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long sales = readQ();
			long price = readQ();
			int type = readC();
			if ((itemId < 1) || (sales < 0) || (price < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new Crop(itemId, sales, price, type);
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		L2PcInstance player = getClient().getActiveChar();
		// check player privileges
		if ((player == null) || (player.getClan() == null) || !player.hasClanPrivilege(ClanPrivilege.CS_MANOR_ADMIN))
		{
			return;
		}
		
		// check castle owner
		Castle currentCastle = CastleManager.getInstance().getCastleById(_manorId);
		if (currentCastle.getOwnerId() != player.getClanId())
		{
			return;
		}
		
		if (!player.isInsideRadius(player.getLastFolkNPC(), INTERACTION_DISTANCE, true, false))
		{
			return;
		}
		
		List<CropProcure> crops = new ArrayList<>(_items.length);
		for (Crop i : _items)
		{
			CropProcure s = i.getCrop();
			if (s == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to overflow while setting manor.", Config.DEFAULT_PUNISH);
				return;
			}
			crops.add(s);
		}
		
		currentCastle.setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			currentCastle.saveCropData(CastleManorManager.PERIOD_NEXT);
		}
	}
	
	private static class Crop
	{
		private final int _itemId;
		private final long _sales;
		private final long _price;
		private final int _type;
		
		public Crop(int id, long s, long p, int t)
		{
			_itemId = id;
			_sales = s;
			_price = p;
			_type = t;
		}
		
		public CropProcure getCrop()
		{
			if ((_sales != 0) && ((MAX_ADENA / _sales) < _price))
			{
				return null;
			}
			
			return CastleManorManager.getInstance().getNewCropProcure(_itemId, _sales, _type, _price, _sales);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_04_REQUESTSETCROP;
	}
}
