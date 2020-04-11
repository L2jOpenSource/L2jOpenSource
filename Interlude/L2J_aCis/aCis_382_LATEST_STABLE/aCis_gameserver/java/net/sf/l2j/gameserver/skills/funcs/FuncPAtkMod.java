package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncPAtkMod extends Func
{
	static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncPAtkMod()
	{
		super(Stats.POWER_ATTACK, 0x30, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.STR_BONUS[env.getCharacter().getSTR()] * env.getCharacter().getLevelMod());
	}
}