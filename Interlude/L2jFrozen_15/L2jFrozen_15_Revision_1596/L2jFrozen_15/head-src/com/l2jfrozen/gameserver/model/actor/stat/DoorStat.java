package com.l2jfrozen.gameserver.model.actor.stat;

import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStat extends CharStat
{
	public DoorStat(final L2DoorInstance activeChar)
	{
		super(activeChar);
		
		setLevel((byte) 1);
	}
	
	@Override
	public L2DoorInstance getActiveChar()
	{
		return (L2DoorInstance) super.getActiveChar();
	}
	
	@Override
	public final int getLevel()
	{
		return 1;
	}
}
