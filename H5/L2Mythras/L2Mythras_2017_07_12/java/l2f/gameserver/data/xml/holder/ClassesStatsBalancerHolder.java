package l2f.gameserver.data.xml.holder;

import l2f.gameserver.stats.Stats;
import l2f.gameserver.templates.StatsSet;

/**
 * @author Grivesky
 */
public class ClassesStatsBalancerHolder
{
	private final Stats _stat;
	private final int _fValue;
	private final float _pValue;
	
	public ClassesStatsBalancerHolder(StatsSet set)
	{
		_stat = Stats.valueOfXml(set.getString("name"));
		_fValue = set.getInteger("fValue", 0);
		_pValue = set.getFloat("pValue", 1.0f);
	}
	
	public Stats getStat()
	{
		return _stat;
	}
	
	public int getFixedValue()
	{
		return _fValue;
	}
	
	public float getPercentValue()
	{
		return _pValue;
	}
}