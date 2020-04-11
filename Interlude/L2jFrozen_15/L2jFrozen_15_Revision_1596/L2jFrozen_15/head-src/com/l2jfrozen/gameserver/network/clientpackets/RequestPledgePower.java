package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(ManagePledgePower.class);
	private int rank;
	private int action;
	private int privs;
	
	@Override
	protected void readImpl()
	{
		rank = readD();
		action = readD();
		if (action == 2)
		{
			privs = readD();
		}
		else
		{
			privs = 0;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (action == 2)
		{
			if (player.getClan() != null && player.isClanLeader())
			{
				if (rank == 9)
				{
					// The rights below cannot be bestowed upon Academy members:
					// Join a clan or be dismissed
					// Title management, crest management, master management, level management,
					// bulletin board administration
					// Clan war, right to dismiss, set functions
					// Auction, manage taxes, attack/defend registration, mercenary management
					// => Leaves only CP_CL_VIEW_WAREHOUSE, CP_CH_OPEN_DOOR, CP_CS_OPEN_DOOR?
					privs = (privs & L2Clan.CP_CL_VIEW_WAREHOUSE) + (privs & L2Clan.CP_CH_OPEN_DOOR) + (privs & L2Clan.CP_CS_OPEN_DOOR);
				}
				player.getClan().setRankPrivs(rank, privs);
			}
		}
		else
		{
			final ManagePledgePower mpp = new ManagePledgePower(getClient().getActiveChar().getClan(), action, rank);
			player.sendPacket(mpp);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] C0 RequestPledgePower";
	}
}
