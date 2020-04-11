package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author LBaldi
 */
public class EffectBigHead extends L2Effect
{
	public EffectBigHead(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.BIG_HEAD);
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}