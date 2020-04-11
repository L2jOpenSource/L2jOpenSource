package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ConditionPlayerWeight extends Condition
{
	private final int _weight;
	
	public ConditionPlayerWeight(int weight)
	{
		_weight = weight;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, L2Skill skill, Item item)
	{
		if (effector instanceof Player)
		{
			final Player player = (Player) effector;
			if (player.getMaxLoad() > 0)
				return player.getCurrentLoad() * 100 / player.getMaxLoad() < _weight;
		}
		return true;
	}
}