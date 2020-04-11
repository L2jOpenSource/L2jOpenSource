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
package l2r.gameserver.model;

import l2r.gameserver.model.interfaces.IUniqueId;

/**
 * @author xban1x
 */
public final class AbsorberInfo implements IUniqueId
{
	private int _objectId;
	private double _absorbedHp;
	
	public AbsorberInfo(int objectId, double pAbsorbedHp)
	{
		_objectId = objectId;
		_absorbedHp = pAbsorbedHp;
	}
	
	public double getAbsorbedHp()
	{
		return _absorbedHp;
	}
	
	public void setAbsorbedHp(double absorbedHp)
	{
		_absorbedHp = absorbedHp;
	}
	
	@Override
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof AbsorberInfo)
		{
			return (((AbsorberInfo) obj).getObjectId() == _objectId);
		}
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _objectId;
	}
}
