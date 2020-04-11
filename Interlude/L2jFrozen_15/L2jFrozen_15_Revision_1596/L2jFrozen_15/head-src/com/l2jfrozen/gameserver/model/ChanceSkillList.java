package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.skills.Formulas;

import javolution.util.FastMap;

/**
 * @author kombat
 */
public class ChanceSkillList extends FastMap<L2Skill, ChanceCondition>
{
	private static final long serialVersionUID = -3523525435531L;
	
	private L2Character owner;
	
	public ChanceSkillList(final L2Character owner)
	{
		super();
		shared();
		this.owner = owner;
	}
	
	public L2Character getOwner()
	{
		return owner;
	}
	
	public void setOwner(final L2Character owner)
	{
		this.owner = owner;
	}
	
	public void onHit(final L2Character target, final boolean ownerWasHit, final boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_CRIT;
			}
		}
		
		onEvent(event, target);
	}
	
	public void onSkillHit(final L2Character target, final boolean ownerWasHit, final boolean wasMagic, final boolean wasOffensive)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (wasOffensive)
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= wasMagic ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= wasOffensive ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		onEvent(event, target);
	}
	
	public static boolean canTriggerByCast(final L2Character caster, final L2Character target, final L2Skill trigger)
	{
		// crafting does not trigger any chance skills
		// possibly should be unhardcoded
		switch (trigger.getSkillType())
		{
			case COMMON_CRAFT:
			case DWARVEN_CRAFT:
				return false;
		}
		
		if (trigger.isToggle() || trigger.isPotion())
		{
			return false; // No buffing with toggle skills or potions
		}
		
		if (trigger.getId() == 1320)
		{
			return false; // No buffing with Common
		}
		
		if (trigger.isOffensive() && !Formulas.calcMagicSuccess(caster, target, trigger))
		{
			return false; // Low grade skills won't trigger for high level targets
		}
		
		return true;
	}
	
	public void onEvent(final int event, final L2Character target)
	{
		for (FastMap.Entry<L2Skill, ChanceCondition> e = head(), end = tail(); (e = e.getNext()) != end;)
		{
			if (e.getValue() != null && e.getValue().trigger(event))
			{
				makeCast(e.getKey(), target);
			}
		}
	}
	
	private void makeCast(L2Skill skill, final L2Character target)
	{
		try
		{
			if (skill.getWeaponDependancy(owner, true))
			{
				if (skill.triggerAnotherSkill()) // should we use this skill or this skill is just referring to another one ...
				{
					skill = owner.skills.get(skill.getTriggeredId());
					if (skill == null)
					{
						return;
					}
				}
				
				final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				final L2Object[] targets = skill.getTargetList(owner, false, target);
				
				owner.broadcastPacket(new MagicSkillLaunched(owner, skill.getDisplayId(), skill.getLevel(), targets));
				owner.broadcastPacket(new MagicSkillUser(owner, (L2Character) targets[0], skill.getDisplayId(), skill.getLevel(), 0, 0));
				
				// Launch the magic skill and calculate its effects
				if (handler != null)
				{
					handler.useSkill(owner, skill, targets);
				}
				else
				{
					skill.useSkill(owner, targets);
				}
			}
		}
		catch (final Exception e)
		{
			// null
		}
	}
}
