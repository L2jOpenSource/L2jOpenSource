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
package l2r.gameserver.scripting.scriptengine.listeners.clan;

import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.scripting.scriptengine.events.ClanWarEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Notifies when a clan war starts or ends
 * @author TheOne
 */
public abstract class ClanWarListener extends L2JListener
{
	public ClanWarListener()
	{
		register();
	}
	
	/**
	 * Clan war just started
	 * @param event
	 * @return
	 */
	public abstract boolean onWarStart(ClanWarEvent event);
	
	/**
	 * Clan war just ended
	 * @param event
	 * @return
	 */
	public abstract boolean onWarEnd(ClanWarEvent event);
	
	@Override
	public void register()
	{
		ClanTable.addClanWarListener(this);
	}
	
	@Override
	public void unregister()
	{
		ClanTable.removeClanWarListener(this);
	}
}
