package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.AskJoinParty;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinParty extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestJoinParty.class);
	
	private String name;
	private int itemDistribution;
	
	@Override
	protected void readImpl()
	{
		name = readS();
		itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance requestor = getClient().getActiveChar();
		final L2PcInstance target = L2World.getInstance().getPlayer(name);
		
		if (requestor == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getPartyInvitation().tryPerformAction("PartyInvitation"))
		{
			requestor.sendMessage("You Cannot Invite into Party So Fast!");
			return;
		}
		
		if (target == null)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		if ((requestor.inEventDM && (DM.is_teleport() || DM.isStarted())) || (target.inEventDM && (DM.is_teleport() || DM.isStarted())))
		{
			requestor.sendMessage("You can't invite that player in party!");
			return;
		}
		
		if ((requestor.inEventTvT && !target.inEventTvT && (TvT.isStarted() || TvT.isTeleport())) || (!requestor.inEventTvT && target.inEventTvT && (TvT.isStarted() || TvT.isTeleport())) || (requestor.inEventCTF && !target.inEventCTF && (CTF.isStarted() || CTF.isTeleport())) || (!requestor.inEventCTF && target.inEventCTF && (CTF.isStarted() || CTF.isTeleport())))
		{
			requestor.sendMessage("You can't invite that player in party: you or your target are in Event");
			return;
		}
		
		if (target.isInParty())
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		
		if (target == requestor)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (target.isCursedWeaponEquiped() || requestor.isCursedWeaponEquiped())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (target.isGM() && target.getAppearance().isInvisible())
		{
			requestor.sendMessage("You can't invite GM in invisible mode.");
			return;
		}
		
		if (target.isInJail() || requestor.isInJail())
		{
			final SystemMessage sm = SystemMessage.sendString("Player is in Jail");
			requestor.sendPacket(sm);
			return;
		}
		
		if (target.getBlockList().isInBlockList(requestor.getName()))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addString(target.getName());
			requestor.sendPacket(sm);
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
		{
			return;
		}
		
		if (target.isInDuel() || requestor.isInDuel())
		{
			return;
		}
		
		if (!requestor.isInParty()) // Asker has no party
		{
			createNewParty(target, requestor);
		}
		else
		// Asker is in party
		{
			if (requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
			}
			else
			{
				addTargetToParty(target, requestor);
			}
		}
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(final L2PcInstance target, final L2PcInstance requestor)
	{
		SystemMessage msg;
		
		// summary of ppl already in party and ppl that get invitation
		if (requestor.getParty().getMemberCount() >= 9)
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.PARTY_FULL));
			return;
		}
		
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEADER_CAN_INVITE));
			return;
		}
		
		if (requestor.getParty().getPendingInvitation() && !requestor.getParty().isInvitationRequestExpired())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("sent out a party invitation to:" + target.getName());
			}
			
			msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			
			if (Config.DEBUG)
			{
				LOGGER.warn(requestor.getName() + " already received a party invitation");
			}
		}
		msg = null;
	}
	
	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(final L2PcInstance target, final L2PcInstance requestor)
	{
		SystemMessage msg;
		
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, itemDistribution));
			
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
			requestor.getParty().setPendingInvitation(true);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("sent out a party invitation to:" + target.getName());
			}
			
			msg = new SystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			
			if (Config.DEBUG)
			{
				LOGGER.warn(requestor.getName() + " already received a party invitation");
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 29 RequestJoinParty";
	}
}