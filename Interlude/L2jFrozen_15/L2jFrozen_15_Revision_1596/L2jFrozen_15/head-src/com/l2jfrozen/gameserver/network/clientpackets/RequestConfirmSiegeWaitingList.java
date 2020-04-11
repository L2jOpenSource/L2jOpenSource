package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private int approved;
	private int castleId;
	private int clanId;
	
	@Override
	protected void readImpl()
	{
		castleId = readD();
		clanId = readD();
		approved = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Check if the player has a clan
		if (activeChar.getClan() == null)
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(castleId);
		if (castle == null)
		{
			return;
		}
		
		// Check if leader of the clan who owns the castle?
		if (castle.getOwnerId() != activeChar.getClanId() || !activeChar.isClanLeader())
		{
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		if (!castle.getSiege().getIsRegistrationOver())
		{
			if (approved == 1)
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan))
				{
					castle.getSiege().approveSiegeDefenderClan(clanId);
				}
				else
				{
					return;
				}
			}
			else
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan) || castle.getSiege().checkIsDefender(clan))
				{
					castle.getSiege().removeSiegeClan(clanId);
				}
			}
		}
		
		// Update the defender list
		activeChar.sendPacket(new SiegeDefenderList(castle));
		
	}
	
	@Override
	public String getType()
	{
		return "[C] a5 RequestConfirmSiegeWaitingList";
	}
}
