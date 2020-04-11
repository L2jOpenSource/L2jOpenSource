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
package l2r.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import l2r.L2DatabaseFactory;
import l2r.gameserver.enums.SiegeClanType;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.ClanHall;
import l2r.gameserver.model.zone.type.L2SiegableHallZone;
import l2r.gameserver.model.zone.type.L2SiegeZone;
import l2r.gameserver.network.serverpackets.SiegeInfo;

/**
 * @author BiggBoss
 */
public final class SiegableHall extends ClanHall
{
	private static final String SQL_SAVE = "UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE clanHallId=?";
	
	private Calendar _nextSiege;
	private final long _siegeLength;
	private final int[] _scheduleConfig =
	{
		7,
		0,
		0,
		12,
		0
	};
	
	private SiegeStatus _status = SiegeStatus.REGISTERING;
	private L2SiegeZone _siegeZone;
	
	private ClanHallSiegeEngine _siege;
	
	public SiegableHall(StatsSet set)
	{
		super(set);
		_siegeLength = set.getLong("siegeLenght");
		String[] rawSchConfig = set.getString("scheduleConfig").split(";");
		if (rawSchConfig.length == 5)
		{
			for (int i = 0; i < 5; i++)
			{
				try
				{
					_scheduleConfig[i] = Integer.parseInt(rawSchConfig[i]);
				}
				catch (Exception e)
				{
					_log.warn("SiegableHall - " + getName() + ": Wrong schedule_config parameters!");
				}
			}
		}
		else
		{
			_log.warn(getName() + ": Wrong schedule_config value in siegable_halls table, using default (7 days)");
		}
		
		_nextSiege = Calendar.getInstance();
		long nextSiege = set.getLong("nextSiege");
		if ((nextSiege - System.currentTimeMillis()) < 0)
		{
			updateNextSiege();
		}
		else
		{
			_nextSiege.setTimeInMillis(nextSiege);
		}
	}
	
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	public void spawnDoor(boolean isDoorWeak)
	{
		for (L2DoorInstance door : getDoors())
		{
			if (door.isDead())
			{
				door.doRevive();
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
			}
			
			if (door.getOpen())
			{
				door.closeMe();
			}
		}
	}
	
	@Override
	public final void updateDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_SAVE))
		{
			statement.setInt(1, getOwnerId());
			statement.setLong(2, getNextSiegeTime());
			statement.setInt(3, getId());
			statement.execute();
		}
		catch (Exception e)
		{
			_log.warn("Exception: SiegableHall.updateDb(): " + e.getMessage(), e);
		}
	}
	
	public final void setSiege(final ClanHallSiegeEngine siegable)
	{
		_siege = siegable;
		_siegeZone.setSiegeInstance(siegable);
	}
	
	public final ClanHallSiegeEngine getSiege()
	{
		return _siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return _nextSiege;
	}
	
	public final long getNextSiegeTime()
	{
		return _nextSiege.getTimeInMillis();
	}
	
	public long getSiegeLenght()
	{
		return _siegeLength;
	}
	
	public final void setNextSiegeDate(long date)
	{
		_nextSiege.setTimeInMillis(date);
	}
	
	public final void setNextSiegeDate(final Calendar c)
	{
		_nextSiege = c;
	}
	
	public final void updateNextSiege()
	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, _scheduleConfig[0]);
		c.add(Calendar.MONTH, _scheduleConfig[1]);
		c.add(Calendar.YEAR, _scheduleConfig[2]);
		c.set(Calendar.HOUR_OF_DAY, _scheduleConfig[3]);
		c.set(Calendar.MINUTE, _scheduleConfig[4]);
		c.set(Calendar.SECOND, 0);
		setNextSiegeDate(c);
		updateDb();
	}
	
	public final void addAttacker(final L2Clan clan)
	{
		if (getSiege() != null)
		{
			getSiege().getAttackers().put(clan.getId(), new L2SiegeClan(clan.getId(), SiegeClanType.ATTACKER));
		}
	}
	
	public final void removeAttacker(final L2Clan clan)
	{
		if (getSiege() != null)
		{
			getSiege().getAttackers().remove(clan.getId());
		}
	}
	
	public final boolean isRegistered(L2Clan clan)
	{
		if (getSiege() == null)
		{
			return false;
		}
		
		return getSiege().checkIsAttacker(clan);
	}
	
	public SiegeStatus getSiegeStatus()
	{
		return _status;
	}
	
	public final boolean isRegistering()
	{
		return _status == SiegeStatus.REGISTERING;
	}
	
	public final boolean isInSiege()
	{
		return _status == SiegeStatus.RUNNING;
	}
	
	public final boolean isWaitingBattle()
	{
		return _status == SiegeStatus.WAITING_BATTLE;
	}
	
	public final void updateSiegeStatus(SiegeStatus status)
	{
		_status = status;
	}
	
	public final L2SiegeZone getSiegeZone()
	{
		return _siegeZone;
	}
	
	public final void setSiegeZone(L2SiegeZone zone)
	{
		_siegeZone = zone;
	}
	
	public final void updateSiegeZone(boolean active)
	{
		_siegeZone.setIsActive(active);
	}
	
	public final void showSiegeInfo(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(this));
	}
	
	@Override
	public final boolean isSiegableHall()
	{
		return true;
	}
	
	@Override
	public L2SiegableHallZone getZone()
	{
		return (L2SiegableHallZone) super.getZone();
	}
}
