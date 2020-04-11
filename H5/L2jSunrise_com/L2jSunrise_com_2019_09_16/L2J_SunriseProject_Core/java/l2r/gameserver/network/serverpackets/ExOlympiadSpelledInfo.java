/*
 * Copyright (C) 2004-2016 L2J Server
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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;

/**
 * @author godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private final int _playerId;
	private final List<L2Effect> _effects = new ArrayList<>();
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_playerId = player.getObjectId();
	}
	
	public void addSkill(L2Effect info)
	{
		_effects.add(info);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x7B);
		writeD(_playerId);
		writeD(_effects.size());
		for (L2Effect info : _effects)
		{
			if ((info != null) && info.getInUse())
			{
				writeD(info.getSkill().getDisplayId());
				writeH(info.getSkill().getDisplayLevel());
				writeD(info.getRemainingTime());
			}
		}
	}
}
