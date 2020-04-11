package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class TradeDone extends L2GameClientPacket
{
	private int response;
	
	@Override
	protected void readImpl()
	{
		response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("trade"))
		{
			player.sendMessage("You trading too fast.");
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			// LOGGER.warn("player.getTradeList == null in " + getType() + " for player " + player.getName());
			return;
		}
		
		if (trade.getOwner().getActiveEnchantItem() != null || trade.getPartner().getActiveEnchantItem() != null)
		{
			return;
		}
		
		if (trade.isLocked())
		{
			return;
		}
		
		// abort cast anyway
		player.abortCast(true);
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (response == 1)
		{
			if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
			{
				// Trade partner not found, cancel trade
				player.cancelActiveTrade();
				SystemMessage msg = new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				player.sendPacket(msg);
				msg = null;
				return;
			}
			
			if (!player.getAccessLevel().allowTransaction())
			{
				player.sendMessage("Unsufficient privileges.");
				player.cancelActiveTrade();
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			trade.confirm();
		}
		else
		{
			player.cancelActiveTrade();
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 17 TradeDone";
	}
}
