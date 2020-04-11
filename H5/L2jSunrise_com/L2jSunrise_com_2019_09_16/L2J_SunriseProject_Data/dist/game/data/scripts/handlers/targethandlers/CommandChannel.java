/*
 * Copyright (C) 2004-2015 L2J DataPack
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

import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class CommandChannel implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final List<L2Character> targetList = new ArrayList<>();
		final L2PcInstance player = activeChar.getActingPlayer();
		if (player == null)
		{
			return _emptyTargetList;
		}
		
		targetList.add(player);
		
		final int radius = skill.getAffectRange();
		final L2Party party = player.getParty();
		final boolean hasChannel = (party != null) && party.isInCommandChannel();
		
		if (L2Skill.addSummon(activeChar, player, radius, false))
		{
			targetList.add(player.getSummon());
		}
		
		// if player in not in party
		if (party == null)
		{
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		
		// Get all visible objects in a spherical area near the L2Character
		int maxTargets = skill.getAffectLimit();
		final List<L2PcInstance> members = hasChannel ? party.getCommandChannel().getMembers() : party.getMembers();
		
		for (L2PcInstance member : members)
		{
			if (activeChar == member)
			{
				continue;
			}
			
			if (L2Skill.addCharacter(activeChar, member, radius, false))
			{
				targetList.add(member);
				if ((maxTargets > 0) && (targetList.size() >= maxTargets))
				{
					break;
				}
			}
		}
		
		return targetList.toArray(new L2Character[targetList.size()]);
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.COMMAND_CHANNEL;
	}
}
