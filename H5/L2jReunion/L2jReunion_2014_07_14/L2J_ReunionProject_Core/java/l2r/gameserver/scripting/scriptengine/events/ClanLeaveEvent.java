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
package l2r.gameserver.scripting.scriptengine.events;

import l2r.gameserver.model.L2Clan;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class ClanLeaveEvent implements L2Event
{
	private int playerId;
	private L2Clan clan;
	
	/**
	 * @return the playerId
	 */
	public int getPlayerId()
	{
		return playerId;
	}
	
	/**
	 * @param playerId the playerId to set
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}
	
	/**
	 * @return the clan
	 */
	public L2Clan getClan()
	{
		return clan;
	}
	
	/**
	 * @param clan the clan to set
	 */
	public void setClan(L2Clan clan)
	{
		this.clan = clan;
	}
}
