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
package l2r.gameserver.model.actor.tasks.character;

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;

/**
 * Task dedicated to magic use of character
 * @author xban1x
 */
public final class MagicUseTask implements Runnable
{
	private final L2Character _character;
	private L2Object[] _targets;
	private final L2Skill _skill;
	private int _count;
	private int _skillTime;
	private int _phase;
	private final boolean _simultaneously;
	
	public MagicUseTask(L2Character character, L2Object[] tgts, L2Skill s, int hit, boolean simultaneous)
	{
		_character = character;
		_targets = tgts;
		_skill = s;
		_count = 0;
		_phase = 1;
		_skillTime = hit;
		_simultaneously = simultaneous;
	}
	
	@Override
	public void run()
	{
		if (_character == null)
		{
			return;
		}
		switch (_phase)
		{
			case 1:
			{
				_character.onMagicLaunchedTimer(this);
				break;
			}
			case 2:
			{
				_character.onMagicHitTimer(this);
				break;
			}
			case 3:
			{
				_character.onMagicFinalizer(this);
				break;
			}
		}
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getPhase()
	{
		return _phase;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	public int getSkillTime()
	{
		return _skillTime;
	}
	
	public L2Object[] getTargets()
	{
		return _targets;
	}
	
	public boolean isSimultaneous()
	{
		return _simultaneously;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public void setPhase(int phase)
	{
		_phase = phase;
	}
	
	public void setSkillTime(int skillTime)
	{
		_skillTime = skillTime;
	}
	
	public void setTargets(L2Object[] targets)
	{
		_targets = targets;
	}
}
