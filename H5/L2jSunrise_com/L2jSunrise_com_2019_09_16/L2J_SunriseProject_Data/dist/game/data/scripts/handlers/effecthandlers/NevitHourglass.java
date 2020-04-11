package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

public class NevitHourglass extends L2Effect
{
	public NevitHourglass(Env env, EffectTemplate template)
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
		if (!getEffected().isPlayer())
		{
			return false;
		}
		
		getEffected().getActingPlayer().startHourglassEffect();
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (!getEffected().isPlayer())
		{
			return;
		}
		
		getEffected().getActingPlayer().stopHourglassEffect();
	}
}