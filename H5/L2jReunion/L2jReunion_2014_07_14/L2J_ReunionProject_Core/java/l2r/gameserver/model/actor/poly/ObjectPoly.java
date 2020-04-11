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
package l2r.gameserver.model.actor.poly;

import l2r.gameserver.model.L2Object;

public class ObjectPoly
{
	private final L2Object _activeObject;
	private int _polyId;
	private String _polyType;
	
	public ObjectPoly(L2Object activeObject)
	{
		_activeObject = activeObject;
	}
	
	public void setPolyInfo(String polyType, String polyId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}
	
	public final L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	public final boolean isMorphed()
	{
		return getPolyType() != null;
	}
	
	public final int getPolyId()
	{
		return _polyId;
	}
	
	public final void setPolyId(int value)
	{
		_polyId = value;
	}
	
	public final String getPolyType()
	{
		return _polyType;
	}
	
	public final void setPolyType(String value)
	{
		_polyType = value;
	}
}
