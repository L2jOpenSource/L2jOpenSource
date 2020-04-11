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

import l2r.gameserver.GeoData;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.SystemMessageId;

/**
 * @author Adry_85
 */
public class AreaFriendly implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if (!checkTarget(activeChar, target) && (skill.getCastRange() >= 0))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
		
		if (onlyFirst)
		{
			return new L2Character[]
			{
				target
			};
		}
		
		targetList.add(target); // Add target to target list
		
		if (target != null)
		{
			int maxTargets = skill.getAffectLimit();
			final Collection<L2Character> objs = target.getKnownList().getKnownCharactersInRadius(skill.getAffectRange());
			for (L2Character obj : objs)
			{
				if (!checkTarget(activeChar, obj) || (obj == activeChar))
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
	
	private boolean checkTarget(L2Character activeChar, L2Character target)
	{
		if ((target == null) || !GeoData.getInstance().canSeeTarget(activeChar, target))
		{
			return false;
		}
		
		if (target.isDead() || (!target.isPlayer() && !target.isSummon()))
		{
			return false;
		}
		
		L2PcInstance actingPlayer = activeChar.getActingPlayer();
		L2PcInstance targetPlayer = target.getActingPlayer();
		if ((actingPlayer == null) || (targetPlayer == null))
		{
			return false;
		}
		
		if (!actingPlayer.isFriend(targetPlayer))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.AREA_FRIENDLY;
	}
}