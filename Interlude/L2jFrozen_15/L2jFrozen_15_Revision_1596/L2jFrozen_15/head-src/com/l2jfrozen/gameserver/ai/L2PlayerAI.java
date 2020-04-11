package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.EmptyStackException;
import java.util.Stack;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Character.AIAccessor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfrozen.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class L2PlayerAI extends L2CharacterAI
{
	private boolean thinking; // to prevent recursive thinking
	
	class IntentionCommand
	{
		protected CtrlIntention crtlIntention;
		protected Object arg0, arg1;
		
		protected IntentionCommand(final CtrlIntention pIntention, final Object pArg0, final Object pArg1)
		{
			crtlIntention = pIntention;
			arg0 = pArg0;
			arg1 = pArg1;
		}
	}
	
	private final Stack<IntentionCommand> interuptedIntentions = new Stack<>();
	
	private synchronized Stack<IntentionCommand> getInterruptedIntentions()
	{
		return interuptedIntentions;
	}
	
	public L2PlayerAI(final AIAccessor accessor)
	{
		super(accessor);
	}
	
	/**
	 * Saves the current Intention for this L2PlayerAI if necessary and calls changeIntention in AbstractAI.<BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	@Override
	public void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1)
	{
		/*
		 * if (Config.DEBUG) LOGGER.warn("L2PlayerAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */
		
		// nothing to do if it does not CAST intention
		if (intention != AI_INTENTION_CAST)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		final CtrlIntention theIntention = getIntention();
		final Object theIntentionArg0 = get_intentionArg0();
		final Object theIntentionArg1 = get_intentionArg1();
		
		// do nothing if next intention is same as current one.
		if (intention == theIntention && arg0 == theIntentionArg0 && arg1 == theIntentionArg1)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		/*
		 * if (Config.DEBUG) LOGGER.warn("L2PlayerAI: changeIntention -> Saving current intention: " +ntention + " " + intention_arg0 + " " + intention_arg1);
		 */
		
		// push current intention to stack
		getInterruptedIntentions().push(new IntentionCommand(theIntention, theIntentionArg0, theIntentionArg1));
		super.changeIntention(intention, arg0, arg1);
	}
	
	/**
	 * Finalize the casting of a skill. This method overrides L2CharacterAI method.<BR>
	 * <BR>
	 * <B>What it does:</B> Check if actual intention is set to CAST and, if so, retrieves latest intention before the actual CAST and set it as the current intention for the player
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// forget interupted actions after offensive skill
		final L2Skill skill = get_skill();
		
		if (skill != null && skill.isOffensive())
		{
			getInterruptedIntentions().clear();
		}
		
		if (getIntention() == AI_INTENTION_CAST)
		{
			// run interupted intention if it remain.
			if (!getInterruptedIntentions().isEmpty())
			{
				IntentionCommand cmd = null;
				try
				{
					cmd = getInterruptedIntentions().pop();
				}
				catch (final EmptyStackException ese)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						ese.printStackTrace();
					}
				}
				
				/*
				 * if (Config.DEBUG) LOGGER.warn("L2PlayerAI: onEvtFinishCasting -> " + cmd._intention + " " + cmd._arg0 + " " + cmd._arg1);
				 */
				
				if (cmd != null && cmd.crtlIntention != AI_INTENTION_CAST) // previous state shouldn't be casting
				{
					setIntention(cmd.crtlIntention, cmd.arg0, cmd.arg1);
					cmd = null;
				}
				else
				{
					setIntention(AI_INTENTION_IDLE);
				}
				
				cmd = null;
			}
			else
			{
				/*
				 * if (Config.DEBUG) LOGGER.warn("L2PlayerAI: no previous intention set... Setting it to IDLE");
				 */
				// set intention to idle if skill doesn't change intention.
				setIntention(AI_INTENTION_IDLE);
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void clientStopMoving(final L2CharPosition pos)
	{
		super.clientStopMoving(pos);
		final L2PcInstance player = (L2PcInstance) actor;
		if (player.getPosticipateSit())
		{
			player.sitDown();
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		clientMovingToPawnOffset = 0;
		clientMoving = false;
		
		super.clientNotifyDead();
	}
	
	private void thinkAttack()
	{
		final L2Character target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		
		if (checkTargetLostOrDead(target))
		{
			// Notify the target
			setAttackTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(target, actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		accessor.doAttack(target);
		return;
	}
	
	private void thinkCast()
	{
		
		final L2Character target = getCastTarget();
		final L2Skill skill = get_skill();
		// if (Config.DEBUG) LOGGER.warn("L2PlayerAI: thinkCast -> Start");
		
		if (checkTargetLost(target))
		{
			if (skill.isOffensive() && getAttackTarget() != null)
			{
				// Notify the target
				setCastTarget(null);
			}
			return;
		}
		
		if (target != null)
		{
			if (maybeMoveToPawn(target, actor.getMagicalAttackRange(skill)))
			{
				return;
			}
		}
		
		if (skill.getHitTime() > 50)
		{
			clientStopMoving(null);
		}
		
		final L2Object oldTarget = actor.getTarget();
		
		if (oldTarget != null)
		{
			// Replace the current target by the cast target
			if (target != null && oldTarget != target)
			{
				actor.setTarget(getCastTarget());
			}
			
			// Launch the Cast of the skill
			accessor.doCast(get_skill());
			
			// Restore the initial target
			if (target != null && oldTarget != target)
			{
				actor.setTarget(oldTarget);
			}
		}
		else
		{
			accessor.doCast(skill);
		}
		
		return;
	}
	
	private void thinkPickUp()
	{
		if (actor.isAllSkillsDisabled())
		{
			return;
		}
		
		final L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor) accessor).doPickupItem(target);
		
		return;
	}
	
	private void thinkInteract()
	{
		if (actor.isAllSkillsDisabled())
		{
			return;
		}
		
		final L2Object target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		if (!(target instanceof L2StaticObjectInstance))
		{
			((L2PcInstance.AIAccessor) accessor).doInteract((L2Character) target);
		}
		
		setIntention(AI_INTENTION_IDLE);
		return;
	}
	
	@Override
	protected void onEvtThink()
	{
		if (thinking || actor.isAllSkillsDisabled())
		{
			return;
		}
		
		thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			thinking = false;
		}
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		ThreadPoolManager.getInstance().executeTask(new KnownListAsynchronousUpdateTask(actor));
		super.onEvtArrivedRevalidate();
	}
	
}
