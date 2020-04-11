package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class FriendlyMobKnownList extends AttackableKnownList
{
	public FriendlyMobKnownList(final L2FriendlyMobInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object, final L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(final L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		if (!(object instanceof L2Character))
		{
			return true;
		}
		
		if (getActiveChar().hasAI())
		{
			L2Character temp = (L2Character) object;
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
			
			if (getActiveChar().getTarget() == temp)
			{
				getActiveChar().setTarget(null);
			}
			
			temp = null;
		}
		
		if (getActiveChar().isVisible() && getKnownPlayers().isEmpty())
		{
			getActiveChar().clearAggroList();
			// removeAllKnownObjects();
			if (getActiveChar().hasAI())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}
		
		return true;
	}
	
	@Override
	public final L2FriendlyMobInstance getActiveChar()
	{
		return (L2FriendlyMobInstance) super.getActiveChar();
	}
}
