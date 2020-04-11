/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.olympiad;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author godson
 */
public class Olympiad
{
	protected static final Logger _log = LoggerFactory.getLogger(Olympiad.class);
	protected static final java.util.logging.Logger _logResults = java.util.logging.Logger.getLogger("olympiad");
	
	private static final Map<Integer, StatsSet> _nobles = new FastMap<>();
	protected static List<StatsSet> _heroesToBe;
	private static final Map<Integer, Integer> _noblesRank = new HashMap<>();
	
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, olympiad_end, validation_end, " + "next_weekly_change FROM olympiad_data WHERE id = 0";
	private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, " + "period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, " + "validation_end=?, next_weekly_change=?";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, " + "characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, " + "olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, " + "olympiad_nobles.competitions_done_week, olympiad_nobles.competitions_done_week_classed, olympiad_nobles.competitions_done_week_non_classed, olympiad_nobles.competitions_done_week_team " + "FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles " + "(`charId`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`," + "`competitions_drawn`, `competitions_done_week`, `competitions_done_week_classed`, `competitions_done_week_non_classed`, `competitions_done_week_team`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET " + "olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ?, competitions_done_week_classed = ?, competitions_done_week_non_classed = ?, competitions_done_week_team = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name " + "FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId " + "AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
	private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT charId from olympiad_nobles_eom " + "WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters " + "WHERE characters.charId = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? " + "AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " " + "ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT characters.char_name from olympiad_nobles, characters " + "WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id = ? " + "AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT characters.char_name from olympiad_nobles_eom, characters " + "WHERE characters.charId = olympiad_nobles_eom.charId AND (olympiad_nobles_eom.class_id = ? OR olympiad_nobles_eom.class_id = 133) " + "AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " " + "ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND = "SELECT characters.char_name from olympiad_nobles, characters " + "WHERE characters.charId = olympiad_nobles.charId AND (olympiad_nobles.class_id = ? OR olympiad_nobles.class_id = 133) " + "AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
	
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";
	private static final int[] HERO_IDS =
	{
		88,
		89,
		90,
		91,
		92,
		93,
		94,
		95,
		96,
		97,
		98,
		99,
		100,
		101,
		102,
		103,
		104,
		105,
		106,
		107,
		108,
		109,
		110,
		111,
		112,
		113,
		114,
		115,
		116,
		117,
		118,
		131,
		132,
		133,
		134
	};
	
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
	
	protected static final int DEFAULT_POINTS = Config.ALT_OLY_START_POINTS;
	protected static final int WEEKLY_POINTS = Config.ALT_OLY_WEEKLY_POINTS;
	
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	public static final String COMP_DONE_WEEK = "competitions_done_week";
	public static final String COMP_DONE_WEEK_CLASSED = "competitions_done_week_classed";
	public static final String COMP_DONE_WEEK_NON_CLASSED = "competitions_done_week_non_classed";
	public static final String COMP_DONE_WEEK_TEAM = "competitions_done_week_team";
	
	protected long _olympiadEnd;
	protected long _validationEnd;
	
	/**
	 * The current period of the olympiad.<br>
	 * <b>0 -</b> Competition period<br>
	 * <b>1 -</b> Validation Period
	 */
	protected int _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	private Calendar _compStart;
	protected static boolean _inCompPeriod;
	protected static boolean _compStarted = false;
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValdationTask;
	protected ScheduledFuture<?> _gameManager = null;
	protected ScheduledFuture<?> _gameAnnouncer = null;
	
	protected Olympiad()
	{
		load();
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.OLYMPIAD_ID);
		
