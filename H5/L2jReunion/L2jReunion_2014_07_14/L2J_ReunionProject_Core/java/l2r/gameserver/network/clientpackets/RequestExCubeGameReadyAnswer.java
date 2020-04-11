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

import l2r.gameserver.instancemanager.HandysBlockCheckerManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: chddd d: Arena d: Answer
 * @author mrTJO
 */
public final class RequestExCubeGameReadyAnswer extends L2GameClientPacket
{
	private static final String _C__D0_5C_REQUESTEXCUBEGAMEREADYANSWER = "[C] D0:5C RequestExCubeGameReadyAnswer";
	
	private int _arena;
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		// client sends -1,0,1,2 for arena parameter
		_arena = readD() + 1;
		// client sends 1 if clicked confirm on not clicked, 0 if clicked cancel
		_answer = readD();
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		switch (_answer)
		{
			case 0:
				// Cancel - Answer No
				break;
			case 1:
				// OK or Time Over
				HandysBlockCheckerManager.getInstance().increaseArenaVotes(_arena);
				break;
			default:
				_log.warn("Unknown Cube Game Answer ID: " + _answer);
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_5C_REQUESTEXCUBEGAMEREADYANSWER;
	}
}
