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
public class CompStartTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(CompStartTask.class);
	
	@Override
	public void run()
	{
		if (Olympiad.getInstance().isOlympiadEnd())
		{
			return;
		}
		
		Olympiad.getInstance()._inCompPeriod = true;
		
		Olympiad.getInstance()._gameManagerTask = ThreadPoolManager.getInstance().scheduleGeneral(OlympiadGameManager.getInstance(), 30000);
		
		long regEnd = Olympiad.getInstance().getMillisToCompEnd() - 600000;
		if (regEnd > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(() -> Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)), regEnd);
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new CompEndTask(), Olympiad.getInstance().getMillisToCompEnd());
		
		Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
		_log.info("Olympiad System: Olympiad Game Started");
	}
}