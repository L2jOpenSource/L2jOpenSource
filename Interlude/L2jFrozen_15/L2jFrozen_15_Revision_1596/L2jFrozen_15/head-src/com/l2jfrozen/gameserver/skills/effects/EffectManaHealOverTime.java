package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.skills.Env;

class EffectManaHealOverTime extends L2Effect
{
	public EffectManaHealOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MANA_HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		double mp = getEffected().getCurrentMp();
		final double maxmp = getEffected().getMaxMp();
		mp += calc();
		if (mp > maxmp)
		{
			mp = maxmp;
		}
		getEffected().setCurrentMp(mp);
		final StatusUpdate sump = new StatusUpdate(getEffected().getObjectId());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		return true;
	}
}
