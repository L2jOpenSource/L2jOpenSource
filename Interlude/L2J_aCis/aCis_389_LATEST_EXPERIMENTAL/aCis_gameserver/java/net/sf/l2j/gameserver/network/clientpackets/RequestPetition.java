package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.PetitionManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetition extends L2GameClientPacket
{
	private String _content;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_content = readS();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (!AdminData.getInstance().isGmOnline(false))
		{
			player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			player.sendPacket(new PlaySound("systemmsg_e.702"));
			return;
		}
		
		if (!Config.PETITIONING_ALLOWED)
		{
			player.sendPacket(SystemMessageId.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER);
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerPetitionPending(player))
		{
			player.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
			return;
		}
		
		if (PetitionManager.getInstance().getPendingPetitions().size() == Config.MAX_PETITIONS_PENDING)
		{
			player.sendPacket(SystemMessageId.PETITION_SYSTEM_CURRENT_UNAVAILABLE);
			return;
		}
		
		final int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(player) + 1;
		if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY).addNumber(totalPetitions));
			return;
		}
		
		if (_content.length() > 255)
		{
			player.sendPacket(SystemMessageId.PETITION_MAX_CHARS_255);
			return;
		}
		
		final int petitionId = PetitionManager.getInstance().submitPetition(player, _content, _type);
		
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(petitionId));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT).addNumber(totalPetitions).addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PETITION_ON_WAITING_LIST).addNumber(PetitionManager.getInstance().getPendingPetitions().size()));
	}
}