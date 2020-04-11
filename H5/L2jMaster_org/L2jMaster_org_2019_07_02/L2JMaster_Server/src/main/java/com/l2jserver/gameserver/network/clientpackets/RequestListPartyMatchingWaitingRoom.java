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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * @author Gnacik
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static final String _C__D0_31_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:31 RequestListPartyMatchingWaitingRoom";
	private int _page;
	private int _minlvl;
	private int _maxlvl;
	private int _mode; // 1 - waitlist 0 - room waitlist
	
	@Override
	protected void readImpl()
	{
		_page = readD();
		_minlvl = readD();
		_maxlvl = readD();
		_mode = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
		{
			return;
		}
		
		_activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(_activeChar, _page, _minlvl, _maxlvl, _mode));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_31_REQUESTLISTPARTYMATCHINGWAITINGROOM;
	}
	
}