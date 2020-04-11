package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author Kerberos
 */
public final class EffectFusion extends L2Effect
{
	public int effect;
	public int maxEffect;
	
	public EffectFusion(final Env env, final EffectTemplate template)
	{
		super(env, template);
		effect = getSkill().getLevel();
		maxEffect = 10;
	}
	
	@Override
	public boolean onActionTime()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FUSION;
	}
	
	public void increaseEffect()
	{
		if (effect < maxEffect)
		{
			effect++;
			updateBuff();
		}
	}
	
	public void decreaseForce()
	{
		effect--;
		if (effect < 1)
		{
			exit(false);
		}
		else
		{
			updateBuff();
		}
	}
	
	private void updateBuff()
	{
		exit(false);
		SkillTable.getInstance().getInfo(getSkill().getId(), effect).getEffects(getEffector(), getEffected(), false, false, false);
	}
}
