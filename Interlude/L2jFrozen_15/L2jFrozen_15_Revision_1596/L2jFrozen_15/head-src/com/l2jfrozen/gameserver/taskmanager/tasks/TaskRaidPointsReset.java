package com.l2jfrozen.gameserver.taskmanager.tasks;

import java.util.Calendar;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.RaidBossPointsManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.taskmanager.Task;
import com.l2jfrozen.gameserver.taskmanager.TaskManager;
import com.l2jfrozen.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jfrozen.gameserver.taskmanager.TaskTypes;

public class TaskRaidPointsReset extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskRaidPointsReset.class);
	public static final String NAME = "raid_points_reset";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(final ExecutedTask task)
	{
		String playerName = "";
		final Calendar cal = Calendar.getInstance();
		
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			// reward clan reputation points
			final Map<Integer, Integer> rankList = RaidBossPointsManager.getRankList();
			for (final L2Clan c : ClanTable.getInstance().getClans())
			{
				for (final Map.Entry<Integer, Integer> entry : rankList.entrySet())
				{
					final L2Object obj = L2World.getInstance().findObject(entry.getKey());
					if (obj instanceof L2PcInstance)
					{
						playerName = ((L2PcInstance) obj).getName();
					}
					if (entry.getValue() <= 100 && c.isMember(playerName))
					{
						int reputation = 0;
						switch (entry.getValue())
						{
							case 1:
								reputation = Config.RAID_RANKING_1ST;
								break;
							case 2:
								reputation = Config.RAID_RANKING_2ND;
								break;
							case 3:
								reputation = Config.RAID_RANKING_3RD;
								break;
							case 4:
								reputation = Config.RAID_RANKING_4TH;
								break;
							case 5:
								reputation = Config.RAID_RANKING_5TH;
								break;
							case 6:
								reputation = Config.RAID_RANKING_6TH;
								break;
							case 7:
								reputation = Config.RAID_RANKING_7TH;
								break;
							case 8:
								reputation = Config.RAID_RANKING_8TH;
								break;
							case 9:
								reputation = Config.RAID_RANKING_9TH;
								break;
							case 10:
								reputation = Config.RAID_RANKING_10TH;
								break;
							default:
								if (entry.getValue() <= 50)
								{
									reputation = Config.RAID_RANKING_UP_TO_50TH;
								}
								else
								{
									reputation = Config.RAID_RANKING_UP_TO_100TH;
								}
								break;
						}
						c.setReputationScore(c.getReputationScore() + reputation, true);
					}
				}
			}
			
			RaidBossPointsManager.cleanUp();
			LOGGER.info("[GlobalTask] Raid Points Reset launched.");
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:10:00", "");
	}
}