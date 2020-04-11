package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.extractable.ExtractableProductItem;
import net.sf.l2j.gameserver.skills.extractable.ExtractableSkill;

public class Extractable implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.EXTRACTABLE,
		SkillType.EXTRACTABLE_FISH
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final ExtractableSkill exItem = skill.getExtractableSkill();
		if (exItem == null || exItem.getProductItems().isEmpty())
		{
			LOGGER.warn("Missing informations for extractable skill id: {}.", skill.getId());
			return;
		}
		
		final Player player = activeChar.getActingPlayer();
		final int chance = Rnd.get(100000);
		
		boolean created = false;
		int chanceIndex = 0;
		
		for (ExtractableProductItem expi : exItem.getProductItems())
		{
			chanceIndex += (int) (expi.getChance() * 1000);
			if (chance <= chanceIndex)
			{
				for (IntIntHolder item : expi.getItems())
				{
					final Item template = ItemData.getInstance().getTemplate(item.getId());
					if (template == null)
						continue;
					
					// The inventory is full, break the loop.
					if (!player.getInventory().validateCapacityByItemId(item.getId()))
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						break;
					}
					
					player.addItem("Extract", item.getId(), item.getValue(), player, true);
				}
				created = true;
				break;
			}
		}
		
		if (!created)
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return;
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}