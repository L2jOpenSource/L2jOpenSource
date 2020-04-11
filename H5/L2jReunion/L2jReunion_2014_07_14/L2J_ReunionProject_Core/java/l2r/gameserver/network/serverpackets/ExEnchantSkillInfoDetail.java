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

import l2r.Config;
import l2r.gameserver.datatables.xml.EnchantSkillGroupsData;
import l2r.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import l2r.gameserver.model.L2EnchantSkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;

/**
 * @author KenM
 */
public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
	private static final int TYPE_NORMAL_ENCHANT = 0;
	private static final int TYPE_SAFE_ENCHANT = 1;
	private static final int TYPE_UNTRAIN_ENCHANT = 2;
	private static final int TYPE_CHANGE_ENCHANT = 3;
	
	private int bookId = 0;
	private int reqCount = 0;
	private int multi = 1;
	private final int _type;
	private final int _skillid;
	private final int _skilllvl;
	private final int _chance;
	private int _sp;
	private final int _adenacount;
	
	public ExEnchantSkillInfoDetail(int type, int skillid, int skilllvl, L2PcInstance ply)
	{
		
		L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(skillid);
		EnchantSkillHolder esd = null;
		// do we have this skill?
		if (enchantLearn != null)
		{
			if (skilllvl > 100)
			{
				esd = enchantLearn.getEnchantSkillHolder(skilllvl);
			}
			else
			{
				esd = enchantLearn.getFirstRouteGroup().getEnchantGroupDetails().get(0);
			}
		}
		
		if (esd == null)
		{
			throw new IllegalArgumentException("Skill " + skillid + " dont have enchant data for level " + skilllvl);
		}
		
		if (type == 0)
		{
			multi = EnchantSkillGroupsData.NORMAL_ENCHANT_COST_MULTIPLIER;
		}
		else if (type == 1)
		{
			multi = EnchantSkillGroupsData.SAFE_ENCHANT_COST_MULTIPLIER;
		}
		_chance = esd.getRate(ply);
		_sp = esd.getSpCost();
		if (type == TYPE_UNTRAIN_ENCHANT)
		{
			_sp = (int) (0.8 * _sp);
		}
		_adenacount = esd.getAdenaCost() * multi;
		_type = type;
		_skillid = skillid;
		_skilllvl = skilllvl;
		
		switch (type)
		{
			case TYPE_NORMAL_ENCHANT:
				bookId = EnchantSkillGroupsData.NORMAL_ENCHANT_BOOK;
				reqCount = (((_skilllvl % 100) > 1) ? 0 : 1);
				break;
			case TYPE_SAFE_ENCHANT:
				bookId = EnchantSkillGroupsData.SAFE_ENCHANT_BOOK;
				reqCount = 1;
				break;
			case TYPE_UNTRAIN_ENCHANT:
				bookId = EnchantSkillGroupsData.UNTRAIN_ENCHANT_BOOK;
				reqCount = 1;
				break;
			case TYPE_CHANGE_ENCHANT:
				bookId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK;
				reqCount = 1;
				break;
			default:
				return;
		}
		
		if ((type != TYPE_SAFE_ENCHANT) && !Config.ES_SP_BOOK_NEEDED)
		{
			reqCount = 0;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x5E);
		
		writeD(_type);
		writeD(_skillid);
		writeD(_skilllvl);
		writeD(_sp * multi); // sp
		writeD(_chance); // exp
		writeD(0x02); // items count?
		writeD(Inventory.ADENA_ID); // Adena
		writeD(_adenacount); // Adena count
		writeD(bookId); // ItemId Required
		writeD(reqCount);
	}
}
