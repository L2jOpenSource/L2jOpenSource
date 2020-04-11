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

import java.util.List;

import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.entity.Fort;

/**
 * @author KenM
 */
public class ExShowFortressInfo extends L2GameServerPacket
{
	public static final ExShowFortressInfo STATIC_PACKET = new ExShowFortressInfo();
	
	private ExShowFortressInfo()
	{
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x15);
		List<Fort> forts = FortManager.getInstance().getForts();
		writeD(forts.size());
		for (Fort fort : forts)
		{
			L2Clan clan = fort.getOwnerClan();
			writeD(fort.getResidenceId());
			writeS(clan != null ? clan.getName() : "");
			writeD(fort.getSiege().isInProgress() ? 0x01 : 0x00);
			// Time of possession
			writeD(fort.getOwnedTime());
		}
	}
}
