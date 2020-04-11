package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.skills.Env;

class EffectHealOverTime extends L2Effect
{
	public EffectHealOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		if (getEffected() instanceof L2DoorInstance)
		{
			return false;
		}
		
		double hp = getEffected().getCurrentHp();
		final double maxhp = getEffected().getMaxHp();
		hp += calc();
		if (hp > maxhp)
		{
			hp = maxhp;
		}
		getEffected().setCurrentHp(hp);
		final StatusUpdate suhp = new StatusUpdate(getEffected().getObjectId());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
		getEffected().sendPacket(suhp);
		return true;
	}
}