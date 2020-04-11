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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ExShowFortressMapInfo;

/**
 * @author KenM
 */
public class RequestFortressMapInfo extends L2GameClientPacket
{
	private static final String _C_D0_48_REQUESTFORTRESSMAPINFO = "[C] D0:48 RequestFortressMapInfo";
	private int _fortressId;
	
	@Override
	protected void readImpl()
	{
		_fortressId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Fort fort = FortManager.getInstance().getFortById(_fortressId);
		
		if (fort == null)
		{
			_log.warning("Fort is not found with id (" + _fortressId + ") in all forts with size of (" + FortManager.getInstance().getForts().size() + ") called by player (" + getActiveChar() + ")");
			
			if (getActiveChar() == null)
			{
				return;
			}
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		sendPacket(new ExShowFortressMapInfo(fort));
	}
	
	@Override
	public String getType()
	{
		return _C_D0_48_REQUESTFORTRESSMAPINFO;
	}
}
