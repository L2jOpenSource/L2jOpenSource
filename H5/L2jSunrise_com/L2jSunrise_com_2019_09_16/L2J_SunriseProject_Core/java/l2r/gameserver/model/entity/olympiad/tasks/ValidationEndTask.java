package l2r.gameserver.model.entity.olympiad.tasks;

import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class ValidationEndTask implements Runnable
{
	protected static final Logger _log = LoggerFactory.getLogger(ValidationEndTask.class);
	
	@Override
	public void run()
	{
		Broadcast.toAllOnlinePlayers("Olympiad Validation Period has ended");
		_log.info("Olympiad System: Olympiad Validation Period has ended");
		Olympiad.getInstance().setPeriod(0);
		Olympiad.getInstance().increaseCycle();
		Olympiad.getInstance().deleteNobles();
		Olympiad.getInstance().setNewOlympiadEnd();
		Olympiad.getInstance().init();
	}
}