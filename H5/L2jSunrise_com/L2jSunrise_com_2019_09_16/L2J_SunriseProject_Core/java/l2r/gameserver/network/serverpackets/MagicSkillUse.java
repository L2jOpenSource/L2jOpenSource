/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;

/**
 * MagicSkillUse server packet implementation.
 * @author UnAfraid, NosBit, vGodFather
 */
public class MagicSkillUse extends L2GameServerPacket
{
	private final L2Character _activeChar;
	private final L2Character _target;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int skillTime, int reuseDelay)
	{
		_activeChar = cha;
		_target = target;
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = skillTime;
		_reuseDelay = reuseDelay;
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int skillTime, int reuseDelay)
	{
		this(cha, cha, skillId, skillLevel, skillTime, reuseDelay);
	}
	
	public MagicSkillUse(L2Character cha, L2Character target, L2Skill skill, int skillTime, int reuseDelay)
	{
		this(cha, target, skill.getDisplayId(), skill.getLevel(), skillTime, reuseDelay);
	}
	
	public MagicSkillUse(L2Character cha, L2Skill skill, int skillTime, int reuseDelay)
	{
		this(cha, cha, skill.getDisplayId(), skill.getLevel(), skillTime, reuseDelay);
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
		
		// vGodFather FIXME missing info?
		writeH(0x00);
		writeH(0x00);
		
		writeLoc(_target);
	}
}