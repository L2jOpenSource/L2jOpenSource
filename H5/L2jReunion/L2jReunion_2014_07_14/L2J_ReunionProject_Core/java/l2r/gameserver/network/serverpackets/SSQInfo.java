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

import l2r.gameserver.SevenSigns;

/**
 * Changes the sky color depending on the outcome of the Seven Signs competition.
 * @author Tempy
 */
public class SSQInfo extends L2GameServerPacket
{
	private int _state = 0;
	
	public SSQInfo()
	{
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			if (compWinner == SevenSigns.CABAL_DAWN)
			{
				_state = 2;
			}
			else if (compWinner == SevenSigns.CABAL_DUSK)
			{
				_state = 1;
			}
		}
	}
	
	public SSQInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x73);
		writeH(256 + _state);
	}
}
