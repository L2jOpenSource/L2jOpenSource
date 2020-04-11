package com.l2jfrozen.gameserver.skills.effects;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @author kombat
 */
public class EffectForce extends L2Effect
{
	protected static final Logger LOGGER = Logger.getLogger(EffectForce.class);
	
	public int forces = 0;
	private int range = -1;
	
	public EffectForce(final Env env, final EffectTemplate template)
	{
		super(env, template);
		forces = getSkill().getLevel();
		range = getSkill().getCastRange();
	}
	
	@Override
	public boolean onActionTime()
	{
		return Util.checkIfInRange(range, getEffector(), getEffected(), true);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	public void increaseForce()
	{
		forces++;
		updateBuff();
	}
	
	public void decreaseForce()
	{
		forces--;
		if (forces < 1)
		{
			exit(false);
		}
		else
		{
			updateBuff();
		}
	}
	
	public void updateBuff()
	{
		exit(false);
		final L2Skill newSkill = SkillTable.getInstance().getInfo(getSkill().getId(), forces);
		if (newSkill != null)
		{
			newSkill.getEffects(getEffector(), getEffected(), false, false, false);
		}
	}
	
	@Override
	public void onExit()
	{
		// try
		// {
		// getEffector().abortCast();
		// if(getEffector().getForceBuff() != null)
		// getEffector().getForceBuff().delete();
		// }
		// catch(Exception e)
		// {
		// null
		// }
	}
}
