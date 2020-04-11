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

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author mrTJO
 */
public class ExCubeGameAddPlayer extends L2GameServerPacket
{
	L2PcInstance _player;
	boolean _isRedTeam;
	
	/**
	 * Add Player To Minigame Waiting List
	 * @param player Player Instance
	 * @param isRedTeam Is Player from Red Team?
	 */
	public ExCubeGameAddPlayer(L2PcInstance player, boolean isRedTeam)
	{
		_player = player;
		_isRedTeam = isRedTeam;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x97);
		writeD(0x01);
		
		writeD(0xffffffff);
		
		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
		writeS(_player.getName());
	}
}