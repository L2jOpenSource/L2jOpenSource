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
package handlers.targethandlers;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
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
		int maxTargets = skill.getAffectLimit();
		final List<L2Character> targetList = new ArrayList<>();
		final boolean srcInArena = (activeChar.isInsideZone(ZoneIdType.PVP) && !activeChar.isInsideZone(ZoneIdType.SIEGE));
		for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getAffectRange()))
		{
			if (obj.isDoor() || obj.isAttackable() || obj.isPlayable() || obj.isTrap())
			{
				// Stealth door targeting.
				if (obj.isDoor())
				{
					final L2DoorInstance door = (L2DoorInstance) obj;
					if (!door.getTemplate().isStealth())
					{
						continue;
					}
				}
				
				if ((obj instanceof L2GuardInstance) && !obj.isAutoAttackable(activeChar))
				{
					continue;
				}
				
				if (!L2Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena))
				{
					continue;
				}
				
				if (activeChar.isPlayable() && obj.isAttackable() && !skill.isOffensive())
				{
					continue;
				}
				
				if (obj.isPlayer() && activeChar.isPlayer() && skill.isOffensive() && activeChar.getActingPlayer().isFriend(obj.getActingPlayer()))
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
				
				if ((maxTargets > 0) && (targetList.size() >= maxTargets))
				{
					break;
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
