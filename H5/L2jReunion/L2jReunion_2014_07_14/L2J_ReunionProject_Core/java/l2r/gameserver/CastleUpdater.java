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
package l2r.gameserver;

import l2r.Config;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.itemcontainer.ItemContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class managing periodical events with castle
 * @author Thorgrim - 2005
 */
public class CastleUpdater implements Runnable
{
	protected static final Logger _log = LoggerFactory.getLogger(CastleUpdater.class);
	private final L2Clan _clan;
	private int _runCount = 0;
	
	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}
	
	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if ((warehouse != null) && (_clan.getCastleId() > 0))
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getCastleId());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if ((_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE) == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						if (Config.DEBUG)
						{
							_log.info("Manor System: all data for " + castle.getName() + " saved");
						}
					}
				}
				CastleUpdater cu = new CastleUpdater(_clan, ++_runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
	}
}
