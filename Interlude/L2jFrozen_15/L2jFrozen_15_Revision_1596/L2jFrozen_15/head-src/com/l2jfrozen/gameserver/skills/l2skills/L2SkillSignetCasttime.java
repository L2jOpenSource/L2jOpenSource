package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.templates.StatsSet;

public final class L2SkillSignetCasttime extends L2Skill
{
	public int effectNpcId;
	public int effectId;
	
	public L2SkillSignetCasttime(final StatsSet set)
	{
		super(set);
		effectNpcId = set.getInteger("effectNpcId", -1);
		effectId = set.getInteger("effectId", -1);
	}
	
	@Override
	public void useSkill(final L2Character caster, final L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		getEffectsSelf(caster);
	}
}
