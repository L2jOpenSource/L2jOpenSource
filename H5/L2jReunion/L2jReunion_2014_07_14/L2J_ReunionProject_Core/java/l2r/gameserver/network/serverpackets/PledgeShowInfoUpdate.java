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

import l2r.gameserver.model.L2Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private final L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8E);
		// sending empty data so client will ask all the info in response ;)
		writeD(_clan.getId());
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel()); // clan level
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore()); // clan reputation score
		writeD(0x00); // ?
		writeD(0x00); // ?
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName()); // c5
		writeD(_clan.getAllyCrestId()); // c5
		writeD(_clan.isAtWar() ? 1 : 0); // c5
	}
}
