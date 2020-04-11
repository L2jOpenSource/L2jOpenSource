package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.Summon;

public class SummonStat extends PlayableStat
{
	public SummonStat(Summon summon)
	{
		super(summon);
	}
	
	@Override
	public Summon getActor()
	{
		return (Summon) super.getActor();
	}
}