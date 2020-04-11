package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author kombat
 */
public final class ChanceCondition
{
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	
	public static enum TriggerType
	{
		// You hit an enemy
		ON_HIT(1),
		// You hit an enemy - was crit
		ON_CRIT(2),
		// You cast a skill
		ON_CAST(4),
		// You cast a skill - it was a physical one
		ON_PHYSICAL(8),
		// You cast a skill - it was a magic one
		ON_MAGIC(16),
		// You cast a skill - it was a magic one - good magic
		ON_MAGIC_GOOD(32),
		// You cast a skill - it was a magic one - offensive magic
		ON_MAGIC_OFFENSIVE(64),
		// You are attacked by enemy
		ON_ATTACKED(128),
		// You are attacked by enemy - by hit
		ON_ATTACKED_HIT(256),
		// You are attacked by enemy - by hit - was crit
		ON_ATTACKED_CRIT(512),
		// A skill was casted on you
		ON_HIT_BY_SKILL(1024),
		// An evil skill was casted on you
		ON_HIT_BY_OFFENSIVE_SKILL(2048),
		// A good skill was casted on you
		ON_HIT_BY_GOOD_MAGIC(4096);
		
		private int mask;
		
		private TriggerType(final int mask)
		{
			this.mask = mask;
		}
		
		public boolean check(final int event)
		{
			return (mask & event) != 0; // Trigger (sub-)type contains event (sub-)type
		}
	}
	
	private final TriggerType triggerType;
	
	private final int chance;
	
	private ChanceCondition(final TriggerType trigger, final int chance)
	{
		triggerType = trigger;
		this.chance = chance;
	}
	
	public static ChanceCondition parse(final StatsSet set)
	{
		try
		{
			final TriggerType trigger = set.getEnum("chanceType", TriggerType.class);
			final int chance = set.getInteger("activationChance", 0);
			if (trigger != null && chance > 0)
			{
				return new ChanceCondition(trigger, chance);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean trigger(final int event)
	{
		return triggerType.check(event) && Rnd.get(100) < chance;
	}
	
	@Override
	public String toString()
	{
		return "Trigger[" + chance + ";" + triggerType.toString() + "]";
	}
}
