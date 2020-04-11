package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.effects.EffectBattleForce;
import com.l2jfrozen.gameserver.skills.effects.EffectSpellForce;

/**
 * @author kombat
 */
public class ConditionForceBuff extends Condition
{
	private static int BATTLE_FORCE = 5104;
	private static int SPELL_FORCE = 5105;
	
	private final int battleForces;
	private final int spellForces;
	
	public ConditionForceBuff(final int[] forces)
	{
		battleForces = forces[0];
		spellForces = forces[1];
	}
	
	public ConditionForceBuff(final int battle, final int spell)
	{
		battleForces = battle;
		spellForces = spell;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		final int neededBattle = battleForces;
		if (neededBattle > 0)
		{
			final L2Effect battleForce = env.player.getFirstEffect(BATTLE_FORCE);
			if (!(battleForce instanceof EffectBattleForce) || ((EffectBattleForce) battleForce).forces < neededBattle)
			{
				return false;
			}
		}
		final int neededSpell = spellForces;
		if (neededSpell > 0)
		{
			final L2Effect spellForce = env.player.getFirstEffect(SPELL_FORCE);
			if (!(spellForce instanceof EffectSpellForce) || ((EffectSpellForce) spellForce).forces < neededSpell)
			{
				return false;
			}
		}
		return true;
	}
}
