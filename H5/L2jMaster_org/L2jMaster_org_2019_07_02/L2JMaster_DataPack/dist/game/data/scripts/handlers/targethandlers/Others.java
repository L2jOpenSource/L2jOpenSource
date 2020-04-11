/*
 * Copyright (C) 2004-2019 L2J DataPack
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

import com.l2jserver.gameserver.handler.ITargetTypeHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.skills.targets.L2TargetType;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author MaGa
 */
public class Others implements ITargetTypeHandler
{
	// Skills
	private static final int SUMMON_FRIEND = 1403;
	private static final int WORD_OF_INVITATION = 1404;
	private static final int SUMMON_FRIEND_2 = 8329;
	private static final int SUMMON_FRIEND_3 = 8510;
	private static final int SUMMON_FRIEND_4 = 21088;
	private static final int SUMMON_FRIEND_5 = 21097;
	private static final int SUMMON_FRIEND_6 = 21264;
	private static final int SUMMON_FRIEND_7 = 21265;
	
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		switch (skill.getAffectScope())
		{
			case SINGLE:
			{
				if (target == null)
				{
					return EMPTY_TARGET_LIST;
				}
				
				final L2PcInstance player = activeChar.getActingPlayer();
				SystemMessage sm;
				
				if ((target.isDead() && (skill.getId() == SUMMON_FRIEND)) || (skill.getId() == WORD_OF_INVITATION) || (skill.getId() == SUMMON_FRIEND_2) || (skill.getId() == SUMMON_FRIEND_3) || (skill.getId() == SUMMON_FRIEND_4) || (skill.getId() == SUMMON_FRIEND_5)
					|| (skill.getId() == SUMMON_FRIEND_6) || (skill.getId() == SUMMON_FRIEND_7))
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
					sm.addCharName(target);
					player.sendPacket(sm);
					return EMPTY_TARGET_LIST;
				}
				
				if ((target.isInCombat() && (skill.getId() == SUMMON_FRIEND)) || (skill.getId() == WORD_OF_INVITATION) || (skill.getId() == SUMMON_FRIEND_2) || (skill.getId() == SUMMON_FRIEND_3) || (skill.getId() == SUMMON_FRIEND_4) || (skill.getId() == SUMMON_FRIEND_5)
					|| (skill.getId() == SUMMON_FRIEND_6) || (skill.getId() == SUMMON_FRIEND_7))
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
					sm.addCharName(target);
					player.sendPacket(sm);
					return EMPTY_TARGET_LIST;
				}
				
				if (activeChar.isSummon())
				{
					target = ((L2Summon) activeChar).getOwner();
					if ((target != null) && !target.isDead())
					{
						return new L2Character[]
						{
							target
						};
					}
				}
				
				return new L2Character[]
				{
					target
				};
			}
		}
		return EMPTY_TARGET_LIST;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.OTHERS;
	}
}