package com.l2jfrozen.gameserver.ai;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author mkizub
 */
public class L2DoorAI extends L2CharacterAI
{
	
	public L2DoorAI(final L2DoorInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	// rather stupid AI... well, it's for doors :D
	@Override
	protected void onIntentionIdle()
	{
		// null;
	}
	
	@Override
	protected void onIntentionActive()
	{
		// null;
	}
	
	@Override
	protected void onIntentionRest()
	{
		// null;
	}
	
	@Override
	protected void onIntentionAttack(final L2Character target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionCast(final L2Skill skill, final L2Object target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionMoveTo(final L2CharPosition destination)
	{
		// null;
	}
	
	@Override
	protected void onIntentionFollow(final L2Character target)
	{
		// null;
	}
	
	@Override
	protected void onIntentionPickUp(final L2Object item)
	{
		// null;
	}
	
	@Override
	protected void onIntentionInteract(final L2Object object)
	{
		// null;
	}
	
	@Override
	protected void onEvtThink()
	{
		// null;
	}
	
	@Override
	protected void onEvtAttacked(final L2Character attacker)
	{
		L2DoorInstance me = (L2DoorInstance) actor;
		ThreadPoolManager.getInstance().executeTask(new onEventAttackedDoorTask(me, attacker));
		me = null;
	}
	
	@Override
	protected void onEvtAggression(final L2Character target, final int aggro)
	{
		// null;
	}
	
	@Override
	protected void onEvtStunned(final L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtSleeping(final L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtRooted(final L2Character attacker)
	{
		// null;
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// null;
	}
	
	@Override
	protected void onEvtUserCmd(final Object arg0, final Object arg1)
	{
		// null;
	}
	
	@Override
	protected void onEvtArrived()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// null;
	}
	
	@Override
	protected void onEvtArrivedBlocked(final L2CharPosition blocked_at_pos)
	{
		// null;
	}
	
	@Override
	protected void onEvtForgetObject(final L2Object object)
	{
		// null;
	}
	
	@Override
	protected void onEvtCancel()
	{
		// null;
	}
	
	@Override
	protected void onEvtDead()
	{
		// null;
	}
	
	private class onEventAttackedDoorTask implements Runnable
	{
		private final L2DoorInstance door;
		private final L2Character attacker;
		
		public onEventAttackedDoorTask(final L2DoorInstance door, final L2Character attacker)
		{
			this.door = door;
			this.attacker = attacker;
		}
		
		@Override
		public void run()
		{
			door.getKnownList().updateKnownObjects();
			
			for (final L2SiegeGuardInstance guard : door.getKnownSiegeGuards())
			{
				if (guard != null && guard.getAI() != null && actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && Math.abs(attacker.getZ() - guard.getZ()) < 200)
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 15);
				}
			}
			for (final L2FortSiegeGuardInstance guard : door.getKnownFortSiegeGuards())
			{
				if (guard != null && guard.getAI() != null && actor.isInsideRadius(guard, guard.getFactionRange(), false, true) && Math.abs(attacker.getZ() - guard.getZ()) < 200)
				{
					guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 15);
				}
			}
		}
	}
}
