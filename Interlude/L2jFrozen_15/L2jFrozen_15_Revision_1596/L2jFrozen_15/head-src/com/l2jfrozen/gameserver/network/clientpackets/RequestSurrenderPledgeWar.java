package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
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
		
		clanInstance = activeChar.getClan();
		if (clanInstance == null)
		{
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(pledgeName);
		if (clan == null)
		{
			activeChar.sendMessage("No such clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		LOGGER.info("RequestSurrenderPledgeWar by " + getClient().getActiveChar().getClan().getName() + " with " + pledgeName);
		
		if (!clanInstance.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendMessage("You aren't at war with this clan.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(pledgeName);
		activeChar.sendPacket(msg);
		msg = null;
		activeChar.deathPenalty(false);
		ClanTable.getInstance().deleteClanWars(clanInstance.getClanId(), clan.getClanId());
		/*
		 * L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName()); if(leader != null && leader.isOnline() == 0) { player.sendMessage("Clan leader isn't online."); player.sendPacket(ActionFailed.STATIC_PACKET); return; } if (leader.isTransactionInProgress()) { SystemMessage sm = new
		 * SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER); sm.addString(leader.getName()); player.sendPacket(sm); return; } leader.setTransactionRequester(player); player.setTransactionRequester(leader); leader.sendPacket(new SurrenderPledgeWar(_clan.getName(),player.getName()));
		 */
	}
	
	@Override
	public String getType()
	{
		return "[C] 51 RequestSurrenderPledgeWar";
	}
}
