
package com.l2jfrozen.gameserver.model;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.skills.effects.EffectForce;

/**
 * @author ProGramMoS, l2jfrozen
 */
public final class ForceBuff
{
	protected int forceId;
	protected int forceLevel;
	protected L2Character caster;
	protected L2Character targetCharacter;
	
	static final Logger LOGGER = Logger.getLogger(ForceBuff.class);
	
	public L2Character getCaster()
	{
		return caster;
	}
	
	public L2Character getTarget()
	{
		return targetCharacter;
	}
	
	public ForceBuff(final L2Character caster, final L2Character target, final L2Skill skill)
	{
		this.caster = caster;
		targetCharacter = target;
		forceId = skill.getTriggeredId();
		forceLevel = skill.getTriggeredLevel();
		
		L2Effect effect = targetCharacter.getFirstEffect(forceId);
		if (effect != null)
		{
			((EffectForce) effect).increaseForce();
		}
		else
		{
			final L2Skill force = SkillTable.getInstance().getInfo(forceId, forceLevel);
			if (force != null)
			{
				force.getEffects(this.caster, targetCharacter, false, false, false);
			}
			else
			{
				LOGGER.warn("Triggered skill [" + forceId + ";" + forceLevel + "] not found!");
			}
		}
		effect = null;
	}
	
	public void onCastAbort()
	{
		caster.setForceBuff(null);
		L2Effect effect = targetCharacter.getFirstEffect(forceId);
		if (effect != null)
		{
			if (Config.DEVELOPER)
			{
				LOGGER.info(" -- Removing ForceBuff " + effect.getSkill().getId());
			}
			
			if (effect instanceof EffectForce)
			{
				((EffectForce) effect).decreaseForce();
			}
			else
			{
				effect.exit(false);
			}
		}
		effect = null;
	}
}
