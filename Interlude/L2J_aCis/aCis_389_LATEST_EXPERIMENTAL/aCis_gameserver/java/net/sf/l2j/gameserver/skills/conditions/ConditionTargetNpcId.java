package net.sf.l2j.gameserver.skills.conditions;

import java.util.List;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ConditionTargetNpcId extends Condition
{
	private final List<Integer> _npcIds;
	
	public ConditionTargetNpcId(List<Integer> npcIds)
	{
		_npcIds = npcIds;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, L2Skill skill, Item item)
	{
		if (effected instanceof Npc)
			return _npcIds.contains(((Npc) effected).getNpcId());
		
		if (effected instanceof Door)
			return _npcIds.contains(((Door) effected).getDoorId());
		
		return false;
	}
}