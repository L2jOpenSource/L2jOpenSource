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
package l2r.gameserver.scripts.handlers.itemhandlers;

import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2ChestInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;

/**
 * @author l3x
 */
public class Seed implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		if (CastleManorManager.getInstance().isDisabled())
		{
			return false;
		}
		
		final L2Object tgt = playable.getTarget();
		if (!(tgt instanceof L2Npc))
		{
			playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (!(tgt instanceof L2MonsterInstance) || (tgt instanceof L2ChestInstance) || ((L2Character) tgt).isRaid())
		{
			playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final L2MonsterInstance target = (L2MonsterInstance) tgt;
		if (target.isDead())
		{
			playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (target.isSeeded())
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final int seedId = item.getId();
		if (!areaValid(seedId, MapRegionManager.getInstance().getAreaCastle(playable)))
		{
			playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
			return false;
		}
		
		target.setSeeded(seedId, (L2PcInstance) playable);
		final SkillHolder[] skills = item.getItem().getSkills();
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (skills != null)
		{
			for (SkillHolder sk : skills)
			{
				activeChar.useMagic(sk.getSkill(), false, false);
			}
		}
		return true;
	}
	
	/**
	 * @param seedId
	 * @param castleId
	 * @return
	 */
	private boolean areaValid(int seedId, int castleId)
	{
		return (ManorData.getInstance().getCastleIdForSeed(seedId) == castleId);
	}
}