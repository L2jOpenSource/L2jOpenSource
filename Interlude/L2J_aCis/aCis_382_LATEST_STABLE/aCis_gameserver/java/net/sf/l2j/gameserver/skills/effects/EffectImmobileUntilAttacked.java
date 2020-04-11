package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author Ahmed
 */
public class EffectImmobileUntilAttacked extends L2Effect
{
	public EffectImmobileUntilAttacked(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.IMMOBILEUNTILATTACKED;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startImmobileUntilAttacked();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopImmobileUntilAttacked(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopImmobileUntilAttacked(this);
		// just stop this effect
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.MEDITATING.getMask();
	}
}