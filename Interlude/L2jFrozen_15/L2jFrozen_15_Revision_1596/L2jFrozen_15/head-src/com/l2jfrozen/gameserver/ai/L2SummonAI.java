package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Character.AIAccessor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;

public class L2SummonAI extends L2CharacterAI
{
	
	private boolean thinking; // to prevent recursive thinking
	private L2Summon summon;
	
	public L2SummonAI(final AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		L2Summon summon = (L2Summon) actor;
		
		if (summon.getFollowStatus())
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
		
		summon = null;
	}
	
	private void thinkAttack()
	{
		summon = (L2Summon) actor;
		L2Object target = null;
		target = summon.getTarget();
		
		// Like L2OFF if the target is dead the summon must go back to his owner
		if (target != null && summon != null && ((L2Character) target).isDead())
		{
			summon.setFollowStatus(true);
		}
		
		if (checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(getAttackTarget(), actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		clientStopMoving(null);
		accessor.doAttack(getAttackTarget());
		return;
	}
	
	private void thinkCast()
	{
		L2Summon summon = (L2Summon) actor;
		
		final L2Character target = getCastTarget();
		if (checkTargetLost(target))
		{
			setCastTarget(null);
			return;
		}
		
		final L2Skill skill = get_skill();
		if (maybeMoveToPawn(target, actor.getMagicalAttackRange(skill)))
		{
			return;
		}
		
		clientStopMoving(null);
		summon.setFollowStatus(false);
		summon = null;
		setIntention(AI_INTENTION_IDLE);
		accessor.doCast(skill);
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
		((L2Summon.AIAccessor) accessor).doPickupItem(target);
		
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
	protected void onEvtFinishCasting()
	{
		super.onEvtFinishCasting();
		
		final L2Summon summon = (L2Summon) actor;
		L2Object target = null;
		target = summon.getTarget();
		
		if (target == null)
		{
			return;
		}
		
		if (summon.getAI().getIntention() != AI_INTENTION_ATTACK)
		{
			summon.setFollowStatus(true);
		}
		else if (((L2Character) target).isDead())
		{
			summon.setFollowStatus(true);
		}
		
	}
}