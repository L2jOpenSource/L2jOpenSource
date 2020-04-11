package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.Npc;

public class NpcStat extends CreatureStat
{
	public NpcStat(Npc npc)
	{
		super(npc);
	}
	
	@Override
	public Npc getActor()
	{
		return (Npc) super.getActor();
	}
	
	@Override
	public byte getLevel()
	{
		return getActor().getTemplate().getLevel();
	}
}