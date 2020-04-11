package l2f.gameserver.stats.funcs;

import java.util.HashMap;
import java.util.Map;

import l2f.gameserver.data.xml.holder.ClassesStatsBalancerHolder;
import l2f.gameserver.data.xml.parser.ClassesStatsBalancerParser;
import l2f.gameserver.model.Creature;
import l2f.gameserver.stats.Env;
import l2f.gameserver.stats.Stats;

/**
 * @author Grivesky
 */
public class FuncClassesBalancer extends Func
{
	private static final Map<Stats, FuncClassesBalancer> _fh_instance = new HashMap<Stats, FuncClassesBalancer>();

	public static Func getInstance(Stats st, Creature cha)
	{
		if (!_fh_instance.containsKey(st))
			_fh_instance.put(st, new FuncClassesBalancer(st, cha));

		return _fh_instance.get(st);
	}

	public FuncClassesBalancer(Stats stat, Object owner)
	{
		super(stat, 0x80, owner);
	}
	
	@Override
	public void calc(Env env)
	{
		final ClassesStatsBalancerHolder balance = ClassesStatsBalancerParser.getInstance().getBalanceForClass(env.character.getPlayer().getClassId().getId(), stat);
		if (balance != null)
		{
			// Apply the stats!
			env.value += balance.getFixedValue();
			env.value *= balance.getPercentValue();
		}
	}
}