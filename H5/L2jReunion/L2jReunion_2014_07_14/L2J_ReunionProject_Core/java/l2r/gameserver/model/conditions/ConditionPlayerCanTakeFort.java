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

import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;

/**
 * Player Can Take Fort condition implementation.
 * @author Adry_85
 */
public class ConditionPlayerCanTakeFort extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanTakeFort(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canTakeFort = true;
		if ((env.getPlayer() == null) || env.getPlayer().isAlikeDead() || env.getPlayer().isCursedWeaponEquipped() || (env.getPlayer().getClan() == null))
		{
			canTakeFort = false;
		}
		
		final Fort fort = FortManager.getInstance().getFort(env.getPlayer());
		final SystemMessage sm;
		if ((fort == null) || (fort.getResidenceId() <= 0) || !fort.getSiege().isInProgress() || (fort.getSiege().getAttackerClan(env.getPlayer().getClan()) == null))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(env.getSkill());
			env.getPlayer().sendPacket(sm);
			canTakeFort = false;
		}
		else if (fort.getFlagPole() != env.getTarget())
		{
			env.getPlayer().sendPacket(SystemMessageId.INCORRECT_TARGET);
			canTakeFort = false;
		}
		else if (!Util.checkIfInRange(200, env.getPlayer(), env.getTarget(), true))
		{
			env.getPlayer().sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
			canTakeFort = false;
		}
		return (_val == canTakeFort);
	}
}