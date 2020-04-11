package com.l2jfrozen.gameserver.managers;

import com.l2jfrozen.Config;

public class Manager
{
	public static void reloadAll()
	{
		AuctionManager.getInstance().reload();
		
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			QuestManager.getInstance();
			QuestManager.reload();
		}
		
	}
}
