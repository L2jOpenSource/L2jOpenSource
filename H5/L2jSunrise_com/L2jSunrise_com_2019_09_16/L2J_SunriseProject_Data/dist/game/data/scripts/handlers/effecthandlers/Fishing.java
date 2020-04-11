/*
 * Copyright (C) 2004-2015 L2J DataPack
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

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.model.zone.type.L2FishingZone;
import l2r.gameserver.model.zone.type.L2WaterZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * Fishing effect implementation.
 * @author UnAfraid
 */
public final class Fishing extends L2Effect
{
	private static final int MIN_BAIT_DISTANCE = 90;
	private static final int MAX_BAIT_DISTANCE = 250;
	
	public Fishing(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FISHING_START;
	}
	
	@Override
	public boolean onStart()
	{
		final L2Character activeChar = getEffector();
		if (!activeChar.isPlayer())
		{
			return false;
		}
		
		final L2PcInstance player = activeChar.getActingPlayer();
		
		if (!Config.ALLOWFISHING && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			player.sendMessage("Fishing is disabled!");
			return false;
		}
		
		if (player.getFishingEx().isFishing())
		{
			if (player.getFishingEx().getFishCombat() != null)
			{
				player.getFishingEx().getFishCombat().doDie(false);
			}
			else
			{
				player.getFishingEx().endFishing(false);
			}
			
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return false;
		}
		
		// check for equiped fishing rod
		L2Weapon equipedWeapon = player.getActiveWeaponItem();
		if (((equipedWeapon == null) || (equipedWeapon.getItemType() != WeaponType.FISHINGROD)))
		{
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return false;
		}
		
		// check for equiped lure
		L2ItemInstance equipedLeftHand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if ((equipedLeftHand == null) || (equipedLeftHand.getItemType() != EtcItemType.LURE))
		{
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return false;
		}
		
		if (!player.isGM())
		{
			if (player.isInBoat())
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
				return false;
			}
			
			if (player.isInCraftMode() || player.isInStoreMode())
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
				return false;
			}
			
			if (player.isInsideZone(ZoneIdType.WATER))
			{
				player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
				return false;
			}
		}
		
		// calculate a position in front of the player with a random distance
		int distance = Rnd.get(MIN_BAIT_DISTANCE, MAX_BAIT_DISTANCE);
		final double angle = Util.convertHeadingToDegree(player.getHeading());
		final double radian = Math.toRadians(angle);
		final double sin = Math.sin(radian);
		final double cos = Math.cos(radian);
		int baitX = (int) (player.getX() + (cos * distance));
		int baitY = (int) (player.getY() + (sin * distance));
		
		// search for fishing and water zone
		L2FishingZone fishingZone = null;
		L2WaterZone waterZone = null;
		for (final L2ZoneType zone : ZoneManager.getInstance().getZones(baitX, baitY))
		{
			if (zone instanceof L2FishingZone)
			{
				fishingZone = (L2FishingZone) zone;
			}
			else if (zone instanceof L2WaterZone)
			{
				waterZone = (L2WaterZone) zone;
			}
			
			if ((fishingZone != null) && (waterZone != null))
			{
				break;
			}
		}
		
		int baitZ = computeBaitZ(player, baitX, baitY, fishingZone, waterZone);
		if (baitZ == Integer.MIN_VALUE)
		{
			for (distance = MAX_BAIT_DISTANCE; distance >= MIN_BAIT_DISTANCE; --distance)
			{
				baitX = (int) (player.getX() + (cos * distance));
				baitY = (int) (player.getY() + (sin * distance));
				
				// search for fishing and water zone again
				fishingZone = null;
				waterZone = null;
				for (final L2ZoneType zone : ZoneManager.getInstance().getZones(baitX, baitY))
				{
					if (zone instanceof L2FishingZone)
					{
						fishingZone = (L2FishingZone) zone;
					}
					else if (zone instanceof L2WaterZone)
					{
						waterZone = (L2WaterZone) zone;
					}
					
					if ((fishingZone != null) && (waterZone != null))
					{
						break;
					}
				}
				
				baitZ = computeBaitZ(player, baitX, baitY, fishingZone, waterZone);
				if (baitZ != Integer.MIN_VALUE)
				{
					break;
				}
			}
			
			if (baitZ == Integer.MIN_VALUE)
			{
				if (player.isGM())
				{
					baitZ = player.getZ();
				}
				else
				{
					player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
					return false;
				}
			}
		}
		
		if (!player.destroyItem("Fishing", equipedLeftHand, 1, null, false))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			return false;
		}
		
		player.getFishingEx().setLure(equipedLeftHand);
		player.getFishingEx().startFishing(baitX, baitY, baitZ);
		return true;
	}
	
	/**
	 * Computes the Z of the bait.
	 * @param player the player
	 * @param baitX the bait x
	 * @param baitY the bait y
	 * @param fishingZone the fishing zone
	 * @param waterZone the water zone
	 * @return the bait z or {@link Integer#MIN_VALUE} when you cannot fish here
	 */
	private static int computeBaitZ(final L2PcInstance player, final int baitX, final int baitY, final L2FishingZone fishingZone, final L2WaterZone waterZone)
	{
		if ((fishingZone == null))
		{
			return Integer.MIN_VALUE;
		}
		
		if ((waterZone == null))
		{
			return Integer.MIN_VALUE;
		}
		
		// always use water zone, fishing zone high z is high in the air...
		int baitZ = waterZone.getWaterZ();
		
		if (!GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), player.getZ(), baitX, baitY, baitZ))
		{
			return Integer.MIN_VALUE;
		}
		
		if (GeoData.getInstance().hasGeo(baitX, baitY))
		{
			if (GeoData.getInstance().getHeight(baitX, baitY, baitZ) > baitZ)
			{
				return Integer.MIN_VALUE;
			}
			
			if (GeoData.getInstance().getHeight(baitX, baitY, player.getZ()) > baitZ)
			{
				return Integer.MIN_VALUE;
			}
		}
		
		return baitZ;
	}
}
