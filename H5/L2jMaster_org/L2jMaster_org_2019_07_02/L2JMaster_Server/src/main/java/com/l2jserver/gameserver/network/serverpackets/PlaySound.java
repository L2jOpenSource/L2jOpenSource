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

import com.l2jserver.gameserver.model.L2Object;

/**
 * @author Zealar
 */
public class PlaySound extends L2GameServerPacket
{
	private final int _type;
	// 0 Sound, 1 Music, 2 Voice
	private final String _soundFile;
	private final int _bindToObject;
	private final int _objectId; // used for ships
	private final int _locX;
	private final int _locY;
	private final int _locZ;
	// Only for Music and Voice
	private final int _delay;
	
	public static PlaySound createSound(String soundName)
	{
		return new PlaySound(soundName);
	}
	
	public static PlaySound createSound(String soundName, L2Object obj)
	{
		return new PlaySound(soundName, obj);
	}
	
	public static PlaySound createMusic(String soundName)
	{
		return createMusic(soundName, 0);
	}
	
	public static PlaySound createMusic(String soundName, int delay)
	{
		return new PlaySound(1, soundName, delay);
	}
	
	public static PlaySound createVoice(String soundName)
	{
		return createVoice(soundName, 0);
	}
	
	public static PlaySound createVoice(String soundName, int delay)
	{
		return new PlaySound(2, soundName, delay);
	}
	
	public PlaySound(String soundFile)
	{
		_type = 0;
		_soundFile = soundFile;
		_bindToObject = 0;
		_objectId = 0;
		_locX = 0;
		_locY = 0;
		_locZ = 0;
		_delay = 0;
	}
	
	private PlaySound(String soundFile, L2Object obj)
	{
		_type = 0;
		_soundFile = soundFile;
		if (obj != null)
		{
			_bindToObject = 1;
			_objectId = obj.getObjectId();
			_locX = obj.getX();
			_locY = obj.getY();
			_locZ = obj.getZ();
		}
		else
		{
			_bindToObject = 0;
			_objectId = 0;
			_locX = 0;
			_locY = 0;
			_locZ = 0;
		}
		_delay = 0;
	}
	
	private PlaySound(int type, String soundFile, int radius)
	{
		_type = type;
		_soundFile = soundFile;
		_bindToObject = 0;
		_objectId = 0;
		_locX = 0;
		_locY = 0;
		_locZ = 0;
		_delay = radius;
	}
	
	public String getSoundName()
	{
		return _soundFile;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9e);
		writeD(_type);
		writeS(_soundFile);
		writeD(_bindToObject);
		writeD(_objectId);
		writeD(_locX);
		writeD(_locY);
		writeD(_locZ);
		writeD(_delay);
	}
}