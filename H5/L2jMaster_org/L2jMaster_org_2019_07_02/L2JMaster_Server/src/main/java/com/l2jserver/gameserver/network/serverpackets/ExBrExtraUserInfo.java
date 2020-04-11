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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * ExBrExtraUserInfo server packet implementation.
 * @author Kerberos, Zoey76
 */
public class ExBrExtraUserInfo extends L2GameServerPacket
{
	/** Player object ID. */
	private final int _charObjId;
	/** Event abnormal visual effects map. */
	private final int _abnormalVisualEffectsEvent;
	/** Lecture mark. */
	private final int _lectureMark;
	
	public ExBrExtraUserInfo(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_abnormalVisualEffectsEvent = player.getAbnormalVisualEffectEvent();
		_lectureMark = 0; // TODO: Implement.
		setInvisible(player.isInvisible());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xDA);
		writeD(_charObjId);
		writeD(_abnormalVisualEffectsEvent);
		writeC(_lectureMark);
	}
}
