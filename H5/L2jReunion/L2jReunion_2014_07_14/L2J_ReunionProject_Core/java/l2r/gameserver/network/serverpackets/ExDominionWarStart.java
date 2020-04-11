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

import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author JIV
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	private final int _objId;
	private final int _terId;
	private final boolean _isDisguised;
	
	public ExDominionWarStart(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_terId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		_isDisguised = TerritoryWarManager.getInstance().isDisguised(_objId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xA3);
		writeD(_objId);
		writeD(0x01); // ??
		writeD(_terId);
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _terId : 0);
	}
}
