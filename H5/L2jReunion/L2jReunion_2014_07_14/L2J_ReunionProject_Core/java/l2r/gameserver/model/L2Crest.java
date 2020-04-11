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

import l2r.Config;
import l2r.gameserver.enums.CrestType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.network.serverpackets.AllyCrest;
import l2r.gameserver.network.serverpackets.ExPledgeCrestLarge;
import l2r.gameserver.network.serverpackets.PledgeCrest;

/**
 * @author Nos
 */
public final class L2Crest implements IIdentifiable
{
	private final int _id;
	private final byte[] _data;
	private final CrestType _type;
	
	public L2Crest(int id, byte[] data, CrestType type)
	{
		_id = id;
		_data = data;
		_type = type;
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public byte[] getData()
	{
		return _data;
	}
	
	public CrestType getType()
	{
		return _type;
	}
	
	/**
	 * Gets the client path to crest for use in html and sends the crest to {@code L2PcInstance}
	 * @param activeChar the @{code L2PcInstance} where html is send to.
	 * @return the client path to crest
	 */
	public String getClientPath(L2PcInstance activeChar)
	{
		String path = null;
		switch (getType())
		{
			case PLEDGE:
			{
				activeChar.sendPacket(new PledgeCrest(getId(), getData()));
				path = "Crest.crest_" + Config.SERVER_ID + "_" + getId();
				break;
			}
			case PLEDGE_LARGE:
			{
				activeChar.sendPacket(new ExPledgeCrestLarge(getId(), getData()));
				path = "Crest.crest_" + Config.SERVER_ID + "_" + getId() + "_l";
				break;
			}
			case ALLY:
			{
				activeChar.sendPacket(new AllyCrest(getId(), getData()));
				path = "Crest.crest_" + Config.SERVER_ID + "_" + getId();
				break;
			}
		}
		return path;
	}
}
