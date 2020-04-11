package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class FocusEnergy extends L2Effect
{
	private final int _charge;
	
	public FocusEnergy(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_charge = template.getParameters().getInt("charge", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		
		getEffected().getActingPlayer().increaseCharges(1, _charge);
		return true;
	}
}