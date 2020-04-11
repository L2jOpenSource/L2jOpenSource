/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.model.L2Clan;

/**
 * @author -Wooden-
 */
public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final int _tab;
	
	public PledgeReceiveWarList(L2Clan clan, int tab)
	{
		_clan = clan;
		_tab = tab;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3f);
		
		writeD(_tab); // type : 0 = Declared, 1 = Under Attack
		writeD(0x00); // page
		writeD(_tab == 0 ? _clan.getWarList().size() : _clan.getAttackerList().size());
		for (Integer i : _tab == 0 ? _clan.getWarList() : _clan.getAttackerList())
		{
			L2Clan clan = ClanTable.getInstance().getClan(i);
			if (clan == null)
			{
				continue;
			}
			
			writeS(clan.getName());
			writeD(_tab); // ??
			writeD(_tab); // ??
		}
	}
}
