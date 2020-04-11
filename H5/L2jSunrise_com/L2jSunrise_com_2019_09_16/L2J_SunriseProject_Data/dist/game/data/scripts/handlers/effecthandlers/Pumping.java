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

import l2r.gameserver.data.xml.impl.FishingRodsData;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.fishing.L2Fishing;
import l2r.gameserver.model.fishing.L2FishingRod;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;

/**
 * Pumping effect implementation.
 * @author UnAfraid
 */
public final class Pumping extends L2Effect
{
	private final double _power;
	
	public Pumping(Env env, EffectTemplate template)
	{
		super(env, template);
		
		if (template.getParameters().getString("power", null) == null)
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": effect without power!");
		}
		_power = template.getParameters().getDouble("power");
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FISHING;
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
		final L2Fishing fish = player.getFishingEx().getFishCombat();
		if (fish == null)
		{
			// Pumping skill is available only while fishing
			player.sendPacket(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		final L2Weapon weaponItem = player.getActiveWeaponItem();
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if ((weaponInst == null) || (weaponItem == null))
		{
			return false;
		}
		int SS = activeChar.isChargedShot(ShotType.FISH_SOULSHOTS) ? 2 : 1;
		int pen = 0;
		
		final L2FishingRod fishingRod = FishingRodsData.getInstance().getFishingRod(weaponItem.getId());
		final double gradeBonus = fishingRod.getFishingRodLevel() * 0.1; // TODO: Check this formula (is guessed)
		int dmg = (int) ((fishingRod.getFishingRodDamage() + player.calcStat(Stats.FISHING_EXPERTISE, 1, null, null) + _power) * gradeBonus * SS);
		// Penalty 5% less damage dealt
		if (player.getSkillLevel(1315) <= (getSkill().getLevel() - 2)) // 1315 - Fish Expertise
		{
			player.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
			pen = (int) (dmg * 0.05);
			dmg = dmg - pen;
		}
		if (SS > 1)
		{
			weaponInst.setChargedShot(ShotType.FISH_SOULSHOTS, false);
		}
		
		fish.usePumping(dmg, pen);
		return true;
	}
}
