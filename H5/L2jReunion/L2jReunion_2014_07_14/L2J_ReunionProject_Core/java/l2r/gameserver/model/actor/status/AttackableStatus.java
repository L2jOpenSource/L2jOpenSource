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
package l2r.gameserver.model.actor.status;

import java.util.Collection;

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.AbstractNpcInfo;

public class AttackableStatus extends NpcStatus
{
	public AttackableStatus(L2Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		if (value > 0)
		{
			if (getActiveChar().isOverhit())
			{
				getActiveChar().setOverhitValues(attacker, value);
			}
			else
			{
				getActiveChar().overhitEnabled(false);
			}
		}
		else
		{
			getActiveChar().overhitEnabled(false);
		}
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
		
		if (!getActiveChar().isDead())
		{
			// And the attacker's hit didn't kill the mob, clear the over-hit flag
			getActiveChar().overhitEnabled(false);
		}
	}
	
	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}
	
	@Override
	public boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		super.setCurrentHp(newHp, broadcastPacket);
		
		if (getActiveChar().getFakePc() != null)
		{
			Collection<L2PcInstance> plrs = getActiveChar().getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if (player != null)
				{
					player.sendPacket(new AbstractNpcInfo.NpcInfo(getActiveChar(), player));
				}
			}
		}
		return true;
	}
}