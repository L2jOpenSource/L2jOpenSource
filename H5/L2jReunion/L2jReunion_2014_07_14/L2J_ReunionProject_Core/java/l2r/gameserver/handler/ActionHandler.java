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

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ArtefactInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2DecoyAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2DoorInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ItemInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2NpcAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PcInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PetInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2StaticObjectInstanceAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2SummonAction;
import l2r.gameserver.scripts.handlers.actionhandlers.L2TrapAction;

/**
 * @author UnAfraid
 */
public class ActionHandler implements IHandler<IActionHandler, InstanceType>
{
	private final Map<InstanceType, IActionHandler> _actions;
	
	public static ActionHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ActionHandler()
	{
		_actions = new HashMap<>();
		
		registerHandler(new L2ArtefactInstanceAction());
		registerHandler(new L2DecoyAction());
		registerHandler(new L2DoorInstanceAction());
		registerHandler(new L2ItemInstanceAction());
		registerHandler(new L2NpcAction());
		registerHandler(new L2PcInstanceAction());
		registerHandler(new L2PetInstanceAction());
		registerHandler(new L2StaticObjectInstanceAction());
		registerHandler(new L2SummonAction());
		registerHandler(new L2TrapAction());
	}
	
	@Override
	public void registerHandler(IActionHandler handler)
	{
		_actions.put(handler.getInstanceType(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IActionHandler handler)
	{
		_actions.remove(handler.getInstanceType());
	}
	
	@Override
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actions.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}
	
	@Override
	public int size()
	{
		return _actions.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ActionHandler _instance = new ActionHandler();
	}
}