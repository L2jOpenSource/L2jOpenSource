package com.l2jfrozen.gameserver.handler.skillhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/04 19:08:01 $
 */

public class Charge implements ISkillHandler
{
	static Logger LOGGER = Logger.getLogger(Charge.class);
	
	private static final SkillType[] SKILL_IDS =
	{
		/* SkillType.CHARGE */
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		
		for (final L2Object target1 : targets)
		{
			if (!(target1 instanceof L2PcInstance))
			{
				continue;
			}
			L2PcInstance target = (L2PcInstance) target1;
			skill.getEffects(activeChar, target, false, false, false);
			target = null;
		}
		// self Effect :]
		
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit(false);
		}
		skill.getEffectsSelf(activeChar);
		
		effect = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
