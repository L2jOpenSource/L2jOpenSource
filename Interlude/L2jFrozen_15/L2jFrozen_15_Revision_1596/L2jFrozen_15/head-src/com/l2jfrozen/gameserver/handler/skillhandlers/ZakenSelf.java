package com.l2jfrozen.gameserver.handler.skillhandlers;

import java.util.concurrent.ThreadLocalRandom;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;

public class ZakenSelf implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(ZakenSelf.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.ZAKENSELF
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		// Zaken must be the ONLY who use this skill
		int location = ThreadLocalRandom.current().nextInt(1, 13);
		if (location == 1)
		{
			activeChar.teleToLocation(55299, 219120, -2952);
		}
		else if (location == 2)
		{
			activeChar.teleToLocation(56363, 218043, -2952);
		}
		else if (location == 3)
		{
			activeChar.teleToLocation(54245, 220162, -2952);
		}
		else if (location == 4)
		{
			activeChar.teleToLocation(56289, 220126, -2952);
		}
		else if (location == 5)
		{
			activeChar.teleToLocation(55299, 219120, -3224);
		}
		else if (location == 6)
		{
			activeChar.teleToLocation(56363, 218043, -3224);
		}
		else if (location == 7)
		{
			activeChar.teleToLocation(54245, 220162, -3224);
		}
		else if (location == 8)
		{
			activeChar.teleToLocation(56289, 220126, -3224);
		}
		else if (location == 9)
		{
			activeChar.teleToLocation(55299, 219120, -3496);
		}
		else if (location == 10)
		{
			activeChar.teleToLocation(56363, 218043, -3496);
		}
		else if (location == 11)
		{
			activeChar.teleToLocation(54245, 220162, -3496);
		}
		else if (location == 12)
		{
			activeChar.teleToLocation(56289, 220126, -3496);
		}
		else
		{
			activeChar.teleToLocation(53930, 217760, -2944);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}