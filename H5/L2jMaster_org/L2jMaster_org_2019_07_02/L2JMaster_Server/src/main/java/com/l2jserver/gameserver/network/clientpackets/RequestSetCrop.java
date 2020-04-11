/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.instancemanager.CastleManorManager;
import com.l2jserver.gameserver.model.ClanPrivilege;
import com.l2jserver.gameserver.model.CropProcure;
import com.l2jserver.gameserver.model.L2Seed;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author l3x
 */
public final class RequestSetCrop extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 21; // length of the one item
	
	private int _manorId;
	private List<CropProcure> _items;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		final int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
		{
			final int itemId = readD();
			final long sales = readQ();
			final long price = readQ();
			final int type = readC();
			if ((itemId < 1) || (sales < 0) || (price < 0))
			{
				_items.clear();
				return;
			}
			
			if (sales > 0)
			{
				_items.add(new CropProcure(itemId, sales, type, sales, price));
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items.isEmpty())
		{
			return;
		}
		
		final CastleManorManager manor = CastleManorManager.getInstance();
		if (!manor.isModifiablePeriod())
		{
			sendActionFailed();
			return;
		}
		
		// Check player privileges
		final L2PcInstance player = getActiveChar();
		if ((player == null) || (player.getClan() == null) || (player.getClan().getCastleId() != _manorId) || !player.hasClanPrivilege(ClanPrivilege.CS_MANOR_ADMIN) || !player.getLastFolkNPC().canInteract(player))
		{
			sendActionFailed();
			return;
		}
		
		// Filter crops with start amount lower than 0 and incorrect price
		final List<CropProcure> list = new ArrayList<>(_items.size());
		for (CropProcure cp : _items)
		{
			final L2Seed s = manor.getSeedByCrop(cp.getId(), _manorId);
			if ((s != null) && (cp.getStartAmount() <= s.getCropLimit()) && (cp.getPrice() >= s.getCropMinPrice()) && (cp.getPrice() <= s.getCropMaxPrice()))
			{
				list.add(cp);
			}
		}
		
		// Save crop list
		manor.setNextCropProcure(list, _manorId);
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:04 RequestSetCrop";
	}
}