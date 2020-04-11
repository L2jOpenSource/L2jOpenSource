package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;

public class ManagePledgePower extends L2GameServerPacket
{
	private final int action;
	private final L2Clan clan;
	private final int rank;
	private int privs;
	
	public ManagePledgePower(final L2Clan clan, final int action, final int rank)
	{
		this.clan = clan;
		this.action = action;
		this.rank = rank;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (action == 1)
		{
			privs = clan.getRankPrivs(rank);
		}
		else
		{
			return;
		}
		/*
		 * if (L2World.getInstance().findObject(_clanId) == null) return; privs = ((L2PcInstance)L2World.getInstance().findObject(_clanId)).getClanPrivileges();
		 */
		writeC(0x30);
		writeD(0);
		writeD(0);
		writeD(privs);
	}
	
	@Override
	public String getType()
	{
		return "[S] 30 ManagePledgePower";
	}
	
}
