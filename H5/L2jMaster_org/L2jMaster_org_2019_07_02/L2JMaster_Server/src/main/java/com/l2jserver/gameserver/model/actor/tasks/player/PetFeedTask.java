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
package com.l2jserver.gameserver.model.actor.tasks.player;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Task dedicated for feeding player's pet.
 * @author UnAfraid
 */
public class PetFeedTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(PetFeedTask.class);
	
	private final L2PcInstance _player;
	
	public PetFeedTask(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		try
		{
			if (!_player.isMounted() || (_player.getMountNpcId() == 0) || (PetDataTable.getInstance().getPetData(_player.getMountNpcId()) == null))
			{
				_player.stopFeed();
				return;
			}
			
			if (_player.getCurrentFeed() > _player.getFeedConsume())
			{
				// eat
				_player.setCurrentFeed(_player.getCurrentFeed() - _player.getFeedConsume());
			}
			else
			{
				// go back to pet control item, or simply said, unsummon it
				_player.setCurrentFeed(0);
				_player.stopFeed();
				_player.dismount();
				_player.sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
				return;
			}
			
			final List<Integer> foodIds = PetDataTable.getInstance().getPetData(_player.getMountNpcId()).getFood();
			if (foodIds.isEmpty())
			{
				return;
			}
			
			L2ItemInstance food = null;
			for (int id : foodIds)
			{
				// TODO: possibly pet inv?
				food = _player.getInventory().getItemByItemId(id);
				if (food != null)
				{
					break;
				}
			}
			
			if ((food != null) && _player.isHungry())
			{
				IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
				if (handler != null)
				{
					handler.useItem(_player, food, false);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
					sm.addItemName(food.getId());
					_player.sendPacket(sm);
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Could not feed mounted Pet NPC ID {}, a feed task error has occurred", _player.getMountNpcId(), e);
		}
	}
}
