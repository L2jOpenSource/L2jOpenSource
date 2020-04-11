package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ConditionPlayerHpPercentage extends Condition
{
	private final double _p;
	
	public ConditionPlayerHpPercentage(double p)
	{
		_p = p;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, L2Skill skill, Item item)
	{
		return effector.getCurrentHp() <= effector.getMaxHp() * _p;
	}
}