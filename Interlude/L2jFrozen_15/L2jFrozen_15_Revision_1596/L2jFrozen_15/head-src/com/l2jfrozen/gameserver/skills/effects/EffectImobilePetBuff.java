package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author demonia
 */
final class EffectImobilePetBuff extends L2Effect
{
	private L2Summon pet;
	
	public EffectImobilePetBuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		pet = null;
		
		if (getEffected() instanceof L2Summon && getEffector() instanceof L2PcInstance && ((L2Summon) getEffected()).getOwner() == getEffector())
		{
			pet = (L2Summon) getEffected();
			pet.setIsImobilised(true);
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if (pet != null)
		{
			pet.setIsImobilised(false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