		if (_period == 0)
		{
			init();
		}
	}
	
	private void load()
	{
		_nobles.clear();
		boolean loaded = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_DATA);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				_period = rset.getInt("period");
				_olympiadEnd = rset.getLong("olympiad_end");
				_validationEnd = rset.getLong("validation_end");
				_nextWeeklyChange = rset.getLong("next_weekly_change");
				loaded = true;
			}
		}
		catch (Exception e)
		{
			_log.warn("Olympiad System: Error loading olympiad data from database: ", e);
		}
		
		if (!loaded)
		{
			_log.info("Olympiad System: failed to load data from database, trying to load from file.");
			
			Properties OlympiadProperties = new Properties();
			try (InputStream is = new FileInputStream(Config.OLYMPIAD_CONFIG_FILE))
			{
				
				OlympiadProperties.load(is);
			}
			catch (Exception e)
			{
				_log.error("Olympiad System: Error loading olympiad properties: ", e);
				return;
			}
			
			_currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
			_period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
			_olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
			_validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValidationEnd", "0"));
			_nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
		}
		
		switch (_period)
		{
			case 0:
				if ((_olympiadEnd == 0) || (_olympiadEnd < Calendar.getInstance().getTimeInMillis()))
				{
					setNewOlympiadEnd();
				}
				else
				{
					scheduleWeeklyChange();
				}
				break;
			case 1:
				if (_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					loadNoblesRank();
					_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			default:
				_log.warn("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = statement.executeQuery())
		{
			StatsSet statData;
			while (rset.next())
			{
				statData = new StatsSet();
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set(COMP_DONE_WEEK, rset.getInt(COMP_DONE_WEEK));
				statData.set(COMP_DONE_WEEK_CLASSED, rset.getInt(COMP_DONE_WEEK_CLASSED));
				statData.set(COMP_DONE_WEEK_NON_CLASSED, rset.getInt(COMP_DONE_WEEK_NON_CLASSED));
				statData.set(COMP_DONE_WEEK_TEAM, rset.getInt(COMP_DONE_WEEK_TEAM));
				statData.set("to_save", false);
				
				addNobleStats(rset.getInt(CHAR_ID), statData);
			}
		}
		catch (Exception e)
		{
			_log.warn("Olympiad System: Error loading noblesse data from database: ", e);
		}
		
		synchronized (this)
		{
			_log.info("Olympiad System: Loading Olympiad System....");
			if (_period == 0)
			{
				_log.info("Olympiad System: Currently in Olympiad Period");
			}
			else
			{
				_log.info("Olympiad System: Currently in Validation Period");
			}
			
			long milliToEnd;
			if (_period == 0)
			{
				milliToEnd = getMillisToOlympiadEnd();
			}
			else
			{
				milliToEnd = getMillisToValidationEnd();
			}
			
			_log.info("Olympiad System: " + (milliToEnd / 60000) + " minutes until period ends");
			
			if (_period == 0)
			{
				milliToEnd = getMillisToWeekChange();
				
				_log.info("Olympiad System: Next weekly change is in " + (milliToEnd / 60000) + " minutes");
			}
		}
		
		_log.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");
		
	}
	
	public void loadNoblesRank()
	{
		_noblesRank.clear();
		Map<Integer, Integer> tmpPlace = new HashMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);
			ResultSet rset = statement.executeQuery())
		{
			int place = 1;
			while (rset.next())
			{
				tmpPlace.put(rset.getInt(CHAR_ID), place++);
			}
		}
		catch (Exception e)
		{
			_log.warn("Olympiad System: Error loading noblesse data from database for Ranking: ", e);
		}
		
		int rank1 = (int) Math.round(tmpPlace.size() * 0.01);
		int rank2 = (int) Math.round(tmpPlace.size() * 0.10);
		int rank3 = (int) Math.round(tmpPlace.size() * 0.25);
		int rank4 = (int) Math.round(tmpPlace.size() * 0.50);
		if (rank1 == 0)
		{
			rank1 = 1;
			rank2++;
			rank3++;
			rank4++;
		}
		for (Entry<Integer, Integer> chr : tmpPlace.entrySet())
		{
			if (chr.getValue() <= rank1)
			{
				_noblesRank.put(chr.getKey(), 1);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank2)
			{
				_noblesRank.put(chr.getKey(), 2);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank3)
			{
				_noblesRank.put(chr.getKey(), 3);
			}
			else if (tmpPlace.get(chr.getKey()) <= rank4)
			{
				_noblesRank.put(chr.getKey(), 4);
			}
			else
			{
				_noblesRank.put(chr.getKey(), 5);
			}
		}
	}
	
	protected void init()
	{
		if (_period == 1)
		{
			return;
		}
		
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), getMillisToOlympiadEnd());
		
		updateCompStatus();
	}
	
	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
			sm.addInt(_currentCycle);
			
			Announcements.getInstance().announceToAll(sm);
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
			
			if (_scheduledWeeklyTask != null)
			{
				_scheduledWeeklyTask.cancel(true);
			}
			
			saveNobleData();
			
			_period = 1;
			sortHerosToBe();
			Hero.getInstance().resetData();
			Hero.getInstance().computeNewHeroes(_heroesToBe);
			
			saveOlympiadStatus();
			updateMonthlyData();
			
			Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
			
			loadNoblesRank();
			_scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}
	
	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
			_period = 0;
			_currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
	
	protected static int getNobleCount()
	{
		return _nobles.size();
	}
	
	protected static StatsSet getNobleStats(int playerId)
	{
		return _nobles.get(playerId);
	}
	
	private void updateCompStatus()
	{
		// _compStarted = false;
		
		synchronized (this)
		{
			long milliToStart = getMillisToCompBegin();
			
			double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000.) - numSecs) / 60;
			int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			int numHours = (int) Math.floor(countDown % 24);
			int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
			
			_log.info("Olympiad System: Event starts/started : " + _compStart.getTime());
		}
		
		_scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (isOlympiadEnd())
			{
				return;
			}
			
			_inCompPeriod = true;
			
			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
			_log.info("Olympiad System: Olympiad Game Started");
			_logResults.info("Result,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Points,Classed");
			
			_gameManager = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(OlympiadGameManager.getInstance(), 30000, 30000);
			if (Config.ALT_OLY_ANNOUNCE_GAMES)
			{
				_gameAnnouncer = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new OlympiadAnnouncer(), 30000, 500);
			}
			
			long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)), regEnd);
			}
			
			_scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (isOlympiadEnd())
				{
					return;
				}
				
				_inCompPeriod = true;
				_inCompPeriod = false;
				Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
				_log.info("Olympiad System: Olympiad Game Ended");
				
				while (OlympiadGameManager.getInstance().isBattleStarted()) // cleared in game manager
				{
					try
					{
						// wait 1 minutes for end of pendings games
						Thread.sleep(60000);
					}
					catch (InterruptedException e)
					{
						
					}
				}
				
				if (_gameManager != null)
				{
					_gameManager.cancel(false);
					_gameManager = null;
				}
				
				if (_gameAnnouncer != null)
				{
					_gameAnnouncer.cancel(false);
					_gameAnnouncer = null;
				}
				
				saveOlympiadStatus();
				
				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private long getMillisToOlympiadEnd()
	{
		// if (_olympiadEnd > Calendar.getInstance().getTimeInMillis())
		return (_olympiadEnd - Calendar.getInstance().getTimeInMillis());
		// return 10L;
	}
	
	public void manualSelectHeroes()
	{
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), 0);
	}
	
	protected long getMillisToValidationEnd()
	{
		if (_validationEnd > Calendar.getInstance().getTimeInMillis())
		{
			return (_validationEnd - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	public boolean isOlympiadEnd()
	{
		return (_period != 0);
	}
	
	protected void setNewOlympiadEnd()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addInt(_currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		Calendar currentTime = Calendar.getInstance();
		
		if (Config.ENABLE_REUNION_PERIOD)
		{
			int nearest = 0;
			Calendar[] cals = new Calendar[Config.ALT_OLY_END_DATE.length];
			for (int i = 0; i < cals.length; i++)
			{
				cals[i] = Calendar.getInstance();
				cals[i].set(Calendar.DAY_OF_MONTH, Config.ALT_OLY_END_DATE[i]);
				if (cals[i].before(currentTime))
				{
					cals[i].add(Calendar.MONTH, 1);
				}
				
				if (cals[i].before(cals[nearest]))
				{
					nearest = i;
				}
			}
			
			cals[nearest].set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_END_HOUR[0]);
			cals[nearest].set(Calendar.MINUTE, Config.ALT_OLY_END_HOUR[1]);
			cals[nearest].set(Calendar.SECOND, Config.ALT_OLY_END_HOUR[2]);
			_olympiadEnd = cals[nearest].getTimeInMillis();
		}
		else
		{
			if (Config.OLYMPIAD_PERIOD.equalsIgnoreCase("MONTH"))
			{
				currentTime.add(Calendar.MONTH, 1);
				currentTime.set(Calendar.DAY_OF_MONTH, 1);
			}
			if (Config.OLYMPIAD_PERIOD.equalsIgnoreCase("WEEKS"))
			{
				currentTime.add(Calendar.HOUR, 336);
				currentTime.set(Calendar.DAY_OF_WEEK, 1);
			}
			if (Config.OLYMPIAD_PERIOD.equalsIgnoreCase("WEEK"))
			{
				currentTime.add(Calendar.HOUR, 168);
				currentTime.set(Calendar.DAY_OF_WEEK, 1);
			}
			
			currentTime.set(Calendar.HOUR, Config.ALT_OLY_END_HOUR[0]);
			currentTime.set(Calendar.MINUTE, Config.ALT_OLY_END_HOUR[1]);
			currentTime.set(Calendar.SECOND, Config.ALT_OLY_END_HOUR[2]);
			_olympiadEnd = currentTime.getTimeInMillis();
		}
		
		Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		scheduleWeeklyChange();
	}
	
	public boolean inCompPeriod()
	{
		return _inCompPeriod;
	}
	
	private long getMillisToCompBegin()
	{
		if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
		{
			return 10L;
		}
		
		if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		}
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());
		
		return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	protected long getMillisToCompEnd()
	{
		// if (_compEnd > Calendar.getInstance().getTimeInMillis())
		return (_compEnd - Calendar.getInstance().getTimeInMillis());
		// return 10L;
	}
	
	private long getMillisToWeekChange()
	{
		if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
		{
			return (_nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	private void scheduleWeeklyChange()
	{
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			addWeeklyPoints();
			_log.info("Olympiad System: Added weekly points to nobles");
			resetWeeklyMatches();
			_log.info("Olympiad System: Reset weekly matches to nobles");
			
			Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}, getMillisToWeekChange(), WEEKLY_PERIOD);
	}
	
	protected synchronized void addWeeklyPoints()
	{
		if (_period == 1)
		{
			return;
		}
		
		int currentPoints;
		for (StatsSet nobleInfo : _nobles.values())
		{
			currentPoints = nobleInfo.getInteger(POINTS);
			currentPoints += WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);
		}
	}
	
	/**
	 * Resets number of matches, classed matches, non classed matches, team matches done by noble characters in the week.
	 */
	protected synchronized void resetWeeklyMatches()
	{
		if (_period == 1)
		{
			return;
		}
		
		for (StatsSet nobleInfo : _nobles.values())
		{
			nobleInfo.set(COMP_DONE_WEEK, 0);
			nobleInfo.set(COMP_DONE_WEEK_CLASSED, 0);
			nobleInfo.set(COMP_DONE_WEEK_NON_CLASSED, 0);
			nobleInfo.set(COMP_DONE_WEEK_TEAM, 0);
		}
	}
	
	public int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public int getPeriod()
	{
		return _period;
	}
	
	public boolean playerInStadia(L2PcInstance player)
	{
		return (ZoneManager.getInstance().getOlympiadStadium(player) != null);
	}
	
	/**
	 * Save noblesse data to database
	 */
	protected synchronized void saveNobleData()
	{
		if ((_nobles == null) || _nobles.isEmpty())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (Entry<Integer, StatsSet> entry : _nobles.entrySet())
			{
				StatsSet nobleInfo = entry.getValue();
				
				if (nobleInfo == null)
				{
					continue;
				}
				
				int charId = entry.getKey();
				int classId = nobleInfo.getInteger(CLASS_ID);
				int points = nobleInfo.getInteger(POINTS);
				int compDone = nobleInfo.getInteger(COMP_DONE);
				int compWon = nobleInfo.getInteger(COMP_WON);
				int compLost = nobleInfo.getInteger(COMP_LOST);
				int compDrawn = nobleInfo.getInteger(COMP_DRAWN);
				int compDoneWeek = nobleInfo.getInteger(COMP_DONE_WEEK);
				int compDoneWeekClassed = nobleInfo.getInteger(COMP_DONE_WEEK_CLASSED);
				int compDoneWeekNonClassed = nobleInfo.getInteger(COMP_DONE_WEEK_NON_CLASSED);
				int compDoneWeekTeam = nobleInfo.getInteger(COMP_DONE_WEEK_TEAM);
				boolean toSave = nobleInfo.getBool("to_save");
				
				try (PreparedStatement statement = con.prepareStatement(toSave ? OLYMPIAD_SAVE_NOBLES : OLYMPIAD_UPDATE_NOBLES))
				{
					if (toSave)
					{
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						statement.setInt(3, points);
						statement.setInt(4, compDone);
						statement.setInt(5, compWon);
						statement.setInt(6, compLost);
						statement.setInt(7, compDrawn);
						statement.setInt(8, compDoneWeek);
						statement.setInt(9, compDoneWeekClassed);
						statement.setInt(10, compDoneWeekNonClassed);
						statement.setInt(11, compDoneWeekTeam);
						
						nobleInfo.set("to_save", false);
					}
					else
					{
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, compWon);
						statement.setInt(4, compLost);
						statement.setInt(5, compDrawn);
						statement.setInt(6, compDoneWeek);
						statement.setInt(7, compDoneWeekClassed);
						statement.setInt(8, compDoneWeekNonClassed);
						statement.setInt(9, compDoneWeekTeam);
						statement.setInt(10, charId);
					}
					statement.execute();
				}
			}
		}
		catch (SQLException e)
		{
			_log.error("Olympiad System: Failed to save noblesse data to database: ", e);
		}
	}
	
	/**
	 * Save olympiad.properties file with current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_SAVE_DATA))
		{
			statement.setInt(1, _currentCycle);
			statement.setInt(2, _period);
			statement.setLong(3, _olympiadEnd);
			statement.setLong(4, _validationEnd);
			statement.setLong(5, _nextWeeklyChange);
			statement.setInt(6, _currentCycle);
			statement.setInt(7, _period);
			statement.setLong(8, _olympiadEnd);
			statement.setLong(9, _validationEnd);
			statement.setLong(10, _nextWeeklyChange);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.error("Olympiad System: Failed to save olympiad data to database: ", e);
		}
		//@formatter:off
		/*
		Properties OlympiadProperties = new Properties();
		try (FileOutputStream fos = new FileOutputStream(new File("./" + OLYMPIAD_DATA_FILE)))
		{
			OlympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
			OlympiadProperties.setProperty("Period", String.valueOf(_period));
			OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
			OlympiadProperties.setProperty("ValdationEnd", String.valueOf(_validationEnd));
			OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));
			OlympiadProperties.store(fos, "Olympiad Properties");
		}
		catch (Exception e)
		{
			_log.warn("Olympiad System: Unable to save olympiad properties to file: ", e);
		}
		*/
		//@formatter:on
	}
	
	protected void updateMonthlyData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(OLYMPIAD_MONTH_CLEAR);
			PreparedStatement ps2 = con.prepareStatement(OLYMPIAD_MONTH_CREATE))
		{
			ps1.execute();
			ps2.execute();
		}
		catch (SQLException e)
		{
			_log.error("Olympiad System: Failed to update monthly noblese data: ", e);
		}
	}
	
	protected void sortHerosToBe()
	{
		if (_period != 1)
		{
			return;
		}
		
		LogRecord record;
		if (_nobles != null)
		{
			_logResults.info("Noble,charid,classid,compDone,points");
			StatsSet nobleInfo;
			for (Entry<Integer, StatsSet> entry : _nobles.entrySet())
			{
				nobleInfo = entry.getValue();
				if (nobleInfo == null)
				{
					continue;
				}
				
				int charId = entry.getKey();
				int classId = nobleInfo.getInteger(CLASS_ID);
				String charName = nobleInfo.getString(CHAR_NAME);
				int points = nobleInfo.getInteger(POINTS);
				int compDone = nobleInfo.getInteger(COMP_DONE);
				
				record = new LogRecord(Level.INFO, charName);
				record.setParameters(new Object[]
				{
					charId,
					classId,
					compDone,
					points
				});
				_logResults.log(record);
			}
		}
		
		_heroesToBe = new FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROS))
		{
			ResultSet rset;
			StatsSet hero;
			List<StatsSet> soulHounds = new ArrayList<>();
			for (int element : HERO_IDS)
			{
				statement.setInt(1, element);
				rset = statement.executeQuery();
				statement.clearParameters();
				
				if (rset.next())
				{
					hero = new StatsSet();
					hero.set(CLASS_ID, element);
					hero.set(CHAR_ID, rset.getInt(CHAR_ID));
					hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
					
					if ((element == 132) || (element == 133)) // Male & Female Soulhounds rank as one hero class
					{
						hero = _nobles.get(hero.getInteger(CHAR_ID));
						hero.set(CHAR_ID, rset.getInt(CHAR_ID));
						soulHounds.add(hero);
					}
					else
					{
						record = new LogRecord(Level.INFO, "Hero " + hero.getString(CHAR_NAME));
						record.setParameters(new Object[]
						{
							hero.getInteger(CHAR_ID),
							hero.getInteger(CLASS_ID)
						});
						_logResults.log(record);
						_heroesToBe.add(hero);
					}
				}
				rset.close();
			}
			
			switch (soulHounds.size())
			{
				case 0:
				{
					break;
				}
				case 1:
				{
					hero = new StatsSet();
					StatsSet winner = soulHounds.get(0);
					hero.set(CLASS_ID, winner.getInteger(CLASS_ID));
					hero.set(CHAR_ID, winner.getInteger(CHAR_ID));
					hero.set(CHAR_NAME, winner.getString(CHAR_NAME));
					
					record = new LogRecord(Level.INFO, "Hero " + hero.getString(CHAR_NAME));
					record.setParameters(new Object[]
					{
						hero.getInteger(CHAR_ID),
						hero.getInteger(CLASS_ID)
					});
					_logResults.log(record);
					_heroesToBe.add(hero);
					break;
				}
				case 2:
				{
					hero = new StatsSet();
					StatsSet winner;
					StatsSet hero1 = soulHounds.get(0);
					StatsSet hero2 = soulHounds.get(1);
					int hero1Points = hero1.getInteger(POINTS);
					int hero2Points = hero2.getInteger(POINTS);
					int hero1Comps = hero1.getInteger(COMP_DONE);
					int hero2Comps = hero2.getInteger(COMP_DONE);
					int hero1Wins = hero1.getInteger(COMP_WON);
					int hero2Wins = hero2.getInteger(COMP_WON);
					
					if (hero1Points > hero2Points)
					{
						winner = hero1;
					}
					else if (hero2Points > hero1Points)
					{
						winner = hero2;
					}
					else
					{
						if (hero1Comps > hero2Comps)
						{
							winner = hero1;
						}
						else if (hero2Comps > hero1Comps)
						{
							winner = hero2;
						}
						else
						{
							if (hero1Wins > hero2Wins)
							{
								winner = hero1;
							}
							else
							{
								winner = hero2;
							}
						}
					}
					
					hero.set(CLASS_ID, winner.getInteger(CLASS_ID));
					hero.set(CHAR_ID, winner.getInteger(CHAR_ID));
					hero.set(CHAR_NAME, winner.getString(CHAR_NAME));
					
					record = new LogRecord(Level.INFO, "Hero " + hero.getString(CHAR_NAME));
					record.setParameters(new Object[]
					{
						hero.getInteger(CHAR_ID),
						hero.getInteger(CLASS_ID)
					});
					_logResults.log(record);
					_heroesToBe.add(hero);
					break;
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Olympiad System: Couldnt load heros from DB");
		}
	}
	
	public List<String> getClassLeaderBoard(int classId)
	{
		final List<String> names = new ArrayList<>();
		String query = Config.ALT_OLY_SHOW_MONTHLY_WINNERS ? ((classId == 132) ? GET_EACH_CLASS_LEADER_SOULHOUND : GET_EACH_CLASS_LEADER) : ((classId == 132) ? GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND : GET_EACH_CLASS_LEADER_CURRENT);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			ps.setInt(1, classId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					names.add(rset.getString(CHAR_NAME));
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Olympiad System: Couldn't load olympiad leaders from DB!");
		}
		return names;
	}
	
	public int getNoblessePasses(L2PcInstance player, boolean clear)
	{
		if ((player == null) || (_period != 1) || _noblesRank.isEmpty())
		{
			return 0;
		}
		
		final int objId = player.getObjectId();
		if (!_noblesRank.containsKey(objId))
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if ((noble == null) || (noble.getInteger(POINTS) == 0))
		{
			return 0;
		}
		
		final int rank = _noblesRank.get(objId);
		int points = (player.isHero() ? Config.ALT_OLY_HERO_POINTS : 0);
		switch (rank)
		{
			case 1:
				points += Config.ALT_OLY_RANK1_POINTS;
				break;
			case 2:
				points += Config.ALT_OLY_RANK2_POINTS;
				break;
			case 3:
				points += Config.ALT_OLY_RANK3_POINTS;
				break;
			case 4:
				points += Config.ALT_OLY_RANK4_POINTS;
				break;
			default:
				points += Config.ALT_OLY_RANK5_POINTS;
		}
		
		if (clear)
		{
			noble.set(POINTS, 0);
		}
		points *= Config.ALT_OLY_GP_PER_POINT;
		return points;
	}
	
	public int getNoblePoints(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(POINTS);
	}
	
	public int getLastNobleOlympiadPoints(int objId)
	{
		int result = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE charId = ?"))
		{
			ps.setInt(1, objId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getInt(1);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not load last olympiad points:", e);
		}
		return result;
	}
	
	public int getCompetitionDone(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE);
	}
	
	public int getCompetitionWon(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_WON);
	}
	
	public int getCompetitionLost(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_LOST);
	}
	
	/**
	 * Gets how many matches a noble character did in the week
	 * @param objId id of a noble character
	 * @return number of weekly competitions done
	 */
	public int getCompetitionDoneWeek(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEK);
	}
	
	/**
	 * Gets how many classed matches a noble character did in the week
	 * @param objId id of a noble character
	 * @return number of weekly <i>classed</i> competitions done
	 */
	public int getCompetitionDoneWeekClassed(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEK_CLASSED);
	}
	
	/**
	 * Gets how many non classed matches a noble character did in the week
	 * @param objId id of a noble character
	 * @return number of weekly <i>non classed</i> competitions done
	 */
	public int getCompetitionDoneWeekNonClassed(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEK_NON_CLASSED);
	}
	
	/**
	 * Gets how many team matches a noble character did in the week
	 * @param objId id of a noble character
	 * @return number of weekly <i>team</i> competitions done
	 */
	public int getCompetitionDoneWeekTeam(int objId)
	{
		if ((_nobles == null) || !_nobles.containsKey(objId))
		{
			return 0;
		}
		return _nobles.get(objId).getInteger(COMP_DONE_WEEK_TEAM);
	}
	
	/**
	 * Number of remaining matches a noble character can join in the week
	 * @param objId id of a noble character
	 * @return difference between maximum allowed weekly matches and currently done weekly matches.
	 */
	public int getRemainingWeeklyMatches(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES - getCompetitionDoneWeek(objId), 0);
	}
	
	/**
	 * Number of remaining <i>classed</i> matches a noble character can join in the week
	 * @param objId id of a noble character
	 * @return difference between maximum allowed weekly classed matches and currently done weekly classed matches.
	 */
	public int getRemainingWeeklyMatchesClassed(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED - getCompetitionDoneWeekClassed(objId), 0);
	}
	
	/**
	 * Number of remaining <i>non classed</i> matches a noble character can join in the week
	 * @param objId id of a noble character
	 * @return difference between maximum allowed weekly non classed matches and currently done weekly non classed matches.
	 */
	public int getRemainingWeeklyMatchesNonClassed(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED - getCompetitionDoneWeekNonClassed(objId), 0);
	}
	
	/**
	 * Number of remaining <i>team</i> matches a noble character can join in the week
	 * @param objId id of a noble character
	 * @return difference between maximum allowed weekly team matches and currently done weekly team matches.
	 */
	public int getRemainingWeeklyMatchesTeam(int objId)
	{
		return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_TEAM - getCompetitionDoneWeekTeam(objId), 0);
	}
	
	protected void deleteNobles()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_DELETE_ALL))
		{
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.warn("Olympiad System: Couldn't delete nobles from DB!");
		}
		_nobles.clear();
	}
	
	/**
	 * @param charId the noble object Id.
	 * @param data the stats set data to add.
	 * @return the old stats set if the noble is already present, null otherwise.
	 */
	protected static StatsSet addNobleStats(int charId, StatsSet data)
	{
		return _nobles.put(Integer.valueOf(charId), data);
	}
	
	public static Olympiad getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Olympiad _instance = new Olympiad();
	}
}
