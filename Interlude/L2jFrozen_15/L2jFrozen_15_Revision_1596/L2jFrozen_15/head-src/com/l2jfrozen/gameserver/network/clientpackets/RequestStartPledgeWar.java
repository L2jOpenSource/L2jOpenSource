package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestStartPledgeWar extends L2GameClientPacket
{
	private String pledgeName;
	private L2Clan clanInstance;
	private L2PcInstance player;
	
	@Override
	protected void readImpl()
	{
		pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		clanInstance = getClient().getActiveChar().getClan();
		if (clanInstance == null)
		{
			return;
		}
		
		if (clanInstance.getLevel() < 3 || clanInstance.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (!player.isClanLeader())
		{
			player.sendMessage("You can't declare war. You are not clan leader.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(pledgeName);
		if (clan == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (clanInstance.getAllyId() == clan.getAllyId() && clanInstance.getAllyId() != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		else if (clan.getLevel() < 3 || clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (clanInstance.isAtWarWith(clan.getClanId()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS); // msg id 628
			sm.addString(clan.getName());
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		
		ClanTable.getInstance().storeClanWars(player.getClanId(), clan.getClanId());
		for (final L2PcInstance cha : L2World.getInstance().getAllPlayers())
		{
			if (cha.getClan() == player.getClan() || cha.getClan() == clan)
			{
				cha.broadcastUserInfo();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 4D RequestStartPledgewar";
	}
}