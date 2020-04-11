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
package l2r.gameserver.scripts.handlers.skillhandlers;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.network.SystemMessageId;

/**
 * @author Ahmed
 */
public class TransformDispel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TRANSFORMDISPEL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		L2PcInstance pc = activeChar.getActingPlayer();
		
		if (pc.isAlikeDead() || pc.isCursedWeaponEquipped())
		{
			return;
		}
		
		if (pc.isTransformed() || pc.isInStance())
		{
			if (pc.isFlyingMounted() && !pc.isInsideZone(ZoneIdType.LANDING))
			{
				pc.sendPacket(SystemMessageId.TOO_HIGH_TO_PERFORM_THIS_ACTION);
			}
			else
			{
				pc.stopTransformation(true);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}