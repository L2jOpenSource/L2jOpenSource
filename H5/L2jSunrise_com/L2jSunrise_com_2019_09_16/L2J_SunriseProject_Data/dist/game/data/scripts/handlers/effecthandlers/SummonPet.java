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
package handlers.effecthandlers;

import l2r.gameserver.GeoData;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.PetData;
import l2r.gameserver.model.L2PetData;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.PetItemHolder;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.PetItemList;

/**
 * @author UnAfraid
 */
public class SummonPet extends L2Effect
{
	public SummonPet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SUMMON_PET;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}
		
		final L2PcInstance player = getEffector().getActingPlayer();
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		
		if ((player.hasSummon() || player.isMounted()))
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		
		final PetItemHolder holder = player.removeScript(PetItemHolder.class);
		if (holder == null)
		{
			_log.warn("Summoning pet without attaching PetItemHandler!", new Throwable());
			return false;
		}
		
		final L2ItemInstance item = holder.getItem();
		if (player.getInventory().getItemByObjectId(item.getObjectId()) != item)
		{
			_log.warn("Player: " + player + " is trying to summon pet from item that he doesn't owns.");
			return false;
		}
		
		final L2PetData petData = PetData.getInstance().getPetDataByItemId(item.getId());
		if ((petData == null) || (petData.getNpcId() == -1))
		{
			return false;
		}
		
		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(petData.getNpcId());
		final L2PetInstance pet = L2PetInstance.spawnPet(npcTemplate, player, item);
		if (pet == null)
		{
			return false;
		}
		
		pet.setShowSummonAnimation(true);
		if (!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp());
			pet.setCurrentMp(pet.getMaxMp());
			pet.getStat().setExp(pet.getExpForThisLevel());
			pet.setCurrentFed(pet.getMaxFed());
		}
		
		pet.setRunning();
		
		if (!pet.isRespawned())
		{
			pet.store();
		}
		
		item.setEnchantLevel(pet.getLevel());
		player.setPet(pet);
		
		final Location loc = GeoData.getInstance().moveCheck(player, new Location(player.getX() + 20, player.getY() + 20, player.getZ()));
		pet.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		
		pet.startFeed();
		pet.setFollowStatus(true);
		pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
		pet.broadcastStatusUpdate();
		return true;
	}
}
