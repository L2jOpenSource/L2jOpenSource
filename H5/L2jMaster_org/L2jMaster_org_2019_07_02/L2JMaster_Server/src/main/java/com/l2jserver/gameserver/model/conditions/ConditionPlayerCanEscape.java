/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.conditions;

import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.KrateisCubeManager;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Player Can Escape condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanEscape extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanEscape(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		boolean canTeleport = true;
		final L2PcInstance player = effector.getActingPlayer();
		if (player == null)
		{
			canTeleport = false;
		}
		else if (!TvTEvent.onEscapeUse(player.getObjectId()))
		{
			canTeleport = false;
		}
		else if (player.isInDuel())
		{
			canTeleport = false;
		}
		else if (player.isAfraid())
		{
			canTeleport = false;
		}
		else if (player.isCombatFlagEquipped())
		{
			canTeleport = false;
		}
		else if (player.isFlying() || player.isFlyingMounted())
		{
			canTeleport = false;
		}
		else if (player.isInOlympiadMode())
		{
			canTeleport = false;
		}
		else if ((GrandBossManager.getInstance().getZone(player) != null) && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
		{
			canTeleport = false;
		}
		else if (KrateisCubeManager.checkIsInsided(player))
		{
			canTeleport = false;
		}
		return (_val == canTeleport);
	}
}