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
package l2r.gameserver.scripting.scriptengine.events;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripting.scriptengine.events.impl.L2Event;

/**
 * @author TheOne
 */
public class SkillUseEvent implements L2Event
{
	private L2Character _caster;
	private L2Skill _skill;
	private L2Character _target;
	private L2Object[] _targets;
	
	/**
	 * @return the caster
	 */
	public L2Character getCaster()
	{
		return _caster;
	}
	
	/**
	 * @param caster the caster to set
	 */
	public void setCaster(L2Character caster)
	{
		_caster = caster;
	}
	
	/**
	 * @return the targets
	 */
	public L2Object[] getTargets()
	{
		return _targets;
	}
	
	/**
	 * @param targets the targets to set
	 */
	public void setTargets(L2Object[] targets)
	{
		_targets = targets;
	}
	
	/**
	 * @return the skill
	 */
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * @param skill the skill to set
	 */
	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}
	
	/**
	 * @return Caster's selected target.
	 */
	public L2Character getTarget()
	{
		return _target;
	}
	
	/**
	 * @param target
	 */
	public void setTarget(L2Character target)
	{
		_target = target;
	}
}
