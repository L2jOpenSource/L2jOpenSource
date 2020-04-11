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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.datatables.xml.EnchantSkillGroupsData;
import l2r.gameserver.model.L2EnchantSkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;

/**
 * Format (ch) ddd c: (id) 0xD0 h: (subid) 0x31 d: type d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private static final String _C_D0_46_REQUESTEXENCHANTSKILLINFO = "[C] D0:46 RequestExEnchantSkillInfoDetail";
	
	private int _type;
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		int reqSkillLvl = -2;
		
		if ((_type == 0) || (_type == 1))
		{
			reqSkillLvl = _skillLvl - 1; // enchant
		}
		else if (_type == 2)
		{
			reqSkillLvl = _skillLvl + 1; // untrain
		}
		else if (_type == 3)
		{
			reqSkillLvl = _skillLvl; // change route
		}
		
		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		
		// dont have such skill
		if (playerSkillLvl == -1)
		{
			return;
		}
		
		// if reqlvl is 100,200,.. check base skill lvl enchant
		if ((reqSkillLvl % 100) == 0)
		{
			L2EnchantSkillLearn esl = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
			if (esl != null)
			{
				// if player dont have min level to enchant
				if (playerSkillLvl != esl.getBaseLevel())
				{
					return;
				}
			}
			// enchant data dont exist?
			else
			{
				return;
			}
		}
		else if (playerSkillLvl != reqSkillLvl)
		{
			// change route is different skill lvl but same enchant
			if ((_type == 3) && ((playerSkillLvl % 100) != (_skillLvl % 100)))
			{
				return;
			}
		}
		
		// send skill enchantment detail
		ExEnchantSkillInfoDetail esd = new ExEnchantSkillInfoDetail(_type, _skillId, _skillLvl, activeChar);
		activeChar.sendPacket(esd);
	}
	
	@Override
	public String getType()
	{
		return _C_D0_46_REQUESTEXENCHANTSKILLINFO;
	}
}
