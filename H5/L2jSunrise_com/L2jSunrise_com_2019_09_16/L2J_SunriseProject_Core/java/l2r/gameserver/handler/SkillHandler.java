/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.gameserver.model.skills.L2SkillType;

/**
 * @author UnAfraid
 */
public class SkillHandler implements IHandler<ISkillHandler, L2SkillType>
{
	private final Map<Integer, ISkillHandler> _datatable;
	
	protected SkillHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.put(t.ordinal(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.remove(t.ordinal());
		}
	}
	
	@Override
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _datatable.get(skillType.ordinal());
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
