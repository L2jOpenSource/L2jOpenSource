/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.actor.tasks.player;

import java.util.List;

import l2r.gameserver.data.xml.impl.PetData;
import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.handler.ItemHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			if (!_player.isMounted() || (_player.getMountNpcId() == 0) || (PetData.getInstance().getPetData(_player.getMountNpcId()) == null))
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
			
			final List<Integer> foodIds = PetData.getInstance().getPetData(_player.getMountNpcId()).getFood();
			if (foodIds.isEmpty())
			{
				return;
			}
			
			boolean summonHaveFood = false;
			
			L2ItemInstance food = null;
			if (_player.getSummon() != null)
			{
				for (int id : foodIds)
				{
					food = _player.getSummon().getInventory().getItemByItemId(id);
					if (food != null)
					{
						summonHaveFood = true;
						break;
					}
				}
			}
			
			if (food == null)
			{
				for (int id : foodIds)
				{
					food = _player.getInventory().getItemByItemId(id);
					if (food != null)
					{
						break;
					}
				}
			}
			
			if ((food != null) && _player.isHungry())
			{
				IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
				if (handler != null)
				{
					handler.useItem(summonHaveFood ? _player.getSummon() : _player, food, false);
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