package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.managers.PetitionManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (c) d
 * @author -Wooden-, TempyIncursion
 */
public final class RequestPetitionCancel extends L2GameClientPacket
{
	// private int unknown;
	
	@Override
	protected void readImpl()
	{
		// unknown = readD(); This is pretty much a trigger packet.
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if (activeChar.isGM())
			{
				PetitionManager.getInstance().endActivePetition(activeChar);
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_UNDER_PROCESS));
			}
		}
		else
		{
			if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
			{
				if (PetitionManager.getInstance().cancelActivePetition(activeChar))
				{
					final int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY);
					sm.addString(String.valueOf(numRemaining));
					activeChar.sendPacket(sm);
					sm = null;
					
					// Notify all GMs that the player's pending petition has been cancelled.
					final String msgContent = activeChar.getName() + " has canceled a pending petition.";
					GmListTable.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), 17, "Petition System", msgContent));
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER));
				}
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.PETITION_NOT_SUBMITTED));
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 80 RequestPetitionCancel";
	}
	
}
