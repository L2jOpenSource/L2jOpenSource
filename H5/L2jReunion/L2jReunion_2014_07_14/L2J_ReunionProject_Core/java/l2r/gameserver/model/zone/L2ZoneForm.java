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
package l2r.gameserver.model.zone;

import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * Abstract base class for any zone form
 * @author durgus
 */
public abstract class L2ZoneForm
{
	protected static final int STEP = 10;
	
	public abstract boolean isInsideZone(int x, int y, int z);
	
	public abstract boolean intersectsRectangle(int x1, int x2, int y1, int y2);
	
	public abstract double getDistanceToZone(int x, int y);
	
	public abstract int getLowZ(); // Support for the ability to extract the z coordinates of zones.
	
	public abstract int getHighZ(); // New fishing patch makes use of that to get the Z for the hook
	
	// landing coordinates.
	
	protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
	{
		return java.awt.geom.Line2D.linesIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
	}
	
	public abstract void visualizeZone(int z);
	
	protected final void dropDebugItem(int itemId, int num, int x, int y, int z)
	{
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(num);
		item.spawnMe(x, y, z + 5);
		ZoneManager.getInstance().getDebugItems().add(item);
	}
	
	public abstract int[] getRandomPoint();
}
