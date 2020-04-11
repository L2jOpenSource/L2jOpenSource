package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CommanderInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author programmos
 */

public class CommanderKnownList extends AttackableKnownList
{
	public CommanderKnownList(final L2CommanderInstance activeChar)
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
		
		if (getActiveChar().getHomeX() == 0)
		{
			getActiveChar().getHomeLocation();
		}
		
		// Check if siege is in progress
		if (getActiveChar().getFort() != null && getActiveChar().getFort().getSiege().getIsInProgress())
		{
			L2PcInstance player = null;
			
			if (object instanceof L2PcInstance)
			{
				player = (L2PcInstance) object;
			}
			else if (object instanceof L2Summon)
			{
				player = ((L2Summon) object).getOwner();
			}
			
			// Check if player is not the defender
			if (player != null && (player.getClan() == null || getActiveChar().getFort().getSiege().getAttackerClan(player.getClan()) != null))
			{
				// LOGGER.info(getActiveChar().getName()+": PK "+player.getObjectId()+" entered scan range");
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);// (L2Character)object);
				}
			}
			
			player = null;
		}
		
		return true;
	}
	
	@Override
	public final L2CommanderInstance getActiveChar()
	{
		return (L2CommanderInstance) super.getActiveChar();
	}
}
