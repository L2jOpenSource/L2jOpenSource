package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (c) Sd (S: content - d: type)
 * @author -Wooden-, TempyIncursion
 */
public final class RequestPetition extends L2GameClientPacket
{
	private String content;
	private int type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		content = readS();
		type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!GmListTable.getInstance().getAllGms(false).isEmpty())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW));
			return;
		}
		
		if (!PetitionManager.getInstance().isPetitioningAllowed())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER));
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME));
			return;
		}
		
		if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_SYSTEM_CURRENT_UNAVAILABLE));
			return;
		}
		
		final int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1;
		
		if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY);
			sm.addNumber(totalPetitions);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (content.length() > 255)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_MAX_CHARS_255));
			return;
		}
		
		final int petitionId = PetitionManager.getInstance().submitPetition(activeChar, content, type);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
		sm.addNumber(petitionId);
		activeChar.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT);
		sm.addNumber(totalPetitions);
		sm.addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions);
		activeChar.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.S1_PETITION_ON_WAITING_LIST);
		sm.addNumber(PetitionManager.getInstance().getPendingPetitionCount());
		activeChar.sendPacket(sm);
		sm = null;
	}
	
	@Override
	public String getType()
	{
		return "[C] 7F RequestPetition";
	}
}
