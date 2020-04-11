/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2r.gameserver.model.actor.instance;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.network.serverpackets.ExColosseumFenceInfoPacket;

/**
 * @author KKnD
 */
public final class L2FenceInstance extends L2Object
{
	private final int _type;
	private final int _width;
	private final int _length;
	private final int _xLoc, _yLoc, _zLoc, _mapId;
	
	public L2FenceInstance(int objectId, int type, int width, int length, int x, int y, int z, int eventId)
	{
		super(objectId);
		_type = type;
		_width = width;
		_length = length;
		_xLoc = x;
		_yLoc = y;
		_zLoc = z;
		_mapId = eventId;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExColosseumFenceInfoPacket(this));
	}
	
	public int getXLoc()
	{
		return _xLoc;
	}
	
	public int getYLoc()
	{
		return _yLoc;
	}
	
	public int getZLoc()
	{
		return _zLoc;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getMapId()
	{
		return _mapId;
	}
	
	public int getWidth()
	{
		return _width;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	@Override
	public int getId()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}
