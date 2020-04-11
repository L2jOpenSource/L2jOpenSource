/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.items;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.items.type.ArmorType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.util.StringUtil;

/**
 * This class is dedicated to the management of armors.
 */
public final class L2Armor extends L2Item
{
	/**
	 * Skill that activates when armor is enchanted +4.
	 */
	private SkillHolder _enchant4Skill = null;
	private ArmorType _type;
	
	/**
	 * Constructor for Armor.
	 * @param set the StatsSet designating the set of couples (key,value) characterizing the armor.
	 */
	public L2Armor(StatsSet set)
	{
		super(set);
		_type = set.getEnum("armor_type", ArmorType.class, ArmorType.NONE);
		
		int _bodyPart = getBodyPart();
		if ((_bodyPart == L2Item.SLOT_NECK) || ((_bodyPart & L2Item.SLOT_L_EAR) != 0) || ((_bodyPart & L2Item.SLOT_L_FINGER) != 0) || ((_bodyPart & L2Item.SLOT_R_BRACELET) != 0) || ((_bodyPart & L2Item.SLOT_L_BRACELET) != 0))
		{
			_type1 = L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
			_type2 = L2Item.TYPE2_ACCESSORY;
		}
		else
		{
			if ((_type == ArmorType.NONE) && (getBodyPart() == L2Item.SLOT_L_HAND))
			{
				_type = ArmorType.SHIELD;
			}
			_type1 = L2Item.TYPE1_SHIELD_ARMOR;
			_type2 = L2Item.TYPE2_SHIELD_ARMOR;
		}
		
		String skill = set.getString("enchant4_skill", null);
		if (skill != null)
		{
			String[] info = skill.split("-");
			
			if ((info != null) && (info.length == 2))
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch (Exception nfe)
				{
					// Incorrect syntax, don't add new skill
					_log.info(StringUtil.concat("> Couldnt parse ", skill, " in armor enchant skills! item ", toString()));
				}
				if ((id > 0) && (level > 0))
				{
					_enchant4Skill = new SkillHolder(id, level);
				}
			}
		}
	}
	
	/**
	 * @return the type of the armor.
	 */
	@Override
	public ArmorType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the item after applying the mask.
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return skill that player get when has equipped armor +4 or more
	 */
	@Override
	public L2Skill getEnchant4Skill()
	{
		if (_enchant4Skill == null)
		{
			return null;
		}
		return _enchant4Skill.getSkill();
	}
}
