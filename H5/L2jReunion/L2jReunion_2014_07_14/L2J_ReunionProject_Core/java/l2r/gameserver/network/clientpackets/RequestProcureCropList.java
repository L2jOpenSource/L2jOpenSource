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
import l2r.Config;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.CropProcure;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) d [dddd] d: size [ d obj id d item id d manor id d count ]
 * @author l3x
 */
public class RequestProcureCropList extends L2GameClientPacket
{
	private static final String _C__D0_02_REQUESTPROCURECROPLIST = "[C] D0:02 RequestProcureCropList";
	
	private static final int BATCH_LENGTH = 20; // length of the one item
	
	private Crop[] _items = null;
	
	@Override
	protected void readImpl()
	{
		int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new Crop[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			int itemId = readD();
			int manorId = readD();
			long cnt = readQ();
			if ((objId < 1) || (itemId < 1) || (manorId < 0) || (cnt < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new Crop(objId, itemId, manorId, cnt);
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
		if (player == null)
		{
			return;
		}
		
		L2Object manager = player.getTarget();
		
		if (!(manager instanceof L2ManorManagerInstance))
		{
			manager = player.getLastFolkNPC();
		}
		
		if (!(manager instanceof L2ManorManagerInstance))
		{
			return;
		}
		
		if (!player.isInsideRadius(manager, INTERACTION_DISTANCE, false, false))
		{
			return;
		}
		
		int castleId = ((L2ManorManagerInstance) manager).getCastle().getResidenceId();
		
		// Calculate summary values
		int slots = 0;
		int weight = 0;
		
		for (Crop i : _items)
		{
			if (!i.getCrop())
			{
				continue;
			}
			
			L2Item template = ItemData.getInstance().getTemplate(i.getReward());
			weight += i.getCount() * template.getWeight();
			
			if (!template.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		// Proceed the purchase
		for (Crop i : _items)
		{
			if (i.getReward() == 0)
			{
				continue;
			}
			
			long fee = i.getFee(castleId); // fee for selling to other manors
			
			long rewardPrice = ItemData.getInstance().getTemplate(i.getReward()).getReferencePrice();
			if (rewardPrice == 0)
			{
				continue;
			}
			
			long rewardItemCount = i.getPrice() / rewardPrice;
			if (rewardItemCount < 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(i.getId());
				sm.addLong(i.getCount());
				player.sendPacket(sm);
				continue;
			}
			
			if (player.getAdena() < fee)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(i.getId());
				sm.addLong(i.getCount());
				player.sendPacket(sm);
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				continue;
			}
			
			// check if player have correct items count
			L2ItemInstance item = player.getInventory().getItemByObjectId(i.getObjectId());
			if ((item == null) || (item.getCount() < i.getCount()))
			{
				continue;
			}
			
			// try modify castle crop
			if (!i.setCrop())
			{
				continue;
			}
			
			if ((fee > 0) && !player.reduceAdena("Manor", fee, manager, true))
			{
				continue;
			}
			
			if (!player.destroyItem("Manor", i.getObjectId(), i.getCount(), manager, true))
			{
				continue;
			}
			
			player.addItem("Manor", i.getReward(), rewardItemCount, manager, true);
		}
	}
	
	private static class Crop
	{
		private final int _objectId;
		private final int _itemId;
		private final int _manorId;
		private final long _count;
		private int _reward = 0;
		private CropProcure _crop = null;
		
		public Crop(int obj, int id, int m, long num)
		{
			_objectId = obj;
			_itemId = id;
			_manorId = m;
			_count = num;
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		public int getId()
		{
			return _itemId;
		}
		
		public long getCount()
		{
			return _count;
		}
		
		public int getReward()
		{
			return _reward;
		}
		
		public long getPrice()
		{
			return _crop.getPrice() * _count;
		}
		
		public long getFee(int castleId)
		{
			if (_manorId == castleId)
			{
				return 0;
			}
			
			return (getPrice() / 100) * 5; // 5% fee for selling to other manor
		}
		
		public boolean getCrop()
		{
			try
			{
				_crop = CastleManager.getInstance().getCastleById(_manorId).getCrop(_itemId, CastleManorManager.PERIOD_CURRENT);
			}
			catch (NullPointerException e)
			{
				return false;
			}
			if ((_crop == null) || (_crop.getId() == 0) || (_crop.getPrice() == 0) || (_count == 0))
			{
				return false;
			}
			
			if (_count > _crop.getAmount())
			{
				return false;
			}
			
			if ((MAX_ADENA / _count) < _crop.getPrice())
			{
				return false;
			}
			
			_reward = ManorData.getInstance().getRewardItem(_itemId, _crop.getReward());
			return true;
		}
		
		public boolean setCrop()
		{
			synchronized (_crop)
			{
				long amount = _crop.getAmount();
				if (_count > amount)
				{
					return false; // not enough crops
				}
				_crop.setAmount(amount - _count);
			}
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				CastleManager.getInstance().getCastleById(_manorId).updateCrop(_itemId, _crop.getAmount(), CastleManorManager.PERIOD_CURRENT);
			}
			return true;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_02_REQUESTPROCURECROPLIST;
	}
}
