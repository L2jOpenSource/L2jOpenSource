package handlers.effecthandlers;

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * ImmobileUntilAttacked effect implementation.
 * @author vGodFather
 */
public class ImmobileUntilAttacked extends L2Effect
{
	public ImmobileUntilAttacked(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isPlayer())
		{
			getEffected().setIsImmobilized(false);
			getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().abortAttack();
		getEffected().abortCast();
		getEffected().stopMove(null);
		getEffected().setIsImmobilized(true);
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		return super.onStart();
	}
}
