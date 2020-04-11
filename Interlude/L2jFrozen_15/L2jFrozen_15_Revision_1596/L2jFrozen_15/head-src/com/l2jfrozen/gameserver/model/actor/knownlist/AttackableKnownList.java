package com.l2jfrozen.gameserver.model.actor.knownlist;

import java.util.Collection;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

public class AttackableKnownList extends NpcKnownList
{
	public AttackableKnownList(final L2Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean removeKnownObject(final L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Remove the L2Object from the aggrolist of the L2Attackable
		if (object != null && object instanceof L2Character)
		{
			getActiveChar().getAggroList().remove(object);
		}
		
		// Set the L2Attackable Intention to AI_INTENTION_IDLE
		final Collection<L2PcInstance> known = getKnownPlayers().values();
		
		// FIXME: This is a temporary solution
		L2CharacterAI ai = getActiveChar().getAI();
		
		if (ai != null && (known == null || known.isEmpty()))
		{
			ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		return true;
	}
	
	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(final L2Object object)
	{
		if (getActiveChar().getAggroListRP() != null)
		{
			if (getActiveChar().getAggroListRP().get(object) != null)
			{
				return 3000;
			}
		}
		return Math.min(2200, 2 * getDistanceToWatchObject(object));
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return 1500;
		}
		
		if (getActiveChar().getAggroRange() > getActiveChar().getFactionRange())
		{
			return getActiveChar().getAggroRange();
		}
		
		if (getActiveChar().getFactionRange() > 200)
		{
			return getActiveChar().getFactionRange();
		}
		
		return 200;
	}
}
