package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

public final class SevenSignsUpdate extends ScheduledQuest
{
	public SevenSignsUpdate()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		if (!SevenSignsManager.getInstance().isSealValidationPeriod())
			FestivalOfDarknessManager.getInstance().saveFestivalData(false);
		
		SevenSignsManager.getInstance().saveSevenSignsData();
		SevenSignsManager.getInstance().saveSevenSignsStatus();
	}
	
	@Override
	public final void onEnd()
	{
	}
}