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

import java.util.List;

import com.l2jserver.util.StringUtil;

public class ShowBoard extends L2GameServerPacket
{
	private final String _content;
	private int _hide = 1;
	
	public ShowBoard(String htmlCode, String id)
	{
		_content = id + "\u0008" + htmlCode;
		_hide = Integer.parseInt(id);
	}
	
	public ShowBoard(List<String> arg)
	{
		StringBuilder builder = new StringBuilder(5 + StringUtil.getLength(arg) + arg.size()).append("1002\u0008");
		for (String str : arg)
		{
			builder.append(str).append("\u0008");
		}
		_content = builder.toString();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7B);
		if (_hide == 0)
		{
			writeC(0x00);
		}
		else
		{
			writeC(0x01); // c4 1 to show community 00 to hide
		}
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		writeS(_content);
	}
}
