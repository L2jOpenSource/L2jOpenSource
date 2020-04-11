package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;

/**
 * @author Forsaiken
 */

public class GiveSp implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.GIVE_SP
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		for (final L2Object obj : targets)
		{
			L2Character target = (L2Character) obj;
			if (target != null)
			{
				final int spToAdd = (int) skill.getPower();
				target.addExpAndSp(0, spToAdd);
			}
			target = null;
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
