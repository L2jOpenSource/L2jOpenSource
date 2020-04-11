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
package l2r.gameserver.model.skills.l2skills;

import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

public class L2SkillLearnSkill extends L2Skill
{
	private final int[] _learnSkillId;
	private final int[] _learnSkillLvl;
	
	public L2SkillLearnSkill(StatsSet set)
	{
		super(set);
		
		String[] ar = set.getString("learnSkillId", "0").split(",");
		int[] ar2 = new int[ar.length];
		
		for (int i = 0; i < ar.length; i++)
		{
			ar2[i] = Integer.parseInt(ar[i]);
		}
		
		_learnSkillId = ar2;
		
		ar = set.getString("learnSkillLvl", "1").split(",");
		ar2 = new int[_learnSkillId.length];
		
		for (int i = 0; i < _learnSkillId.length; i++)
		{
			ar2[i] = 1;
		}
		
		for (int i = 0; i < ar.length; i++)
		{
			ar2[i] = Integer.parseInt(ar[i]);
		}
		
		_learnSkillLvl = ar2;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final L2PcInstance player = activeChar.getActingPlayer();
		L2Skill newSkill;
		
		for (int i = 0; i < _learnSkillId.length; i++)
		{
			if ((player.getSkillLevel(_learnSkillId[i]) < _learnSkillLvl[i]) && (_learnSkillId[i] != 0))
			{
				newSkill = SkillData.getInstance().getInfo(_learnSkillId[i], _learnSkillLvl[i]);
				if (newSkill != null)
				{
					player.addSkill(newSkill, true);
				}
			}
		}
	}
}