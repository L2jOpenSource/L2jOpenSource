package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format:(ch) ddd
 * @author L2JFrozen
 */
public final class RequestDuelAnswerStart extends L2GameClientPacket
{
	private int partyDuel;
	@SuppressWarnings("unused")
	private int unk1;
	private int response;
	
	@Override
	protected void readImpl()
	{
		partyDuel = readD();
		unk1 = readD();
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
		
		final L2PcInstance requestor = player.getActiveRequester();
		
		if (requestor == null)
		{
			return;
		}
		
		if (response == 1)
		{
			SystemMessage msg1 = null, msg2 = null;
			if (requestor.isInDuel())
			{
				msg1 = new SystemMessage(SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL);
				msg1.addString(requestor.getName());
				player.sendPacket(msg1);
				return;
			}
			else if (player.isInDuel())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME));
				return;
			}
			
			if (partyDuel == 1)
			{
				msg1 = new SystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg1.addString(requestor.getName());
				
				msg2 = new SystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg2.addString(player.getName());
			}
			else
			{
				msg1 = new SystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg1.addString(requestor.getName());
				
				msg2 = new SystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
				msg2.addString(player.getName());
			}
			
			player.sendPacket(msg1);
			requestor.sendPacket(msg2);
			
			DuelManager.getInstance().addDuel(requestor, player, partyDuel);
		}
		else
		{
			SystemMessage msg = null;
			if (partyDuel == 1)
			{
				msg = new SystemMessage(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
			}
			else
			{
				msg = new SystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
				msg.addString(player.getName());
			}
			requestor.sendPacket(msg);
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:28 RequestDuelAnswerStart";
	}
}
