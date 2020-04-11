package handlers.effecthandlers;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;

/**
 * @author vGodFather
 */
public class FocusMaxEnergy extends L2Effect
{
	public FocusMaxEnergy(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			final L2Skill sonicMastery = getEffected().getSkills().get(992);
			final L2Skill focusMastery = getEffected().getSkills().get(993);
			int maxCharge = (sonicMastery != null) ? sonicMastery.getLevel() : (focusMastery != null) ? focusMastery.getLevel() : 0;
			if (maxCharge != 0)
			{
				int count = maxCharge - getEffected().getActingPlayer().getCharges();
				getEffected().getActingPlayer().increaseCharges(count, maxCharge);
			}
		}
		return true;
	}
}