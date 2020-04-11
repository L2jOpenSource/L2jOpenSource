package com.l2jfrozen.gameserver.model.entity.olympiad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.OlympiadStadiaManager;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.Hero;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.L2FastList;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import javolution.text.TextBuilder;

/**
 * @author godson
 */
public class Olympiad
{
	protected static final Logger LOGGER = Logger.getLogger(Olympiad.class);
	private static Olympiad instance;
	
	private static Map<Integer, StatsSet> nobles;
	private static Map<Integer, StatsSet> oldnobles;
	protected static L2FastList<StatsSet> heroesToBe;
	private static L2FastList<L2PcInstance> nonClassBasedRegisters;
	private static Map<Integer, L2FastList<L2PcInstance>> classBasedRegisters;
	public static final int OLY_MANAGER = 31688;
	public static List<L2Spawn> olymanagers = new ArrayList<>();
	
	private static final String OLYMPIAD_DATA_FILE = "config/olympiad.cfg";
	public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, " + "characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, " + "olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn "
		+ "FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.charId";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`charId`,`class_id`,`char_name`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`) VALUES (?,?,?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE charId=?";
	private static final String OLYMPIAD_UPDATE_OLD_NOBLES = "UPDATE olympiad_nobles_eom SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE charId = ?";
	private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= 9 AND olympiad_nobles.competitions_won >= 1 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_won/olympiad_nobles.competitions_done DESC LIMIT "
		+ Config.ALT_OLY_NUMBER_HEROS_EACH_CLASS;
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters " + "WHERE characters.obj_Id = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? " + "AND olympiad_nobles_eom.competitions_done >= 9 " + "ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC LIMIT 10";
	private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT characters.char_name from olympiad_nobles, characters " + "WHERE characters.obj_Id = olympiad_nobles.charId AND olympiad_nobles.class_id = ? " + "AND olympiad_nobles.competitions_done >= 9 " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC LIMIT 10";
	private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
	private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
	private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT * FROM olympiad_nobles";
	private static final String OLYMPIAD_LOAD_OLD_NOBLES = "SELECT olympiad_nobles_eom.charId, olympiad_nobles_eom.class_id, " + "characters.char_name, olympiad_nobles_eom.olympiad_points, olympiad_nobles_eom.competitions_done, " + "olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, olympiad_nobles_eom.competitions_drawn "
		+ "FROM olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.charId";
	
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
		118
	};
	
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 6PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 6 hours
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
	
	private static final int DEFAULT_POINTS = 18;
	protected static final int WEEKLY_POINTS = 3;
	
	public static final String CHAR_ID = "charId";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	public static final String COMP_DRAWN = "competitions_drawn";
	
	protected long olympiadEnd;
	protected long olympiadValidationEnd;
	
	/**
	 * The current period of the olympiad.<br>
	 * <b>0 -</b> Competition period<br>
	 * <b>1 -</b> Validation Period
	 */
	protected int period;
	protected long nextWeeklyChange;
	protected int currentCycle;
	private long compEnd;
	private Calendar compStart;
	protected static boolean inCompPeriod;
	protected static boolean compStarted = false;
	protected ScheduledFuture<?> scheduledCompStart;
	protected ScheduledFuture<?> scheduledCompEnd;
	protected ScheduledFuture<?> scheduledOlympiadEnd;
	protected ScheduledFuture<?> scheduledWeeklyTask;
	protected ScheduledFuture<?> scheduledValdationTask;
	
	protected static enum COMP_TYPE
	{
		CLASSED,
		NON_CLASSED
	}
	
	public static Olympiad getInstance()
	{
		if (instance == null)
		{
			instance = new Olympiad();
		}
		return instance;
	}
	
	public Olympiad()
	{
		load();
		
		if (period == 0)
		{
			init();
		}
	}
	
	public static Integer getStadiumCount()
	{
		return OlympiadManager.STADIUMS.length;
	}
	
	private void load()
	{
		nobles = new HashMap<>();
		oldnobles = new HashMap<>();
		
		final Properties OlympiadProperties = new Properties();
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File("./" + OLYMPIAD_DATA_FILE));
			OlympiadProperties.load(is);
		}
		catch (final Exception e)
		{
			LOGGER.warn(OLYMPIAD_DATA_FILE + " cannot be loaded... It will be created on next save or server shutdown..");
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (final Exception e)
			{
			}
		}
		
		currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
		period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
		olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
		olympiadValidationEnd = Long.parseLong(OlympiadProperties.getProperty("ValdationEnd", "0"));
		nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
		
		switch (period)
		{
			case 0:
				if (olympiadEnd == 0 || olympiadEnd < Calendar.getInstance().getTimeInMillis())
				{
					setNewOlympiadEnd();
				}
				else
				{
					scheduleWeeklyChange();
				}
				break;
			case 1:
				if (olympiadValidationEnd > Calendar.getInstance().getTimeInMillis())
				{
					scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					currentCycle++;
					period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			default:
				LOGGER.warn("Olympiad System: Omg something went wrong in loading!! Period = " + period);
				return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				StatsSet statData = new StatsSet();
				int charId = rset.getInt(CHAR_ID);
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set("to_save", false);
				
				nobles.put(charId, statData);
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Olympiad.load : Error loading noblesse data from database: ", e);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_OLD_NOBLES);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				StatsSet statData = new StatsSet();
				int charId = rset.getInt(CHAR_ID);
				statData.set(CLASS_ID, rset.getInt(CLASS_ID));
				statData.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statData.set(POINTS, rset.getInt(POINTS));
				statData.set(COMP_DONE, rset.getInt(COMP_DONE));
				statData.set(COMP_WON, rset.getInt(COMP_WON));
				statData.set(COMP_LOST, rset.getInt(COMP_LOST));
				statData.set(COMP_DRAWN, rset.getInt(COMP_DRAWN));
				statData.set("to_save", false);
				
				oldnobles.put(charId, statData);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Olympiad.load : Error loading noblesse data from database: ", e);
		}
		
		synchronized (this)
		{
			if (period == 0)
			{
				LOGGER.info("Olympiad currently in normal period");
			}
			else
			{
				LOGGER.info("Olympiad currently in validation eriod");
			}
			
			long milliToEnd;
			if (period == 0)
			{
				milliToEnd = getMillisToOlympiadEnd();
			}
			else
			{
				milliToEnd = getMillisToValidationEnd();
			}
			
			LOGGER.info("Minutes until period ends: " + Math.round(milliToEnd / 60000));
			
			if (period == 0)
			{
				milliToEnd = getMillisToWeekChange();
				
				LOGGER.info("Next weekly change is in " + Math.round(milliToEnd / 60000) + " minutes");
			}
		}
	}
	
	protected final void init()
	{
		if (period == 1)
		{
			return;
		}
		
		nonClassBasedRegisters = new L2FastList<>();
		classBasedRegisters = new HashMap<>();
		
		compStart = Calendar.getInstance();
		compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		compStart.set(Calendar.MINUTE, COMP_MIN);
		compEnd = compStart.getTimeInMillis() + COMP_PERIOD;
		
		if (scheduledOlympiadEnd != null)
		{
			scheduledOlympiadEnd.cancel(true);
		}
		
		scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), getMillisToOlympiadEnd());
		
		updateCompStatus();
	}
	
	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
			sm.addNumber(currentCycle);
			
			Announcements.getInstance().announceToAll(sm);
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
			
			if (scheduledWeeklyTask != null)
			{
				scheduledWeeklyTask.cancel(true);
			}
			
			saveNobleData();
			
			period = 1;
			sortHerosToBe();
			giveHeroBonus();
			Hero.getInstance().computeNewHeroes(heroesToBe);
			
			saveOlympiadStatus();
			updateMonthlyData();
			
			final Calendar validationEnd = Calendar.getInstance();
			olympiadValidationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
			
			scheduledValdationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}
	
	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");
			period = 0;
			currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
	
	public boolean registerNoble(final L2PcInstance noble, final boolean classBased)
	{
		SystemMessage sm;
		
		/** Begin Olympiad Restrictions */
		
		if (noble.isDead())
		{
			sm = new SystemMessage(SystemMessageId.CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
			noble.sendPacket(sm);
			return false;
		}
		
		if (noble.isRegisteredInFunEvent())
		{
			noble.sendMessage("You are already registered to another Event");
			return false;
		}
		
		if (noble.inObserverMode())
		{
			noble.sendMessage("You can not participate to Olympiad. You are in Observer Mode, try to restart!");
			return false;
		}
		
		if (!inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}
		
		if (noble.getBaseClass() != noble.getClassId().getId())
		{
			sm = new SystemMessage(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
			noble.sendPacket(sm);
			return false;
		}
		
		if (noble.isCursedWeaponEquiped())
		{
			sm = new SystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1);
			sm.addItemName(noble.getCursedWeaponEquipedId());
			noble.sendPacket(sm);
			return false;
		}
		
		if (noble.getInventoryLimit() * 0.8 <= noble.getInventory().getSize())
		{
			sm = new SystemMessage(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}
		
		if (getMillisToCompEnd() < 600000)
		{
			sm = new SystemMessage(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
			noble.sendPacket(sm);
			return false;
		}
		
		// Olympiad dualbox protection
		if (noble.activeBoxesCount > 1 && !Config.ALLOW_DUALBOX_OLY)
		{
			final List<String> players_in_boxes = noble.active_boxes_characters;
			
			if (players_in_boxes != null && players_in_boxes.size() > 1)
			{
				for (final String character_name : players_in_boxes)
				{
					final L2PcInstance player = L2World.getInstance().getPlayer(character_name);
					
					if (player == null)
					{
						continue;
					}
					
					if (player.getOlympiadGameId() > 0 || player.isInOlympiadMode())
					{
						noble.sendMessage("You are already participating in Olympiad with another char!");
						return false;
					}
				}
			}
		}
		
		/** End Olympiad Restrictions */
		
		if (classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final L2FastList<L2PcInstance> classed = classBasedRegisters.get(noble.getClassId().getId());
			for (final L2PcInstance participant : classed)
			{
				if (participant.getObjectId() == noble.getObjectId())
				{
					sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS);
					noble.sendPacket(sm);
					return false;
				}
			}
		}
		
		if (isRegisteredInComp(noble))
		{
			sm = new SystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!nobles.containsKey(noble.getObjectId()))
		{
			final StatsSet statDat = new StatsSet();
			statDat.set(CLASS_ID, noble.getClassId().getId());
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WON, 0);
			statDat.set(COMP_LOST, 0);
			statDat.set(COMP_DRAWN, 0);
			statDat.set("to_save", true);
			
			nobles.put(noble.getObjectId(), statDat);
		}
		
		if (classBased && getNoblePoints(noble.getObjectId()) < 3)
		{
			noble.sendMessage("Cant register when you have less than 3 points");
			return false;
		}
		if (!classBased && getNoblePoints(noble.getObjectId()) < 5)
		{
			noble.sendMessage("Cant register when you have less than 5 points");
			return false;
		}
		
		if (classBased)
		{
			if (classBasedRegisters.containsKey(noble.getClassId().getId()))
			{
				final L2FastList<L2PcInstance> classed = classBasedRegisters.get(noble.getClassId().getId());
				classed.add(noble);
				
				classBasedRegisters.remove(noble.getClassId().getId());
				classBasedRegisters.put(noble.getClassId().getId(), classed);
			}
			else
			{
				final L2FastList<L2PcInstance> classed = new L2FastList<>();
				classed.add(noble);
				
				classBasedRegisters.put(noble.getClassId().getId(), classed);
			}
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
			noble.sendPacket(sm);
		}
		else
		{
			nonClassBasedRegisters.add(noble);
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
			noble.sendPacket(sm);
		}
		
		noble.setIsInOlympiadMode(true);
		
		return true;
	}
	
	protected static int getNobleCount()
	{
		return nobles.size();
	}
	
	protected static StatsSet getNobleStats(final int playerId)
	{
		return nobles.get(playerId);
	}
	
	protected static synchronized void updateNobleStats(final int playerId, final StatsSet stats)
	{
		nobles.remove(playerId);
		nobles.put(playerId, stats);
	}
	
	protected static synchronized void updateOldNobleStats(final int playerId, final StatsSet stats)
	{
		oldnobles.remove(playerId);
		oldnobles.put(playerId, stats);
		Olympiad.getInstance().saveOldNobleData(playerId);
	}
	
	protected static L2FastList<L2PcInstance> getRegisteredNonClassBased()
	{
		return nonClassBasedRegisters;
	}
	
	protected static Map<Integer, L2FastList<L2PcInstance>> getRegisteredClassBased()
	{
		return classBasedRegisters;
	}
	
	protected static L2FastList<Integer> hasEnoughRegisteredClassed()
	{
		final L2FastList<Integer> result = new L2FastList<>();
		
		for (final Integer classList : getRegisteredClassBased().keySet())
		{
			if (getRegisteredClassBased().get(classList).size() >= Config.ALT_OLY_CLASSED)
			{
				result.add(classList);
			}
		}
		
		if (!result.isEmpty())
		{
			return result;
		}
		return null;
	}
	
	protected static boolean hasEnoughRegisteredNonClassed()
	{
		return Olympiad.getRegisteredNonClassBased().size() >= Config.ALT_OLY_NONCLASSED;
	}
	
	protected static void clearRegistered()
	{
		nonClassBasedRegisters.clear();
		classBasedRegisters.clear();
	}
	
	public boolean isRegistered(final L2PcInstance noble)
	{
		boolean result = false;
		
		if (nonClassBasedRegisters != null && nonClassBasedRegisters.contains(noble))
		{
			result = true;
		}
		else if (classBasedRegisters != null && classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final L2FastList<L2PcInstance> classed = classBasedRegisters.get(noble.getClassId().getId());
			if (classed != null && classed.contains(noble))
			{
				result = true;
			}
		}
		
		return result;
	}
	
	public boolean unRegisterNoble(final L2PcInstance noble)
	{
		SystemMessage sm;
		
		if (!inCompPeriod)
		{
			sm = new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!noble.isNoble())
		{
			sm = new SystemMessage(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			noble.sendPacket(sm);
			return false;
		}
		
		if (!isRegistered(noble))
		{
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			noble.sendPacket(sm);
			return false;
		}
		
		for (final OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
		{
			if (game == null)
			{
				continue;
			}
			
			if ((game.playerOne != null && game.playerOne.getObjectId() == noble.getObjectId()) || (game.playerTwo != null && game.playerTwo.getObjectId() == noble.getObjectId()))
			{
				noble.sendMessage("Can't deregister whilst you are already selected for a game");
				return false;
			}
		}
		
		if (nonClassBasedRegisters.contains(noble))
		{
			nonClassBasedRegisters.remove(noble);
		}
		else
		{
			final L2FastList<L2PcInstance> classed = classBasedRegisters.get(noble.getClassId().getId());
			classed.remove(noble);
			
			classBasedRegisters.remove(noble.getClassId().getId());
			classBasedRegisters.put(noble.getClassId().getId(), classed);
		}
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
		noble.sendPacket(sm);
		
		noble.setIsInOlympiadMode(false);
		
		return true;
	}
	
	public void removeDisconnectedCompetitor(final L2PcInstance player)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(player.getOlympiadGameId()) != null)
		{
			OlympiadManager.getInstance().getOlympiadGame(player.getOlympiadGameId()).handleDisconnect(player);
		}
		
		final L2FastList<L2PcInstance> classed = classBasedRegisters.get(player.getClassId().getId());
		
		if (nonClassBasedRegisters.contains(player))
		{
			nonClassBasedRegisters.remove(player);
		}
		else if (classed != null && classed.contains(player))
		{
			classed.remove(player);
			
			classBasedRegisters.remove(player.getClassId().getId());
			classBasedRegisters.put(player.getClassId().getId(), classed);
		}
	}
	
	public void notifyCompetitorDamage(final L2PcInstance player, final int damage, final int gameId)
	{
		if (OlympiadManager.getInstance().getOlympiadGames().get(gameId) != null)
		{
			OlympiadManager.getInstance().getOlympiadGames().get(gameId).addDamage(player, damage);
		}
	}
	
	private void updateCompStatus()
	{
		synchronized (this)
		{
			final long milliToStart = getMillisToCompBegin();
			
			final double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000) - numSecs) / 60;
			final int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			final int numHours = (int) Math.floor(countDown % 24);
			final int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			LOGGER.info("Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
			LOGGER.info("Event starts/started : " + compStart.getTime());
		}
		
		scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (isOlympiadEnd())
			{
				return;
			}
			
			inCompPeriod = true;
			final OlympiadManager om = OlympiadManager.getInstance();
			
			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
			LOGGER.info("Olympiad System: Olympiad Game Started");
			
			final Thread olyCycle = new Thread(om);
			olyCycle.start();
			
			final long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)), regEnd);
			}
			
			scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (isOlympiadEnd())
				{
					return;
				}
				inCompPeriod = false;
				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
				LOGGER.info("Olympiad System: Olympiad Game Ended");
				
				while (OlympiadGame.battleStarted)
				{
					try
					{
						// wait 1 minutes for end of pendings games
						Thread.sleep(60000);
					}
					catch (final InterruptedException e)
					{
					}
				}
				saveOlympiadStatus();
				
				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private long getMillisToOlympiadEnd()
	{
		// if (_olympiadEnd > Calendar.getInstance().getTimeInMillis())
		return (olympiadEnd - Calendar.getInstance().getTimeInMillis());
		// return 10L;
	}
	
	public void manualSelectHeroes()
	{
		if (scheduledOlympiadEnd != null)
		{
			scheduledOlympiadEnd.cancel(true);
		}
		
		scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), 0);
	}
	
	protected long getMillisToValidationEnd()
	{
		if (olympiadValidationEnd > Calendar.getInstance().getTimeInMillis())
		{
			return (olympiadValidationEnd - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	public boolean isOlympiadEnd()
	{
		return (period != 0);
	}
	
	protected void setNewOlympiadEnd()
	{
		
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			setNewOlympiadEndCustom();
			return;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addNumber(currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		final Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		olympiadEnd = currentTime.getTimeInMillis();
		
		final Calendar nextChange = Calendar.getInstance();
		nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		scheduleWeeklyChange();
	}
	
	public boolean inCompPeriod()
	{
		return inCompPeriod;
	}
	
	private long getMillisToCompBegin()
	{
		if (compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && compEnd > Calendar.getInstance().getTimeInMillis())
		{
			return 10L;
		}
		
		if (compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return (compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		}
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		compStart = Calendar.getInstance();
		compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		compStart.set(Calendar.MINUTE, COMP_MIN);
		compStart.add(Calendar.HOUR_OF_DAY, 24);
		compEnd = compStart.getTimeInMillis() + COMP_PERIOD;
		
		LOGGER.info("Olympiad System: New Schedule @ " + compStart.getTime());
		
		return (compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	protected long getMillisToCompEnd()
	{
		// if (_compEnd > Calendar.getInstance().getTimeInMillis())
		return (compEnd - Calendar.getInstance().getTimeInMillis());
		// return 10L;
	}
	
	private long getMillisToWeekChange()
	{
		if (nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
		{
			return (nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	private void scheduleWeeklyChange()
	{
		
		if (Config.ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS)
		{
			schedulePointsRestoreCustom();
			return;
		}
		
		scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			addWeeklyPoints();
			LOGGER.info("Olympiad System: Added weekly points to nobles");
			
			final Calendar nextChange = Calendar.getInstance();
			nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}, getMillisToWeekChange(), WEEKLY_PERIOD);
	}
	
	protected synchronized void addWeeklyPoints()
	{
		if (period == 1)
		{
			return;
		}
		
		for (final Integer nobleId : nobles.keySet())
		{
			final StatsSet nobleInfo = nobles.get(nobleId);
			int currentPoints = nobleInfo.getInteger(POINTS);
			currentPoints += WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);
			
			updateNobleStats(nobleId, nobleInfo);
		}
	}
	
	public Map<Integer, String> getMatchList()
	{
		return OlympiadManager.getInstance().getAllTitles();
	}
	
	// returns the players for the given olympiad game Id
	public L2PcInstance[] getPlayers(final int Id)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(Id) == null)
		{
			return null;
		}
		return OlympiadManager.getInstance().getOlympiadGame(Id).getPlayers();
	}
	
	public int getCurrentCycle()
	{
		return currentCycle;
	}
	
	public static void addSpectator(final int id, final L2PcInstance spectator, final boolean storeCoords)
	{
		if (getInstance().isRegisteredInComp(spectator))
		{
			spectator.sendPacket(new SystemMessage(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME));
			return;
		}
		if (spectator.isRegisteredInFunEvent())
		{
			spectator.sendMessage("You are already registered to an Event");
			return;
		}
		
		OlympiadManager.STADIUMS[id].addSpectator(id, spectator, storeCoords);
		if (OlympiadManager.getInstance().getOlympiadGame(id) != null)
		{
			OlympiadManager.getInstance().getOlympiadGame(id).sendPlayersStatus(spectator);
		}
	}
	
	public static int getSpectatorArena(final L2PcInstance player)
	{
		for (int i = 0; i < OlympiadManager.STADIUMS.length; i++)
		{
			if (OlympiadManager.STADIUMS[i].getSpectators().contains(player))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static void removeSpectator(final int id, final L2PcInstance spectator)
	{
		OlympiadManager.STADIUMS[id].removeSpectator(spectator);
	}
	
	public L2FastList<L2PcInstance> getSpectators(final int id)
	{
		if (OlympiadManager.getInstance().getOlympiadGame(id) == null)
		{
			return null;
		}
		return OlympiadManager.STADIUMS[id].getSpectators();
	}
	
	public Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return OlympiadManager.getInstance().getOlympiadGames();
	}
	
	public boolean playerInStadia(final L2PcInstance player)
	{
		return (OlympiadStadiaManager.getInstance().getStadium(player) != null);
	}
	
	public int[] getWaitingList()
	{
		final int[] array = new int[2];
		
		if (!inCompPeriod())
		{
			return null;
		}
		
		int classCount = 0;
		
		if (!classBasedRegisters.isEmpty())
		{
			for (final L2FastList<L2PcInstance> classed : classBasedRegisters.values())
			{
				classCount += classed.size();
			}
		}
		
		array[0] = classCount;
		array[1] = nonClassBasedRegisters.size();
		
		return array;
	}
	
	/**
	 * Save noblesse data to database
	 */
	protected synchronized void saveNobleData()
	{
		if (nobles == null || nobles.isEmpty())
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			for (int nobleId : nobles.keySet())
			{
				StatsSet nobleInfo = nobles.get(nobleId);
				
				if (nobleInfo == null)
				{
					continue;
				}
				
				int charId = nobleId;
				int classId = nobleInfo.getInteger(CLASS_ID);
				String charName = nobleInfo.getString(CHAR_NAME);
				int points = nobleInfo.getInteger(POINTS);
				int compDone = nobleInfo.getInteger(COMP_DONE);
				int compWon = nobleInfo.getInteger(COMP_WON);
				int compLost = nobleInfo.getInteger(COMP_LOST);
				int compDrawn = nobleInfo.getInteger(COMP_DRAWN);
				boolean toSave = nobleInfo.getBool("to_save");
				
				if (toSave)
				{
					try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES))
					{
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						statement.setString(3, charName);
						statement.setInt(4, points);
						statement.setInt(5, compDone);
						statement.setInt(6, compWon);
						statement.setInt(7, compLost);
						statement.setInt(8, compDrawn);
						
						nobleInfo.set("to_save", false);
						
						updateNobleStats(nobleId, nobleInfo);
						
						statement.executeUpdate();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES))
					{
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, compWon);
						statement.setInt(4, compLost);
						statement.setInt(5, compDrawn);
						statement.setInt(6, charId);
						
						statement.executeUpdate();
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Olympiad.saveNobleData : Failed to save noblesse data to database: ", e);
		}
	}
	
	/**
	 * Save noblesse data to database
	 * @param nobleId
	 */
	protected synchronized void saveOldNobleData(final int nobleId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_UPDATE_OLD_NOBLES))
		{
			StatsSet nobleInfo = oldnobles.get(nobleId);
			
			if (nobleInfo == null)
			{
				return;
			}
			
			int charId = nobleId;
			int points = nobleInfo.getInteger(POINTS);
			int compDone = nobleInfo.getInteger(COMP_DONE);
			int compWon = nobleInfo.getInteger(COMP_WON);
			int compLost = nobleInfo.getInteger(COMP_LOST);
			int compDrawn = nobleInfo.getInteger(COMP_DRAWN);
			
			statement.setInt(1, points);
			statement.setInt(2, compDone);
			statement.setInt(3, compWon);
			statement.setInt(4, compLost);
			statement.setInt(5, compDrawn);
			statement.setInt(6, charId);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.error("Olympiad.saveOldNobleData : Failed to save old noblesse data to database: ", e);
		}
	}
	
	/**
	 * Save olympiad.properties file with current olympiad status and update noblesse table in database
	 */
	public void saveOlympiadStatus()
	{
		saveNobleData();
		
		final Properties OlympiadProperties = new Properties();
		try (FileOutputStream fos = new FileOutputStream(new File("./" + OLYMPIAD_DATA_FILE));)
		{
			OlympiadProperties.setProperty("CurrentCycle", String.valueOf(currentCycle));
			OlympiadProperties.setProperty("Period", String.valueOf(period));
			OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(olympiadEnd));
			OlympiadProperties.setProperty("ValdationEnd", String.valueOf(olympiadValidationEnd));
			OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(nextWeeklyChange));
			
			final GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
			gc.clear();
			gc.setTimeInMillis(nextWeeklyChange);
			
			OlympiadProperties.setProperty("NextWeeklyChange_DateFormat", DateFormat.getDateTimeInstance().format(gc.getTime()));
			// LOGGER.info("NextPoints: "+DateFormat.getInstance().format(gc.getTime()));
			
			gc.clear();
			gc.setTimeInMillis(olympiadEnd);
			
			OlympiadProperties.setProperty("OlympiadEnd_DateFormat", DateFormat.getDateTimeInstance().format(gc.getTime()));
			// LOGGER.info("NextOlyDate: "+DateFormat.getInstance().format(gc.getTime()));
			
			OlympiadProperties.store(fos, "Olympiad Properties");
		}
		catch (final Exception e)
		{
			LOGGER.warn("Olympiad System: Unable to save olympiad properties to file: ", e);
		}
		
		LOGGER.info("Olympiad System: Data saved!!");
	}
	
	protected void updateMonthlyData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_MONTH_CLEAR))
			{
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_MONTH_CREATE))
			{
				statement.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Olympiad.updateMonthlyData : Failed to update monthly noblese data", e);
		}
	}
	
	protected void sortHerosToBe()
	{
		if (period != 1)
		{
			return;
		}
		
		if (nobles != null)
		{
			for (final Integer nobleId : nobles.keySet())
			{
				final StatsSet nobleInfo = nobles.get(nobleId);
				
				if (nobleInfo == null)
				{
					continue;
				}
				
				final int charId = nobleId;
				final int classId = nobleInfo.getInteger(CLASS_ID);
				final String charName = nobleInfo.getString(CHAR_NAME);
				final int points = nobleInfo.getInteger(POINTS);
				final int compDone = nobleInfo.getInteger(COMP_DONE);
				
				logResult(charName, "", Double.valueOf(charId), Double.valueOf(classId), compDone, points, "noble-charId-classId-compdone-points", 0, "");
			}
		}
		
		heroesToBe = new L2FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			
			StatsSet hero;
			for (int HERO_ID : HERO_IDS)
			{
				try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROS))
				{
					statement.setInt(1, HERO_ID);
					
					try (ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							hero = new StatsSet();
							hero.set(CLASS_ID, HERO_ID);
							hero.set(CHAR_ID, rset.getInt(CHAR_ID));
							hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
							
							logResult(hero.getString(CHAR_NAME), "", hero.getDouble(CHAR_ID), hero.getDouble(CLASS_ID), 0, 0, "awarded hero", 0, "");
							heroesToBe.add(hero);
						}
					}
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("Olympiad.sortHerosToBe : Couldnt load heros from DB", e);
		}
	}
	
	public L2FastList<String> getClassLeaderBoard(final int classId)
	{
		L2FastList<String> names = new L2FastList<>();
		String sql = GET_EACH_CLASS_LEADER_CURRENT;
		
		if (Config.ALT_OLY_SHOW_MONTHLY_WINNERS)
		{
			sql = GET_EACH_CLASS_LEADER;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql))
		{
			statement.setInt(1, classId);
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					names.add(rset.getString(CHAR_NAME));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warn("Olympiad.getClassLeaderBoard : Couldnt load olympiad leaders from DB", e);
		}
		
		return names;
	}
	
	protected void giveHeroBonus()
	{
		if (heroesToBe.size() == 0)
		{
			return;
		}
		
		for (final StatsSet hero : heroesToBe)
		{
			final int charId = hero.getInteger(CHAR_ID);
			
			final StatsSet noble = nobles.get(charId);
			int currentPoints = noble.getInteger(POINTS);
			currentPoints += Config.ALT_OLY_HERO_POINTS;
			noble.set(POINTS, currentPoints);
			
			updateNobleStats(charId, noble);
		}
	}
	
	public int getNoblessePasses(final int objId)
	{
		if (period == 1)
		{
			if (nobles.isEmpty())
			{
				return 0;
			}
			
			final StatsSet noble = nobles.get(objId);
			if (noble == null)
			{
				return 0;
			}
			int points = noble.getInteger(POINTS);
			if (points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH)
			{
				return 0;
			}
			
			noble.set(POINTS, 0);
			updateNobleStats(objId, noble);
			
			points *= Config.ALT_OLY_GP_PER_POINT;
			
			return points;
		}
		
		if (oldnobles.isEmpty())
		{
			return 0;
		}
		
		final StatsSet noble = oldnobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		int points = noble.getInteger(POINTS);
		if (points <= Config.ALT_OLY_MIN_POINT_FOR_EXCH)
		{
			return 0;
		}
		
		noble.set(POINTS, 0);
		updateOldNobleStats(objId, noble);
		
		points *= Config.ALT_OLY_GP_PER_POINT;
		
		return points;
	}
	
	public boolean isRegisteredInComp(final L2PcInstance player)
	{
		boolean result = isRegistered(player);
		
		if (inCompPeriod)
		{
			for (final OlympiadGame game : OlympiadManager.getInstance().getOlympiadGames().values())
			{
				if ((game.playerOne != null && game.playerOne.getObjectId() == player.getObjectId()) || (game.playerTwo != null && game.playerTwo.getObjectId() == player.getObjectId()))
				{
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public int getNoblePoints(final int objId)
	{
		if (nobles.isEmpty())
		{
			return 0;
		}
		
		final StatsSet noble = nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(POINTS);
		
		return points;
	}
	
	public int getCompetitionDone(final int objId)
	{
		if (nobles.isEmpty())
		{
			return 0;
		}
		
		final StatsSet noble = nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_DONE);
		
		return points;
	}
	
	public int getCompetitionWon(final int objId)
	{
		if (nobles.isEmpty())
		{
			return 0;
		}
		
		final StatsSet noble = nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_WON);
		
		return points;
	}
	
	public int getCompetitionLost(final int objId)
	{
		if (nobles.isEmpty())
		{
			return 0;
		}
		
		final StatsSet noble = nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_LOST);
		
		return points;
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
			LOGGER.error("Olympiad.deleteNobles : Couldnt delete nobles from DB", e);
		}
		
		oldnobles.clear();
		oldnobles = nobles;
		nobles = new HashMap<>();
	}
	
	/**
	 * Logs result of Olympiad to a csv file.
	 * @param playerOne
	 * @param playerTwo
	 * @param p1hp
	 * @param p2hp
	 * @param p1dmg
	 * @param p2dmg
	 * @param result
	 * @param points
	 * @param classed
	 */
	public static synchronized void logResult(final String playerOne, final String playerTwo, final Double p1hp, final Double p2hp, final int p1dmg, final int p2dmg, final String result, final int points, final String classed)
	{
		if (!Config.ALT_OLY_LOG_FIGHTS)
		{
			return;
		}
		
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String date = formatter.format(new Date());
		FileWriter save = null;
		try
		{
			final File file = new File("log/olympiad.csv");
			
			boolean writeHead = false;
			if (!file.exists())
			{
				writeHead = true;
			}
			
			save = new FileWriter(file, true);
			
			if (writeHead)
			{
				final String header = "Date,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Result,Points,Classed\r\n";
				save.write(header);
			}
			
			final String out = date + "," + playerOne + "," + playerTwo + "," + p1hp + "," + p2hp + "," + p1dmg + "," + p2dmg + "," + result + "," + points + "," + classed + "\r\n";
			save.write(out);
		}
		catch (final IOException e)
		{
			LOGGER.warn("Olympiad System: Olympiad LOGGER could not be saved: ", e);
		}
		finally
		{
			try
			{
				if (save != null)
				{
					save.close();
				}
			}
			catch (final Exception e)
			{
			}
		}
	}
	
	public static void sendMatchList(final L2PcInstance player)
	{
		final NpcHtmlMessage message = new NpcHtmlMessage(0);
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<center><br>Grand Olympiad Game View<table width=270 border=0 bgcolor=\"000000\">");
		replyMSG.append("<tr><td fixwidth=30>NO.</td><td fixwidth=60>Status</td><td>Player1 / Player2</td></tr>");
		
		final Map<Integer, String> matches = getInstance().getMatchList();
		for (int i = 0; i < Olympiad.getStadiumCount(); i++)
		{
			final int arenaID = i + 1;
			String players = "&nbsp;";
			String state = "Initial State";
			if (matches.containsKey(i))
			{
				state = "In Progress";
				players = matches.get(i);
			}
			replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h OlympiadArenaChange " + i + "\">" + arenaID + "</a></td><td fixwidth=60>" + state + "</td><td>" + players + "</td></tr>");
		}
		replyMSG.append("</table></center></body></html>");
		
		message.setHtml(replyMSG.toString());
		player.sendPacket(message);
	}
	
	public static void bypassChangeArena(final String command, final L2PcInstance player)
	{
		final String[] commands = command.split(" ");
		final int id = Integer.parseInt(commands[1]);
		final int arena = getSpectatorArena(player);
		if (arena >= 0)
		{
			Olympiad.removeSpectator(arena, player);
		}
		Olympiad.addSpectator(id, player, false);
	}
	
	protected void setNewOlympiadEndCustom()
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addNumber(currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		final Calendar currentTime = Calendar.getInstance();
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		
		final Calendar nextChange = Calendar.getInstance();
		
		switch (Config.ALT_OLY_PERIOD)
		{
			case DAY:
			{
				currentTime.add(Calendar.DAY_OF_MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				
				if (Config.ALT_OLY_PERIOD_MULTIPLIER >= 14)
				{
					nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else if (Config.ALT_OLY_PERIOD_MULTIPLIER >= 7)
				{
					nextWeeklyChange = nextChange.getTimeInMillis() + (WEEKLY_PERIOD / 2);
				}
				else
				{
					// nothing to do, too low period
				}
				
			}
				break;
			case WEEK:
			{
				currentTime.add(Calendar.WEEK_OF_MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				
				if (Config.ALT_OLY_PERIOD_MULTIPLIER > 1)
				{
					nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				}
				else
				{
					nextWeeklyChange = nextChange.getTimeInMillis() + (WEEKLY_PERIOD / 2);
				}
				
			}
				break;
			case MONTH:
			{
				currentTime.add(Calendar.MONTH, Config.ALT_OLY_PERIOD_MULTIPLIER);
				currentTime.add(Calendar.DAY_OF_MONTH, -1); // last day is for validation
				
				nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
				
			}
				break;
		}
		
		olympiadEnd = currentTime.getTimeInMillis();
		
		scheduleWeeklyChange();
	}
	
	private void schedulePointsRestoreCustom()
	{
		long final_change_period = WEEKLY_PERIOD;
		
		switch (Config.ALT_OLY_PERIOD)
		{
			case DAY:
			{
				
				if (Config.ALT_OLY_PERIOD_MULTIPLIER < 10)
				{
					
					final_change_period = WEEKLY_PERIOD / 2;
					
				}
				
			}
				break;
			case WEEK:
			{
				
				if (Config.ALT_OLY_PERIOD_MULTIPLIER == 1)
				{
					final_change_period = WEEKLY_PERIOD / 2;
				}
				
			}
				break;
		}
		
		scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new OlympiadPointsRestoreTask(final_change_period), getMillisToWeekChange(), final_change_period);
		
	}
	
	class OlympiadPointsRestoreTask implements Runnable
	{
		
		private final long restoreTime;
		
		public OlympiadPointsRestoreTask(final long restoreTime)
		{
			this.restoreTime = restoreTime;
		}
		
		@Override
		public void run()
		{
			addWeeklyPoints();
			LOGGER.info("Olympiad System: Added points to nobles");
			
			final Calendar nextChange = Calendar.getInstance();
			nextWeeklyChange = nextChange.getTimeInMillis() + restoreTime;
		}
		
	}
}
