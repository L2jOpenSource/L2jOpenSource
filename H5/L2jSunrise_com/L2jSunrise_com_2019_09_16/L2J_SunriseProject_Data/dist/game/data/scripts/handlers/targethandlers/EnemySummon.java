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

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;

/**
 * Enemy Summon target handler implementation.
 * @author UnAfraid
 */
public class EnemySummon implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if ((target != null) && target.isSummon())
		{
			final L2Summon targetSummon = (L2Summon) target;
			if ((activeChar.isPlayer() && (activeChar.getSummon() != targetSummon) && //
			!targetSummon.isDead() && ((targetSummon.getOwner().getPvpFlag() != 0) || (targetSummon.getOwner().getKarma() > 0))) || //
			(targetSummon.getOwner().isInsideZone(ZoneIdType.PVP) && activeChar.getActingPlayer().isInsideZone(ZoneIdType.PVP)) || //
			(targetSummon.getOwner().isInDuel() && activeChar.getActingPlayer().isInDuel() && (targetSummon.getOwner().getDuelId() == activeChar.getActingPlayer().getDuelId())))
			{
				return new L2Character[]
				{
					targetSummon
				};
			}
		}
		return _emptyTargetList;
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.ENEMY_SUMMON;
	}
}
