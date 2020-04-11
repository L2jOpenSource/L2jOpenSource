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
package l2r.gameserver.model.actor.instance.PcInstance;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcExtension
{
	protected static final Logger _log = LoggerFactory.getLogger(PcExtension.class.getName());
	
	private L2PcInstance _activeChar = null;
	
	public PcExtension(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		_activeChar = activeChar;
		if (Config.DEBUG)
		{
			_log.info("[PcExtension] _activeChar: " + _activeChar.getObjectId() + " - " + _activeChar.getName() + ".");
		}
	}
	
	public L2PcInstance getPlayer()
	{
		return _activeChar;
	}
}
