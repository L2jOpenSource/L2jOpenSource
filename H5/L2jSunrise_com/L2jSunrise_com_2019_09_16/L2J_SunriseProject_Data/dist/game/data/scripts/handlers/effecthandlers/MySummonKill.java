package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

public class MySummonKill extends L2Effect
{
	public MySummonKill(Env env, EffectTemplate template)
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
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}
		
		if (getEffected().getActingPlayer().hasSummon())
		{
			getEffected().getActingPlayer().getSummon().unSummon(getEffected().getActingPlayer());
		}
		return true;
	}
}
