/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import java.util.List;

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.ActionType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Template for item skills handler.
 * @author Zoey76
 */
public class ItemSkillsTemplate implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer() && !playable.isPet())
		{
			return false;
		}
		
		// Pets can use items only when they are tradable.
		if (playable.isPet() && !item.isTradeable())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		// Verify that item is not under reuse.
		if (!checkReuse(playable, null, item))
		{
			return false;
		}
		
		final List<SkillHolder> skills = item.getItem().getSkills();
		if (skills.isEmpty())
		{
			_log.info("Item " + item + " does not have registered any skill for handler.");
			return false;
		}
		
		boolean hasConsumeSkill = false;
		boolean isSimultaniously = false;
		for (SkillHolder skillInfo : skills)
		{
			if (skillInfo == null)
			{
				continue;
			}
			
			L2Skill itemSkill = skillInfo.getSkill();
			if (itemSkill != null)
			{
				if ((itemSkill.getItemConsumeId() == 0) && (itemSkill.getItemConsumeCount() > 0))
				{
					hasConsumeSkill = true;
				}
				
				if (itemSkill.isSimultaneousCast())
				{
					isSimultaniously = true;
				}
				
				if (!itemSkill.checkCondition(playable, playable.getTarget(), false))
				{
					return false;
				}
				
				if (playable.isSkillDisabled(itemSkill))
				{
					return false;
				}
				
				// Verify that skill is not under reuse.
				if (!checkReuse(playable, itemSkill, item))
				{
					return false;
				}
				
				if (!item.isPotion() && !item.isElixir() && !item.isScroll() && playable.isCastingNow())
				{
					return false;
				}
				
				// Send message to the master.
				if (playable.isPet())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
					sm.addSkillName(itemSkill);
					playable.sendPacket(sm);
				}
				
				if (itemSkill.isSimultaneousCast() || ((item.getItem().hasImmediateEffect() || item.getItem().hasExImmediateEffect()) && itemSkill.isStatic()))
				{
					playable.doSimultaneousCast(itemSkill);
				}
				else
				{
					if (!playable.useMagic(itemSkill, forceUse, false))
					{
						return false;
					}
				}
				
				if (itemSkill.getReuseDelay() > 0)
				{
					playable.addTimeStamp(itemSkill, itemSkill.getReuseDelay());
				}
			}
		}
		
		final boolean isCapsuleItem = (item.getItem().getDefaultAction() == ActionType.CAPSULE) || item.getItem().mustConsume();
		if (isCapsuleItem || (hasConsumeSkill && (item.isPotion() || item.isElixir() || item.isScroll() || isSimultaniously)))
		{
			if (!playable.destroyItem("Consume", item.getObjectId(), 1, playable, false))
			{
				playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @param playable the character using the item or skill
	 * @param skill the skill being used, can be null
	 * @param item the item being used
	 * @return {@code true} if the the item or skill to check is available, {@code false} otherwise
	 */
	private boolean checkReuse(L2Playable playable, L2Skill skill, L2ItemInstance item)
	{
		final long remainingTime = (skill != null) ? playable.getSkillRemainingReuseTime(skill.getReuseHashCode()) : playable.getItemRemainingReuseTime(item.getObjectId());
		final boolean isAvailable = remainingTime <= 0;
		if (playable.isPlayer())
		{
			if (!isAvailable)
			{
				final int hours = (int) (remainingTime / 3600000L);
				final int minutes = (int) (remainingTime % 3600000L) / 60000;
				final int seconds = (int) ((remainingTime / 1000) % 60);
				SystemMessage sm = null;
				if (hours > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
					sm.addInt(hours);
					sm.addInt(minutes);
				}
				else if (minutes > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
					sm.addInt(minutes);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
					if ((skill == null) || skill.isStatic())
					{
						sm.addItemName(item);
					}
					else
					{
						sm.addSkillName(skill);
					}
				}
				sm.addInt(seconds);
				playable.sendPacket(sm);
			}
		}
		return isAvailable;
	}
}
