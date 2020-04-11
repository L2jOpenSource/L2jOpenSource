/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.Comparator;
import java.util.List;

import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.model.entity.Castle;

/**
 * @author l3x
 */
public final class ExSendManorList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		final List<Castle> castles = CastleManager.getInstance().getCastles();
		castles.sort(Comparator.comparing(Castle::getResidenceId));
		
		writeC(0xFE);
		writeH(0x22);
		writeD(castles.size());
		for (Castle castle : castles)
		{
			writeD(castle.getResidenceId());
			writeS(castle.getName().toLowerCase());
		}
	}
}