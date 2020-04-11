package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;

/**
 * @author -Wooden-
 */
public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final L2Clan clan;
	private final int tab;
	
	public PledgeReceiveWarList(final L2Clan clan, final int tab)
	{
		this.clan = clan;
		this.tab = tab;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3e);
		
		writeD(tab); // type : 0 = Declared, 1 = Under Attack
		writeD(0x00); // page
		writeD(tab == 0 ? clan.getWarList().size() : clan.getAttackerList().size());
		for (final Integer i : tab == 0 ? clan.getWarList() : clan.getAttackerList())
		{
			final L2Clan clan = ClanTable.getInstance().getClan(i);
			if (clan == null)
			{
				continue;
			}
			
			writeS(clan.getName());
			writeD(tab); // ??
			writeD(tab); // ??
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3E PledgeReceiveWarList";
	}
}
