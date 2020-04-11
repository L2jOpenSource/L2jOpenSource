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
import l2r.gameserver.model.items.L2Henna;

/**
 * @author Zoey76
 */
public class HennaItemDrawInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2Henna _henna;
	
	public HennaItemDrawInfo(L2Henna henna, L2PcInstance player)
	{
		_henna = henna;
		_activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE4);
		writeD(_henna.getDyeId()); // symbol Id
		writeD(_henna.getDyeItemId()); // item id of dye
		writeQ(_henna.getWearCount()); // total amount of dye require
		writeQ(_henna.getWearFee()); // total amount of Adena require to draw symbol
		writeD(_henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00); // able to draw or not 0 is false and 1 is true
		writeQ(_activeChar.getAdena());
		writeD(_activeChar.getINT()); // current INT
		writeC(_activeChar.getINT() + _henna.getStatINT()); // equip INT
		writeD(_activeChar.getSTR()); // current STR
		writeC(_activeChar.getSTR() + _henna.getStatSTR()); // equip STR
		writeD(_activeChar.getCON()); // current CON
		writeC(_activeChar.getCON() + _henna.getStatCON()); // equip CON
		writeD(_activeChar.getMEN()); // current MEN
		writeC(_activeChar.getMEN() + _henna.getStatMEN()); // equip MEN
		writeD(_activeChar.getDEX()); // current DEX
		writeC(_activeChar.getDEX() + _henna.getStatDEX()); // equip DEX
		writeD(_activeChar.getWIT()); // current WIT
		writeC(_activeChar.getWIT() + _henna.getStatWIT()); // equip WIT
	}
}
