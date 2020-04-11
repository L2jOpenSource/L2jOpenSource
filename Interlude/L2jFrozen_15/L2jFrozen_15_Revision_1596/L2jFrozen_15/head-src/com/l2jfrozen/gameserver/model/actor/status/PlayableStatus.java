package com.l2jfrozen.gameserver.model.actor.status;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

public class PlayableStatus extends CharStatus
{
	public PlayableStatus(final L2PlayableInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(final double value, final L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public void reduceHp(final double value, final L2Character attacker, final boolean awake)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		super.reduceHp(value, attacker, awake);
		/*
		 * if (attacker != null && attacker != getActiveChar()) { // Flag the attacker if it's a L2PcInstance outside a PvP area L2PcInstance player = null; if (attacker instanceof L2PcInstance) player = (L2PcInstance)attacker; else if (attacker instanceof L2Summon) player = ((L2Summon)attacker).getOwner();
		 * if (player != null) player.updatePvPStatus(getActiveChar()); }
		 */
	}
	
	@Override
	public L2PlayableInstance getActiveChar()
	{
		return (L2PlayableInstance) super.getActiveChar();
	}
}
