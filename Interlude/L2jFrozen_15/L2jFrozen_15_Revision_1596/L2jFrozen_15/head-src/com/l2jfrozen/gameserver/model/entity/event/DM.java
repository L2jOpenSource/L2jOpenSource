package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.event.manager.EventTask;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

import javolution.text.TextBuilder;

public class DM implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(DM.class);
	private static String eventName = new String();
	private static String eventDesc = new String();
	private static String joiningLocationName = new String();
	private static L2Spawn npcSpawn;
	private static boolean joining = false;
	private static boolean teleport = false;
	private static boolean started = false;
	private static boolean aborted = false;
	private static boolean sitForced = false;
	private static boolean inProgress = false;
	protected static int npcId = 0;
	protected static int npcX = 0;
	protected static int npcY = 0;
	protected static int npcZ = 0;
	protected static int npcHeading = 0;
	protected static int rewardId = 0;
	protected static int rewardAmount = 0;
	protected static int minlvl = 0;
	protected static int maxlvl = 0;
	protected static int joinTime = 0;
	protected static int eventTime = 0;
	protected static int minPlayers = 0;
	protected static int maxPlayers = 0;
	protected static int topKills = 0;
	protected static int playerColors = 0;
	protected static int playerX = 0;
	protected static int playerY = 0;
	protected static int playerZ = 0;
	private static long intervalBetweenMatchs = 0;
	private String startEventTime;
	protected static boolean teamEvent = false;
	public static Vector<L2PcInstance> players = new Vector<>();
	public static List<L2PcInstance> topPlayers = new ArrayList<>();
	public static Vector<String> savePlayers = new Vector<>();
	
	/**
	 * Instantiates a new dM.
	 */
	private DM()
	{
	}
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static DM getNewInstance()
	{
		return new DM();
	}
	
	public static String get_eventName()
	{
		return eventName;
	}
	
	public static boolean set_eventName(final String eventName)
	{
		if (!is_inProgress())
		{
			DM.eventName = eventName;
			return true;
		}
		return false;
	}
	
	public static String get_eventDesc()
	{
		return eventDesc;
	}
	
	public static boolean set_eventDesc(final String eventDesc)
	{
		if (!is_inProgress())
		{
			DM.eventDesc = eventDesc;
			return true;
		}
		return false;
	}
	
	public static String get_joiningLocationName()
	{
		return joiningLocationName;
	}
	
	public static boolean set_joiningLocationName(final String joiningLocationName)
	{
		if (!is_inProgress())
		{
			DM.joiningLocationName = joiningLocationName;
			return true;
		}
		return false;
	}
	
	public static int get_npcId()
	{
		return npcId;
	}
	
	public static boolean set_npcId(final int npcId)
	{
		if (!is_inProgress())
		{
			DM.npcId = npcId;
			return true;
		}
		return false;
	}
	
	public static Location get_npcLocation()
	{
		final Location npc_loc = new Location(npcX, npcY, npcZ, npcHeading);
		
		return npc_loc;
	}
	
	public static int get_rewardId()
	{
		return rewardId;
	}
	
	public static boolean set_rewardId(final int rewardId)
	{
		if (!is_inProgress())
		{
			DM.rewardId = rewardId;
			return true;
		}
		return false;
	}
	
	public static int get_rewardAmount()
	{
		return rewardAmount;
	}
	
	public static boolean set_rewardAmount(final int rewardAmount)
	{
		if (!is_inProgress())
		{
			DM.rewardAmount = rewardAmount;
			return true;
		}
		return false;
	}
	
	public static int get_minlvl()
	{
		return minlvl;
	}
	
	public static boolean set_minlvl(final int minlvl)
	{
		if (!is_inProgress())
		{
			DM.minlvl = minlvl;
			return true;
		}
		return false;
	}
	
	public static int get_maxlvl()
	{
		return maxlvl;
	}
	
	public static boolean set_maxlvl(final int maxlvl)
	{
		if (!is_inProgress())
		{
			DM.maxlvl = maxlvl;
			return true;
		}
		return false;
	}
	
	public static int get_joinTime()
	{
		return joinTime;
	}
	
	public static boolean set_joinTime(final int joinTime)
	{
		if (!is_inProgress())
		{
			DM.joinTime = joinTime;
			return true;
		}
		return false;
	}
	
	public static int get_eventTime()
	{
		return eventTime;
	}
	
	public static boolean set_eventTime(final int eventTime)
	{
		if (!is_inProgress())
		{
			DM.eventTime = eventTime;
			return true;
		}
		return false;
	}
	
	public static int get_minPlayers()
	{
		return minPlayers;
	}
	
	public static boolean set_minPlayers(final int minPlayers)
	{
		if (!is_inProgress())
		{
			DM.minPlayers = minPlayers;
			return true;
		}
		return false;
	}
	
	public static int get_maxPlayers()
	{
		return maxPlayers;
	}
	
	public static boolean set_maxPlayers(final int maxPlayers)
	{
		if (!is_inProgress())
		{
			DM.maxPlayers = maxPlayers;
			return true;
		}
		return false;
	}
	
	public static long get_intervalBetweenMatchs()
	{
		return intervalBetweenMatchs;
	}
	
	public static boolean set_intervalBetweenMatchs(final long intervalBetweenMatchs)
	{
		if (!is_inProgress())
		{
			DM.intervalBetweenMatchs = intervalBetweenMatchs;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the start event time.
	 * @return the startEventTime
	 */
	public String getStartEventTime()
	{
		return startEventTime;
	}
	
	/**
	 * Sets the start event time.
	 * @param  startEventTime the startEventTime to set
	 * @return                true, if successful
	 */
	public boolean setStartEventTime(final String startEventTime)
	{
		if (!is_inProgress())
		{
			this.startEventTime = startEventTime;
			return true;
		}
		return false;
	}
	
	public static boolean is_joining()
	{
		return joining;
	}
	
	public static boolean is_teleport()
	{
		return teleport;
	}
	
	public static boolean isStarted()
	{
		return started;
	}
	
	public static boolean is_aborted()
	{
		return aborted;
	}
	
	public static boolean is_sitForced()
	{
		return sitForced;
	}
	
	public static boolean is_inProgress()
	{
		return inProgress;
	}
	
	/**
	 * Check max level.
	 * @param  maxlvl the maxlvl
	 * @return        true, if successful
	 */
	public static boolean checkMaxLevel(final int maxlvl)
	{
		if (minlvl >= maxlvl)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check min level.
	 * @param  minlvl the minlvl
	 * @return        true, if successful
	 */
	public static boolean checkMinLevel(final int minlvl)
	{
		if (maxlvl <= minlvl)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * returns true if participated players is higher or equal then minimum needed players.
	 * @param  players the players
	 * @return         true, if successful
	 */
	public static boolean checkMinPlayers(final int players)
	{
		if (minPlayers <= players)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * returns true if max players is higher or equal then participated players.
	 * @param  players the players
	 * @return         true, if successful
	 */
	public static boolean checkMaxPlayers(final int players)
	{
		if (maxPlayers > players)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check start join ok.
	 * @return true, if successful
	 */
	public static boolean checkStartJoinOk()
	{
		if (started || teleport || joining || eventName.equals("") || joiningLocationName.equals("") || eventDesc.equals("") || npcId == 0 || npcX == 0 || npcY == 0 || npcZ == 0 || rewardId == 0 || rewardAmount == 0)
		{
			return false;
		}
		
		if (teamEvent)
		{
			if (!checkStartJoinTeamInfo())
			{
				return false;
			}
		}
		else
		{
			if (!checkStartJoinPlayerInfo())
			{
				return false;
			}
		}
		
		if (!Config.ALLOW_EVENTS_DURING_OLY && Olympiad.getInstance().inCompPeriod())
		{
			return false;
		}
		
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null && castle.getSiege() != null && castle.getSiege().getIsInProgress())
			{
				return false;
			}
		}
		
		if (!checkOptionalEventStartJoinOk())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check start join team info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinTeamInfo()
	{
		// TODO be integrated
		return true;
	}
	
	/**
	 * Check start join player info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinPlayerInfo()
	{
		if (playerX == 0 || playerY == 0 || playerZ == 0 || playerColors == 0)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check auto event start join ok.
	 * @return true, if successful
	 */
	private static boolean checkAutoEventStartJoinOk()
	{
		if (joinTime == 0 || eventTime == 0)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check optional event start join ok.
	 * @return true, if successful
	 */
	private static boolean checkOptionalEventStartJoinOk()
	{
		// TODO be integrated
		return true;
	}
	
	/**
	 * Sets the npc pos.
	 * @param activeChar the new npc pos
	 */
	public static void setNpcPos(final L2PcInstance activeChar)
	{
		npcX = activeChar.getX();
		npcY = activeChar.getY();
		npcZ = activeChar.getZ();
		npcHeading = activeChar.getHeading();
	}
	
	/**
	 * Spawn event npc.
	 */
	private static void spawnEventNpc()
	{
		final L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(npcId);
		
		try
		{
			npcSpawn = new L2Spawn(tmpl);
			
			npcSpawn.setLocx(npcX);
			npcSpawn.setLocy(npcY);
			npcSpawn.setLocz(npcZ);
			npcSpawn.setAmount(1);
			npcSpawn.setHeading(npcHeading);
			npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			
			npcSpawn.init();
			npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			npcSpawn.getLastSpawn().setTitle(eventName);
			npcSpawn.getLastSpawn().isEventMobDM = true;
			npcSpawn.getLastSpawn().isAggressive();
			npcSpawn.getLastSpawn().decayMe();
			npcSpawn.getLastSpawn().spawnMe(npcSpawn.getLastSpawn().getX(), npcSpawn.getLastSpawn().getY(), npcSpawn.getLastSpawn().getZ());
			
			npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(npcSpawn.getLastSpawn(), npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(eventName + " Engine[spawnEventNpc(exception: " + e.getMessage());
		}
	}
	
	/**
	 * Unspawn event npc.
	 */
	private static void unspawnEventNpc()
	{
		if (npcSpawn == null || npcSpawn.getLastSpawn() == null)
		{
			return;
		}
		
		npcSpawn.getLastSpawn().deleteMe();
		npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(npcSpawn, true);
	}
	
	/**
	 * Start join.
	 * @return true, if successful
	 */
	public static boolean startJoin()
	{
		if (!checkStartJoinOk())
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[startJoin]: startJoinOk() = false");
			}
			return false;
		}
		
		inProgress = true;
		joining = true;
		spawnEventNpc();
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Event " + eventName + "!");
		if (Config.DM_ANNOUNCE_REWARD && ItemTable.getInstance().getTemplate(rewardId) != null)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Reward: " + rewardAmount + " " + ItemTable.getInstance().getTemplate(rewardId).getName());
		}
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Recruiting levels: " + minlvl + " to " + maxlvl);
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Joinable in " + joiningLocationName);
		
		if (Config.DM_COMMAND)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Commands .dmjoin .dmleave .dminfo");
		}
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": FULL BUFF Event: be ready with your buffs, they won't be deleted!!!");
		
		return true;
	}
	
	/**
	 * Start teleport.
	 * @return true, if successful
	 */
	public static boolean startTeleport()
	{
		if (!joining || started || teleport)
		{
			return false;
		}
		
		removeOfflinePlayers();
		
		if (teamEvent)
		{
			
		}
		else
		{
			// final int size = getPlayers().size();
			synchronized (players)
			{
				final int size = players.size();
				if (!checkMinPlayers(size))
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + ": Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + size);
					if (Config.DM_STATS_LOGGER)
					{
						LOGGER.info(eventName + ":Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + size);
					}
					
					return false;
				}
			}
		}
		
		joining = false;
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Teleport to team spot in 20 seconds!");
		
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			sit();
			afterTeleportOperations();
			
			// final Vector<L2PcInstance> players = getPlayers();
			synchronized (players)
			{
				
				for (final L2PcInstance player : players)
				{
					if (player != null)
					{
						if (Config.DM_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if (player.getPet() != null)
							{
								final L2Summon summon = player.getPet();
								summon.stopAllEffects();
								
								if (summon instanceof L2PetInstance)
								{
									summon.unSummon(player);
								}
							}
						}
						
						if (Config.DM_ON_START_REMOVE_ALL_EFFECTS)
						{
							player.stopAllEffects();
							
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							final L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						final int offset = Config.DM_SPAWN_OFFSET;
						player.teleToLocation(playerX + Rnd.get(offset), playerY + Rnd.get(offset), playerZ);
					}
				}
				
			}
			
		}, 20000);
		teleport = true;
		return true;
	}
	
	/**
	 * After teleport operations.
	 */
	protected static void afterTeleportOperations()
	{
		
	}
	
	/**
	 * Start event.
	 * @return true, if successful
	 */
	public static boolean startEvent()
	{
		if (!startEventOk())
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[startEvent()]: startEventOk() = false");
			}
			return false;
		}
		
		teleport = false;
		
		sit();
		removeParties();
		
		afterStartOperations();
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Started. Go to kill your enemies!");
		started = true;
		
		return true;
	}
	
	/**
	 * Removes the parties.
	 */
	private static void removeParties()
	{
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			
			for (final L2PcInstance player : players)
			{
				if (player.getParty() != null)
				{
					final L2Party party = player.getParty();
					party.removePartyMember(player);
				}
			}
		}
	}
	
	/**
	 * After start operations.
	 */
	private static void afterStartOperations()
	{
		
	}
	
	/**
	 * Restarts Event checks if event was aborted. and if true cancels restart task
	 */
	public synchronized static void restartEvent()
	{
		LOGGER.info(eventName + ": Event has been restarted...");
		joining = false;
		started = false;
		inProgress = false;
		aborted = false;
		final long delay = intervalBetweenMatchs;
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": joining period will be avaible again in " + intervalBetweenMatchs + " minute(s)!");
		
		waiter(delay);
		
		try
		{
			if (!aborted)
			{
				autoEvent(); // start a new event
			}
			else
			{
				Announcements.getInstance().gameAnnounceToAll(eventName + ": next event aborted!");
			}
		}
		catch (final Exception e)
		{
			LOGGER.error(eventName + ": Error While Trying to restart Event...", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Finish event.
	 */
	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[finishEvent]: finishEventOk() = false");
			}
			return;
		}
		
		started = false;
		aborted = false;
		unspawnEventNpc();
		
		afterFinishOperations();
		
		if (teamEvent)
		{
			processTopTeam();
		}
		else
		{
			processTopPlayer();
			
			if (topKills != 0)
			{
				String winners = "";
				for (final L2PcInstance winner : topPlayers)
				{
					winners = winners + " " + winner.getName();
				}
				Announcements.getInstance().gameAnnounceToAll(eventName + ": " + winners + " win the match! " + topKills + " kills.");
				rewardPlayer();
				
				if (Config.DM_STATS_LOGGER)
				{
					LOGGER.info("**** " + eventName + " ****");
					LOGGER.info(eventName + ": " + winners + " win the match! " + topKills + " kills.");
				}
			}
			else
			{
				
				Announcements.getInstance().gameAnnounceToAll(eventName + ": No players win the match(nobody killed).");
				if (Config.DM_STATS_LOGGER)
				{
					LOGGER.info(eventName + ": No players win the match(nobody killed).");
				}
			}
		}
		
		teleportFinish();
	}
	
	/**
	 * After finish operations.
	 */
	private static void afterFinishOperations()
	{
		
	}
	
	/**
	 * Abort event.
	 */
	public static void abortEvent()
	{
		if (!joining && !teleport && !started)
		{
			return;
		}
		
		if (joining && !teleport && !started)
		{
			unspawnEventNpc();
			cleanDM();
			joining = false;
			inProgress = false;
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Match aborted!");
			return;
		}
		joining = false;
		teleport = false;
		started = false;
		aborted = true;
		unspawnEventNpc();
		
		afterFinish();
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Match aborted!");
		teleportFinish();
	}
	
	/**
	 * After finish.
	 */
	private static void afterFinish()
	{
		
	}
	
	/**
	 * Teleport finish.
	 */
	public static void teleportFinish()
	{
		sit();
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Teleport back to participation NPC in 20 seconds!");
		
		removeUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			// final Vector<L2PcInstance> players = getPlayers();
			synchronized (players)
			{
				
				for (final L2PcInstance player : players)
				{
					if (player != null)
					{
						if (player.isOnline())
						{
							player.teleToLocation(npcX, npcY, npcZ, false);
						}
						else
						{
							java.sql.Connection con = null;
							try
							{
								con = L2DatabaseFactory.getInstance().getConnection();
								
								final PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
								statement.setInt(1, npcX);
								statement.setInt(2, npcY);
								statement.setInt(3, npcZ);
								statement.setString(4, player.getName());
								statement.execute();
								DatabaseUtils.close(statement);
							}
							catch (final Exception e)
							{
								if (Config.ENABLE_ALL_EXCEPTIONS)
								{
									e.printStackTrace();
								}
								
								LOGGER.error(e.getMessage(), e);
							}
							finally
							{
								CloseUtil.close(con);
								con = null;
							}
						}
					}
				}
				
			}
			
			sit();
			cleanDM();
		}, 20000);
	}
	
	/**
	 * Auto event.
	 */
	public static void autoEvent()
	{
		LOGGER.info("Starting " + eventName + "!");
		LOGGER.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatchs() + " Minutes.");
		if (checkAutoEventStartJoinOk() && startJoin() && !aborted)
		{
			if (joinTime > 0)
			{
				waiter(joinTime * 60 * 1000); // minutes for join event
			}
			else if (joinTime <= 0)
			{
				LOGGER.info(eventName + ": join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (startTeleport() && !aborted)
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startEvent() && !aborted)
				{
					LOGGER.warn(eventName + ": waiting.....minutes for event time " + eventTime);
					
					waiter(eventTime * 60 * 1000); // minutes for event time
					finishEvent();
					
					LOGGER.info(eventName + ": waiting... delay for final messages ");
					waiter(60000);// just a give a delay delay for final messages
					sendFinalMessages();
					
					if (!started && !aborted)
					{ // if is not already started and it's not aborted
						
						LOGGER.info(eventName + ": waiting.....delay for restart event  " + intervalBetweenMatchs + " minutes.");
						waiter(60000);// just a give a delay to next restart
						
						try
						{
							if (!aborted)
							{
								restartEvent();
							}
						}
						catch (final Exception e)
						{
							LOGGER.error("Error while tying to Restart Event", e);
							e.printStackTrace();
						}
						
					}
					
				}
			}
			else if (!aborted)
			{
				
				abortEvent();
				restartEvent();
				
			}
		}
	}
	
	// start without restart
	/**
	 * Event once start.
	 */
	public static void eventOnceStart()
	{
		if (startJoin() && !aborted)
		{
			if (joinTime > 0)
			{
				waiter(joinTime * 60 * 1000); // minutes for join event
			}
			else if (joinTime <= 0)
			{
				abortEvent();
				return;
			}
			if (startTeleport() && !aborted)
			{
				waiter(1 * 60 * 1000); // 1 min wait time untill start fight after teleported
				if (startEvent() && !aborted)
				{
					waiter(eventTime * 60 * 1000); // minutes for event time
					finishEvent();
				}
			}
			else if (!aborted)
			{
				abortEvent();
			}
		}
	}
	
	/**
	 * Waiter.
	 * @param interval the interval
	 */
	private static void waiter(final long interval)
	{
		final long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && !aborted)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			if (joining || started || teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						removeOfflinePlayers();
						
						if (joining)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": Joinable in " + joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds / 60 / 60 + " hour(s) till registration close!");
						}
						else if (started)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds / 60 / 60 + " hour(s) till event finish!");
						}
						
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 240: // 4 minutes left
					case 180: // 3 minutes left
					case 120: // 2 minutes left
					case 60: // 1 minute left
						// removeOfflinePlayers();
						
						if (joining)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": Joinable in " + joiningLocationName + "!");
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds / 60 + " minute(s) till registration close!");
						}
						else if (started)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds / 60 + " minute(s) till event finish!");
						}
						
						break;
					case 30: // 30 seconds left
					case 15: // 15 seconds left
					case 10: // 10 seconds left
						removeOfflinePlayers();
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						
						if (joining)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds + " second(s) till registration close!");
						}
						else if (teleport)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds + " seconds(s) till start fight!");
						}
						else if (started)
						{
							Announcements.getInstance().gameAnnounceToAll(eventName + ": " + seconds + " second(s) till event finish!");
						}
						
						break;
				}
			}
			
			final long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (final InterruptedException ie)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						ie.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Sit.
	 */
	public static void sit()
	{
		if (sitForced)
		{
			sitForced = false;
		}
		else
		{
			sitForced = true;
		}
		
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					if (sitForced)
					{
						player.stopMove(null, false);
						player.abortAttack();
						player.abortCast();
						
						if (!player.isSitting())
						{
							player.sitDown();
						}
					}
					else
					{
						if (player.isSitting())
						{
							player.standUp();
						}
					}
				}
			}
			
		}
		
	}
	
	/**
	 * Removes the offline players.
	 */
	public static void removeOfflinePlayers()
	{
		try
		{
			// final Vector<L2PcInstance> players = getPlayers();
			synchronized (players)
			{
				
				if (players == null || players.isEmpty())
				{
					return;
				}
				
				final List<L2PcInstance> toBeRemoved = new ArrayList<>();
				
				for (final L2PcInstance player : players)
				{
					if (player == null)
					{
						continue;
					}
					else if (player.inEventDM && !player.isOnline() || player.isInJail() || player.isInOfflineMode())
					{
						
						if (!joining)
						{
							player.getAppearance().setNameColor(player.originalNameColorDM);
							player.setTitle(player.originalTitleDM);
							player.setKarma(player.originalKarmaDM);
							
							player.broadcastUserInfo();
							
						}
						
						// after remove, all event data must be cleaned in player
						player.originalNameColorDM = 0;
						player.originalTitleDM = null;
						player.originalKarmaDM = 0;
						player.countDMkills = 0;
						player.inEventDM = false;
						
						toBeRemoved.add(player);
						
						player.sendMessage("Your participation in the DeathMatch event has been removed.");
					}
					
				}
				players.removeAll(toBeRemoved);
				
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(e.getMessage(), e);
			return;
		}
	}
	
	/**
	 * Start event ok.
	 * @return true, if successful
	 */
	private static boolean startEventOk()
	{
		if (joining || !teleport || started)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Finish event ok.
	 * @return true, if successful
	 */
	private static boolean finishEventOk()
	{
		if (!started)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Adds the player ok.
	 * @param  eventPlayer the event player
	 * @return             true, if successful
	 */
	private static boolean addPlayerOk(final L2PcInstance eventPlayer)
	{
		if (eventPlayer.isAio() && !Config.ALLOW_AIO_IN_EVENTS)
		{
			eventPlayer.sendMessage("AIO charactes are not allowed to participate in events :/");
		}
		if (eventPlayer.inEventDM)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer.inEventTvT || eventPlayer.inEventCTF)
		{
			eventPlayer.sendMessage("You already participated to another event!");
			return false;
		}
		
		if (eventPlayer.isInOlympiadMode())
		{
			eventPlayer.sendMessage("You already participated in Olympiad!");
			return false;
		}
		
		if (eventPlayer.activeBoxesCount > 1 && !Config.ALLOW_DUALBOX_EVENT)
		{
			final List<String> players_in_boxes = eventPlayer.active_boxes_characters;
			
			if (players_in_boxes != null && players_in_boxes.size() > 1)
			{
				for (final String character_name : players_in_boxes)
				{
					final L2PcInstance player = L2World.getInstance().getPlayer(character_name);
					
					if (player != null && player.inEventDM)
					{
						eventPlayer.sendMessage("You already participated in event with another char!");
						return false;
					}
				}
			}
			
			/*
			 * eventPlayer.sendMessage("Dual Box not allowed in Events"); return false;
			 */
		}
		
		if (!Config.DM_ALLOW_HEALER_CLASSES && (eventPlayer.getClassId() == ClassId.Cardinal || eventPlayer.getClassId() == ClassId.Evas_Saint || eventPlayer.getClassId() == ClassId.Shillien_Saint))
		{
			eventPlayer.sendMessage("You cant join with Healer Class!");
			return false;
		}
		
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			if (players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You already participated in the event!");
				return false;
			}
			
			for (final L2PcInstance player : players)
			{
				if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.sendMessage("You already participated in the event!");
					return false;
				}
				else if (player.getName().equalsIgnoreCase(eventPlayer.getName()))
				{
					eventPlayer.sendMessage("You already participated in the event!");
					return false;
				}
			}
			
		}
		
		return true;
	}
	
	/**
	 * Sets the user data.
	 */
	public static void setUserData()
	{
		// final Vector<L2PcInstance> players = getPlayers();
		
		synchronized (players)
		{
			
			for (final L2PcInstance player : players)
			{
				player.originalNameColorDM = player.getAppearance().getNameColor();
				player.originalKarmaDM = player.getKarma();
				player.originalTitleDM = player.getTitle();
				player.getAppearance().setNameColor(playerColors);
				player.setKarma(0);
				player.setTitle("Kills: " + player.countDMkills);
				
				if (player.isMounted())
				{
					if (player.setMountType(0))
					{
						if (player.isFlying())
						{
							player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
						}
						
						final Ride dismount = new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT, 0);
						player.broadcastPacket(dismount);
						player.setMountObjectID(0);
					}
				}
				player.broadcastUserInfo();
			}
		}
	}
	
	/**
	 * Dump data.
	 */
	public static void dumpData()
	{
		LOGGER.info("");
		LOGGER.info("");
		
		if (!joining && !teleport && !started)
		{
			LOGGER.info("<<---------------------------------->>");
			LOGGER.info(">> " + eventName + " Engine infos dump (INACTIVE) <<");
			LOGGER.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (joining && !teleport && !started)
		{
			LOGGER.info("<<--------------------------------->>");
			LOGGER.info(">> " + eventName + " Engine infos dump (JOINING) <<");
			LOGGER.info("<<--^----^^-----^----^^------^----->>");
		}
		else if (!joining && teleport && !started)
		{
			LOGGER.info("<<---------------------------------->>");
			LOGGER.info(">> " + eventName + " Engine infos dump (TELEPORT) <<");
			LOGGER.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (!joining && !teleport && started)
		{
			LOGGER.info("<<--------------------------------->>");
			LOGGER.info(">> " + eventName + " Engine infos dump (STARTED) <<");
			LOGGER.info("<<--^----^^-----^----^^------^----->>");
		}
		
		LOGGER.info("Name: " + eventName);
		LOGGER.info("Desc: " + eventDesc);
		LOGGER.info("Join location: " + joiningLocationName);
		LOGGER.info("Min lvl: " + minlvl);
		LOGGER.info("Max lvl: " + maxlvl);
		
		LOGGER.info("");
		LOGGER.info("##################################");
		LOGGER.info("# players(Vector<L2PcInstance>) #");
		LOGGER.info("##################################");
		
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			LOGGER.info("Total Players : " + players.size());
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					LOGGER.info("Name: " + player.getName() + " kills :" + player.countDMkills);
				}
			}
		}
		
		LOGGER.info("");
		LOGGER.info("################################");
		LOGGER.info("# savePlayers(Vector<String>) #");
		LOGGER.info("################################");
		
		for (final String player : savePlayers)
		{
			LOGGER.info("Name: " + player);
		}
		
		LOGGER.info("");
		LOGGER.info("");
		
		dumpLocalEventInfo();
	}
	
	/**
	 * Dump local event info.
	 */
	private static void dumpLocalEventInfo()
	{
		
	}
	
	/**
	 * Load data.
	 */
	public static void loadData()
	{
		eventName = new String();
		eventDesc = new String();
		joiningLocationName = new String();
		savePlayers = new Vector<>();
		
		synchronized (players)
		{
			players.clear();
		}
		
		topPlayers = new ArrayList<>();
		npcSpawn = null;
		joining = false;
		teleport = false;
		started = false;
		sitForced = false;
		aborted = false;
		inProgress = false;
		
		npcId = 0;
		npcX = 0;
		npcY = 0;
		npcZ = 0;
		npcHeading = 0;
		rewardId = 0;
		rewardAmount = 0;
		topKills = 0;
		minlvl = 0;
		maxlvl = 0;
		joinTime = 0;
		eventTime = 0;
		minPlayers = 0;
		maxPlayers = 0;
		intervalBetweenMatchs = 0;
		playerColors = 0;
		playerX = 0;
		playerY = 0;
		playerZ = 0;
		
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("Select * from dm");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				eventName = rs.getString("eventName");
				eventDesc = rs.getString("eventDesc");
				joiningLocationName = rs.getString("joiningLocation");
				minlvl = rs.getInt("minlvl");
				maxlvl = rs.getInt("maxlvl");
				npcId = rs.getInt("npcId");
				npcX = rs.getInt("npcX");
				npcY = rs.getInt("npcY");
				npcZ = rs.getInt("npcZ");
				npcHeading = rs.getInt("npcHeading");
				rewardId = rs.getInt("rewardId");
				rewardAmount = rs.getInt("rewardAmount");
				joinTime = rs.getInt("joinTime");
				eventTime = rs.getInt("eventTime");
				minPlayers = rs.getInt("minPlayers");
				maxPlayers = rs.getInt("maxPlayers");
				playerColors = rs.getInt("color");
				playerX = rs.getInt("playerX");
				playerY = rs.getInt("playerY");
				playerZ = rs.getInt("playerZ");
				intervalBetweenMatchs = rs.getInt("delayForNextEvent");
			}
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			LOGGER.error("Exception: DM.loadData(): " + e.getMessage());
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	/**
	 * Show event html.
	 * @param eventPlayer the event player
	 * @param objectId    the object id
	 */
	public static void showEventHtml(final L2PcInstance eventPlayer, final String objectId)
	{
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			final TextBuilder replyMSG = new TextBuilder("<html><title>" + eventName + "</title><body>");
			replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
			replyMSG.append("<center><font color=\"3366CC\">Current event:</font></center><br1>");
			replyMSG.append("<center>Name:&nbsp;<font color=\"00FF00\">" + eventName + "</font></center><br1>");
			replyMSG.append("<center>Description:&nbsp;<font color=\"00FF00\">" + eventDesc + "</font></center><br><br>");
			replyMSG.append("<center>Event Type:&nbsp;<font color=\"00FF00\"> Full Buff Event!!! </font></center><br><br>");
			
			// final Vector<L2PcInstance> players = getPlayers();
			synchronized (players)
			{
				
				if (!started && !joining)
				{
					replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
				}
				else if (!checkMaxPlayers(players.size()))
				{
					if (!started)
					{
						replyMSG.append("Currently participated: <font color=\"00FF00\">" + players.size() + ".</font><br>");
						replyMSG.append("Max players: <font color=\"00FF00\">" + maxPlayers + "</font><br><br>");
						replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
					}
				}
				else if (eventPlayer.isCursedWeaponEquiped() && !Config.DM_JOIN_CURSED)
				{
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
				}
				else if (!started && joining && eventPlayer.getLevel() >= minlvl && eventPlayer.getLevel() <= maxlvl)
				{
					if (players.contains(eventPlayer))
					{
						replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
						
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + players.size() + "</font></center><br>");
						replyMSG.append("<table border=\"0\"><tr>");
						replyMSG.append("<td width=\"200\">Wait till event start or</td>");
						replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_dmevent_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
						replyMSG.append("<td width=\"100\">your participation!</td>");
						replyMSG.append("</tr></table>");
					}
					else
					{
						replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + players.size() + "</font></center><br>");
						replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
						replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + minlvl + "</font></center></td><br>");
						replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + maxlvl + "</font></center></td><br><br>");
						replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_dmevent_player_join\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");
						
					}
				}
				else if (started && !joining)
				{
					replyMSG.append("<center>" + eventName + " match is in progress.</center>");
				}
				else if (eventPlayer.getLevel() < minlvl || eventPlayer.getLevel() > maxlvl)
				{
					replyMSG.append("Your lvl: <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
					replyMSG.append("Min lvl: <font color=\"00FF00\">" + minlvl + "</font><br>");
					replyMSG.append("Max lvl: <font color=\"00FF00\">" + maxlvl + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
				
			}
			
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error(eventName + " Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
		}
	}
	
	/**
	 * Adds the player.
	 * @param player the player
	 */
	public static void addPlayer(final L2PcInstance player)
	{
		if (!addPlayerOk(player))
		{
			return;
		}
		
		synchronized (players)
		{
			players.add(player);
		}
		
		player.inEventDM = true;
		player.countDMkills = 0;
		savePlayers.add(player.getName());
		player.sendMessage("DM: You successfully registered for the DeathMatch event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(final L2PcInstance player)
	{
		if (player != null && player.inEventDM)
		{
			if (!joining)
			{
				player.getAppearance().setNameColor(player.originalNameColorDM);
				player.setTitle(player.originalTitleDM);
				player.setKarma(player.originalKarmaDM);
				
				player.broadcastUserInfo();
				
			}
			
			// after remove, all event data must be cleaned in player
			player.originalNameColorDM = 0;
			player.originalTitleDM = null;
			player.originalKarmaDM = 0;
			player.countDMkills = 0;
			player.inEventDM = false;
			
			synchronized (players)
			{
				players.remove(player);
			}
			
			player.sendMessage("Your participation in the DeathMatch event has been removed.");
			
		}
	}
	
	/**
	 * Clean dm.
	 */
	public static void cleanDM()
	{
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					
					cleanEventPlayer(player);
					
					if (player.inEventDM)
					{
						if (!joining)
						{
							player.getAppearance().setNameColor(player.originalNameColorDM);
							player.setTitle(player.originalTitleDM);
							player.setKarma(player.originalKarmaDM);
							
							player.broadcastUserInfo();
							
						}
						
						// after remove, all event data must be cleaned in player
						player.originalNameColorDM = 0;
						player.originalTitleDM = null;
						player.originalKarmaDM = 0;
						player.countDMkills = 0;
						player.inEventDM = false;
						
						player.sendMessage("Your participation in the DeathMatch event has been removed.");
						
					}
					
					if (savePlayers.contains(player.getName()))
					{
						savePlayers.remove(player.getName());
					}
					player.inEventDM = false;
				}
			}
			
			players.clear();
			
		}
		
		topKills = 0;
		savePlayers = new Vector<>();
		topPlayers = new ArrayList<>();
		
		cleanLocalEventInfo();
		
		inProgress = false;
		
		loadData();
	}
	
	/**
	 * Clean local event info.
	 */
	private static void cleanLocalEventInfo()
	{
		// nothing
	}
	
	/**
	 * Clean event player.
	 * @param player the player
	 */
	private static void cleanEventPlayer(final L2PcInstance player)
	{
		// nothing
	}
	
	/**
	 * Adds the disconnected player.
	 * @param player the player
	 */
	public static void addDisconnectedPlayer(final L2PcInstance player)
	{
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			
			if (!players.contains(player) && savePlayers.contains(player.getName()))
			{
				if (Config.DM_ON_START_REMOVE_ALL_EFFECTS)
				{
					player.stopAllEffects();
					
				}
				
				players.add(player);
				
				player.originalNameColorDM = player.getAppearance().getNameColor();
				player.originalTitleDM = player.getTitle();
				player.originalKarmaDM = player.getKarma();
				player.inEventDM = true;
				player.countDMkills = 0;
				if (teleport || started)
				{
					player.setTitle("Kills: " + player.countDMkills);
					player.getAppearance().setNameColor(playerColors);
					player.setKarma(0);
					player.broadcastUserInfo();
					player.teleToLocation(playerX + Rnd.get(Config.DM_SPAWN_OFFSET), playerY + Rnd.get(Config.DM_SPAWN_OFFSET), playerZ);
				}
			}
		}
	}
	
	public static int get_playerColors()
	{
		return playerColors;
	}
	
	public static boolean set_playerColors(final int playerColors)
	{
		if (!is_inProgress())
		{
			DM.playerColors = playerColors;
			return true;
		}
		return false;
	}
	
	/**
	 * Reward player.
	 */
	public static void rewardPlayer()
	{
		if (topPlayers.size() > 0)
		{
			
			for (final L2PcInstance topPlayer : topPlayers)
			{
				topPlayer.addItem("DM Event: " + eventName, rewardId, rewardAmount, topPlayer, true);
				
				final StatusUpdate su = new StatusUpdate(topPlayer.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, topPlayer.getCurrentLoad());
				topPlayer.sendPacket(su);
				
				final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
				final TextBuilder replyMSG = new TextBuilder("");
				
				replyMSG.append("<html><body>You won the event. Look in your inventory for the reward.</body></html>");
				
				nhm.setHtml(replyMSG.toString());
				topPlayer.sendPacket(nhm);
				
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				topPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	/**
	 * Process top player.
	 */
	private static void processTopPlayer()
	{
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			
			for (final L2PcInstance player : players)
			{
				if (player.countDMkills > topKills)
				{
					topPlayers.clear();
					topPlayers.add(player);
					topKills = player.countDMkills;
					
				}
				else if (player.countDMkills == topKills)
				{
					if (!topPlayers.contains(player))
					{
						topPlayers.add(player);
					}
				}
			}
		}
	}
	
	/**
	 * Process top team.
	 */
	private static void processTopTeam()
	{
		
	}
	
	public static Location get_playersSpawnLocation()
	{
		final Location npc_loc = new Location(playerX + Rnd.get(Config.DM_SPAWN_OFFSET), playerY + Rnd.get(Config.DM_SPAWN_OFFSET), playerZ, 0);
		
		return npc_loc;
	}
	
	public static void setPlayersPos(final L2PcInstance activeChar)
	{
		playerX = activeChar.getX();
		playerY = activeChar.getY();
		playerZ = activeChar.getZ();
	}
	
	/**
	 * Removes the user data.
	 */
	public static void removeUserData()
	{
		// final Vector<L2PcInstance> players = getPlayers();
		synchronized (players)
		{
			for (final L2PcInstance player : players)
			{
				player.getAppearance().setNameColor(player.originalNameColorDM);
				player.setTitle(player.originalTitleDM);
				player.setKarma(player.originalKarmaDM);
				player.inEventDM = false;
				player.countDMkills = 0;
				player.broadcastUserInfo();
			}
		}
		
	}
	
	/**
	 * just an announcer to send termination messages.
	 */
	public static void sendFinalMessages()
	{
		if (!started && !aborted)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Thank you For participating!");
		}
	}
	
	/**
	 * returns the interval between each event.
	 * @return the interval between matchs
	 */
	public static int getIntervalBetweenMatchs()
	{
		final long actualTime = System.currentTimeMillis();
		final long totalTime = actualTime + intervalBetweenMatchs;
		final long interval = totalTime - actualTime;
		final int seconds = (int) (interval / 1000);
		
		return seconds / 60;
	}
	
	/**
	 * Sets the event start time.
	 * @param newTime the new event start time
	 */
	public void setEventStartTime(final String newTime)
	{
		startEventTime = newTime;
	}
	
	@Override
	public String getEventIdentifier()
	{
		return eventName;
	}
	
	@Override
	public void run()
	{
		LOGGER.info("DM: Event notification start");
		eventOnceStart();
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	/**
	 * On disconnect.
	 * @param player the player
	 */
	public static void onDisconnect(final L2PcInstance player)
	{
		if (player.inEventDM)
		{
			removePlayer(player);
			player.teleToLocation(npcX, npcY, npcZ);
		}
	}
}