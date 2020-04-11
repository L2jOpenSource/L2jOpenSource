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
import java.util.Map;

import l2r.gameserver.model.TimeStamp;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * Skill Cool Time server packet implementation.
 * @author KenM, Zoey76
 */
public class SkillCoolTime extends L2GameServerPacket
{
	private final List<TimeStamp> _skillReuseTimeStamps = new ArrayList<>();
	
	public SkillCoolTime(L2PcInstance player)
	{
		final Map<Integer, TimeStamp> skillReuseTimeStamps = player.getSkillReuseTimeStamps();
		if (skillReuseTimeStamps != null)
		{
			for (TimeStamp ts : skillReuseTimeStamps.values())
			{
				if (ts.hasNotPassed())
				{
					_skillReuseTimeStamps.add(ts);
				}
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC7);
		writeD(_skillReuseTimeStamps.size());
		for (TimeStamp ts : _skillReuseTimeStamps)
		{
			writeD(ts.getSkillId());
			writeD(ts.getSkillLvl());
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
}
