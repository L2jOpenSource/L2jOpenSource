package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SendTradeRequest;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

public final class TradeRequest extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(TradeRequest.class);
	
	private int objectId;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object target = L2World.getInstance().findObject(objectId);
		if (target == null || !player.getKnownList().knowsObject(target) || !(target instanceof L2PcInstance) || target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance partner = (L2PcInstance) target;
		
		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target can't request trade in Olympiad mode");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isAway())
		{
			player.sendMessage("You can't Request a Trade when partner is Away");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isStunned())
		{
			player.sendMessage("You can't Request a Trade when partner Stunned");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isConfused())
		{
			player.sendMessage("You can't Request a Trade when partner Confused");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isCastingNow() || partner.isCastingPotionNow())
		{
			player.sendMessage("You can't Request a Trade when partner Casting Now");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isInDuel())
		{
			player.sendMessage("You can't Request a Trade when partner in Duel");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isImobilised())
		{
			player.sendMessage("You can't Request a Trade when partner is Imobilised");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isInFunEvent())
		{
			player.sendMessage("You can't Request a Trade when partner in Event");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.getActiveEnchantItem() != null)
		{
			player.sendMessage("You can't Request a Trade when partner Enchanting");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isParalyzed())
		{
			player.sendMessage("You can't Request a Trade when partner is Paralyzed");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.inObserverMode())
		{
			player.sendMessage("You can't Request a Trade when partner in Observation Mode");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isAttackingNow())
		{
			player.sendMessage("You can't Request a Trade when partner Attacking Now");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isStunned())
		{
			player.sendMessage("You can't Request a Trade when you Stunned");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isAway())
		{
			player.sendMessage("You can't Request a Trade when you Away");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			player.sendMessage("You can't Request a Trade when you Confused");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendMessage("You can't Request a Trade when you Casting");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInDuel())
		{
			player.sendMessage("You can't Request a Trade when you in Duel");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isImobilised())
		{
			player.sendMessage("You can't Request a Trade when you are Imobilised");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInFunEvent())
		{
			player.sendMessage("You can't Request a Trade when you are in Event");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.sendMessage("You can't Request a Trade when you Enchanting");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isParalyzed())
		{
			player.sendMessage("You can't Request a Trade when you are Paralyzed");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendMessage("You can't Request a Trade when you in Observation Mode");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getDistanceSq(partner) > 22500) // 150
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || partner.getKarma() > 0))
		{
			player.sendMessage("Chaotic players can't use Trade.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInStoreMode() || partner.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!Config.ALLOW_LOW_LEVEL_TRADE)
		{
			if (player.getLevel() < 76 && partner.getLevel() >= 76 || partner.getLevel() < 76 || player.getLevel() >= 76)
			{
				player.sendMessage("You Cannot Trade a Lower Level Character");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (player.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Already trading with someone");
			}
			
			player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_TRADING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				LOGGER.info("Transaction already in progress.");
			}
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Util.calculateDistance(player, partner, true) > 150)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.TARGET_TOO_FAR);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		final SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return "[C] 15 TradeRequest";
	}
}