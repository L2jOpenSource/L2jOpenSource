package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ConditionTargetHpMinMax extends Condition
{
	private final int _minHp, _maxHp;
	
	public ConditionTargetHpMinMax(int minHp, int maxHp)
	{
		_minHp = minHp;
		_maxHp = maxHp;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, L2Skill skill, Item item)
	{
		if (effected == null)
			return false;
		
		final int currentHp = (int) effected.getCurrentHp() * 100 / effected.getMaxHp();
		return currentHp >= _minHp && currentHp <= _maxHp;
	}
}