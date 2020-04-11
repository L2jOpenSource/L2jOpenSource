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

import l2r.gameserver.model.L2Clan;
import l2r.gameserver.scripting.scriptengine.events.ClanJoinEvent;
import l2r.gameserver.scripting.scriptengine.events.ClanLeaderChangeEvent;
import l2r.gameserver.scripting.scriptengine.events.ClanLeaveEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Clan join and leave notifiers
 * @author TheOne
 */
public abstract class ClanMembershipListener extends L2JListener
{
	public ClanMembershipListener()
	{
		register();
	}
	
	/**
	 * A player just joined the clan
	 * @param event
	 * @return
	 */
	public abstract boolean onJoin(ClanJoinEvent event);
	
	/**
	 * A player just left the clan
	 * @param event
	 * @return
	 */
	public abstract boolean onLeave(ClanLeaveEvent event);
	
	/**
	 * Fired when the clan leader changes
	 * @param event
	 * @return
	 */
	public abstract boolean onLeaderChange(ClanLeaderChangeEvent event);
	
	@Override
	public void register()
	{
		L2Clan.addClanMembershipListener(this);
	}
	
	@Override
	public void unregister()
	{
		L2Clan.removeClanMembershipListener(this);
	}
}
