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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.interfaces.IPositionable;
import l2r.gameserver.network.L2GameClient;

import org.mmocore.network.SendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Logger _log = LoggerFactory.getLogger(L2GameServerPacket.class);
	
	protected boolean _invisible = false;
	
	/**
	 * @return True if packet originated from invisible character.
	 */
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	/**
	 * Set "invisible" boolean flag in the packet.<br>
	 * Packets from invisible characters will not be broadcasted to players.
	 * @param b
	 */
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}
	
	/**
	 * Writes 3 D (int32) with current location x, y, z
	 * @param loc
	 */
	protected void writeLoc(IPositionable loc)
	{
		writeD(loc.getX());
		writeD(loc.getY());
		writeD(loc.getZ());
	}
	
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch (Exception e)
		{
			_log.error("Client: " + getClient().toString() + " - Failed writing: " + getClass().getSimpleName() + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{
		
	}
	
	protected abstract void writeImpl();
}
