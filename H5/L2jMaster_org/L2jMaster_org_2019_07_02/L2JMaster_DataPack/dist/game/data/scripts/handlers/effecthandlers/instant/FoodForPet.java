/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package handlers.effecthandlers.instant;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.MountType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.skills.BuffInfo;

/**
 * Food For Pet effect implementation.
 * @author Adry_85
 * @since 2.6.0.0
 */
public final class FoodForPet extends AbstractEffect
{
	private final int _normal;
	private final int _ride;
	private final int _wyvern;
	
	public FoodForPet(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_normal = params.getInt("normal", 0);
		_ride = params.getInt("ride", 0);
		_wyvern = params.getInt("wyvern", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character activeChar = info.getEffector();
		
		if (activeChar.isPet())
		{
			final L2PetInstance pet = (L2PetInstance) activeChar;
			pet.setCurrentFed(pet.getCurrentFed() + (_normal * Config.PET_FOOD_RATE));
		}
		else if (activeChar.isPlayer())
		{
			final L2PcInstance player = activeChar.getActingPlayer();
			if (player.getMountType() == MountType.WYVERN)
			{
				player.setCurrentFeed(player.getCurrentFeed() + _wyvern);
			}
			else
			{
				player.setCurrentFeed(player.getCurrentFeed() + _ride);
			}
		}
	}
}
