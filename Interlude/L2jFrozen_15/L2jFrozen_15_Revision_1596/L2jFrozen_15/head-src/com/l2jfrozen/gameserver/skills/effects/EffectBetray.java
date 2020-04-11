package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author decad
 */
final class EffectBetray extends L2Effect
{
	public EffectBetray(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BETRAY;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected() != null && getEffector() instanceof L2PcInstance && getEffected() instanceof L2Summon)
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
			targetOwner.setIsBetrayed(true);
			onActionTime();
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if (getEffected() != null && getEffector() instanceof L2PcInstance && getEffected() instanceof L2Summon)
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			targetOwner.setIsBetrayed(false);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		L2PcInstance targetOwner = null;
		targetOwner = ((L2Summon) getEffected()).getOwner();
		targetOwner.setIsBetrayed(true);
		return false;
	}
}
