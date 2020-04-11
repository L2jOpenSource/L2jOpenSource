package l2f.gameserver.stats.funcs;

import java.util.HashMap;
import java.util.Map;

import l2f.gameserver.data.xml.holder.NpcStatsBalancerHolder;
import l2f.gameserver.data.xml.parser.NpcStatsBalancerParser;
import l2f.gameserver.stats.Env;
import l2f.gameserver.stats.Stats;

/**
 * @author DevGrivesky
 */
public class FuncMonsterBalancer extends Func
{
	private static final Map<Stats, FuncMonsterBalancer> _fh_instance = new HashMap<Stats, FuncMonsterBalancer>();

	public static Func getInstance(Stats st)
	{
		if (!_fh_instance.containsKey(st))
			_fh_instance.put(st, new FuncMonsterBalancer(st));

		return _fh_instance.get(st);
	}
	
	private FuncMonsterBalancer(Stats stat)
	{
		super(stat, 0x80, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final NpcStatsBalancerHolder monsterXmlbalance = NpcStatsBalancerParser.getInstance().getXmlStatsForNpc(env.character.getNpcId(), stat);

		if (monsterXmlbalance != null)
		{
			env.value += monsterXmlbalance.getFixedValue();
			env.value *= monsterXmlbalance.getPercentValue();
		}
	}
}