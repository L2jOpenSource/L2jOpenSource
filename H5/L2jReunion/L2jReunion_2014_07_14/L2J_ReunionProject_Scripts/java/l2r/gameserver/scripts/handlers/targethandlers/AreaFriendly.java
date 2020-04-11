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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
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
		
		if (activeChar.getActingPlayer().isInOlympiadMode())
		{
			return new L2Character[]
			{
				activeChar
			};
		}
		targetList.add(target); // Add target to target list
		
		if (target != null)
		{
			int maxTargets = skill.getAffectLimit();
			final Collection<L2Character> objs = target.getKnownList().getKnownCharactersInRadius(skill.getAffectRange());
			
			// TODO: Chain Heal - The recovery amount decreases starting from the most injured person.
			Collections.sort(targetList, new CharComparator());
			
			for (L2Character obj : objs)
			{
				if (!checkTarget(activeChar, obj) || (obj == activeChar))
				{
					continue;
				}
				
				// TODO: check if this is retail like
				if ((obj instanceof L2GuardInstance) && (skill.getId() == 1553) && (obj.isInsideZone(ZoneIdType.CASTLE) || obj.isInsideZone(ZoneIdType.FORT)))
				{
					continue;
				}
				
				if (targetList.size() >= maxTargets)
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
		if ((Config.GEODATA > 0) && !GeoData.getInstance().canSeeTarget(activeChar, target))
		{
			return false;
		}
		
		if ((target == null) || (target.getActingPlayer() == null) || target.isNpc() || target.isAlikeDead() || target.isDoor() || (target instanceof L2SiegeFlagInstance) || target.isMonster())
		{
			return false;
		}
		
		if ((target.getActingPlayer() != activeChar) && (target.getActingPlayer().inObserverMode() || target.getActingPlayer().isInOlympiadMode()))
		{
			return false;
		}
		
		if (target.getActingPlayer().isCursedWeaponEquipped())
		{
			return false;
		}
		
		if (!activeChar.getActingPlayer().isInSameParty(target.getActingPlayer()) && !activeChar.getActingPlayer().isInSameChannel(target.getActingPlayer()) && !activeChar.getActingPlayer().isInSameClan(target.getActingPlayer()) && !activeChar.getActingPlayer().isInSameAlly(target.getActingPlayer()))
		{
			return false;
		}
		
		return true;
	}
	
	public class CharComparator implements Comparator<L2Character>
	{
		@Override
		public int compare(L2Character char1, L2Character char2)
		{
			return Double.compare((char1.getCurrentHp() / char1.getMaxHp()), (char2.getCurrentHp() / char2.getMaxHp()));
		}
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.AREA_FRIENDLY;
	}
}