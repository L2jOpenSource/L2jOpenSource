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

import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;

/**
 * Player Can Take Castle condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanTakeCastle extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanTakeCastle(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canTakeCastle = true;
		if ((env.getPlayer() == null) || env.getPlayer().isAlikeDead() || env.getPlayer().isCursedWeaponEquipped())
		{
			canTakeCastle = false;
		}
		else if ((env.getPlayer().getClan() == null) || (env.getPlayer().getClan().getLeaderId() != env.getPlayer().getObjectId()))
		{
			canTakeCastle = false;
		}
		
		Castle castle = CastleManager.getInstance().getCastle(env.getPlayer());
		SystemMessage sm;
		if ((castle == null) || (castle.getResidenceId() <= 0) || !castle.getSiege().isInProgress() || (castle.getSiege().getAttackerClan(env.getPlayer().getClan()) == null))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(env.getSkill());
			env.getPlayer().sendPacket(sm);
			canTakeCastle = false;
		}
		else if (!castle.getArtefacts().contains(env.getTarget()))
		{
			env.getPlayer().sendPacket(SystemMessageId.INCORRECT_TARGET);
			canTakeCastle = false;
		}
		else if (!Util.checkIfInRange(env.getSkill().getCastRange(), env.getPlayer(), env.getTarget(), true))
		{
			env.getPlayer().sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
			canTakeCastle = false;
		}
		return (_val == canTakeCastle);
	}
}