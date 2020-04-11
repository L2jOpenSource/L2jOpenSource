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
package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class L2EnchantSkillGroup
{
	private final int _id;
	private final List<EnchantSkillHolder> _enchantDetails = new ArrayList<>();
	
	public L2EnchantSkillGroup(int id)
	{
		_id = id;
	}
	
	public void addEnchantDetail(EnchantSkillHolder detail)
	{
		_enchantDetails.add(detail);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public List<EnchantSkillHolder> getEnchantGroupDetails()
	{
		return _enchantDetails;
	}
	
	public static class EnchantSkillHolder
	{
		private final int _level;
		private final int _adenaCost;
		private final int _expCost;
		private final int _spCost;
		private final byte[] _rate;
		
		public EnchantSkillHolder(StatsSet set)
		{
			_level = set.getInteger("level");
			_adenaCost = set.getInteger("adena");
			_expCost = set.getInteger("exp");
			_spCost = set.getInteger("sp");
			_rate = new byte[24];
			for (int i = 0; i < 24; i++)
			{
				_rate[i] = set.getByte("chance" + (76 + i), (byte) 0);
			}
		}
		
		/**
		 * @return Returns the level.
		 */
		public int getLevel()
		{
			return _level;
		}
		
		/**
		 * @return Returns the spCost.
		 */
		public int getSpCost()
		{
			return _spCost;
		}
		
		public int getExpCost()
		{
			return _expCost;
		}
		
		public int getAdenaCost()
		{
			return _adenaCost;
		}
		
		public byte getRate(L2PcInstance ply)
		{
			if (ply.getLevel() < 76)
			{
				return 0;
			}
			return _rate[ply.getLevel() - 76];
		}
	}
}