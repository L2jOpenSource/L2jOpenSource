package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMAtkCritical extends Func
{
	static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncMAtkCritical()
	{
		super(Stats.MCRITICAL_RATE, 0x09, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Creature player = env.getCharacter();
		if (player instanceof Player)
		{
			if (player.getActiveWeaponInstance() != null)
				env.mulValue(Formulas.WIT_BONUS[player.getWIT()]);
		}
		else
			env.mulValue(Formulas.WIT_BONUS[player.getWIT()]);
		
		env.setBaseValue(env.getValue());
	}
}