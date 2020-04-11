package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ItemSkills implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (playable instanceof Servitor)
			return;
		
		final boolean isPet = playable instanceof Pet;
		final Player player = playable.getActingPlayer();
		
		// Pets can only use tradable items.
		if (isPet && !item.isTradable())
		{
			player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		final IntIntHolder[] skills = item.getEtcItem().getSkills();
		if (skills == null)
		{
			LOGGER.warn("{} doesn't have any registered skill for handler.", item.getName());
			return;
		}
		
		for (final IntIntHolder skillInfo : skills)
		{
			if (skillInfo == null)
				continue;
			
			final L2Skill itemSkill = skillInfo.getSkill();
			if (itemSkill == null)
				continue;
			
			if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				return;
			
			// No message on retail, the use is just forgotten.
			if (playable.isSkillDisabled(itemSkill))
				return;
			
			// Item consumption is setup here.
			if (itemSkill.isPotion() || itemSkill.isSimultaneousCast())
			{
				if (playable.isSkillDisabled(itemSkill))
					return;
				
				// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
				if (!item.isHerb() && !playable.destroyItem("Consume", item.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					return;
				}
				
				playable.getCast().doSimultaneousCast(itemSkill);
				
				if (!isPet && item.isHerb() && player.hasServitor())
					player.getSummon().getCast().doSimultaneousCast(itemSkill);
			}
			else
				playable.getAI().tryTo(IntentionType.CAST, new SkillUseHolder(itemSkill, playable, forceUse, false), (item.isEtcItem() ? item : null));
			
			// Send message to owner.
			if (isPet)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(itemSkill));
			else
			{
				final int skillId = skillInfo.getId();
				
				// Buff icon for healing potions.
				switch (skillId)
				{
					case 2031:
					case 2032:
					case 2037:
						final int buffId = player.getShortBuffTaskSkillId();
						
						// Greater healing potions.
						if (skillId == 2037)
							player.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						// Healing potions.
						else if (skillId == 2032 && buffId != 2037)
							player.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						// Lesser healing potions.
						else
						{
							if (buffId != 2037 && buffId != 2032)
								player.shortBuffStatusUpdate(skillId, skillInfo.getValue(), itemSkill.getBuffDuration() / 1000);
						}
						break;
				}
			}
		}
	}
}