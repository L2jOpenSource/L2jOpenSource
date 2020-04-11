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
package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.Config;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.base.AcquireSkillType;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.skills.L2Skill;

/**
 * Acquire Skill Info server packet implementation.
 * @author Zoey76
 */
public class AcquireSkillInfo extends L2GameServerPacket
{
	private final AcquireSkillType _type;
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final List<Req> _reqs;
	
	/**
	 * Private class containing learning skill requisites.
	 */
	private static class Req
	{
		public int itemId;
		public long count;
		public int type;
		public int unk;
		
		/**
		 * @param pType TODO identify.
		 * @param pItemId the item Id.
		 * @param itemCount the item count.
		 * @param pUnk TODO identify.
		 */
		public Req(int pType, int pItemId, long itemCount, int pUnk)
		{
			itemId = pItemId;
			type = pType;
			count = itemCount;
			unk = pUnk;
		}
	}
	
	/**
	 * Constructor for the acquire skill info object.
	 * @param skillType the skill learning type.
	 * @param skillLearn the skill learn.
	 */
	public AcquireSkillInfo(AcquireSkillType skillType, L2SkillLearn skillLearn)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_spCost = skillLearn.getLevelUpSp();
		_type = skillType;
		_reqs = new ArrayList<>();
		if ((skillType != AcquireSkillType.PLEDGE) || Config.LIFE_CRYSTAL_NEEDED)
		{
			for (ItemHolder item : skillLearn.getRequiredItems())
			{
				if (!Config.DIVINE_SP_BOOK_NEEDED && (_id == L2Skill.SKILL_DIVINE_INSPIRATION))
				{
					continue;
				}
				_reqs.add(new Req(99, item.getId(), item.getCount(), 50));
			}
		}
	}
	
	/**
	 * Special constructor for Alternate Skill Learning system.<br>
	 * Sets a custom amount of SP.
	 * @param skillType the skill learning type.
	 * @param skillLearn the skill learn.
	 * @param sp the custom SP amount.
	 */
	public AcquireSkillInfo(AcquireSkillType skillType, L2SkillLearn skillLearn, int sp)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_spCost = sp;
		_type = skillType;
		_reqs = new ArrayList<>();
		for (ItemHolder item : skillLearn.getRequiredItems())
		{
			_reqs.add(new Req(99, item.getId(), item.getCount(), 50));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_type.ordinal());
		writeD(_reqs.size());
		for (Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.itemId);
			writeQ(temp.count);
			writeD(temp.unk);
		}
	}
}