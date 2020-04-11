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

import l2r.Config;
import l2r.gameserver.datatables.sql.CrestTable;
import l2r.gameserver.model.L2Crest;
import l2r.gameserver.network.serverpackets.PledgeCrest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPledgeCrest extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(RequestPledgeCrest.class);
	
	private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
	
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_crestId == 0)
		{
			return;
		}
		if (Config.DEBUG)
		{
			_log.info("crestid " + _crestId + " requested");
		}
		
		final L2Crest crest = CrestTable.getInstance().getCrest(_crestId);
		byte[] _data = crest != null ? crest.getData() : null;
		
		if (_data != null)
		{
			PledgeCrest pc = new PledgeCrest(_crestId, _data);
			sendPacket(pc);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.info("crest is missing:" + _crestId);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__68_REQUESTPLEDGECREST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
