package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.List;

import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Sweep implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SWEEP
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final Player player = (Player) activeChar;
		
		for (WorldObject target : targets)
		{
			if (!(target instanceof Monster))
				continue;
			
			final Monster monster = ((Monster) target);
			
			final List<IntIntHolder> items = monster.getSpoilState().getSweepItems();
			if (items.isEmpty())
				continue;
			
			// Reward spoiler, based on sweep items retained on List.
			for (IntIntHolder item : items)
			{
				if (player.isInParty())
					player.getParty().distributeItem(player, item, true, monster);
				else
					player.addItem("Sweep", item.getId(), item.getValue(), player, true);
			}
			
			// Reset variables.
			monster.getSpoilState().clear();
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}