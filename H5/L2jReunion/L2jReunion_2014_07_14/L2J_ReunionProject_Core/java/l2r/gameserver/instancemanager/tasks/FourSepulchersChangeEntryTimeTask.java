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
package l2r.gameserver.instancemanager.tasks;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.FourSepulchersManager;

/**
 * Four Sepulchers change entry time task.
 * @author xban1x
 */
public final class FourSepulchersChangeEntryTimeTask implements Runnable
{
	@Override
	public void run()
	{
		final FourSepulchersManager manager = FourSepulchersManager.getInstance();
		manager.setIsEntryTime(true);
		manager.setIsWarmUpTime(false);
		manager.setIsAttackTime(false);
		manager.setIsCoolDownTime(false);
		
		long interval = 0;
		// if this is first launch - search time whFourSepulchersManager_inEntryTime = true;naFourSepulchersManager_inEntryTime = true;maen entry time will be
		// ended:
		// counting difference between time when entry time ends and current
		// time
		// and then launching change time task
		if (manager.isFirstTimeRun())
		{
			interval = manager.getEntrytTimeEnd() - Calendar.getInstance().getTimeInMillis();
		}
		else
		{
			interval = Config.FS_TIME_ENTRY * 60000L; // else use stupid
			// method
		}
		
		// launching saying process...
		ThreadPoolManager.getInstance().scheduleGeneral(new FourSepulchersManagerSayTask(), 0);
		manager.setChangeWarmUpTimeTask(ThreadPoolManager.getInstance().scheduleEffect(new FourSepulchersChangeWarmUpTimeTask(), interval));
		final ScheduledFuture<?> changeEntryTimeTask = manager.getChangeEntryTimeTask();
		
		if (changeEntryTimeTask != null)
		{
			changeEntryTimeTask.cancel(true);
			manager.setChangeEntryTimeTask(null);
		}
	}
}
