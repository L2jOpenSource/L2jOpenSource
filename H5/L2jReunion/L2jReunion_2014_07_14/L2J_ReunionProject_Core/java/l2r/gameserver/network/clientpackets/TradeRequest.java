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

import l2r.Config;
import l2r.gameserver.datatables.sql.BotReportTable;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.BlockList;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.SendTradeRequest;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * This packet manages the trade request.
 */
public final class TradeRequest extends L2GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your current Access Level.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Effect ef = null;
		if (((ef = player.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(BotReportTable.TRADE_ACTION_BLOCK_ID))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object target = L2World.getInstance().findObject(_objectId);
		// If there is no target, target is far away or
		// they are in different instances (except multiverse)
		// trade request is ignored and there is no system message.
		if ((target == null) || !player.getKnownList().knowsObject(target) || ((target.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1)))
		{
			return;
		}
		
		// If target and acting player are the same, trade request is ignored
		// and the following system message is sent to acting player.
		if (target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (!target.isPlayer())
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
			return;
		}
		
		if (((ef = partner.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(BotReportTable.TRADE_ACTION_BLOCK_ID))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_REPORTED_AND_IS_BEING_INVESTIGATED);
			sm.addCharName(partner);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// L2J Customs: Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0))
		{
			player.sendMessage("You cannot trade while you are in a chaotic state.");
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (partner.getKarma() > 0))
		{
			player.sendMessage("You cannot request a trade while your target is in a chaotic state.");
			return;
		}
		
		if (Config.JAIL_DISABLE_TRANSACTION && (player.isJailed() || partner.isJailed()))
		{
			player.sendMessage("You cannot trade while you are in in Jail.");
			return;
		}
		
		if ((player.getPrivateStoreType() != PrivateStoreType.NONE) || (partner.getPrivateStoreType() != PrivateStoreType.NONE))
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				_log.info("Already trading with someone else.");
			}
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		SystemMessage sm;
		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				_log.info("Transaction already in progress.");
			}
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getTradeRefusal())
		{
			player.sendMessage("That person is in trade refusal mode.");
			return;
		}
		
		if (BlockList.isBlocked(partner, player))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (player.calculateDistance(partner, true, false) > 150)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}
		
		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		sm = SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return "[C] 1A TradeRequest";
	}
}
