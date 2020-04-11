package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class AllyLeave extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.getClan() == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
			return;
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_WITHDRAW_ALLY));
			return;
		}
		
		final L2Clan clan = player.getClan();
		
		if (clan.getAllyId() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		
		if (clan.getClanId() == clan.getAllyId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW));
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		
		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000L, L2Clan.PENALTY_TYPE_CLAN_LEAVED); // 24*60*60*1000 = 86400000
		clan.setAllyCrest(0);
		clan.updateClanInDB();
		
		player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE));
	}
	
	@Override
	public String getType()
	{
		return "[C] 84 AllyLeave";
	}
}