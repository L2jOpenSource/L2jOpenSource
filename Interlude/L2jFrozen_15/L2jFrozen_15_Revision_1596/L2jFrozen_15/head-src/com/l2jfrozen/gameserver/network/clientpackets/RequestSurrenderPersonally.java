package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPersonally extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestSurrenderPledgeWar.class);
	
	private String pledgeName;
	private L2Clan clanInstance;
	private L2PcInstance activeChar;
	
	@Override
	protected void readImpl()
	{
		pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		LOGGER.info("RequestSurrenderPersonally by " + getClient().getActiveChar().getName() + " with " + pledgeName);
		clanInstance = getClient().getActiveChar().getClan();
		final L2Clan clan = ClanTable.getInstance().getClanByName(pledgeName);
		if (clanInstance == null)
		{
			return;
		}
		
		if (clan == null)
		{
			activeChar.sendMessage("No such clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!clanInstance.isAtWarWith(clan.getClanId()) || activeChar.getWantsPeace() == 1)
		{
			activeChar.sendMessage("You aren't at war with this clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.setWantsPeace(1);
		activeChar.deathPenalty(false);
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(pledgeName);
		activeChar.sendPacket(msg);
		msg = null;
		ClanTable.getInstance().checkSurrender(clanInstance, clan);
	}
	
	@Override
	public String getType()
	{
		return "[C] 69 RequestSurrenderPersonally";
	}
}
