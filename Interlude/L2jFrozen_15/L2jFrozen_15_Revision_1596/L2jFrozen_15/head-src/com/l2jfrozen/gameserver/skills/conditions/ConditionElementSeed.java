package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.effects.EffectSeed;

/**
 * @author Advi
 */
public class ConditionElementSeed extends Condition
{
	private static int[] seedSkills =
	{
		1285,
		1286,
		1287
	};
	private final int[] requiredSeeds;
	
	public ConditionElementSeed(final int[] seeds)
	{
		requiredSeeds = seeds;
		// if (Config.DEVELOPER) LOGGER.info("Required seeds: " + requiredSeeds[0] + ", " + requiredSeeds[1] + ", " + requiredSeeds[2]+ ", " + requiredSeeds[3]+ ", " + requiredSeeds[4]);
	}
	
	ConditionElementSeed(final int fire, final int water, final int wind, final int various, final int any)
	{
		requiredSeeds = new int[5];
		requiredSeeds[0] = fire;
		requiredSeeds[1] = water;
		requiredSeeds[2] = wind;
		requiredSeeds[3] = various;
		requiredSeeds[4] = any;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final int[] Seeds = new int[3];
		for (int i = 0; i < Seeds.length; i++)
		{
			Seeds[i] = env.player.getFirstEffect(seedSkills[i]) instanceof EffectSeed ? ((EffectSeed) env.player.getFirstEffect(seedSkills[i])).getPower() : 0;
			if (Seeds[i] >= requiredSeeds[i])
			{
				Seeds[i] -= requiredSeeds[i];
			}
			else
			{
				return false;
			}
		}
		
		// if (Config.DEVELOPER) LOGGER.info("Seeds: " + Seeds[0] + ", " + Seeds[1] + ", " + Seeds[2]);
		if (requiredSeeds[3] > 0)
		{
			int count = 0;
			for (int i = 0; i < Seeds.length && count < requiredSeeds[3]; i++)
			{
				if (Seeds[i] > 0)
				{
					Seeds[i]--;
					count++;
				}
			}
			if (count < requiredSeeds[3])
			{
				return false;
			}
		}
		
		if (requiredSeeds[4] > 0)
		{
			int count = 0;
			for (int i = 0; i < Seeds.length && count < requiredSeeds[4]; i++)
			{
				count += Seeds[i];
			}
			if (count < requiredSeeds[4])
			{
				return false;
			}
		}
		
		return true;
	}
}
