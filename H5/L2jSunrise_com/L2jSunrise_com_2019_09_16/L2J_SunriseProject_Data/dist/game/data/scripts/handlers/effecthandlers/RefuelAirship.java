package handlers.effecthandlers;

import l2r.gameserver.model.actor.instance.L2AirShipInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

public class RefuelAirship extends L2Effect
{
	public RefuelAirship(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.REFUEL_AIRSHIP;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		final L2AirShipInstance ship = getEffector().getActingPlayer().getAirShip();
		ship.setFuel(ship.getFuel() + (int) getValue());
		ship.updateAbnormalEffect();
		return true;
	}
}