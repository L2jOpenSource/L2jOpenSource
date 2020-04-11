
/*
 *  Author: Qwerty, Scoria dev.
 *  v 2.1
 */
package com.l2jfrozen.gameserver.model.entity.siege.clanhalls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class FortressOfResistance
{
	private static final Logger LOGGER = Logger.getLogger(FortressOfResistance.class);
	private static final String UPDATE_CLAN_HALL_FORTRESS_OF_RESISTANCE = "UPDATE clanhall SET paidUntil=?, paid=? WHERE id=?";
	private static FortressOfResistance instance;
	private final Map<Integer, DamageInfo> clansDamageInfo;
	
	private static int START_DAY = 1;
	private static int HOUR = Config.PARTISAN_HOUR;
	private static int MINUTES = Config.PARTISAN_MINUTES;
	
	private static final int BOSS_ID = 35368;
	private static final int MESSENGER_ID = 35382;
	
	private ScheduledFuture<?> nurka;
	private ScheduledFuture<?> announce;
	
	private final Calendar capturetime = Calendar.getInstance();
	
	public static FortressOfResistance getInstance()
	{
		if (instance == null)
		{
			instance = new FortressOfResistance();
		}
		return instance;
	}
	
	protected class DamageInfo
	{
		public L2Clan clan;
		public long damage;
	}
	
	private FortressOfResistance()
	{
		if (Config.PARTISAN_DAY == 1)
		{
			START_DAY = Calendar.MONDAY;
		}
		else if (Config.PARTISAN_DAY == 2)
		{
			START_DAY = Calendar.TUESDAY;
		}
		else if (Config.PARTISAN_DAY == 3)
		{
			START_DAY = Calendar.WEDNESDAY;
		}
		else if (Config.PARTISAN_DAY == 4)
		{
			START_DAY = Calendar.THURSDAY;
		}
		else if (Config.PARTISAN_DAY == 5)
		{
			START_DAY = Calendar.FRIDAY;
		}
		else if (Config.PARTISAN_DAY == 6)
		{
			START_DAY = Calendar.SATURDAY;
		}
		else if (Config.PARTISAN_DAY == 7)
		{
			START_DAY = Calendar.SUNDAY;
		}
		else
		{
			START_DAY = Calendar.FRIDAY;
		}
		
		if (HOUR < 0 || HOUR > 23)
		{
			HOUR = 21;
		}
		if (MINUTES < 0 || MINUTES > 59)
		{
			MINUTES = 0;
		}
		
		clansDamageInfo = new HashMap<>();
		
		/*
		 * synchronized (this) { setCalendarForNextCaprture(); long milliToCapture = getMilliToCapture(); RunMessengerSpawn rms = new RunMessengerSpawn(); ThreadPoolManager.getInstance().scheduleGeneral(rms, milliToCapture); LOGGER.info("Fortress of Resistanse: " + milliToCapture / 1000 +
		 * " sec. to capture"); }
		 */
		synchronized (this)
		{
			setCalendarForNextCaprture();
			final long milliToCapture = getMilliToCapture();
			
			RunMessengerSpawn rms = new RunMessengerSpawn();
			ThreadPoolManager.getInstance().scheduleGeneral(rms, milliToCapture);
			
			final long total_millis = System.currentTimeMillis() + milliToCapture;
			
			final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
			cal.setTimeInMillis(total_millis);
			final String next_ch_siege_date = DateFormat.getInstance().format(cal.getTime());
			
			LOGGER.info("Fortress of Resistanse: siege will start the " + next_ch_siege_date);
			rms = null;
		}
	}
	
	private void setCalendarForNextCaprture()
	{
		int daysToChange = getDaysToCapture();
		
		if (daysToChange == 7)
		{
			if (capturetime.get(Calendar.HOUR_OF_DAY) < HOUR)
			{
				daysToChange = 0;
			}
			else if (capturetime.get(Calendar.HOUR_OF_DAY) == HOUR && capturetime.get(Calendar.MINUTE) < MINUTES)
			{
				daysToChange = 0;
			}
		}
		
		if (daysToChange > 0)
		{
			capturetime.add(Calendar.DATE, daysToChange);
		}
		
		capturetime.set(Calendar.HOUR_OF_DAY, HOUR);
		capturetime.set(Calendar.MINUTE, MINUTES);
	}
	
	private int getDaysToCapture()
	{
		final int numDays = capturetime.get(Calendar.DAY_OF_WEEK) - START_DAY;
		
		if (numDays < 0)
		{
			return 0 - numDays;
		}
		
		return 7 - numDays;
	}
	
	private long getMilliToCapture()
	{
		final long currTimeMillis = System.currentTimeMillis();
		final long captureTimeMillis = capturetime.getTimeInMillis();
		
		return captureTimeMillis - currTimeMillis;
	}
	
	protected class RunMessengerSpawn implements Runnable
	{
		@Override
		public void run()
		{
			MessengerSpawn();
		}
	}
	
	public void MessengerSpawn()
	{
		if (!ClanHallManager.getInstance().isFree(21))
		{
			ClanHallManager.getInstance().setFree(21);
		}
		
		Announce("Capture registration of Partisan Hideout has begun!");
		Announce("Now its open for 1 hours!");
		
		L2NpcInstance result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(MESSENGER_ID);
			
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(50335);
			spawn.setLocy(111275);
			spawn.setLocz(-1970);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			template = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		final RunBossSpawn rbs = new RunBossSpawn();
		ThreadPoolManager.getInstance().scheduleGeneral(rbs, 3600000); // 60 * 60 * 1000
		LOGGER.info("Fortress of Resistanse: Messenger spawned!");
		ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000); // 60 * 60 * 1000
	}
	
	protected class RunBossSpawn implements Runnable
	{
		@Override
		public void run()
		{
			BossSpawn();
		}
	}
	
	public void BossSpawn()
	{
		if (!clansDamageInfo.isEmpty())
		{
			clansDamageInfo.clear();
		}
		
		L2NpcInstance result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(BOSS_ID);
			
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(44525);
			spawn.setLocy(108867);
			spawn.setLocz(-2020);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			template = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		LOGGER.info("Fortress of Resistanse: Boss spawned!");
		Announce("Capture of Partisan Hideout has begun!");
		Announce("You have one hour to kill Nurka!");
		
		nurka = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000); // 60 * 60 * 1000
		announce = ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("No one can`t kill Nurka! Partisan Hideout set free until next week!"), 3600000);
	}
	
	protected class DeSpawnTimer implements Runnable
	{
		L2NpcInstance npc = null;
		
		public DeSpawnTimer(final L2NpcInstance npc)
		{
			this.npc = npc;
		}
		
		@Override
		public void run()
		{
			npc.onDecay();
		}
	}
	
	public final boolean Conditions(final L2PcInstance player)
	{
		if (player != null && player.getClan() != null && player.isClanLeader() && player.getClan().getAuctionBiddedAt() <= 0 && ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null && player.getClan().getLevel() > 2)
		{
			return true;
		}
		return false;
	}
	
	protected class AnnounceInfo implements Runnable
	{
		String message;
		
		public AnnounceInfo(final String message)
		{
			this.message = message;
		}
		
		@Override
		public void run()
		{
			Announce(message);
		}
	}
	
	public void Announce(final String message)
	{
		Announcements.getInstance().announceToAll(message);
	}
	
	public void CaptureFinish()
	{
		L2Clan clanIdMaxDamage = null;
		long tempMaxDamage = 0;
		for (final DamageInfo damageInfo : clansDamageInfo.values())
		{
			if (damageInfo != null)
			{
				if (damageInfo.damage > tempMaxDamage)
				{
					tempMaxDamage = damageInfo.damage;
					clanIdMaxDamage = damageInfo.clan;
				}
			}
		}
		if (clanIdMaxDamage != null)
		{
			ClanHallManager.getInstance().setOwner(21, clanIdMaxDamage);
			clanIdMaxDamage.setReputationScore(clanIdMaxDamage.getReputationScore() + 600, true);
			update();
			
			Announce("Capture of Partisan Hideout is over.");
			Announce("Now its belong to: '" + clanIdMaxDamage.getName() + "' until next capture.");
		}
		else
		{
			Announce("Capture of Partisan Hideout is over.");
			Announce("No one can`t capture Partisan Hideout.");
		}
		
		nurka.cancel(true);
		announce.cancel(true);
	}
	
	public void addSiegeDamage(final L2Clan clan, final long damage)
	{
		DamageInfo clanDamage = clansDamageInfo.get(clan.getClanId());
		if (clanDamage != null)
		{
			clanDamage.damage += damage;
		}
		else
		{
			clanDamage = new DamageInfo();
			clanDamage.clan = clan;
			clanDamage.damage += damage;
			clansDamageInfo.put(clan.getClanId(), clanDamage);
		}
	}
	
	private void update()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_HALL_FORTRESS_OF_RESISTANCE))
		{
			statement.setLong(1, System.currentTimeMillis() + 59760000);
			statement.setInt(2, 1);
			statement.setInt(3, 21);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("FortresOfResistance.update : Could not update fortress of resistance clan hall in db", e);
		}
	}
}
