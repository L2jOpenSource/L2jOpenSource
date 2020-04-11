package net.sf.l2j.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.EffectFlag;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Chest;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectConfuseMob extends AbstractEffect
{
	public EffectConfuseMob(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSE_MOB_ONLY;
	}
	
	@Override
	public boolean onStart()
	{
		// Abort move.
		getEffected().getMove().stop();
		
		// Refresh abnormal effects.
		getEffected().updateAbnormalEffect();
		
		onActionTime();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().removeEffect(this);
		
		if (!(getEffected() instanceof Player))
			getEffected().getAI().notifyEvent(AiEventType.THINK, null, null);
		
		// Refresh abnormal effects.
		getEffected().updateAbnormalEffect();
	}
	
	@Override
	public boolean onActionTime()
	{
		final List<Creature> targetList = new ArrayList<>();
		
		// Getting the possible targets
		for (final Attackable obj : getEffected().getKnownType(Attackable.class))
		{
			// Only attackable NPCs are put in the list.
			if (!(obj instanceof Chest))
				targetList.add(obj);
		}
		
		// if there is no target, exit function
		if (targetList.isEmpty())
			return true;
		
		// Choosing randomly a new target
		final WorldObject target = Rnd.get(targetList);
		
		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().tryTo(IntentionType.ATTACK, target, null);
		
		// Add aggro to that target aswell. The aggro power is random.
		final int aggro = (5 + Rnd.get(5)) * getEffector().getLevel();
		((Attackable) getEffected()).addDamageHate((Creature) target, 0, aggro);
		
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}
}