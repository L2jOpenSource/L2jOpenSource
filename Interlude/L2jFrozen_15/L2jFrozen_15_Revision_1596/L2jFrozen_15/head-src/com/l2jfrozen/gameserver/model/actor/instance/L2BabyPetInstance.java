package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2BabyPetInstance extends L2PetInstance
{
	protected L2Skill weakHeal;
	protected L2Skill strongHeal;
	private Future<?> healingTask;
	
	public L2BabyPetInstance(final int objectId, final L2NpcTemplate template, final L2PcInstance owner, final L2ItemInstance control)
	{
		super(objectId, template, owner, control);
		
		// look through the skills that this template has and find the weak and strong heal.
		L2Skill skill1 = null;
		L2Skill skill2 = null;
		
		for (final L2Skill skill : getTemplate().getSkills().values())
		{
			// just in case, also allow cp heal and mp recharges to be considered here...you never know ;)
			if (skill.isActive() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_OWNER_PET
				&& (skill.getSkillType() == L2Skill.SkillType.HEAL || skill.getSkillType() == L2Skill.SkillType.HOT || skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE || skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT || skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC || skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL
					|| skill.getSkillType() == L2Skill.SkillType.COMBATPOINTPERCENTHEAL || skill.getSkillType() == L2Skill.SkillType.CPHOT || skill.getSkillType() == L2Skill.SkillType.MANAHEAL || skill.getSkillType() == L2Skill.SkillType.MANA_BY_LEVEL || skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT || skill.getSkillType() == L2Skill.SkillType.MANARECHARGE
					|| skill.getSkillType() == L2Skill.SkillType.MPHOT))
			{
				// only consider two skills. If the pet has more, too bad...they won't be used by its AI.
				// for now assign the first two skills in the order they come. Once we have both skills, re-arrange them
				if (skill1 == null)
				{
					skill1 = skill;
				}
				else
				{
					skill2 = skill;
					break;
				}
			}
		}
		
		// process the results. Only store the ID of the skills. The levels are generated on the fly, based on the pet's level!
		if (skill1 != null)
		{
			if (skill2 == null)
			{
				// duplicate so that the same skill will be used in both normal and emergency situations
				weakHeal = skill1;
				strongHeal = skill1;
			}
			else
			{
				// arrange the weak and strong skills appropriately
				if (skill1.getPower() > skill2.getPower())
				{
					weakHeal = skill2;
					strongHeal = skill1;
				}
				else
				{
					weakHeal = skill1;
					strongHeal = skill2;
				}
				skill2 = null;
			}
			
			// start the healing task
			healingTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(this), 0, 1000);
			
			skill1 = null;
		}
	}
	
	@Override
	public boolean doDie(final L2Character killer)
	{
		
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (healingTask != null)
		{
			healingTask.cancel(false);
			healingTask = null;
		}
		return true;
	}
	
	@Override
	public synchronized void unSummon(final L2PcInstance owner)
	{
		super.unSummon(owner);
		
		if (healingTask != null)
		{
			healingTask.cancel(false);
			healingTask = null;
		}
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		if (healingTask == null)
		{
			healingTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(this), 0, 1000);
		}
	}
	
	private class Heal implements Runnable
	{
		private final L2BabyPetInstance baby;
		
		public Heal(final L2BabyPetInstance baby)
		{
			this.baby = baby;
		}
		
		@Override
		public void run()
		{
			L2PcInstance owner = baby.getOwner();
			
			// if the owner is dead, merely wait for the owner to be resurrected
			// if the pet is still casting from the previous iteration, allow the cast to complete...
			if (!owner.isDead() && !baby.isCastingNow() && !baby.isBetrayed())
			{
				// casting automatically stops any other action (such as autofollow or a move-to).
				// We need to gather the necessary info to restore the previous state.
				final boolean previousFollowStatus = baby.getFollowStatus();
				
				// if the owner's HP is more than 80%, do nothing.
				// if the owner's HP is very low (less than 20%) have a high chance for strong heal
				// otherwise, have a low chance for weak heal
				if (owner.getCurrentHp() / owner.getMaxHp() < 0.2 && Rnd.get(4) < 3)
				{
					baby.useMagic(strongHeal, false, false);
				}
				else if (owner.getCurrentHp() / owner.getMaxHp() < 0.8 && Rnd.get(4) < 1)
				{
					baby.useMagic(weakHeal, false, false);
				}
				
				// calling useMagic changes the follow status, if the babypet actually casts
				// (as opposed to failing due some factors, such as too low MP, etc).
				// if the status has actually been changed, revert it. Else, allow the pet to
				// continue whatever it was trying to do.
				// NOTE: This is important since the pet may have been told to attack a target.
				// reverting the follow status will abort this attack! While aborting the attack
				// in order to heal is natural, it is not acceptable to abort the attack on its own,
				// merely because the timer stroke and without taking any other action...
				if (previousFollowStatus != baby.getFollowStatus())
				{
					setFollowStatus(previousFollowStatus);
				}
			}
			owner = null;
		}
	}
}
