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
import java.util.Collection;
import java.util.List;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.util.Util;

/**
 * @author UnAfraid, reworked by vGodFather
 */
public class FrontArea implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if ((target == null) || (((target == activeChar) || target.isAlikeDead()) && (skill.getCastRange() >= 0)) || (!(target.isAttackable() || target.isPlayable())))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
		
		final L2Character origin;
		final boolean srcInArena = (activeChar.isInsideZone(ZoneIdType.PVP) && !activeChar.isInsideZone(ZoneIdType.SIEGE));
		
		if (skill.getCastRange() >= 0)
		{
			if (!L2Skill.checkForAreaOffensiveSkills(activeChar, target, skill, srcInArena))
			{
				return _emptyTargetList;
			}
			
			if (onlyFirst)
			{
				return new L2Character[]
				{
					target
				};
			}
			
			origin = target;
			targetList.add(origin); // Add target to target list
		}
		else
		{
			origin = activeChar;
		}
		
		// vGodFather Small trick just in case we miss actor face the target
		activeChar.setHeading(Util.calculateHeadingFrom(activeChar, target));
		
		int affectRange = skill.getFanRange() != null ? skill.getFanRange()[2] : skill.getAffectRange();
		int maxTargets = skill.getAffectLimit();
		final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(target, affectRange);
		for (L2Character obj : objs)
		{
			if (!(obj.isAttackable() || obj.isPlayable()))
			{
				continue;
			}
			
			if ((obj == origin) || obj.isDead())
			{
				continue;
			}
			
			if (skill.getFanRange() != null ? skill.checkFan(activeChar, origin, obj, srcInArena) : skill.checkNormal(activeChar, origin, obj, srcInArena))
			{
				if ((skill.getFanRange() == null) && !Util.isFacing(activeChar, obj, 60))
				{
					continue;
				}
				
				if ((maxTargets > 0) && (targetList.size() >= maxTargets))
				{
					break;
				}
				
				targetList.add(obj);
			}
		}
		
		if (targetList.isEmpty())
		{
			return _emptyTargetList;
		}
		
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.FRONT_AREA;
	}
}
