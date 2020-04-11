package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaINT extends Func
{
	private static final HennaType STAT = HennaType.INT;
	private static final FuncHennaINT INSTANCE = new FuncHennaINT();
	
	public static Func getInstance()
	{
		return INSTANCE;
	}
	
	private FuncHennaINT()
	{
		super(STAT.getStats(), 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final Player player = env.getPlayer();
		if (player != null)
			env.addValue(player.getHennaList().getStat(STAT));
	}
}