package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2CabaleBufferInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FestivalGuideInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

public class NpcKnownList extends CharKnownList
{
	public NpcKnownList(final L2NpcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(final L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
		{
			return 10000;
		}
		
		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2CabaleBufferInstance)
		{
			return 900;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return 1500;
		}
		
		return 500;
	}
}
