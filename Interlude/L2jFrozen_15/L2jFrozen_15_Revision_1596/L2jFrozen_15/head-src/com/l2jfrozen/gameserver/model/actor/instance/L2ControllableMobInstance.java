package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2ControllableMobAI;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author littlecrow
 */
public class L2ControllableMobInstance extends L2MonsterInstance
{
	private boolean isInvul;
	private L2ControllableMobAI aiBackup; // to save ai, avoiding beeing detached
	
	protected class ControllableAIAcessor extends AIAccessor
	{
		@Override
		public void detachAI()
		{
			// do nothing, AI of controllable mobs can't be detached automatically
		}
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		// force mobs to be aggro
		return 500;
	}
	
	public L2ControllableMobInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (aiCharacter == null)
		{
			synchronized (this)
			{
				if (aiCharacter == null && aiBackup == null)
				{
					aiCharacter = new L2ControllableMobAI(new ControllableAIAcessor());
					aiBackup = (L2ControllableMobAI) aiCharacter;
				}
				else
				{
					aiCharacter = aiBackup;
				}
			}
		}
		return aiCharacter;
	}
	
	@Override
	public boolean isInvul()
	{
		return isInvul;
	}
	
	public void setInvul(final boolean isInvul)
	{
		this.isInvul = isInvul;
	}
	
	@Override
	public void reduceCurrentHp(double i, final L2Character attacker, final boolean awake)
	{
		if (isInvul() || isDead())
		{
			return;
		}
		
		if (awake)
		{
			stopSleeping(null);
		}
		
		i = getCurrentHp() - i;
		
		if (i < 0)
		{
			i = 0;
		}
		
		setCurrentHp(i);
		
		if (isDead())
		{
			// first die (and calculate rewards), if currentHp < 0,
			// then overhit may be calculated
			if (Config.DEBUG)
			{
				LOGGER.debug("char is dead.");
			}
			
			stopMove(null);
			
			// Start the doDie process
			doDie(attacker);
			
			// now reset currentHp to zero
			setCurrentHp(0);
		}
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		removeAI();
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		removeAI();
		super.deleteMe();
	}
	
	/**
	 * Definitively remove AI
	 */
	protected void removeAI()
	{
		synchronized (this)
		{
			if (aiBackup != null)
			{
				aiBackup.setIntention(CtrlIntention.AI_INTENTION_IDLE);
				aiBackup = null;
				aiCharacter = null;
			}
		}
	}
}
