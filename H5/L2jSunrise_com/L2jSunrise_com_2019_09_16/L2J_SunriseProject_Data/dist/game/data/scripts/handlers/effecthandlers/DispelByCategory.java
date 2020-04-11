package handlers.effecthandlers;

import java.util.List;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;

/**
 * Dispel By Category effect implementation.
 * @author vGodFather
 */
public final class DispelByCategory extends L2Effect
{
	private final String _slot;
	private final int _rate;
	private final int _max;
	
	public DispelByCategory(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_slot = template.getParameters().getString("slot", null);
		_rate = template.getParameters().getInt("rate", 0);
		_max = template.getParameters().getInt("max", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		final List<L2Effect> canceled = Formulas.calcCancelStealEffects(getEffector(), getEffected(), getSkill(), _slot, _rate, _max);
		for (L2Effect can : canceled)
		{
			can.exit();
		}
		return true;
	}
}