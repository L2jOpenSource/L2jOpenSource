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

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

/**
 * Player Can Untransform condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanUntransform extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanUntransform(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canUntransform = true;
		final L2PcInstance player = env.getPlayer();
		if (player == null)
		{
			canUntransform = false;
		}
		else if (player.isAlikeDead() || player.isCursedWeaponEquipped())
		{
			canUntransform = false;
		}
		else if ((player.isTransformed() || player.isInStance()) && player.isFlyingMounted() && player.isInsideZone(ZoneIdType.LANDING))
		{
			player.sendPacket(SystemMessageId.TOO_HIGH_TO_PERFORM_THIS_ACTION); // TODO: check if message is retail like.
			canUntransform = false;
		}
		return (_val == canUntransform);
	}
}
