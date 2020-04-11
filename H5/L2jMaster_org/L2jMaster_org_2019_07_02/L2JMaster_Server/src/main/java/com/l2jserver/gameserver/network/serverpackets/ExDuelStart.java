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
package com.l2jserver.gameserver.network.serverpackets;

/**
 * Duel Start packet implementation.
 * @author KenM, Zoey76
 */
public class ExDuelStart extends L2GameServerPacket
{
	public static final ExDuelStart PLAYER_DUEL = new ExDuelStart(false);
	public static final ExDuelStart PARTY_DUEL = new ExDuelStart(true);
	
	private final boolean _partyDuel;
	
	public ExDuelStart(boolean partyDuel)
	{
		_partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4e);
		
		writeD(_partyDuel ? 1 : 0);
	}
}
