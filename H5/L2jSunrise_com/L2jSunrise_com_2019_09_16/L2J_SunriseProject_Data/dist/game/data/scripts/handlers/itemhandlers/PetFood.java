/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import l2r.Config;
import l2r.gameserver.data.xml.impl.PetData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Kerberos, Zoey76
 */
public class PetFood implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (playable.isPet() && !((L2PetInstance) playable).canEatFoodId(item.getId()))
		{
			playable.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return false;
		}
		
		for (SkillHolder sk : item.getItem().getSkills())
		{
			useFood(playable, sk.getSkillId(), sk.getSkillLvl(), item);
		}
		return true;
	}
	
	public boolean useFood(L2Playable activeChar, int skillId, int skillLevel, L2ItemInstance item)
	{
		final L2Skill skill = SkillData.getInstance().getInfo(skillId, skillLevel);
		if (skill != null)
		{
			if (activeChar.isPet())
			{
				final L2PcInstance player = activeChar.getActingPlayer();
				final L2PetInstance pet = (L2PetInstance) activeChar;
				if (player.isMounted())
				{
					if (player.getSummon().destroyItem("Consume", item.getObjectId(), 1, null, false))
					{
						player.broadcastPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0));
						player.setCurrentFeed(player.getCurrentFeed() + (skill.getFeed() * Config.PET_FOOD_RATE));
						player.broadcastStatusUpdate();
						if (pet.isHungry())
						{
							pet.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
						}
						return true;
					}
				}
				else if (pet.destroyItem("Consume", item.getObjectId(), 1, null, false))
				{
					pet.broadcastPacket(new MagicSkillUse(pet, pet, skillId, skillLevel, 0, 0));
					pet.setCurrentFed(pet.getCurrentFed() + (skill.getFeed() * Config.PET_FOOD_RATE));
					pet.broadcastStatusUpdate();
					if (pet.isHungry())
					{
						pet.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}
					return true;
				}
			}
			else if (activeChar.isPlayer())
			{
				final L2PcInstance player = activeChar.getActingPlayer();
				if (player.isMounted())
				{
					final List<Integer> foodIds = PetData.getInstance().getPetData(player.getMountNpcId()).getFood();
					if (foodIds.contains(Integer.valueOf(item.getId())))
					{
						if (player.getSummon().destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
							return true;
						}
						else if (player.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
							return true;
						}
					}
				}
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addItemName(item);
				player.sendPacket(sm);
			}
		}
		return false;
	}
}