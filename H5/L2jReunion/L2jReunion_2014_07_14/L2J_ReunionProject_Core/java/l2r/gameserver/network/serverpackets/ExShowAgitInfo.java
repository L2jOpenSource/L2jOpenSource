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

import java.util.Map;

import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.model.entity.clanhall.AuctionableHall;

/**
 * @author KenM
 */
public class ExShowAgitInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x16);
		Map<Integer, AuctionableHall> clannhalls = ClanHallManager.getInstance().getAllAuctionableClanHalls();
		writeD(clannhalls.size());
		for (AuctionableHall ch : clannhalls.values())
		{
			writeD(ch.getId());
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getName()); // owner clan name
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getLeaderName()); // leader name
			writeD(ch.getGrade() > 0 ? 0x00 : 0x01); // 0 - auction 1 - war clanhall 2 - ETC (rainbow spring clanhall)
		}
	}
	
}
