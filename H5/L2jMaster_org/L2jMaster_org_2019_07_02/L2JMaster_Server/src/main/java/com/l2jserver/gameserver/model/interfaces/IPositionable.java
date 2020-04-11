/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.interfaces;

import com.l2jserver.gameserver.model.Location;

/**
 * Object world location storage and update interface.
 * @author Zoey76
 */
public interface IPositionable extends ILocational
{
	/**
	 * Sets the X coordinate of this object.
	 * @param x the new X coordinate
	 */
	public void setX(int x);
	
	/**
	 * Sets the Y coordinate of this object.
	 * @param y the new Y coordinate
	 */
	public void setY(int y);
	
	/**
	 * Sets the Z coordinate of this object.
	 * @param z the new Z coordinate
	 */
	public void setZ(int z);
	
	/**
	 * Sets all three coordinates of this object.
	 * @param x the new X coordinate
	 * @param y the new Y coordinate
	 * @param z the new Z coordinate
	 */
	public void setXYZ(int x, int y, int z);
	
	/**
	 * Sets all three coordinates of this object.
	 * @param loc the object whose coordinates to use
	 */
	public void setXYZ(ILocational loc);
	
	/**
	 * Sets the heading of this object.
	 * @param heading the new heading
	 */
	public void setHeading(int heading);
	
	/**
	 * Changes the instance zone ID of this object.
	 * @param instanceId the ID of the instance zone to put this object in (0 - not in any instance)
	 */
	public void setInstanceId(int instanceId);
	
	/**
	 * Changes the location of this object.
	 * @param loc the new location
	 */
	public void setLocation(Location loc);
}