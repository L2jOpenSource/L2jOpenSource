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

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.ITargetTypeHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.skills.targets.L2TargetType;
import com.l2jserver.gameserver.network.SystemMessageId;

/**
 * Npc Body target handler. This handler should work on summons too.
 * @author MaGa
 */
public class NpcBody implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		switch (skill.getAffectScope())
		{
			case SINGLE:
			{
				if ((target == null))
				{
					return EMPTY_TARGET_LIST;
				}
				
				if (!target.isAttackable() || !target.isDead())
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return EMPTY_TARGET_LIST;
				}
				
				if (skill.hasEffectType(L2EffectType.SUMMON) && target.isServitor() && (target.getActingPlayer() != null) && (target.getActingPlayer().getObjectId() == activeChar.getObjectId()))
				{
					return EMPTY_TARGET_LIST;
				}
				
				if (skill.hasEffectType(L2EffectType.HP_DRAIN) && ((L2Attackable) target).isOldCorpse(activeChar.getActingPlayer(), Config.CORPSE_CONSUME_SKILL_ALLOWED_TIME_BEFORE_DECAY, true))
				{
					return EMPTY_TARGET_LIST;
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
		return L2TargetType.NPC_BODY;
	}
}