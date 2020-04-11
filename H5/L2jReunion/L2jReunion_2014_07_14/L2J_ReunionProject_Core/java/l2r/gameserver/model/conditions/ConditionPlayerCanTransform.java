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

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

/**
 * Player Can Transform condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanTransform extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanTransform(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canTransform = true;
		final L2PcInstance player = env.getPlayer();
		if ((player == null) || player.isAlikeDead() || player.isCursedWeaponEquipped())
		{
			canTransform = false;
		}
		else if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
			canTransform = false;
		}
		else if (player.isTransformed() || player.isInStance())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			canTransform = false;
		}
		else if (player.isInWater())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			canTransform = false;
		}
		else if (player.isFlyingMounted() || player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
			canTransform = false;
		}
		return (_val == canTransform);
	}
}
