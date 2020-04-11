package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.EffectFlag;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectStunSelf extends AbstractEffect
{
	public EffectStunSelf(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN_SELF;
	}
	
	@Override
	public boolean onStart()
	{
		// Abort attack, cast and move.
		getEffector().abortAll(false);
		
		// Trigger onAttacked event.
		getEffector().getAI().notifyEvent(AiEventType.ATTACKED, getEffector(), null);
		
		if (!(getEffector() instanceof Summon))
			getEffector().getAI().tryTo(IntentionType.IDLE, null, null);
		
		// Refresh abnormal effects.
		getEffector().updateAbnormalEffect();
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (!(getEffector() instanceof Player))
			getEffector().getAI().notifyEvent(AiEventType.THINK, null, null);
		
		// Refresh abnormal effects.
		getEffector().updateAbnormalEffect();
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
		return EffectFlag.STUNNED.getMask();
	}
}