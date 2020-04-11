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

import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.PetItemList;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

/**
 * @author Nemesiss
 */
public class L2SkillCreateItem extends L2Skill
{
	private final int[] _createItemId;
	private final int _createItemCount;
	private final int _randomCount;
	
	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getIntegerArray("create_item_id", ";");
		_createItemCount = set.getInteger("create_item_count", 0);
		_randomCount = set.getInteger("random_count", 1);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		L2PcInstance player = activeChar.getActingPlayer();
		if (activeChar.isAlikeDead())
		{
			return;
		}
		if (activeChar.isPlayable())
		{
			if ((_createItemId == null) || (_createItemCount == 0))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(this);
				player.sendPacket(sm);
				return;
			}
			
			int count = _createItemCount + Rnd.nextInt(_randomCount);
			int rndid = Rnd.nextInt(_createItemId.length);
			if (activeChar.isPlayer())
			{
				player.addItem("Skill", _createItemId[rndid], count, activeChar, true);
			}
			else if (activeChar.isPet())
			{
				activeChar.getInventory().addItem("Skill", _createItemId[rndid], count, player, activeChar);
				player.sendPacket(new PetItemList(activeChar.getInventory().getItems()));
			}
		}
	}
}
