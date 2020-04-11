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
import l2r.gameserver.scripts.handlers.actionhandlers.L2DoorInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2ItemInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2NpcActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2PcInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2StaticObjectInstanceActionShift;
import l2r.gameserver.scripts.handlers.actionhandlers.L2SummonActionShift;

/**
 * @author UnAfraid
 */
public class ActionShiftHandler
{
	private final Map<InstanceType, IActionHandler> _actionsShift;
	
	protected ActionShiftHandler()
	{
		_actionsShift = new HashMap<>();
		
		registerHandler(new L2DoorInstanceActionShift());
		registerHandler(new L2ItemInstanceActionShift());
		registerHandler(new L2NpcActionShift());
		registerHandler(new L2PcInstanceActionShift());
		registerHandler(new L2StaticObjectInstanceActionShift());
		registerHandler(new L2SummonActionShift());
	}
	
	public void registerHandler(IActionHandler handler)
	{
		_actionsShift.put(handler.getInstanceType(), handler);
	}
	
	public synchronized void removeHandler(IActionHandler handler)
	{
		_actionsShift.remove(handler.getInstanceType());
	}
	
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _actionsShift.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}
	
	public int size()
	{
		return _actionsShift.size();
	}
	
	public static ActionShiftHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ActionShiftHandler _instance = new ActionShiftHandler();
	}
}