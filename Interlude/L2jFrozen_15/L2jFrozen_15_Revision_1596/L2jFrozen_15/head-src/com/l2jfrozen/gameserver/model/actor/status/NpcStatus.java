package com.l2jfrozen.gameserver.model.actor.status;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;

public class NpcStatus extends CharStatus
{
	public NpcStatus(final L2NpcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(final double value, final L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public final void reduceHp(final double value, final L2Character attacker, final boolean awake)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		// Add attackers to npc's attacker list
		if (attacker != null)
		{
			getActiveChar().addAttackerToAttackByList(attacker);
		}
		
		super.reduceHp(value, attacker, awake);
	}
	
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}
}
