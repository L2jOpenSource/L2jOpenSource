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

public final class SkillList extends L2GameServerPacket
{
	private final List<Skill> _skills = new ArrayList<>();
	
	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
		}
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted)
	{
		_skills.add(new Skill(id, level, passive, disabled, enchanted));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5F);
		writeD(_skills.size());
		
		for (Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeC(temp.disabled ? 1 : 0);
			writeC(temp.enchanted ? 1 : 0);
		}
	}
}
