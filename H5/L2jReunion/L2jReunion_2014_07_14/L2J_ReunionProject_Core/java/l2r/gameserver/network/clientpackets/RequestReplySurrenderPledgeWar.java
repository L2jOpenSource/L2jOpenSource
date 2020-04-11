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

import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	private static final String _C__08_REQUESTREPLYSURRENDERPLEDGEWAR = "[C] 08 RequestReplySurrenderPledgeWar";
	
	private String _reqName;
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_reqName = readS();
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 1)
		{
			requestor.deathPenalty(false, false, false);
			ClanTable.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			_log.info(getClass().getSimpleName() + ": Missing implementation for answer: " + _answer + " and name: " + _reqName + "!");
		}
		activeChar.onTransactionRequest(requestor);
	}
	
	@Override
	public String getType()
	{
		return _C__08_REQUESTREPLYSURRENDERPLEDGEWAR;
	}
}