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
 * Player Call Pc condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCallPc extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCallPc(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canCallPlayer = true;
		final L2PcInstance player = env.getPlayer();
		if (player == null)
		{
			canCallPlayer = false;
		}
		else if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			canCallPlayer = false;
		}
		else if (player.inObserverMode())
		{
			canCallPlayer = false;
		}
		else if (player.isInsideZone(ZoneIdType.NO_SUMMON_FRIEND) || player.isInsideZone(ZoneIdType.JAIL) || player.isFlyingMounted())
		{
			player.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			canCallPlayer = false;
		}
		return (_val == canCallPlayer);
	}
}