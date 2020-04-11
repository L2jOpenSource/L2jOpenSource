package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.TradeOtherAdd;
import com.l2jfrozen.gameserver.network.serverpackets.TradeOwnAdd;
import com.l2jfrozen.gameserver.network.serverpackets.TradeUpdate;

public final class AddTradeItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(AddTradeItem.class);
	private int tradeId;
	private int objectId;
	private int count;
	
	public AddTradeItem()
	{
	}
	
	@Override
	protected void readImpl()
	{
		tradeId = readD();
		objectId = readD();
		count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null) // Trade null
		{
			LOGGER.warn("Character: " + player.getName() + " requested item:" + objectId + " add without active tradelist:" + tradeId);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check Partner and ocbjectId
		if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			if (trade.getPartner() != null)
			{
				LOGGER.warn("Character:" + player.getName() + " requested invalid trade object: " + objectId);
			}
			
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check if player has Access level for Transaction
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level.");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check validateItemManipulation
		if (!player.validateItemManipulation(objectId, "trade"))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Java Emulator Security
		if (player.getInventory().getItemByObjectId(objectId) == null || count <= 0)
		{
			LOGGER.info("Character:" + player.getName() + " requested invalid trade object");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList.TradeItem item = trade.addItem(objectId, count);
		if (item == null)
		{
			return;
		}
		
		if (item.isAugmented())
		{
			return;
		}
		
		player.sendPacket(new TradeOwnAdd(item));
		player.sendPacket(new TradeUpdate(trade, player));
		trade.getPartner().sendPacket(new TradeOtherAdd(item));
	}
	
	@Override
	public String getType()
	{
		return "[C] 16 AddTradeItem";
	}
}