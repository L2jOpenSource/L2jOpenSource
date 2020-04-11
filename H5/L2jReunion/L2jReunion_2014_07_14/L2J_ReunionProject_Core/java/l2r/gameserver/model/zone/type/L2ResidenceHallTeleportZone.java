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
package l2r.gameserver.model.zone.type;

import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

/**
 * Teleport residence zone for clan hall sieges
 * @author BiggBoss
 */
public class L2ResidenceHallTeleportZone extends L2ResidenceTeleportZone
{
	private int _id;
	private ScheduledFuture<?> _teleTask;
	
	/**
	 * @param id
	 */
	public L2ResidenceHallTeleportZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("residenceZoneId"))
		{
			_id = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	public int getResidenceZoneId()
	{
		return _id;
	}
	
	public synchronized void checkTeleporTask()
	{
		if ((_teleTask == null) || _teleTask.isDone())
		{
			_teleTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), 30000);
		}
	}
	
	protected class TeleportTask implements Runnable
	{
		@Override
		public void run()
		{
			int index = 0;
			if (getSpawns().size() > 1)
			{
				index = Rnd.get(getSpawns().size());
			}
			
			final Location loc = getSpawns().get(index);
			if (loc == null)
			{
				throw new NullPointerException();
			}
			
			for (L2PcInstance pc : getPlayersInside())
			{
				if (pc != null)
				{
					pc.teleToLocation(loc, false);
				}
			}
		}
	}
}
