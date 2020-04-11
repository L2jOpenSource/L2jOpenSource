/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.handlers.targethandlers;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class Aura implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final List<L2Character> targetList = new ArrayList<>();
		final boolean srcInArena = (activeChar.isInsideZone(ZoneIdType.PVP) && !activeChar.isInsideZone(ZoneIdType.SIEGE));
		for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getAffectRange()))
		{
			if (obj.isAttackable() || obj.isPlayable())
			{
				if (!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
				{
					continue;
				}
				
				if (activeChar.isPlayable() && obj.isAttackable() && !skill.isOffensive())
				{
					continue;
				}
				
				if (onlyFirst)
				{
					return new L2Character[]
					{
						obj
					};
				}
				
				targetList.add(obj);
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.AURA;
	}
}
