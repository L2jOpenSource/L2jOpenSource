package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMAtkSpeed extends Func
{
	static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();
	
	public static Func getInstance()
	{
		return _fas_instance;
	}
	
	private FuncMAtkSpeed()
	{
		super(Stats.MAGIC_ATTACK_SPEED, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.WIT_BONUS[env.getCharacter().getWIT()]);
	}
}