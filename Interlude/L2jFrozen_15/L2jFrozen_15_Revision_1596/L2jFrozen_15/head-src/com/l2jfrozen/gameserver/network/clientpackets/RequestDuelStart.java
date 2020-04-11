package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelAskStart;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format:(ch) Sd
 * @author L2JFrozen
 */
public final class RequestDuelStart extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestDuelStart.class);
	private String player;
	private int partyDuel;
	
	@Override
	protected void readImpl()
	{
		player = readS();
		partyDuel = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2PcInstance targetChar = L2World.getInstance().getPlayer(player);
		if (activeChar == null)
		{
			return;
		}
		
		if (targetChar == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL));
			return;
		}
		
		if (activeChar == targetChar)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL));
			return;
		}
		
		// Check if duel is possible
		if (!activeChar.canDuel())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME));
			return;
		}
		else if (!targetChar.canDuel())
		{
			activeChar.sendPacket(targetChar.getNoDuelReason());
			return;
		}
		// Players may not be too far apart
		else if (!activeChar.isInsideRadius(targetChar, 250, false, false))
		{
			final SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY);
			msg.addString(targetChar.getName());
			activeChar.sendPacket(msg);
			return;
		}
		
		// Duel is a party duel
		if (partyDuel == 1)
		{
			// Player must be in a party & the party leader
			if (!activeChar.isInParty() || !(activeChar.isInParty() && activeChar.getParty().isLeader(activeChar)))
			{
				activeChar.sendMessage("You have to be the leader of a party in order to request a party duel.");
				return;
			}
			// Target must be in a party
			else if (!targetChar.isInParty())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY));
				return;
			}
			// Target may not be of the same party
			else if (activeChar.getParty().getPartyMembers().contains(targetChar))
			{
				activeChar.sendMessage("This player is a member of your own party.");
				return;
			}
			
			// Check if every player is ready for a duel
			for (final L2PcInstance temp : activeChar.getParty().getPartyMembers())
			{
				if (!temp.canDuel())
				{
					activeChar.sendMessage("Not all the members of your party are ready for a duel.");
					return;
				}
			}
			
			L2PcInstance partyLeader = null; // snatch party leader of targetChar's party
			for (final L2PcInstance temp : targetChar.getParty().getPartyMembers())
			{
				if (partyLeader == null)
				{
					partyLeader = temp;
				}
				if (!temp.canDuel())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL));
					return;
				}
			}
			
			if (partyLeader != null)
			{
				// Send request to targetChar's party leader
				if (!partyLeader.isProcessingRequest())
				{
					activeChar.onTransactionRequest(partyLeader);
					partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), partyDuel));
					
					if (Config.DEBUG)
					{
						LOGGER.debug(activeChar.getName() + " requested a duel with " + partyLeader.getName());
					}
					
					SystemMessage msg = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
					
					msg = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL);
					msg.addString(activeChar.getName());
					targetChar.sendPacket(msg);
				}
				else
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
					msg.addString(partyLeader.getName());
					activeChar.sendPacket(msg);
				}
			}
		}
		else
		// 1vs1 duel
		{
			if (!targetChar.isProcessingRequest())
			{
				activeChar.onTransactionRequest(targetChar);
				targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), partyDuel));
				
				if (Config.DEBUG)
				{
					LOGGER.debug(activeChar.getName() + " requested a duel with " + targetChar.getName());
				}
				
				SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
				
				msg = new SystemMessage(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL);
				msg.addString(activeChar.getName());
				targetChar.sendPacket(msg);
			}
			else
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
				msg.addString(targetChar.getName());
				activeChar.sendPacket(msg);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:27 RequestDuelStart";
	}
	
}
