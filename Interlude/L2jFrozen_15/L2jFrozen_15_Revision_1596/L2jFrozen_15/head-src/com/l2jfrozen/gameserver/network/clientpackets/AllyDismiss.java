package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class AllyDismiss extends L2GameClientPacket
{
	private String clanName;
	
	@Override
	protected void readImpl()
	{
		clanName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (clanName == null)
		{
			return;
		}
		
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
		
		final L2Clan leaderClan = player.getClan();
		
		if (leaderClan.getAllyId() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		
		if (!player.isClanLeader() || leaderClan.getClanId() != leaderClan.getAllyId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
		
		if (clan == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CLAN_DOESNT_EXISTS));
			return;
		}
		
		if (clan.getClanId() == leaderClan.getClanId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW));
			return;
		}
		
		if (clan.getAllyId() != leaderClan.getAllyId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.DIFFERANT_ALLIANCE));
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		
		leaderClan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000L, L2Clan.PENALTY_TYPE_DISMISS_CLAN); // 24*60*60*1000 = 86400000
		
		leaderClan.updateClanInDB();
		
		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000L, L2Clan.PENALTY_TYPE_CLAN_DISMISSED); // 24*60*60*1000 = 86400000
		clan.setAllyCrest(0);
		clan.updateClanInDB();
		
		player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXPELED_A_CLAN));
	}
	
	@Override
	public String getType()
	{
		return "[C] 85 AllyDismiss";
	}
}