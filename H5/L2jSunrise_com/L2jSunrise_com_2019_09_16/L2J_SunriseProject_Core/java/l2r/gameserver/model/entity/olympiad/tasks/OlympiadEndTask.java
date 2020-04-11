package l2r.gameserver.model.entity.olympiad.tasks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class OlympiadEndTask implements Runnable
{
	protected static final Logger _log = LoggerFactory.getLogger(OlympiadEndTask.class);
	
	private final List<StatsSet> _herosToBe;
	
	public OlympiadEndTask(List<StatsSet> herosToBe)
	{
		_herosToBe = herosToBe;
	}
	
	@Override
	public void run()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
		sm.addInt(Olympiad.getInstance().getCurrentCycle());
		
		Broadcast.toAllOnlinePlayers(sm);
		Broadcast.toAllOnlinePlayers("Olympiad Validation Period has began");
		_log.info("Olympiad System: Olympiad Validation Period has began");
		
		if (Olympiad.getInstance()._scheduledWeeklyTask != null)
		{
			Olympiad.getInstance()._scheduledWeeklyTask.cancel(true);
		}
		
		Olympiad.getInstance().saveNobleData();
		
		Olympiad.getInstance().setPeriod(1);
		Olympiad.getInstance().sortHerosToBe();
		Hero.getInstance().resetData();
		Hero.getInstance().computeNewHeroes(_herosToBe);
		
		Olympiad.getInstance().saveOlympiadStatus();
		Olympiad.getInstance().updateMonthlyData();
		
		Calendar validationEnd = Calendar.getInstance();
		Olympiad.getInstance();
		Olympiad.getInstance()._validationEnd = validationEnd.getTimeInMillis() + Olympiad.VALIDATION_PERIOD;
		
		Olympiad.getInstance().loadNoblesRank();
		Olympiad.getInstance()._scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), Olympiad.getInstance().getMillisToValidationEnd());
		
		final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		_log.info("Olympiad System: Validation Period ends at " + format.format(Olympiad.getInstance().getMillisToValidationEnd() + System.currentTimeMillis()));
	}
}