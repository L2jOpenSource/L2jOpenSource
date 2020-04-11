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

import l2r.gameserver.model.L2Object;

public class ExSendUIEvent extends L2GameServerPacket
{
	private final L2Object _player;
	private final boolean _isHide;
	private final boolean _isIncrease;
	private final int _startTime;
	private final int _endTime;
	private final String _text;
	
	public ExSendUIEvent(L2Object player, boolean isHide, boolean isIncrease, int startTime, int endTime, String text)
	{
		_player = player;
		_isHide = isHide;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = text;
	}
	
	/**
	 * FIXME: Structure is wrong <br />
	 * _isHide = <s>0: show timer, 1: hide timer</s> Its a type of the UI actually
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x8E);
		writeD(_player.getObjectId());
		writeD(_isHide ? 0x01 : 0x00); // <s>0: show timer, 1: hide timer</s> Its a type of the UI actually
		writeD(0x00); // unknown
		writeD(0x00); // unknown
		writeS(_isIncrease ? "1" : "0"); // "0": count negative, "1": count positive
		writeS(String.valueOf(_startTime / 60)); // timer starting minute(s)
		writeS(String.valueOf(_startTime % 60)); // timer starting second(s)
		writeS(_text); // text above timer
		writeS(String.valueOf(_endTime / 60)); // timer length minute(s) (timer will disappear 10 seconds before it ends)
		writeS(String.valueOf(_endTime % 60)); // timer length second(s) (timer will disappear 10 seconds before it ends)
	}
}