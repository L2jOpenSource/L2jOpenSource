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
import gr.reunion.interf.ReunionEvents;

/**
 * This class ...
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public final class ObserverReturn extends L2GameClientPacket
{
	private static final String __C__C1_OBSERVERRETURN = "[C] C1 ObserverReturn";
	
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
		if (ReunionEvents.isObserving(activeChar))
		{
			ReunionEvents.endObserving(activeChar);
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.leaveObserverMode();
			// activeChar.teleToLocation(activeChar.getObsX(), activeChar.getObsY(), activeChar.getObsZ());
		}
	}
	
	@Override
	public String getType()
	{
		return __C__C1_OBSERVERRETURN;
	}
}