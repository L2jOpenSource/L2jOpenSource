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
package l2r.gameserver.model.interfaces;

import l2r.gameserver.model.Location;

/**
 * Interface for changing location of object.
 * @author Zoey76
 */
public interface IPositionable extends ILocational
{
	public void setX(int x);
	
	public void setY(int y);
	
	public void setZ(int z);
	
	public void setXYZ(int x, int y, int z);
	
	public void setXYZ(ILocational loc);
	
	public void setHeading(int heading);
	
	public void setInstanceId(int instanceId);
	
	public void setLocation(Location loc);
}
