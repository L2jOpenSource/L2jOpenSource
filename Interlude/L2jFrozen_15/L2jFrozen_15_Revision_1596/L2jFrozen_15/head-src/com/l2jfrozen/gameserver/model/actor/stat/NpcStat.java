package com.l2jfrozen.gameserver.model.actor.stat;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * @author programmos
 */

public class NpcStat extends CharStat
{
	public NpcStat(final L2NpcInstance activeChar)
	{
		super(activeChar);
		
		setLevel(getActiveChar().getTemplate().level);
	}
	
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}
	
	@Override
	public final int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax, null, null);
	}
}
