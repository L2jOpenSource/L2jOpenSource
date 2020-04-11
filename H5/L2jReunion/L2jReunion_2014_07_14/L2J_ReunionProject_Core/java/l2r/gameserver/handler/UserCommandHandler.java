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
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelDelete;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelInfo;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ChannelLeave;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ClanPenalty;
import l2r.gameserver.scripts.handlers.usercommandhandlers.ClanWarsList;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Dismount;
import l2r.gameserver.scripts.handlers.usercommandhandlers.InstanceZone;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Loc;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Mount;
import l2r.gameserver.scripts.handlers.usercommandhandlers.MyBirthday;
import l2r.gameserver.scripts.handlers.usercommandhandlers.OlympiadStat;
import l2r.gameserver.scripts.handlers.usercommandhandlers.PartyInfo;
import l2r.gameserver.scripts.handlers.usercommandhandlers.SiegeStatus;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Time;
import l2r.gameserver.scripts.handlers.usercommandhandlers.Unstuck;

/**
 * @author UnAfraid
 */
public class UserCommandHandler implements IHandler<IUserCommandHandler, Integer>
{
	private final Map<Integer, IUserCommandHandler> _datatable;
	
	protected UserCommandHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new ChannelDelete());
		registerHandler(new ChannelInfo());
		registerHandler(new ChannelLeave());
		registerHandler(new ClanPenalty());
		registerHandler(new ClanWarsList());
		registerHandler(new Dismount());
		registerHandler(new InstanceZone());
		registerHandler(new Loc());
		registerHandler(new Mount());
		registerHandler(new MyBirthday());
		registerHandler(new SiegeStatus());
		registerHandler(new OlympiadStat());
		registerHandler(new PartyInfo());
		registerHandler(new Time());
		registerHandler(new Unstuck());
	}
	
	@Override
	public void registerHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		for (int id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	@Override
	public IUserCommandHandler getHandler(Integer userCommand)
	{
		return _datatable.get(userCommand);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static UserCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final UserCommandHandler _instance = new UserCommandHandler();
	}
}
