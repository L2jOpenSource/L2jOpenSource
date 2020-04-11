package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeReceivePowerInfo;

/**
 * Format: (ch) dS
 * @author -Wooden-
 */
public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk1;
	private String player;
	
	@Override
	protected void readImpl()
	{
		unk1 = readD();
		player = readS();
	}
	
	@Override
	protected void runImpl()
	{
		// LOGGER.info("C5: RequestPledgeMemberPowerInfo d:"+_unk1);
		// LOGGER.info("C5: RequestPledgeMemberPowerInfo S:"+_player);
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		// do we need powers to do that??
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		final L2ClanMember member = clan.getClanMember(player);
		if (member == null)
		{
			return;
		}
		activeChar.sendPacket(new PledgeReceivePowerInfo(member));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:1B RequestPledgeMemberPowerInfo";
	}
}
