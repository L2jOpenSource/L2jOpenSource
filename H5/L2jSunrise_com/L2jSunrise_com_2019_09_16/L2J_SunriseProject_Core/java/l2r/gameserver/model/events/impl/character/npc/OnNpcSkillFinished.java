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
package l2r.gameserver.model.events.impl.character.npc;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.IBaseEvent;
import l2r.gameserver.model.skills.L2Skill;

/**
 * @author UnAfraid
 */
public class OnNpcSkillFinished implements IBaseEvent
{
	private final L2Npc _caster;
	private final L2PcInstance _target;
	private final L2Skill _skill;
	
	public OnNpcSkillFinished(L2Npc caster, L2PcInstance target, L2Skill skill)
	{
		_caster = caster;
		_target = target;
		_skill = skill;
	}
	
	public L2PcInstance getTarget()
	{
		return _target;
	}
	
	public L2Npc getCaster()
	{
		return _caster;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_SKILL_FINISHED;
	}
}
