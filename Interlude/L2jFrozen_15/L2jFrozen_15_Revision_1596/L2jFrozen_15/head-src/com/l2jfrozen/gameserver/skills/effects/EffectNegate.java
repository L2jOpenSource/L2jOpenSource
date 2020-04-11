package com.l2jfrozen.gameserver.skills.effects;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author Gnat
 */
public class EffectNegate extends L2Effect
{
	protected static final Logger LOGGER = Logger.getLogger(EffectNegate.class);
	
	public EffectNegate(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NEGATE;
	}
	
	@Override
	public void onStart()
	{
		
		final L2Skill skill = getSkill();
		
		if (Config.DEBUG)
		{
			LOGGER.debug("effectNegate on " + getEffected().getName() + " with skill " + skill.getId());
		}
		
		if (skill.getNegateId() != 0)
		{
			getEffected().stopSkillEffects(skill.getNegateId());
		}
		
		for (final String negateSkillType : skill.getNegateSkillTypes())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("effectNegate on Type " + negateSkillType + " with power " + skill.getPower());
			}
			
			SkillType type = null;
			try
			{
				type = SkillType.valueOf(negateSkillType);
			}
			catch (final Exception e)
			{
				//
			}
			
			if (type != null)
			{
				getEffected().stopSkillEffects(type, skill.getPower());
			}
		}
		
		for (final String negateEffectType : skill.getNegateEffectTypes())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("effectNegate on Effect Type " + negateEffectType + " with power " + skill.getPower());
			}
			
			EffectType type = null;
			try
			{
				type = EffectType.valueOf(negateEffectType);
			}
			catch (final Exception e)
			{
				//
			}
			
			if (type != null)
			{
				getEffected().stopEffects(type);
			}
		}
		
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
