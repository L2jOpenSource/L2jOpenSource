package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

/**
 * Format: (ch) d[d]
 * @author -Wooden-
 */
public class ExCursedWeaponList extends L2GameServerPacket
{
	private final List<Integer> cursedWeaponIds;
	
	public ExCursedWeaponList(final List<Integer> cursedWeaponIds)
	{
		this.cursedWeaponIds = cursedWeaponIds;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x45);
		
		writeD(cursedWeaponIds.size());
		for (final Integer i : cursedWeaponIds)
		{
			writeD(i.intValue());
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:45 ExCursedWeaponList";
	}
}
