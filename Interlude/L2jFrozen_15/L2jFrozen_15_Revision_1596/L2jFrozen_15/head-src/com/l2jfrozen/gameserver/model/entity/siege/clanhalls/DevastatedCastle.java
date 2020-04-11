
/*
 *  Author: Qwerty, Scoria dev.
 *  v 2.2
 */
package com.l2jfrozen.gameserver.model.entity.siege.clanhalls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.ArrayList;
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
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class DevastatedCastle
{
	private static final Logger LOGGER = Logger.getLogger(DevastatedCastle.class);
	private static final String UPDATE_CLAN_HALL_DEVASTED_CASTLE = "UPDATE clanhall SET paidUntil=?, paid=? WHERE id=?";
	
	private static DevastatedCastle instance;
	private final Map<Integer, DamageInfo> clansDamageInfo;
	
	private static int START_DAY = 1;
	private static int HOUR = Config.DEVASTATED_HOUR;
	private static int MINUTES = Config.DEVASTATED_MINUTES;
	
	private static final int BOSS_ID = 35410; // Gustav @Boss@
	private static final int BOSS1_ID = 35408; // Dietrich @Minion@
	private static final int BOSS2_ID = 35409; // Mikhail @Minion@
	private static final int MESSENGER_ID = 35420;
	
	private ScheduledFuture<?> gustav;
	private ScheduledFuture<?> dietrich;
	private ScheduledFuture<?> mikhail;
	private ScheduledFuture<?> monsterdespawn;
	
	private L2NpcInstance minion1 = null;
	private L2NpcInstance minion2 = null;
	
	private final ArrayList<MonsterLocation> monsters = new ArrayList<>();
	private ArrayList<L2Spawn> spawns = new ArrayList<>();
	
	private final Calendar siegetime = Calendar.getInstance();
	
	public boolean progress = false;
	
	public static DevastatedCastle getInstance()
	{
		if (instance == null)
		{
			instance = new DevastatedCastle();
		}
		return instance;
	}
	
	protected class DamageInfo
	{
		public L2Clan clan;
		public long damage;
	}
	
	private DevastatedCastle()
	{
		if (Config.DEVASTATED_DAY == 1)
		{
			START_DAY = Calendar.MONDAY;
		}
		else if (Config.DEVASTATED_DAY == 2)
		{
			START_DAY = Calendar.TUESDAY;
		}
		else if (Config.DEVASTATED_DAY == 3)
		{
			START_DAY = Calendar.WEDNESDAY;
		}
		else if (Config.DEVASTATED_DAY == 4)
		{
			START_DAY = Calendar.THURSDAY;
		}
		else if (Config.DEVASTATED_DAY == 5)
		{
			START_DAY = Calendar.FRIDAY;
		}
		else if (Config.DEVASTATED_DAY == 6)
		{
			START_DAY = Calendar.SATURDAY;
		}
		else if (Config.DEVASTATED_DAY == 7)
		{
			START_DAY = Calendar.SUNDAY;
		}
		else
		{
			START_DAY = Calendar.MONDAY;
		}
		
		if (HOUR < 0 || HOUR > 23)
		{
			HOUR = 18;
		}
		if (MINUTES < 0 || MINUTES > 59)
		{
			MINUTES = 0;
		}
		
		clansDamageInfo = new HashMap<>();
		
		synchronized (this)
		{
			setCalendarForNextSiege();
			final long milliToSiege = getMilliToSiege();
			
			RunMessengerSpawn rms = new RunMessengerSpawn();
			ThreadPoolManager.getInstance().scheduleGeneral(rms, milliToSiege);
			
			final long total_millis = System.currentTimeMillis() + milliToSiege;
			
			final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
			cal.setTimeInMillis(total_millis);
			final String next_ch_siege_date = DateFormat.getInstance().format(cal.getTime());
			
			LOGGER.info("Devastated Castle: siege will start the " + next_ch_siege_date);
			rms = null;
		}
	}
	
	private void setCalendarForNextSiege()
	{
		int daysToChange = getDaysToSiege();
		
		if (daysToChange == 7)
		{
			if (siegetime.get(Calendar.HOUR_OF_DAY) < HOUR)
			{
				daysToChange = 0;
			}
			else if (siegetime.get(Calendar.HOUR_OF_DAY) == HOUR && siegetime.get(Calendar.MINUTE) < MINUTES)
			{
				daysToChange = 0;
			}
		}
		
		if (daysToChange > 0)
		{
			siegetime.add(Calendar.DATE, daysToChange);
		}
		
		siegetime.set(Calendar.HOUR_OF_DAY, HOUR);
		siegetime.set(Calendar.MINUTE, MINUTES);
	}
	
	private int getDaysToSiege()
	{
		final int numDays = siegetime.get(Calendar.DAY_OF_WEEK) - START_DAY;
		
		if (numDays < 0)
		{
			return 0 - numDays;
		}
		
		return 7 - numDays;
	}
	
	private long getMilliToSiege()
	{
		final long currTimeMillis = System.currentTimeMillis();
		final long siegeTimeMillis = siegetime.getTimeInMillis();
		
		return siegeTimeMillis - currTimeMillis;
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
		if (!ClanHallManager.getInstance().isFree(34))
		{
			ClanHallManager.getInstance().setFree(34);
		}
		
		Announce("Siege registration of Devastated castle has begun!");
		Announce("Now its open for 2 hours!");
		
		L2NpcInstance result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(MESSENGER_ID);
			
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(179040);
			spawn.setLocy(-13717);
			spawn.setLocz(-2263);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			template = null;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		RunSiege rs = new RunSiege();
		ThreadPoolManager.getInstance().scheduleGeneral(rs, 14400000); // 4 * 60 * 60 * 1000
		
		ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 7200000); // 2 * 60 * 60 * 1000
		ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("Siege registration of Devastated castle is over!"), 7200000);
		ThreadPoolManager.getInstance().scheduleGeneral(new AnnounceInfo("2 hours until siege begin."), 7200000);
		
		result = null;
		rs = null;
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
	
	protected class RunSiege implements Runnable
	{
		@Override
		public void run()
		{
			Siege();
		}
	}
	
	public void Siege()
	{
		L2NpcInstance result = null;
		L2NpcTemplate template = null;
		L2Spawn spawn = null;
		
		final ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
		CH.banishForeigners();
		CH.spawnDoor();
		
		setIsInProgress(true);
		
		try
		{
			fillMonsters();
			
			template = NpcTable.getInstance().getTemplate(BOSS_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178298);
			spawn.setLocy(-17624);
			spawn.setLocz(-2194);
			spawn.stopRespawn();
			result = spawn.spawnOne();
			gustav = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(result), 3600000); // 60 * 60 * 1000
			
			template = NpcTable.getInstance().getTemplate(BOSS1_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178306);
			spawn.setLocy(-17535);
			spawn.setLocz(-2195);
			spawn.stopRespawn();
			minion1 = spawn.spawnOne();
			dietrich = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(minion1), 3600000); // 60 * 60 * 1000
			
			template = NpcTable.getInstance().getTemplate(BOSS2_ID);
			spawn = new L2Spawn(template);
			spawn.setLocx(178304);
			spawn.setLocy(-17712);
			spawn.setLocz(-2194);
			spawn.stopRespawn();
			minion2 = spawn.spawnOne();
			mikhail = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTimer(minion2), 3600000); // 60 * 60 * 1000
			
			spawnMonsters();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		result = null;
		template = null;
		spawn = null;
		
		Announce("Siege of Devastated castle has begun!");
	}
	
	private static class MonsterLocation
	{
		private final int id;
		private final int x;
		private final int y;
		private final int z;
		private final int heading;
		
		protected MonsterLocation(final int id, final int x, final int y, final int z, final int heading)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.z = z;
			this.heading = heading;
		}
		
		protected int getId()
		{
			return id;
		}
		
		protected int getX()
		{
			return x;
		}
		
		protected int getY()
		{
			return y;
		}
		
		protected int getZ()
		{
			return z;
		}
		
		protected int getHeading()
		{
			return heading;
		}
		
	}
	
	private void addMonster(final int id, final int x, final int y, final int z, final int heading)
	{
		monsters.add(new MonsterLocation(id, x, y, z, heading));
	}
	
	private void fillMonsters()
	{
		addMonster(35413, 178288, -14924, -2200, 6320);
		addMonster(35412, 178255, -14884, -2200, 6320);
		addMonster(35413, 178222, -14924, -2200, 6320);
		addMonster(35412, 178222, -14884, -2200, 6320);
		addMonster(35412, 178420, -14904, -2200, 6320);
		addMonster(35412, 178387, -14904, -2200, 6320);
		addMonster(35412, 178288, -14884, -2200, 6320);
		addMonster(35412, 178387, -14884, -2200, 6320);
		addMonster(35413, 178354, -14944, -2200, 6320);
		addMonster(35412, 178321, -14884, -2200, 6320);
		addMonster(35413, 178222, -14944, -2200, 6320);
		addMonster(35412, 178354, -14904, -2200, 6320);
		addMonster(35413, 178255, -14924, -2200, 6320);
		addMonster(35413, 178387, -14924, -2200, 6320);
		addMonster(35413, 178354, -14924, -2200, 6320);
		addMonster(35413, 178420, -14924, -2200, 6320);
		addMonster(35412, 178354, -14884, -2200, 6320);
		addMonster(35412, 178420, -14884, -2200, 6320);
		addMonster(35413, 178454, -14944, -2200, 6320);
		addMonster(35413, 178454, -14924, -2200, 6320);
		addMonster(35413, 178420, -14944, -2200, 6320);
		addMonster(35412, 178222, -14904, -2200, 6320);
		addMonster(35413, 178321, -14944, -2200, 6320);
		addMonster(35413, 178321, -14924, -2200, 6320);
		addMonster(35412, 178288, -14904, -2200, 6320);
		addMonster(35412, 178321, -14904, -2200, 6320);
		addMonster(35413, 178255, -14944, -2200, 6320);
		addMonster(35412, 178255, -14904, -2200, 6320);
		addMonster(35413, 178288, -14944, -2200, 6320);
		addMonster(35412, 178454, -14884, -2200, 6320);
		addMonster(35413, 178387, -14944, -2200, 6320);
		addMonster(35412, 178454, -14904, -2200, 6320);
		addMonster(35413, 179052, -15226, -2221, 6320);
		addMonster(35413, 179260, -15341, -2221, 6320);
		addMonster(35413, 179101, -15253, -2221, 6320);
		addMonster(35413, 179073, -15203, -2221, 6320);
		addMonster(35413, 179144, -15271, -2221, 6320);
		addMonster(35413, 179246, -15285, -2221, 6320);
		addMonster(35413, 179164, -15247, -2221, 6320);
		addMonster(35413, 179226, -15309, -2221, 6320);
		addMonster(35413, 179322, -15349, -2221, 6320);
		addMonster(35413, 179302, -15372, -2221, 6320);
		addMonster(35413, 179189, -15286, -2221, 6320);
		addMonster(35413, 179391, -15439, -2221, 6320);
		addMonster(35413, 179341, -15406, -2221, 6320);
		addMonster(35415, 179503, -15925, -2256, 6320);
		addMonster(35415, 179562, -15984, -2256, 6320);
		addMonster(35415, 179491, -15981, -2256, 6320);
		addMonster(35415, 179370, -16196, -2256, 6320);
		addMonster(35411, 179426, -16009, -2253, 6320);
		addMonster(35415, 179544, -15882, -2256, 6320);
		addMonster(35415, 179599, -15943, -2256, 6320);
		addMonster(35415, 179570, -15901, -2256, 6320);
		addMonster(35415, 179408, -16158, -2256, 6320);
		addMonster(35415, 179279, -16219, -2256, 6320);
		addMonster(35411, 179327, -16101, -2253, 6320);
		addMonster(35411, 179540, -16876, -2246, 6320);
		addMonster(35413, 179009, -15201, -2221, 6320);
		addMonster(35413, 178951, -14699, -2080, 6320);
		addMonster(35413, 178801, -14975, -2080, 6320);
		addMonster(35413, 178865, -14857, -2080, 6320);
		addMonster(35413, 178822, -14936, -2080, 6320);
		addMonster(35413, 178843, -14897, -2080, 6320);
		addMonster(35413, 178929, -14739, -2080, 6320);
		addMonster(35413, 178908, -14778, -2080, 6320);
		addMonster(35413, 178886, -14818, -2080, 6320);
		addMonster(35411, 177719, -15951, -2253, 6320);
		addMonster(35413, 177838, -15664, -2226, 6320);
		addMonster(35411, 177627, -15953, -2250, 6320);
		addMonster(35411, 177387, -15955, -2250, 6320);
		addMonster(35411, 177667, -15921, -2253, 6320);
		addMonster(35413, 177859, -15812, -2226, 6320);
		addMonster(35411, 177661, -16014, -2253, 6320);
		addMonster(35411, 177608, -15985, -2250, 6320);
		addMonster(35415, 177663, -16154, -2250, 6320);
		addMonster(35415, 177530, -16079, -2250, 6320);
		addMonster(35411, 177709, -16043, -2253, 6320);
		addMonster(35411, 177703, -15999, -2250, 6320);
		addMonster(35411, 177810, -16145, -2253, 6320);
		addMonster(35415, 177571, -16105, -2250, 6320);
		addMonster(35415, 177473, -16011, -2250, 6320);
		addMonster(35415, 177612, -16090, -2250, 6320);
		addMonster(35415, 177657, -16113, -2250, 6320);
		addMonster(35415, 177387, -15996, -2250, 6320);
		addMonster(35411, 177564, -15963, -2250, 6320);
		addMonster(35411, 177606, -16035, -2253, 6320);
		addMonster(35411, 177470, -15856, -2250, 6320);
		addMonster(35415, 177428, -15981, -2250, 6320);
		addMonster(35411, 177506, -15887, -2250, 6320);
		addMonster(35411, 177517, -15930, -2250, 6320);
		addMonster(35411, 177308, -15861, -2253, 6320);
		addMonster(35411, 177861, -16164, -2253, 6320);
		addMonster(35413, 177906, -15791, -2226, 6320);
		addMonster(35413, 177765, -15643, -2226, 6320);
		addMonster(35413, 177880, -15744, -2226, 6320);
		addMonster(35413, 177788, -15578, -2226, 6320);
		addMonster(35413, 177811, -15622, -2226, 6320);
		addMonster(35413, 177859, -15704, -2226, 6320);
		addMonster(35413, 177769, -15540, -2226, 6320);
		addMonster(35413, 177813, -15726, -2226, 6320);
		addMonster(35413, 177707, -15427, -2226, 6320);
		addMonster(35413, 177680, -15485, -2226, 6320);
		addMonster(35413, 177722, -15561, -2226, 6320);
		addMonster(35413, 177745, -15498, -2226, 6320);
		addMonster(35413, 177803, -14971, -2210, 6320);
		addMonster(35413, 177727, -15464, -2226, 6320);
		addMonster(35415, 177433, -16026, -2250, 6320);
		addMonster(35413, 177649, -14750, -2210, 6320);
		addMonster(35413, 177619, -14705, -2210, 6320);
		addMonster(35413, 177711, -14838, -2210, 6320);
		addMonster(35413, 177834, -15015, -2210, 6320);
		addMonster(35413, 177741, -14883, -2210, 6320);
		addMonster(35413, 177772, -14927, -2210, 6320);
		addMonster(35413, 177680, -14794, -2210, 6320);
		addMonster(35411, 177400, -15854, -2250, 6320);
		addMonster(35415, 179697, -17781, -2256, 6320);
		addMonster(35411, 179479, -17133, -2256, 6320);
		addMonster(35411, 179485, -17213, -2246, 6320);
		addMonster(35411, 179593, -16876, -2246, 6320);
		addMonster(35411, 179468, -17280, -2256, 6320);
		addMonster(35411, 179433, -16991, -2246, 6320);
		addMonster(35411, 179514, -17281, -2256, 6320);
		addMonster(35411, 179525, -17135, -2256, 6320);
		addMonster(35411, 179444, -16937, -2256, 6320);
		addMonster(35411, 179438, -16875, -2246, 6320);
		addMonster(35415, 179633, -17137, -2256, 6320);
		addMonster(35411, 179537, -17214, -2246, 6320);
		addMonster(35411, 179594, -17453, -2246, 6320);
		addMonster(35415, 179576, -17137, -2256, 6320);
		addMonster(35415, 179508, -17341, -2252, 6320);
		addMonster(35415, 179446, -17391, -2252, 6320);
		addMonster(35415, 179437, -17522, -2252, 6320);
		addMonster(35415, 179536, -17842, -2252, 6320);
		addMonster(35415, 179432, -17719, -2252, 6320);
		addMonster(35415, 179436, -17841, -2252, 6320);
		addMonster(35411, 179542, -17453, -2246, 6320);
		addMonster(35415, 179436, -17776, -2256, 6320);
		addMonster(35415, 179534, -17892, -2252, 6320);
		addMonster(35415, 179482, -17841, -2252, 6320);
		addMonster(35415, 179696, -17844, -2252, 6320);
		addMonster(35415, 179604, -17525, -2252, 6320);
		addMonster(35415, 179707, -17722, -2252, 6320);
		addMonster(35411, 179715, -17454, -2246, 6320);
		addMonster(35411, 179641, -17215, -2246, 6320);
		addMonster(35415, 179665, -17527, -2252, 6320);
		addMonster(35415, 179557, -17524, -2252, 6320);
		addMonster(35415, 179636, -17780, -2256, 6320);
		addMonster(35415, 179694, -17897, -2252, 6320);
		addMonster(35414, 178682, -18200, -2200, 6320);
		addMonster(35413, 178577, -18422, -2250, 6320);
		addMonster(35412, 178745, -18186, -2200, 6320);
		addMonster(35413, 178528, -18499, -2250, 6320);
		addMonster(35414, 178640, -18196, -2200, 6320);
		addMonster(35413, 178766, -18228, -2200, 6320);
		addMonster(35412, 178724, -18184, -2200, 6320);
		addMonster(35412, 178703, -18182, -2200, 6320);
		addMonster(35413, 178575, -18500, -2250, 6320);
		addMonster(35413, 178703, -18222, -2200, 6320);
		addMonster(35413, 178530, -18421, -2250, 6320);
		addMonster(35413, 178523, -18696, -2250, 6320);
		addMonster(35412, 178661, -18178, -2200, 6320);
		addMonster(35414, 178661, -18198, -2200, 6320);
		addMonster(35413, 178788, -18229, -2200, 6320);
		addMonster(35414, 178724, -18204, -2200, 6320);
		addMonster(35413, 178574, -18539, -2250, 6320);
		addMonster(35413, 178578, -18383, -2250, 6320);
		addMonster(35413, 178573, -18577, -2250, 6320);
		addMonster(35413, 178530, -18382, -2250, 6320);
		addMonster(35413, 178528, -18461, -2250, 6320);
		addMonster(35413, 178526, -18538, -2250, 6320);
		addMonster(35413, 178526, -18576, -2250, 6320);
		addMonster(35413, 178570, -18736, -2250, 6320);
		addMonster(35413, 178524, -18618, -2250, 6320);
		addMonster(35413, 178571, -18657, -2250, 6320);
		addMonster(35413, 178523, -18656, -2250, 6320);
		addMonster(35413, 178523, -18735, -2250, 6320);
		addMonster(35413, 178571, -18618, -2250, 6320);
		addMonster(35413, 178571, -18697, -2250, 6320);
		addMonster(35413, 178576, -18461, -2250, 6320);
		addMonster(35413, 178682, -18220, -2200, 6320);
		addMonster(35413, 178661, -18218, -2200, 6320);
		addMonster(35414, 178745, -18206, -2200, 6320);
		addMonster(35412, 178682, -18180, -2200, 6320);
		addMonster(35414, 178703, -18202, -2200, 6320);
		addMonster(35412, 178640, -18176, -2200, 6320);
		addMonster(35414, 178788, -18209, -2200, 6320);
		addMonster(35413, 178640, -18216, -2200, 6320);
		addMonster(35412, 178788, -18189, -2200, 6320);
		addMonster(35413, 178745, -18226, -2200, 6320);
		addMonster(35414, 178766, -18208, -2200, 6320);
		addMonster(35412, 178766, -18188, -2200, 6320);
		addMonster(35413, 178724, -18224, -2200, 6320);
		addMonster(35413, 178430, -16901, -2217, 6320);
		addMonster(35415, 178285, -16832, -2217, 6320);
		addMonster(35413, 178153, -16914, -2217, 6320);
		addMonster(35411, 178398, -16781, -2218, 6320);
		addMonster(35415, 178363, -16768, -2217, 6320);
		addMonster(35413, 178437, -16587, -2217, 6320);
		addMonster(35413, 178431, -16784, -2217, 6320);
		addMonster(35411, 178120, -16714, -2218, 6320);
		addMonster(35415, 178366, -16721, -2217, 6320);
		addMonster(35413, 178433, -16742, -2217, 6320);
		addMonster(35413, 178430, -16862, -2217, 6320);
		addMonster(35411, 178395, -16865, -2218, 6320);
		addMonster(35415, 178288, -16783, -2217, 6320);
		addMonster(35411, 178397, -16824, -2218, 6320);
		addMonster(35415, 178090, -16878, -2217, 6320);
		addMonster(35413, 178154, -16797, -2217, 6320);
		addMonster(35415, 178027, -16773, -2217, 6320);
		addMonster(35413, 178435, -16665, -2217, 6320);
		addMonster(35413, 178433, -16704, -2217, 6320);
		addMonster(35411, 178060, -16868, -2218, 6320);
		addMonster(35413, 178156, -16718, -2217, 6320);
		addMonster(35411, 178324, -16762, -2218, 6320);
		addMonster(35413, 177869, -16832, -2217, 6320);
		addMonster(35411, 178117, -16885, -2218, 6320);
		addMonster(35415, 178366, -16674, -2217, 6320);
		addMonster(35413, 178435, -16627, -2217, 6320);
		addMonster(35415, 178366, -16582, -2217, 6320);
		addMonster(35411, 178403, -16566, -2218, 6320);
		addMonster(35413, 178437, -16548, -2217, 6320);
		addMonster(35415, 178290, -16550, -2217, 6320);
		addMonster(35413, 178160, -16600, -2217, 6320);
		addMonster(35411, 178122, -16759, -2218, 6320);
		addMonster(35411, 178122, -16672, -2218, 6320);
		addMonster(35415, 178095, -16736, -2217, 6320);
		addMonster(35411, 178324, -16804, -2218, 6320);
		addMonster(35415, 178029, -16587, -2217, 6320);
		addMonster(35415, 178290, -16690, -2217, 6320);
		addMonster(35415, 178027, -16820, -2217, 6320);
		addMonster(35413, 178431, -16822, -2217, 6320);
		addMonster(35411, 178326, -16720, -2218, 6320);
		addMonster(35413, 178158, -16679, -2217, 6320);
		addMonster(35413, 178161, -16562, -2217, 6320);
		addMonster(35413, 178158, -16640, -2217, 6320);
		addMonster(35413, 177872, -16714, -2217, 6320);
		addMonster(35411, 178061, -16738, -2218, 6320);
		addMonster(35413, 177871, -16752, -2217, 6320);
		addMonster(35413, 177876, -16597, -2217, 6320);
		addMonster(35413, 177873, -16675, -2217, 6320);
		addMonster(35413, 177869, -16873, -2217, 6320);
		addMonster(35413, 177874, -16637, -2217, 6320);
		addMonster(35411, 178060, -16826, -2218, 6320);
		addMonster(35411, 178063, -16784, -2218, 6320);
		addMonster(35415, 178029, -16727, -2217, 6320);
		addMonster(35413, 177876, -16558, -2217, 6320);
		addMonster(35413, 177870, -16794, -2217, 6320);
		addMonster(35413, 177868, -16911, -2217, 6320);
		addMonster(35413, 178156, -16756, -2217, 6320);
		addMonster(35415, 178092, -16782, -2217, 6320);
		addMonster(35413, 178153, -16876, -2217, 6320);
		addMonster(35413, 178154, -16836, -2217, 6320);
		addMonster(35412, 177217, -17168, -2200, 6320);
		addMonster(35413, 177187, -17128, -2200, 6320);
		addMonster(35413, 177276, -17128, -2200, 6320);
		addMonster(35413, 177335, -17128, -2200, 6320);
		addMonster(35414, 177246, -17148, -2200, 6320);
		addMonster(35412, 177246, -17168, -2200, 6320);
		addMonster(35412, 177129, -17168, -2200, 6320);
		addMonster(35412, 177158, -17168, -2200, 6320);
		addMonster(35413, 177217, -17128, -2200, 6320);
		addMonster(35413, 177158, -17128, -2200, 6320);
		addMonster(35413, 177129, -17128, -2200, 6320);
		addMonster(35414, 177129, -17148, -2200, 6320);
		addMonster(35412, 177276, -17168, -2200, 6320);
		addMonster(35414, 177158, -17148, -2200, 6320);
		addMonster(35413, 177305, -17128, -2200, 6320);
		addMonster(35412, 177187, -17168, -2200, 6320);
		addMonster(35414, 177305, -17148, -2200, 6320);
		addMonster(35413, 177396, -17452, -2207, 6320);
		addMonster(35413, 177397, -17634, -2207, 6320);
		addMonster(35416, 177664, -17599, -2219, 6320);
		addMonster(35416, 177721, -17813, -2219, 6320);
		addMonster(35416, 177501, -17424, -2219, 6320);
		addMonster(35415, 177632, -17788, -2215, 6320);
		addMonster(35413, 177466, -17445, -2207, 6320);
		addMonster(35414, 177217, -17148, -2200, 6320);
		addMonster(35413, 177396, -17545, -2207, 6320);
		addMonster(35414, 177187, -17148, -2200, 6320);
		addMonster(35414, 177335, -17148, -2200, 6320);
		addMonster(35412, 177335, -17168, -2200, 6320);
		addMonster(35412, 177305, -17168, -2200, 6320);
		addMonster(35415, 177439, -17426, -2215, 6320);
		addMonster(35415, 177438, -17472, -2215, 6320);
		addMonster(35416, 177662, -17685, -2219, 6320);
		addMonster(35415, 177527, -17578, -2215, 6320);
		addMonster(35415, 177531, -17399, -2215, 6320);
		addMonster(35415, 177561, -17563, -2215, 6320);
		addMonster(35413, 177604, -17718, -2207, 6320);
		addMonster(35413, 177466, -17537, -2207, 6320);
		addMonster(35416, 177500, -17469, -2219, 6320);
		addMonster(35415, 177527, -17537, -2215, 6320);
		addMonster(35413, 177467, -17809, -2207, 6320);
		addMonster(35413, 177466, -17716, -2207, 6320);
		addMonster(35416, 177496, -17645, -2219, 6320);
		addMonster(35413, 177398, -17817, -2207, 6320);
		addMonster(35416, 177560, -17849, -2219, 6320);
		addMonster(35415, 177524, -17763, -2215, 6320);
		addMonster(35415, 177432, -17791, -2215, 6320);
		addMonster(35413, 177397, -17723, -2207, 6320);
		addMonster(35415, 177523, -17810, -2215, 6320);
		addMonster(35416, 177501, -17384, -2219, 6320);
		addMonster(35413, 177603, -17539, -2207, 6320);
		addMonster(35416, 177725, -17503, -2219, 6320);
		addMonster(35416, 177666, -17508, -2219, 6320);
		addMonster(35415, 177564, -17425, -2215, 6320);
		addMonster(35413, 177749, -17538, -2207, 6320);
		addMonster(35413, 177603, -17446, -2207, 6320);
		addMonster(35413, 177749, -17446, -2207, 6320);
		addMonster(35416, 177728, -17377, -2219, 6320);
		addMonster(35415, 177440, -17380, -2215, 6320);
		addMonster(35416, 177567, -17376, -2219, 6320);
		addMonster(35414, 177276, -17148, -2200, 6320);
		addMonster(35416, 177724, -17594, -2219, 6320);
		addMonster(35415, 177436, -17605, -2215, 6320);
		addMonster(35415, 177636, -17516, -2215, 6320);
		addMonster(35413, 177601, -17811, -2207, 6320);
		addMonster(35413, 177693, -17718, -2207, 6320);
		addMonster(35416, 177722, -17728, -2219, 6320);
		addMonster(35415, 177559, -17708, -2215, 6320);
		addMonster(35416, 177662, -17644, -2219, 6320);
		addMonster(35415, 177635, -17603, -2215, 6320);
		addMonster(35413, 177694, -17811, -2207, 6320);
		addMonster(35415, 177558, -17755, -2215, 6320);
		addMonster(35413, 177693, -17447, -2207, 6320);
		addMonster(35413, 177693, -17629, -2207, 6320);
		addMonster(35416, 177664, -17559, -2219, 6320);
		addMonster(35413, 177466, -17397, -2207, 6320);
		addMonster(35413, 177466, -17627, -2207, 6320);
		addMonster(35416, 177724, -17553, -2219, 6320);
		addMonster(35413, 177750, -17628, -2207, 6320);
		addMonster(35413, 177751, -17810, -2207, 6320);
		addMonster(35416, 177661, -17818, -2219, 6320);
		addMonster(35416, 177496, -17686, -2219, 6320);
		addMonster(35413, 177750, -17717, -2207, 6320);
		addMonster(35415, 177633, -17740, -2215, 6320);
		addMonster(35413, 177693, -17539, -2207, 6320);
		addMonster(35413, 177604, -17628, -2207, 6320);
		addMonster(35415, 177431, -17836, -2215, 6320);
		addMonster(35415, 177631, -17835, -2215, 6320);
		addMonster(35416, 177722, -17768, -2219, 6320);
		addMonster(35413, 177246, -17128, -2200, 6320);
	}
	
	public void spawnMonsters()
	{
		for (final MonsterLocation ml : monsters)
		{
			try
			{
				final L2NpcTemplate template = NpcTable.getInstance().getTemplate(ml.getId());
				final L2Spawn sp = new L2Spawn(template);
				sp.setAmount(1);
				sp.setLocx(ml.getX());
				sp.setLocy(ml.getY());
				sp.setLocz(ml.getZ());
				sp.setHeading(ml.getHeading());
				sp.setRespawnDelay(300); // 3 * 60
				sp.setLocation(0);
				sp.init();
				spawns.add(sp);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		monsterdespawn = ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnMonsters(), 3600000); // 60 * 60 * 1000
	}
	
	protected class DeSpawnMonsters implements Runnable
	{
		@Override
		public void run()
		{
			DeSpawn();
		}
	}
	
	public void DeSpawn()
	{
		for (final L2Spawn sp : spawns)
		{
			sp.stopRespawn();
			sp.getLastSpawn().doDie(sp.getLastSpawn());
		}
		spawns.clear();
		setIsInProgress(false);
		spawns = null;
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
			if (npc.getNpcId() == 35410)
			{
				Announce("Siege of Devastated castle is over.");
				Announce("Nobody won! ClanHall belong to NPC until next siege.");
				
				final ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
				CH.banishForeigners();
				CH.spawnDoor();
			}
			npc.onDecay();
		}
	}
	
	public final boolean Conditions(final L2PcInstance player)
	{
		if (player != null && player.getClan() != null && player.isClanLeader() && player.getClan().getAuctionBiddedAt() <= 0 && ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) == null && player.getClan().getLevel() > 3)
		{
			return true;
		}
		return false;
	}
	
	public boolean getIsInProgress()
	{
		return progress;
	}
	
	public void setIsInProgress(final boolean is)
	{
		progress = is;
	}
	
	public void SiegeFinish()
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
			ClanHallManager.getInstance().setOwner(34, clanIdMaxDamage);
			clanIdMaxDamage.setReputationScore(clanIdMaxDamage.getReputationScore() + 600, true);
			update();
			
			Announce("Siege of Devastated castle is over.");
			Announce("Now its belong to: '" + clanIdMaxDamage.getName() + "' until next siege.");
		}
		else
		{
			Announce("Siege of Devastated castle is over..");
			Announce("Nobody won! ClanHall belong to NPC until next siege.");
		}
		
		DeSpawn();
		minion1.onDecay();
		minion2.onDecay();
		gustav.cancel(true);
		dietrich.cancel(true);
		mikhail.cancel(true);
		monsterdespawn.cancel(true);
		
		ClanHall CH = ClanHallManager.getInstance().getClanHallById(34);
		CH.banishForeigners();
		CH.spawnDoor();
		CH = null;
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
			PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_HALL_DEVASTED_CASTLE))
		{
			statement.setLong(1, System.currentTimeMillis() + 59760000);
			statement.setInt(2, 1);
			statement.setInt(3, 34);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("DevastedCastle.update: Could not update clan hall devasted castle in db", e);
		}
	}
}
