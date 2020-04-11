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
import java.util.Collections;
import java.util.List;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.interfaces.IPositionable;

/**
 * MagicSkillUse server packet implementation.
 * @author UnAfraid, Nos
 */
public final class MagicSkillUse extends L2GameServerPacket
{
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final L2Character _activeChar;
	private final L2Character _target;
	private final List<Integer> _unknown = Collections.emptyList();
	private final List<Location> _groundLocations;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_activeChar = cha;
		_target = target;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		Location skillWorldPos = null;
		if (cha.isPlayer())
		{
			final L2PcInstance player = cha.getActingPlayer();
			if (player.getCurrentSkillWorldPosition() != null)
			{
				skillWorldPos = player.getCurrentSkillWorldPosition();
			}
		}
		_groundLocations = skillWorldPos != null ? Arrays.asList(skillWorldPos) : Collections.<Location> emptyList();
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, hitTime, reuseDelay);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_activeChar.getObjectId());
		writeD(_target.getObjectId());
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeLoc(_activeChar);
		writeH(_unknown.size()); // TODO: Implement me!
		for (int unknown : _unknown)
		{
			writeH(unknown);
		}
		writeH(_groundLocations.size());
		for (IPositionable target : _groundLocations)
		{
			writeLoc(target);
		}
		writeLoc(_target);
	}
}
