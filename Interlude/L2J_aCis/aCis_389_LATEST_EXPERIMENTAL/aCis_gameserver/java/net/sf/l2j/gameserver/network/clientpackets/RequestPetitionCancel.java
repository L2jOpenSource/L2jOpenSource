package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.PetitionManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetitionCancel extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (PetitionManager.getInstance().isPlayerInConsultation(player))
		{
			if (player.isGM())
				PetitionManager.getInstance().endActivePetition(player);
			else
				player.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
		}
		else
		{
			if (PetitionManager.getInstance().isPlayerPetitionPending(player))
			{
				if (PetitionManager.getInstance().cancelActivePetition(player))
				{
					int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(player);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY).addNumber(numRemaining));
					
					// Notify all GMs that the player's pending petition has been cancelled.
					String msgContent = player.getName() + " has canceled a pending petition.";
					AdminData.getInstance().broadcastToGMs(new CreatureSay(player.getObjectId(), SayType.HERO_VOICE, "Petition System", msgContent));
				}
				else
					player.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
			}
			else
				player.sendPacket(SystemMessageId.PETITION_NOT_SUBMITTED);
		}
	}
}