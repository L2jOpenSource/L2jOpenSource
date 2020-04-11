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
package com.l2jserver.gameserver.model.actor.instance;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Rnd;

/**
 * This class manages all Grand Bosses.
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2GrandBossInstance extends L2MonsterInstance
{
	private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
	private boolean _useRaidCurse = true;
	
	/**
	 * Creates a grand boss.
	 * @param template the grand boss NPC template
	 */
	public L2GrandBossInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2GrandBossInstance);
		setIsRaid(true);
		setLethalable(false);
	}
	
	@Override
	protected int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		L2PcInstance player = null;
		
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}
		
		if (player != null)
		{
			if (Config.ANNOUNCE_GRANDBOSS_ALIVE_KILL)
			{
				if (player.getClan() == null)
				{
					Broadcast.toAllOnlinePlayers("[Grand Boss]: " + getName() + " Was defeated by " + player.getName());
				}
				else
				{
					Broadcast.toAllOnlinePlayers("[Grand Boss]: " + getName() + " Was defeated by " + player.getName() + " Clan: " + player.getClan().getName());
				}
			}
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (member.isNoble())
					{
						Hero.getInstance().setRBkilled(member.getObjectId(), getId());
					}
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getId(), (getLevel() / 2) + Rnd.get(-5, 5));
				if (player.isNoble())
				{
					Hero.getInstance().setRBkilled(player.getObjectId(), getId());
				}
			}
		}
		return true;
	}
	
	@Override
	public float getVitalityPoints(int damage)
	{
		return -super.getVitalityPoints(damage) / 100;
	}
	
	@Override
	public boolean useVitalityRate()
	{
		return false;
	}
	
	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}
	
	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
}