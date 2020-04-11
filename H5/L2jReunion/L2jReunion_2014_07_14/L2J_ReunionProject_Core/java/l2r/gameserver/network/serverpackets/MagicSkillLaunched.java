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

import java.util.Arrays;
import java.util.List;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final List<L2Object> _targets;
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object... targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		
		//@formatter:off 
		if (targets == null)
		{
			targets = new L2Object[] { cha };
		}
		//@formatter:on 
		_targets = Arrays.asList(targets);
	}
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		this(cha, skillId, skillId, cha);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.size());
		for (L2Object target : _targets)
		{
			writeD(target.getObjectId());
		}
	}
}
