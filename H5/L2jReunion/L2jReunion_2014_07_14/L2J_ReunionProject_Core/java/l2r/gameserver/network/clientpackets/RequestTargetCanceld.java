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
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.TargetUnselected;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestTargetCanceld extends L2GameClientPacket
{
	private static final String _C__48_REQUESTTARGETCANCELD = "[C] 48 RequestTargetCanceld";
	
	private int _unselect;
	
	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isLockedTarget())
		{
			activeChar.sendPacket(SystemMessageId.FAILED_DISABLE_TARGET);
			return;
		}
		
		if (_unselect == 0)
		{
			if (activeChar.isCastingNow() && activeChar.canAbortCast())
			{
				activeChar.abortCast();
			}
			else if (activeChar.getTarget() != null)
			{
				activeChar.setTarget(null);
			}
		}
		else if (activeChar.getTarget() != null)
		{
			activeChar.setTarget(null);
		}
		else if (activeChar.isInAirShip())
		{
			activeChar.broadcastPacket(new TargetUnselected(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__48_REQUESTTARGETCANCELD;
	}
}
