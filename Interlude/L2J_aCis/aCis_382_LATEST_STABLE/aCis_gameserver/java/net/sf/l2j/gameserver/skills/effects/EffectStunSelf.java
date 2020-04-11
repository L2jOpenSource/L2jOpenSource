package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STUN_SELF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().startStunning();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffector().stopStunning(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public boolean isSelfEffectType()
	{
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.STUNNED.getMask();
	}
}