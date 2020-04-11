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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExOlympiadMatchList;

/**
 * Format: (ch)d d: unknown (always 0?)
 * @author mrTJO
 */
public class RequestExOlympiadMatchListRefresh extends L2GameClientPacket
{
	private static final String _C__D0_88_REQUESTEXOLYMPIADMATCHLISTREFRESH = "[C] D0:88 RequestExOlympiadMatchListRefresh";
	
	@Override
	protected void readImpl()
	{
		// readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new ExOlympiadMatchList());
	}
	
	@Override
	public String getType()
	{
		return _C__D0_88_REQUESTEXOLYMPIADMATCHLISTREFRESH;
	}
}