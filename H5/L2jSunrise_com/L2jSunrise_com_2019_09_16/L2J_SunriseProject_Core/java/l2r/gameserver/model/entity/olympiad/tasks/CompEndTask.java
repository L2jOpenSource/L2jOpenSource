package l2r.gameserver.model.entity.olympiad.tasks;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.entity.olympiad.OlympiadGameManager;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class CompEndTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(CompEndTask.class);
	
	@Override
	public void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}
		
		Olympiad.getInstance()._inCompPeriod = false;
		
		// if there are games left, wait for them to finish one more minute
		if (OlympiadGameManager.getInstance().isBattleStarted())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new CompEndTask(), 60000);
			return;
		}
		
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
		_log.info("Olympiad System: Olympiad Game Ended");
		
		if (Olympiad.getInstance()._gameManagerTask != null)
		{
			Olympiad.getInstance()._gameManagerTask.cancel(false);
			Olympiad.getInstance()._gameManagerTask = null;
		}
		
		Olympiad.getInstance().saveOlympiadStatus();
		
		Olympiad.getInstance().init();
	}
}