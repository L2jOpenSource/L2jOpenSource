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
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.UserInfo;

/**
 * Appearing Packet Handler
 * <p>
 * <p>
 * 0000: 30
 * <p>
 * <p>
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class Appearing extends L2GameClientPacket
{
	private static final String _C__3A_APPEARING = "[C] 3A Appearing";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.isTeleporting())
		{
			activeChar.onTeleported();
		}
		
		sendPacket(new UserInfo(activeChar));
		sendPacket(new ExBrExtraUserInfo(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__3A_APPEARING;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
