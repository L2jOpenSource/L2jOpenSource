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
import javolution.util.FastSet;
import l2r.Config;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.ItemRequest;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.TradeList;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final String _C__83_REQUESTPRIVATESTOREBUY = "[C] 83 RequestPrivateStoreBuy";
	
	private static final int BATCH_LENGTH = 20; // length of the one item
	
	private int _storePlayerId;
	private FastSet<ItemRequest> _items = null;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		_items = new FastSet<>();
		
		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			long cnt = readQ();
			long price = readQ();
			
			if ((objectId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return;
			}
			
			_items.add(new ItemRequest(objectId, cnt, price));
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You are buying items too fast.");
			return;
		}
		
		L2Object object = L2World.getInstance().getPlayer(_storePlayerId);
		if (object == null)
		{
			return;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			return;
		}
		
		L2PcInstance storePlayer = (L2PcInstance) object;
		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
		{
			return;
		}
		
		if ((player.getInstanceId() != storePlayer.getInstanceId()) && (player.getInstanceId() != -1))
		{
			return;
		}
		
		if (!((storePlayer.getPrivateStoreType() == PrivateStoreType.SELL) || (storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)))
		{
			return;
		}
		
		TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storePlayer.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _items.size())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items than sold by package-sell, ban this player for bot usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		int result = storeList.privateStoreBuy(player, _items);
		if (result > 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			if (result > 1)
			{
				_log.warn("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			}
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PrivateStoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__83_REQUESTPRIVATESTOREBUY;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
