package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Env;

public final class EffectChameleonRest extends L2Effect
{
	public EffectChameleonRest(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			setChameleon(true);
			((L2PcInstance) effected).setSilentMoving(true);
			((L2PcInstance) effected).sitDown();
		}
		else
		{
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	@Override
	public void onExit()
	{
		setChameleon(false);
		
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		final L2Character effected = getEffected();
		boolean retval = true;
		
		if (effected.isDead())
		{
			retval = false;
		}
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != SkillType.CONT)
		{
			return false;
		}
		
		if (effected instanceof L2PcInstance)
		{
			if (!((L2PcInstance) effected).isSitting())
			{
				retval = false;
			}
		}
		
		final double manaDam = calc();
		
		if (manaDam > effected.getStatus().getCurrentMp())
		{
			effected.sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		
		if (!retval)
		{
			setChameleon(retval);
		}
		else
		{
			effected.reduceCurrentMp(manaDam);
		}
		
		return retval;
	}
	
	private void setChameleon(final boolean val)
	{
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setRelax(val);
		}
	}
}
