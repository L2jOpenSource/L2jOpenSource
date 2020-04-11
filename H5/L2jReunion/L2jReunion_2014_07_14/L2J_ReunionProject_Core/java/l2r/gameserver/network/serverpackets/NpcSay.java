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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.network.NpcStringId;

/**
 * @author Kerberos
 */
public final class NpcSay extends L2GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private final int _npcId;
	private String _text;
	private final int _npcString;
	private List<String> _parameters;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param npcId
	 * @param text
	 */
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = -1;
		_text = text;
	}
	
	public NpcSay(L2Npc npc, int messageType, String text)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getId();
		_npcString = -1;
		_text = text;
	}
	
	public NpcSay(int objectId, int messageType, int npcId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString.getId();
	}
	
	public NpcSay(L2Npc npc, int messageType, NpcStringId npcString)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getId();
		_npcString = npcString.getId();
	}
	
	/**
	 * @param text the text to add as a parameter for this packet's message (replaces S1, S2 etc.)
	 * @return this NpcSay packet object
	 */
	public NpcSay addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
		return this;
	}
	
	/**
	 * @param params a list of strings to add as parameters for this packet's message (replaces S1, S2 etc.)
	 * @return this NpcSay packet object
	 */
	public NpcSay addStringParameters(String... params)
	{
		if ((params != null) && (params.length > 0))
		{
			if (_parameters == null)
			{
				_parameters = new ArrayList<>();
			}
			
			for (String item : params)
			{
				if ((item != null) && (item.length() > 0))
				{
					_parameters.add(item);
				}
			}
		}
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x30);
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		writeD(_npcString);
		if (_npcString == -1)
		{
			writeS(_text);
		}
		else if (_parameters != null)
		{
			for (String s : _parameters)
			{
				writeS(s);
			}
		}
	}
}