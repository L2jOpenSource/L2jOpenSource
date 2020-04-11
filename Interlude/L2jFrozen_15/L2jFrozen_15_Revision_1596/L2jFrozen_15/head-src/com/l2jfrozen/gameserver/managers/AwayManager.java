package com.l2jfrozen.gameserver.managers;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SetupGauge;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Michiru
 */
public final class AwayManager
{
	protected static final Logger LOGGER = Logger.getLogger(AwayManager.class);
	private static AwayManager instance;
	protected Map<L2PcInstance, RestoreData> awayPlayers;
	
	public static final AwayManager getInstance()
	{
		if (instance == null)
		{
			instance = new AwayManager();
			LOGGER.info("AwayManager: initialized.");
		}
		return instance;
	}
	
	private final class RestoreData
	{
		private final String originalTitle;
		private final int originalTitleColor;
		private final boolean sitForced;
		
		public RestoreData(final L2PcInstance activeChar)
		{
			originalTitle = activeChar.getTitle();
			originalTitleColor = activeChar.getAppearance().getTitleColor();
			sitForced = !activeChar.isSitting();
		}
		
		public boolean isSitForced()
		{
			return sitForced;
		}
		
		public void restore(final L2PcInstance activeChar)
		{
			activeChar.getAppearance().setTitleColor(originalTitleColor);
			activeChar.setTitle(originalTitle);
		}
	}
	
	private AwayManager()
	{
		awayPlayers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RestoreData>());
	}
	
	/**
	 * @param activeChar
	 * @param text
	 */
	public void setAway(final L2PcInstance activeChar, final String text)
	{
		activeChar.set_awaying(true);
		activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 9));
		activeChar.sendMessage("Your status is Away in " + Config.AWAY_TIMER + " Sec.");
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.AWAY_TIMER * 1000);
		activeChar.sendPacket(sg);
		sg = null;
		activeChar.setIsImobilised(true);
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerAwayTask(activeChar, text), Config.AWAY_TIMER * 1000);
	}
	
	/**
	 * @param activeChar
	 */
	public void setBack(final L2PcInstance activeChar)
	{
		activeChar.sendMessage("You are back from Away Status in " + Config.BACK_TIMER + " Sec.");
		SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.BACK_TIMER * 1000);
		activeChar.sendPacket(sg);
		sg = null;
		ThreadPoolManager.getInstance().scheduleGeneral(new setPlayerBackTask(activeChar), Config.BACK_TIMER * 1000);
	}
	
	public void extraBack(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		RestoreData rd = awayPlayers.get(activeChar);
		if (rd == null)
		{
			return;
		}
		
		rd.restore(activeChar);
		rd = null;
		awayPlayers.remove(activeChar);
	}
	
	class setPlayerAwayTask implements Runnable
	{
		
		private final L2PcInstance activeChar;
		private final String awayText;
		
		setPlayerAwayTask(final L2PcInstance activeChar, final String awayText)
		{
			this.activeChar = activeChar;
			this.awayText = awayText;
		}
		
		@Override
		public void run()
		{
			if (activeChar == null)
			{
				return;
			}
			if (activeChar.isAttackingNow() || activeChar.isCastingNow())
			{
				return;
			}
			
			awayPlayers.put(activeChar, new RestoreData(activeChar));
			
			activeChar.disableAllSkills();
			activeChar.abortAttack();
			activeChar.abortCast();
			activeChar.setTarget(null);
			activeChar.setIsImobilised(false);
			if (!activeChar.isSitting())
			{
				activeChar.sitDown();
			}
			if (awayText.length() <= 1)
			{
				activeChar.sendMessage("You are now *Away*");
			}
			else
			{
				activeChar.sendMessage("You are now Away *" + awayText + "*");
			}
			
			activeChar.getAppearance().setTitleColor(Config.AWAY_TITLE_COLOR);
			
			if (awayText.length() <= 1)
			{
				activeChar.setTitle("*Away*");
			}
			else
			{
				activeChar.setTitle("Away*" + awayText + "*");
			}
			
			activeChar.broadcastUserInfo();
			activeChar.setIsParalyzed(true);
			activeChar.setIsAway(true);
			activeChar.set_awaying(false);
		}
	}
	
	class setPlayerBackTask implements Runnable
	{
		
		private final L2PcInstance activeChar;
		
		setPlayerBackTask(final L2PcInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (activeChar == null)
			{
				return;
			}
			RestoreData rd = awayPlayers.get(activeChar);
			
			if (rd == null)
			{
				return;
			}
			
			activeChar.setIsParalyzed(false);
			activeChar.enableAllSkills();
			activeChar.setIsAway(false);
			
			if (rd.isSitForced())
			{
				activeChar.standUp();
			}
			
			rd.restore(activeChar);
			rd = null;
			awayPlayers.remove(activeChar);
			activeChar.broadcastUserInfo();
			activeChar.sendMessage("You are Back now!");
		}
	}
}
