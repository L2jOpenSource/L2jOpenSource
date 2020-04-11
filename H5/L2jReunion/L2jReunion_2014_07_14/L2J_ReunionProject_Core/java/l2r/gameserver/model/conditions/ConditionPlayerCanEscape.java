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
package l2r.gameserver.model.conditions;

import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;

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
	public boolean testImpl(Env env)
	{
		boolean canTeleport = true;
		final L2PcInstance player = env.getPlayer();
		if (player == null)
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
		return (_val == canTeleport);
	}
}