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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Target My Party Except Me condition implementation.
 * @author Adry_85
 */
public class ConditionTargetMyPartyExceptMe extends Condition
{
	private final boolean _val;
	
	public ConditionTargetMyPartyExceptMe(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean isPartyMember = true;
		final L2PcInstance player = env.getPlayer();
		final L2Character target = env.getTarget();
		if ((player == null) || (target == null) || !target.isPlayer())
		{
			isPartyMember = false;
		}
		else if (player == target)
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			isPartyMember = false;
		}
		else if (!player.isInParty() || !player.getParty().equals(target.getParty()))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(env.getSkill());
			player.sendPacket(sm);
			isPartyMember = false;
		}
		return (_val == isPartyMember);
	}
}