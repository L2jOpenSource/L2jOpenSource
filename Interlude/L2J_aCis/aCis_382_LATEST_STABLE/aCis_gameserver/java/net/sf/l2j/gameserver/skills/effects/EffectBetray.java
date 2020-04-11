package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author decad
 */
final class EffectBetray extends L2Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BETRAY;
	}
	
	/** Notify started */
	@Override
	public boolean onStart()
	{
		if (getEffector() instanceof Player && getEffected() instanceof Summon)
		{
			Player targetOwner = getEffected().getActingPlayer();
			getEffected().getAI().setIntention(IntentionType.ATTACK, targetOwner);
			return true;
		}
		return false;
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().getAI().setIntention(IntentionType.IDLE);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.BETRAYED.getMask();
	}
}