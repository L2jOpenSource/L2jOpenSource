
package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author ProGramMoS
 */
public final class RequestJoinSiege extends L2GameClientPacket
{
	private int castleId;
	private int isAttacker;
	private int isJoining;
	
	@Override
	protected void readImpl()
	{
		castleId = readD();
		isAttacker = readD();
		isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (!player.isClanLeader())
		{
			return;
		}
		
		if (castleId < 100)
		{
			final Castle castle = CastleManager.getInstance().getCastleById(castleId);
			
			if (castle == null)
			{
				return;
			}
			
			if (isJoining == 1)
			{
				if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
					return;
				}
				
				if (isAttacker == 1)
				{
					castle.getSiege().registerAttacker(player);
				}
				else
				{
					castle.getSiege().registerDefender(player);
				}
			}
			else
			{
				castle.getSiege().removeSiegeClan(player);
			}
			
			castle.getSiege().listRegisterClan(player);
		}
		else
		{
			final Fort fort = FortManager.getInstance().getFortById(castleId);
			
			if (fort == null)
			{
				return;
			}
			
			if (isJoining == 1)
			{
				if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
					return;
				}
				
				if (isAttacker == 1)
				{
					fort.getSiege().registerAttacker(player);
				}
				else
				{
					fort.getSiege().registerDefender(player);
				}
			}
			else
			{
				fort.getSiege().removeSiegeClan(player);
			}
			
			fort.getSiege().listRegisterClan(player);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] a4 RequestJoinSiege";
	}
}
