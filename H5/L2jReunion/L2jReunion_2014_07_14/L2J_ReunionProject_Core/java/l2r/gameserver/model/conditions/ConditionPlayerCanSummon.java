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

import l2r.Config;
import l2r.gameserver.datatables.sql.CharSummonTable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

/**
 * Player Can Summon condition implementation.
 * @author Zoey76
 */
public class ConditionPlayerCanSummon extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanSummon(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2PcInstance player = env.getPlayer();
		if (player == null)
		{
			return false;
		}
		
		boolean canSummon = true;
		if (Config.RESTORE_SERVITOR_ON_RECONNECT && CharSummonTable.getInstance().getServitors().containsKey(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			canSummon = false;
		}
		else if (Config.RESTORE_PET_ON_RECONNECT && CharSummonTable.getInstance().getPets().containsKey(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			canSummon = false;
		}
		else if (player.hasSummon())
		{
			player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			canSummon = false;
		}
		else if (player.isFlyingMounted() || player.isMounted())
		{
			canSummon = false;
		}
		return (_val == canSummon);
	}
}
