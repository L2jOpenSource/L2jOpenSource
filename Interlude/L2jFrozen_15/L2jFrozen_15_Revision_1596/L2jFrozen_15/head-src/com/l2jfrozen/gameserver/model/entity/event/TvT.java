package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
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
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.event.manager.EventTask;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

import javolution.text.TextBuilder;

public class TvT implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(TvT.class);
	private static final String UPDATE_CHARACTER_LOCATION = "UPDATE characters SET x=?, y=?, z=? WHERE char_name=?";
	
	protected static String eventName = new String();
	protected static String eventDesc = new String();
	protected static String joiningLocationName = new String();
	private static L2Spawn npcSpawn;
	protected static boolean joining = false;
	protected static boolean teleport = false;
	protected static boolean started = false;
	protected static boolean aborted = false;
	protected static boolean sitForced = false;
	protected static boolean inProgress = false;
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
	protected static long intervalBetweenMatchs = 0;
	private String startEventTime;
	private static boolean teamEvent = true;
	public static Vector<L2PcInstance> players = new Vector<>();
	private static String topTeam = new String();
	public static Vector<L2PcInstance> playersShuffle = new Vector<>();
	public static Vector<String> tvtTeams = new Vector<>();
	public static Vector<String> savePlayers = new Vector<>();
	public static Vector<String> savePlayerTeams = new Vector<>();
	public static Vector<Integer> tvtTeamPlayersCount = new Vector<>();
	public static Vector<Integer> teamColors = new Vector<>();
	public static Vector<Integer> teamsX = new Vector<>();
	public static Vector<Integer> teamsY = new Vector<>();
	public static Vector<Integer> teamsZ = new Vector<>();
	public static Vector<Integer> teamPointsCount = new Vector<>();
	public static int topKills = 0;
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static TvT getNewInstance()
	{
		return new TvT();
	}
	
	public static String get_eventName()
	{
		return eventName;
	}
	
	public static boolean setEventName(final String eventName)
	{
		if (!isInProgress())
		{
			TvT.eventName = eventName;
			return true;
		}
		return false;
	}
	
	public static String getEventDescription()
	{
		return eventDesc;
	}
	
	public static boolean setEventDescription(final String eventDesc)
	{
		if (!isInProgress())
		{
			TvT.eventDesc = eventDesc;
			return true;
		}
		return false;
	}
	
	public static String getJoiningLocationName()
	{
		return joiningLocationName;
	}
	
	public static boolean setJoiningLocationName(final String joiningLocationName)
	{
		if (!isInProgress())
		{
			TvT.joiningLocationName = joiningLocationName;
			return true;
		}
		return false;
	}
	
	public static int getNpcId()
	{
		return npcId;
	}
	
	public static boolean setNpcId(final int npcId)
	{
		if (!isInProgress())
		{
			TvT.npcId = npcId;
			return true;
		}
		return false;
	}
	
	public static Location getNpcLocation()
	{
		final Location npc_loc = new Location(npcX, npcY, npcZ, npcHeading);
		
		return npc_loc;
	}
	
	public static int getRewardId()
	{
		return rewardId;
	}
	
	public static boolean setRewardId(final int rewardId)
	{
		if (!isInProgress())
		{
			TvT.rewardId = rewardId;
			return true;
		}
		return false;
	}
	
	public static int getRewardAmount()
	{
		return rewardAmount;
	}
	
	public static boolean setRewardAmount(final int rewardAmount)
	{
		if (!isInProgress())
		{
			TvT.rewardAmount = rewardAmount;
			return true;
		}
		return false;
	}
	
	public static int getMinlvl()
	{
		return minlvl;
	}
	
	public static boolean setMinlvl(final int minlvl)
	{
		if (!isInProgress())
		{
			TvT.minlvl = minlvl;
			return true;
		}
		return false;
	}
	
	public static int getMaxlvl()
	{
		return maxlvl;
	}
	
	public static boolean setMaxlvl(final int maxlvl)
	{
		if (!isInProgress())
		{
			TvT.maxlvl = maxlvl;
			return true;
		}
		return false;
	}
	
	public static int getJoinTime()
	{
		return joinTime;
	}
	
	public static boolean setJoinTime(final int joinTime)
	{
		if (!isInProgress())
		{
			TvT.joinTime = joinTime;
			return true;
		}
		return false;
	}
	
	public static int getEventTime()
	{
		return eventTime;
	}
	
	public static boolean setEventTime(final int eventTime)
	{
		if (!isInProgress())
		{
			TvT.eventTime = eventTime;
			return true;
		}
		return false;
	}
	
	public static int getMinPlayers()
	{
		return minPlayers;
	}
	
	public static boolean setMinPlayers(final int minPlayers)
	{
		if (!isInProgress())
		{
			TvT.minPlayers = minPlayers;
			return true;
		}
		return false;
	}
	
	public static int getMaxPlayers()
	{
		return maxPlayers;
	}
	
	public static boolean setMaxPlayers(final int maxPlayers)
	{
		if (!isInProgress())
		{
			TvT.maxPlayers = maxPlayers;
			return true;
		}
		return false;
	}
	
	public static long get_intervalBetweenMatchs()
	{
		return intervalBetweenMatchs;
	}
	
	public static boolean setIntervalBetweenMatchs(final long intervalBetweenMatchs)
	{
		if (!isInProgress())
		{
			TvT.intervalBetweenMatchs = intervalBetweenMatchs;
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
		if (!isInProgress())
		{
			this.startEventTime = startEventTime;
			return true;
		}
		return false;
	}
	
	public static boolean isJoining()
	{
		return joining;
	}
	
	public static boolean isTeleport()
	{
		return teleport;
	}
	
	public static boolean isStarted()
	{
		return started;
	}
	
	public static boolean isAborted()
	{
		return aborted;
	}
	
	public static boolean isSitForced()
	{
		return sitForced;
	}
	
	public static boolean isInProgress()
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
		
		for (Castle castle : CastleManager.getInstance().getCastles())
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
		
		if (tvtTeams.size() < 2 || teamsX.contains(0) || teamsY.contains(0) || teamsZ.contains(0))
		{
			return false;
		}
		
		return true;
		
	}
	
	/**
	 * Check start join player info.
	 * @return true, if successful
	 */
	private static boolean checkStartJoinPlayerInfo()
	{
		return true;
	}
	
	/**
	 * Check auto event start join ok.
	 * @return true, if successful
	 */
	protected static boolean checkAutoEventStartJoinOk()
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
			npcSpawn.getLastSpawn().isEventMobTvT = true;
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
		if (Config.TVT_ANNOUNCE_REWARD && ItemTable.getInstance().getTemplate(rewardId) != null)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Reward: " + rewardAmount + " " + ItemTable.getInstance().getTemplate(rewardId).getName());
		}
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Recruiting levels: " + minlvl + " to " + maxlvl);
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Joinable in " + joiningLocationName + ".");
		
		if (Config.TVT_COMMAND)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Commands .tvtjoin .tvtleave .tvtinfo");
		}
		
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
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(playersShuffle.size()))
		{
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(playersShuffle.size()))
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + playersShuffle.size());
			if (Config.CTF_STATS_LOGGER)
			{
				LOGGER.info(eventName + ":Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + playersShuffle.size());
			}
			
			return false;
		}
		
		joining = false;
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Teleport to team spot in 20 seconds!");
		
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			sit();
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					if (Config.TVT_ON_START_UNSUMMON_PET)
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
					
					if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
					{
						player.stopAllEffects();
						
					}
					
					// Remove player from his party
					if (player.getParty() != null)
					{
						final L2Party party = player.getParty();
						party.removePartyMember(player);
					}
					
					player.teleToLocation(teamsX.get(tvtTeams.indexOf(player.teamNameTvT)) + Rnd.get(201) - 100, teamsY.get(tvtTeams.indexOf(player.teamNameTvT)) + Rnd.get(201) - 100, teamsZ.get(tvtTeams.indexOf(player.teamNameTvT)));
				}
			}
			
		}, 20000);
		teleport = true;
		return true;
	}
	
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
		
		if (Config.TVT_CLOSE_FORT_DOORS)
		{
			closeFortDoors();
		}
		
		if (Config.TVT_CLOSE_ADEN_COLOSSEUM_DOORS)
		{
			closeAdenColosseumDoors();
		}
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Started. Go to kill your enemies!");
		started = true;
		
		return true;
	}
	
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
			
			L2PcInstance bestKiller = findBestKiller(players);
			L2PcInstance looser = findLooser(players);
			
			if (topKills != 0)
			{
				
				playKneelAnimation(topTeam);
				
				if (Config.TVT_ANNOUNCE_TEAM_STATS)
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + " Team Statistics:");
					for (String team : tvtTeams)
					{
						int kills = teamKillsCount(team);
						Announcements.getInstance().gameAnnounceToAll(eventName + ": Team: " + team + " - Kills: " + kills);
					}
					
					if (bestKiller != null)
					{
						Announcements.getInstance().gameAnnounceToAll(eventName + ": Top killer: " + bestKiller.getName() + " - Kills: " + bestKiller.countTvTkills);
					}
					if ((looser != null) && (!looser.equals(bestKiller)))
					{
						Announcements.getInstance().gameAnnounceToAll(eventName + ": Top looser: " + looser.getName() + " - Dies: " + looser.countTvTdies);
					}
				}
				
				if (topTeam != null)
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + ": " + topTeam + "'s win the match! " + topKills + " kills.");
				}
				else
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + ": The event finished with a TIE: " + topKills + " kills by each team!");
				}
				rewardTeam(topTeam, bestKiller, looser);
				
				if (Config.TVT_STATS_LOGGER)
				{
					LOGGER.info("**** " + eventName + " ****");
					LOGGER.info(eventName + " Team Statistics:");
					for (String team : tvtTeams)
					{
						int kills = teamKillsCount(team);
						LOGGER.info("Team: " + team + " - Kills: " + kills);
					}
					
					if (bestKiller != null)
					{
						LOGGER.info("Top killer: " + bestKiller.getName() + " - Kills: " + bestKiller.countTvTkills);
					}
					if ((looser != null) && (!looser.equals(bestKiller)))
					{
						LOGGER.info("Top looser: " + looser.getName() + " - Dies: " + looser.countTvTdies);
					}
					
					LOGGER.info(eventName + ": " + topTeam + "'s win the match! " + topKills + " kills.");
					
				}
				
			}
			else
			{
				
				Announcements.getInstance().gameAnnounceToAll(eventName + ": The event finished with a TIE: No team wins the match(nobody killed)!");
				
				if (Config.TVT_STATS_LOGGER)
				{
					LOGGER.info(eventName + ": No team win the match(nobody killed).");
				}
				
				rewardTeam(topTeam, bestKiller, looser);
			}
		}
		
		teleportFinish();
	}
	
	private static void afterFinishOperations()
	{
		if (Config.TVT_OPEN_FORT_DOORS)
		{
			openFortDoors();
		}
		
		if (Config.TVT_OPEN_ADEN_COLOSSEUM_DOORS)
		{
			openAdenColosseumDoors();
		}
	}
	
	public static void abortEvent()
	{
		if (!joining && !teleport && !started)
		{
			return;
		}
		
		if (joining && !teleport && !started)
		{
			unspawnEventNpc();
			cleanTvT();
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
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Match aborted!");
		teleportFinish();
	}
	
	public static void teleportFinish()
	{
		sit();
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Teleport back to participation NPC in 20 seconds!");
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			
			for (L2PcInstance player : players)
			{
				if (player != null)
				{
					if (player.isOnline())
					{
						player.teleToLocation(npcX, npcY, npcZ, false);
					}
					else
					{
						try (Connection con = L2DatabaseFactory.getInstance().getConnection();
							PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_LOCATION))
						{
							statement.setInt(1, npcX);
							statement.setInt(2, npcY);
							statement.setInt(3, npcZ);
							statement.setString(4, player.getName());
							statement.executeUpdate();
						}
						catch (Exception e)
						{
							LOGGER.error("TvT.teleportFinish.run : Can not updated character location (x,y,z) in characters table.", e);
						}
					}
				}
			}
			
			sit();
			cleanTvT();
		}, 20000);
	}
	
	protected static class AutoEventTask implements Runnable
	{
		@Override
		public void run()
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
					waiter(30 * 1000); // 30 sec wait time until start fight after teleported
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
		
	}
	
	public static void autoEvent()
	{
		ThreadPoolManager.getInstance().executeAi(new AutoEventTask());
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
	protected static void waiter(final long interval)
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
	
	public static void removeOfflinePlayers()
	{
		try
		{
			if (playersShuffle == null || playersShuffle.isEmpty())
			{
				return;
			}
			else if (playersShuffle.size() > 0)
			{
				for (int i = 0; i < playersShuffle.size(); i++)
				{
					L2PcInstance player = playersShuffle.get(i);
					
					if (player == null)
					{
						playersShuffle.remove(player);
					}
					else if (!player.isOnline() || player.isInJail() || player.isInOfflineMode())
					{
						removePlayer(player);
					}
					if (playersShuffle.size() == 0 || playersShuffle.isEmpty())
					{
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("TvT.removeOfflinePlayers : Something went wrong", e);
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
		
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			if (tvtTeamPlayersCount.contains(0))
			{
				return false;
			}
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			final Vector<L2PcInstance> playersShuffleTemp = new Vector<>();
			int loopCount = 0;
			
			loopCount = playersShuffle.size();
			
			for (int i = 0; i < loopCount; i++)
			{
				playersShuffleTemp.add(playersShuffle.get(i));
			}
			
			playersShuffle = playersShuffleTemp;
			playersShuffleTemp.clear();
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
	 * @param  teamName    the team name
	 * @param  eventPlayer the event player
	 * @return             true, if successful
	 */
	private static boolean addPlayerOk(final String teamName, final L2PcInstance eventPlayer)
	{
		if (eventPlayer.isAio() && !Config.ALLOW_AIO_IN_EVENTS)
		{
			eventPlayer.sendMessage("AIO charactes are not allowed to participate in events :/");
		}
		if (checkShufflePlayers(eventPlayer) || eventPlayer.inEventTvT)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer.inEventCTF || eventPlayer.inEventDM)
		{
			eventPlayer.sendMessage("You already participated in another event!");
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
					
					if (player != null && player.inEventTvT)
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
		
		if (players.contains(eventPlayer))
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (Config.TVT_EVEN_TEAMS.equals("NO"))
		{
			return true;
		}
		else if (Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			
			for (final int playersCount : tvtTeamPlayersCount)
			{
				if (countBefore == -1)
				{
					countBefore = playersCount;
				}
				
				if (countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}
				
				countBefore = playersCount;
			}
			
			if (allTeamsEqual)
			{
				return true;
			}
			
			countBefore = Integer.MAX_VALUE;
			
			for (final int teamPlayerCount : tvtTeamPlayersCount)
			{
				if (teamPlayerCount < countBefore)
				{
					countBefore = teamPlayerCount;
				}
			}
			
			final Vector<String> joinableTeams = new Vector<>();
			
			for (final String team : tvtTeams)
			{
				if (teamPlayersCount(team) == countBefore)
				{
					joinableTeams.add(team);
				}
			}
			
			if (joinableTeams.contains(teamName))
			{
				return true;
			}
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			return true;
		}
		
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}
	
	public static void setUserData()
	{
		for (final L2PcInstance player : players)
		{
			player.originalNameColorTvT = player.getAppearance().getNameColor();
			player.originalKarmaTvT = player.getKarma();
			player.originalTitleTvT = player.getTitle();
			player.getAppearance().setNameColor(teamColors.get(tvtTeams.indexOf(player.teamNameTvT)));
			player.setKarma(0);
			player.setTitle("Kills: " + player.countTvTkills);
			if (Config.TVT_AURA)
			{
				if (tvtTeams.size() >= 2)
				{
					player.setTeam(tvtTeams.indexOf(player.teamNameTvT) + 1);
				}
			}
			
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
		LOGGER.info("##########################");
		LOGGER.info("# teams(Vector<String>) #");
		LOGGER.info("##########################");
		
		for (final String team : tvtTeams)
		{
			LOGGER.info(team + " Kills Done :" + teamPointsCount.get(tvtTeams.indexOf(team)));
		}
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			LOGGER.info("");
			LOGGER.info("#########################################");
			LOGGER.info("# playersShuffle(Vector<L2PcInstance>) #");
			LOGGER.info("#########################################");
			
			for (final L2PcInstance player : playersShuffle)
			{
				if (player != null)
				{
					LOGGER.info("Name: " + player.getName());
				}
			}
		}
		
		LOGGER.info("");
		LOGGER.info("##################################");
		LOGGER.info("# players(Vector<L2PcInstance>) #");
		LOGGER.info("##################################");
		
		for (L2PcInstance player : players)
		{
			if (player != null)
			{
				LOGGER.info("Name: " + player.getName() + "   Team: " + player.teamNameTvT + "  Kills Done:" + player.countTvTkills);
			}
		}
		
		LOGGER.info("");
		LOGGER.info("#####################################################################");
		LOGGER.info("# savePlayers(Vector<String>) and savePlayerTeams(Vector<String>) #");
		LOGGER.info("#####################################################################");
		
		for (final String player : savePlayers)
		{
			LOGGER.info("Name: " + player + "	Team: " + savePlayerTeams.get(savePlayers.indexOf(player)));
		}
		
		LOGGER.info("");
		LOGGER.info("");
		
	}
	
	public static void loadData()
	{
		eventName = new String();
		eventDesc = new String();
		joiningLocationName = new String();
		savePlayers = new Vector<>();
		players = new Vector<>();
		
		topTeam = new String();
		tvtTeams = new Vector<>();
		savePlayerTeams = new Vector<>();
		playersShuffle = new Vector<>();
		tvtTeamPlayersCount = new Vector<>();
		teamPointsCount = new Vector<>();
		teamColors = new Vector<>();
		teamsX = new Vector<>();
		teamsY = new Vector<>();
		teamsZ = new Vector<>();
		
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
		
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("Select * from tvt");
			rs = statement.executeQuery();
			
			int teams = 0;
			
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
				teams = rs.getInt("teamsCount");
				joinTime = rs.getInt("joinTime");
				eventTime = rs.getInt("eventTime");
				minPlayers = rs.getInt("minPlayers");
				maxPlayers = rs.getInt("maxPlayers");
				intervalBetweenMatchs = rs.getLong("delayForNextEvent");
			}
			DatabaseUtils.close(statement);
			
			int index = -1;
			if (teams > 0)
			{
				index = 0;
			}
			while (index < teams && index > -1)
			{
				statement = con.prepareStatement("Select * from tvt_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
				{
					tvtTeams.add(rs.getString("teamName"));
					tvtTeamPlayersCount.add(0);
					teamPointsCount.add(0);
					teamColors.add(0);
					teamsX.add(0);
					teamsY.add(0);
					teamsZ.add(0);
					teamsX.set(index, rs.getInt("teamX"));
					teamsY.set(index, rs.getInt("teamY"));
					teamsZ.set(index, rs.getInt("teamZ"));
					teamColors.set(index, rs.getInt("teamColor"));
					
				}
				index++;
				DatabaseUtils.close(statement);
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Exception: loadData(): " + e.getMessage());
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
			
			if (!started && !joining)
			{
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			}
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(playersShuffle.size()))
			{
				if (!started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + playersShuffle.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.TVT_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if (!started && joining && eventPlayer.getLevel() >= minlvl && eventPlayer.getLevel() <= maxlvl)
			{
				if (players.contains(eventPlayer) || playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer.teamNameTvT + "</font><br><br>");
					}
					else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
					}
					
					replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + playersShuffle.size() + "</font></center><br>");
					
					replyMSG.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
					replyMSG.append("<center><button value=\"Remove\" action=\"bypass -h npc_" + objectId + "_tvt_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
				}
				else
				{
					replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
					replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + minlvl + "</font></center></td><br>");
					replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + maxlvl + "</font></center></td><br><br>");
					replyMSG.append("<center><font color=\"3366CC\">Teams:</font></center><br>");
					
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (final String team : tvtTeams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team + "\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
						}
						replyMSG.append("</table></center>");
					}
					else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center>");
						
						for (final String team : tvtTeams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font> &nbsp;</td>");
						}
						
						replyMSG.append("</center><br>");
						
						replyMSG.append("<center><button value=\"Join Event\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
						replyMSG.append("<center><font color=\"3366CC\">Teams will be reandomly generated!</font></center><br>");
						replyMSG.append("<center>Joined Players:</font> <font color=\"LEVEL\">" + playersShuffle.size() + "</center></font><br>");
						replyMSG.append("<center>Reward: <font color=\"LEVEL\">" + rewardAmount + " " + ItemTable.getInstance().getTemplate(rewardId).getName() + "</center></font>");
					}
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
	 * @param player   the player
	 * @param teamName the team name
	 */
	public static void addPlayer(final L2PcInstance player, final String teamName)
	{
		if (!addPlayerOk(teamName, player))
		{
			return;
		}
		
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			player.teamNameTvT = teamName;
			players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			playersShuffle.add(player);
		}
		
		player.inEventTvT = true;
		player.countTvTkills = 0;
		player.countTvTdies = 0;
		player.sendMessage(eventName + ": You successfully registered for the event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(final L2PcInstance player)
	{
		if (player.inEventTvT)
		{
			if (!joining)
			{
				player.getAppearance().setNameColor(player.originalNameColorTvT);
				player.setTitle(player.originalTitleTvT);
				player.setKarma(player.originalKarmaTvT);
				if (Config.TVT_AURA)
				{
					if (tvtTeams.size() >= 2)
					{
						player.setTeam(0);// clear aura :P
					}
				}
				player.broadcastUserInfo();
			}
			
			// after remove, all event data must be cleaned in player
			player.originalNameColorTvT = 0;
			player.originalTitleTvT = null;
			player.originalKarmaTvT = 0;
			player.teamNameTvT = new String();
			player.countTvTkills = 0;
			player.inEventTvT = false;
			
			if ((Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) && players.contains(player))
			{
				setTeamPlayersCount(player.teamNameTvT, teamPlayersCount(player.teamNameTvT) - 1);
				players.remove(player);
			}
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (!playersShuffle.isEmpty() && playersShuffle.contains(player)))
			{
				playersShuffle.remove(player);
			}
			
			player.sendMessage("Your participation in the TvT event has been removed.");
		}
	}
	
	public static void cleanTvT()
	{
		for (final L2PcInstance player : players)
		{
			if (player != null)
			{
				removePlayer(player);
				if (savePlayers.contains(player.getName()))
				{
					savePlayers.remove(player.getName());
				}
				player.inEventTvT = false;
			}
		}
		
		if (playersShuffle != null && !playersShuffle.isEmpty())
		{
			for (final L2PcInstance player : playersShuffle)
			{
				if (player != null)
				{
					player.inEventTvT = false;
				}
			}
		}
		
		topKills = 0;
		topTeam = new String();
		players = new Vector<>();
		playersShuffle = new Vector<>();
		savePlayers = new Vector<>();
		savePlayerTeams = new Vector<>();
		
		teamPointsCount = new Vector<>();
		tvtTeamPlayersCount = new Vector<>();
		
		inProgress = false;
		
		loadData();
	}
	
	/**
	 * Adds the disconnected player.
	 * @param player the player
	 */
	public static synchronized void addDisconnectedPlayer(final L2PcInstance player)
	{
		if ((Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (teleport || started)) || (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE") && (teleport || started)))
		{
			if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
			}
			
			player.teamNameTvT = savePlayerTeams.get(savePlayers.indexOf(player.getName()));
			
			for (final L2PcInstance p : players)
			{
				if (p == null)
				{
					continue;
				}
				// check by name incase player got new objectId
				else if (p.getName().equals(player.getName()))
				{
					player.originalNameColorTvT = player.getAppearance().getNameColor();
					player.originalTitleTvT = player.getTitle();
					player.originalKarmaTvT = player.getKarma();
					player.inEventTvT = true;
					player.countTvTkills = p.countTvTkills;
					players.remove(p); // removing old object id from vector
					players.add(player); // adding new objectId to vector
					break;
				}
			}
			
			player.getAppearance().setNameColor(teamColors.get(tvtTeams.indexOf(player.teamNameTvT)));
			player.setKarma(0);
			if (Config.TVT_AURA)
			{
				if (tvtTeams.size() >= 2)
				{
					player.setTeam(tvtTeams.indexOf(player.teamNameTvT) + 1);
				}
			}
			player.broadcastUserInfo();
			
			player.teleToLocation(teamsX.get(tvtTeams.indexOf(player.teamNameTvT)), teamsY.get(tvtTeams.indexOf(player.teamNameTvT)), teamsZ.get(tvtTeams.indexOf(player.teamNameTvT)));
		}
	}
	
	/**
	 * Shuffle teams.
	 */
	public static void shuffleTeams()
	{
		int teamCount = 0;
		int playersCount = 0;
		
		for (;;)
		{
			if (playersShuffle.isEmpty())
			{
				break;
			}
			
			final int playerToAddIndex = Rnd.nextInt(playersShuffle.size());
			L2PcInstance player = null;
			player = playersShuffle.get(playerToAddIndex);
			
			players.add(player);
			players.get(playersCount).teamNameTvT = tvtTeams.get(teamCount);
			savePlayers.add(players.get(playersCount).getName());
			savePlayerTeams.add(tvtTeams.get(teamCount));
			playersCount++;
			
			if (teamCount == tvtTeams.size() - 1)
			{
				teamCount = 0;
			}
			else
			{
				teamCount++;
			}
			
			playersShuffle.remove(playerToAddIndex);
		}
		
	}
	
	// Show loosers and winners animations
	/**
	 * Play kneel animation.
	 * @param teamName the team name
	 */
	public static void playKneelAnimation(final String teamName)
	{
		for (final L2PcInstance player : players)
		{
			if (player != null)
			{
				if (!player.teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
				}
				else if (player.teamNameTvT.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}
	
	/**
	 * Reward team.
	 * @param teamName   the team name
	 * @param bestKiller the best killer
	 * @param looser     the looser
	 */
	public static void rewardTeam(final String teamName, final L2PcInstance bestKiller, final L2PcInstance looser)
	{
		for (L2PcInstance player : players)
		{
			if (player != null && (player.isOnline()) && (player.inEventTvT) && (!player.equals(looser)) && (player.countTvTkills > 0 || Config.TVT_PRICE_NO_KILLS))
			{
				if ((bestKiller != null) && (bestKiller.equals(player)))
				{
					player.addItem(eventName + " Event: " + eventName, rewardId, rewardAmount, player, true);
					player.addItem(eventName + " Event: " + eventName, Config.TVT_TOP_KILLER_REWARD, Config.TVT_TOP_KILLER_QTY, player, true);
					
				}
				else if (teamName != null && (player.teamNameTvT.equals(teamName)))
				{
					
					player.addItem(eventName + " Event: " + eventName, rewardId, rewardAmount, player, true);
					
					final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					final TextBuilder replyMSG = new TextBuilder("");
					
					replyMSG.append("<html><body>");
					replyMSG.append("<font color=\"FFFF00\">Your team wins the event. Look in your inventory for the reward.</font>");
					replyMSG.append("</body></html>");
					
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
				}
				else if (teamName == null)
				{ // TIE
					
					int minus_reward = 0;
					if (topKills != 0)
					{
						minus_reward = rewardAmount / 2;
					}
					else
					{
						// nobody killed
						minus_reward = rewardAmount / 4;
					}
					
					player.addItem(eventName + " Event: " + eventName, rewardId, minus_reward, player, true);
					
					final NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					final TextBuilder replyMSG = new TextBuilder("");
					
					replyMSG.append("<html><body>");
					replyMSG.append("<font color=\"FFFF00\">Your team had a tie in the event. Look in your inventory for the reward.</font>");
					replyMSG.append("</body></html>");
					
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
				}
			}
		}
	}
	
	/**
	 * Process top team.
	 */
	private static void processTopTeam()
	{
		topTeam = null;
		for (final String team : tvtTeams)
		{
			if (teamKillsCount(team) == topKills && topKills > 0)
			{
				topTeam = null;
			}
			
			if (teamKillsCount(team) > topKills)
			{
				topTeam = team;
				topKills = teamKillsCount(team);
			}
		}
	}
	
	/**
	 * Adds the team.
	 * @param teamName the team name
	 */
	public static void addTeam(final String teamName)
	{
		if (isInProgress())
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
			}
			return;
		}
		
		if (teamName.equals(" "))
		{
			return;
		}
		
		tvtTeams.add(teamName);
		tvtTeamPlayersCount.add(0);
		teamPointsCount.add(0);
		teamColors.add(0);
		teamsX.add(0);
		teamsY.add(0);
		teamsZ.add(0);
		
		addTeamEventOperations(teamName);
		
	}
	
	/**
	 * Adds the team event operations.
	 * @param teamName the team name
	 */
	private static void addTeamEventOperations(final String teamName)
	{
	}
	
	/**
	 * Removes the team.
	 * @param teamName the team name
	 */
	public static void removeTeam(final String teamName)
	{
		if (isInProgress() || tvtTeams.isEmpty())
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
			}
			return;
		}
		
		if (teamPlayersCount(teamName) > 0)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn(eventName + " Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
			}
			return;
		}
		
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamsZ.remove(index);
		teamsY.remove(index);
		teamsX.remove(index);
		teamColors.remove(index);
		teamPointsCount.remove(index);
		tvtTeamPlayersCount.remove(index);
		tvtTeams.remove(index);
		
		removeTeamEventItems(teamName);
		
	}
	
	/**
	 * Removes the team event items.
	 * @param teamName the team name
	 */
	private static void removeTeamEventItems(final String teamName)
	{
		tvtTeams.indexOf(teamName);
	}
	
	/**
	 * Sets the team pos.
	 * @param teamName   the team name
	 * @param activeChar the active char
	 */
	public static void setTeamPos(final String teamName, final L2PcInstance activeChar)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamsX.set(index, activeChar.getX());
		teamsY.set(index, activeChar.getY());
		teamsZ.set(index, activeChar.getZ());
	}
	
	/**
	 * Sets the team pos.
	 * @param teamName the team name
	 * @param x        the x
	 * @param y        the y
	 * @param z        the z
	 */
	public static void setTeamPos(final String teamName, final int x, final int y, final int z)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamsX.set(index, x);
		teamsY.set(index, y);
		teamsZ.set(index, z);
	}
	
	/**
	 * Sets the team color.
	 * @param teamName the team name
	 * @param color    the color
	 */
	public static void setTeamColor(final String teamName, final int color)
	{
		if (isInProgress())
		{
			return;
		}
		
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamColors.set(index, color);
	}
	
	/**
	 * Team players count.
	 * @param  teamName the team name
	 * @return          the int
	 */
	public static int teamPlayersCount(final String teamName)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return -1;
		}
		
		return tvtTeamPlayersCount.get(index);
	}
	
	/**
	 * Sets the team players count.
	 * @param teamName         the team name
	 * @param teamPlayersCount the team players count
	 */
	public static void setTeamPlayersCount(final String teamName, final int teamPlayersCount)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		tvtTeamPlayersCount.set(index, teamPlayersCount);
	}
	
	/**
	 * Check shuffle players.
	 * @param  eventPlayer the event player
	 * @return             true, if successful
	 */
	public static boolean checkShufflePlayers(final L2PcInstance eventPlayer)
	{
		try
		{
			for (final L2PcInstance player : playersShuffle)
			{
				if (player == null || !player.isOnline())
				{
					playersShuffle.remove(player);
					eventPlayer.inEventTvT = false;
					continue;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.inEventTvT = true;
					eventPlayer.countTvTkills = 0;
					return true;
				}
				
				// This 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					playersShuffle.remove(player);
					playersShuffle.add(eventPlayer);
					eventPlayer.inEventTvT = true;
					eventPlayer.countTvTkills = 0;
					return true;
				}
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void sendFinalMessages()
	{
		if (!started && !aborted)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Thank you For Participating At, " + eventName + " Event.");
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
	
	@Override
	public void run()
	{
		LOGGER.info(eventName + ": Event notification start");
		eventOnceStart();
	}
	
	@Override
	public String getEventIdentifier()
	{
		return eventName;
	}
	
	@Override
	public String getEventStartTime()
	{
		return startEventTime;
	}
	
	/**
	 * Sets the event start time.
	 * @param newTime the new event start time
	 */
	public void setEventStartTime(final String newTime)
	{
		startEventTime = newTime;
	}
	
	/**
	 * On disconnect.
	 * @param player the player
	 */
	public static void onDisconnect(final L2PcInstance player)
	{
		
		if (player.inEventTvT)
		{
			removePlayer(player);
			player.teleToLocation(npcX, npcY, npcZ);
		}
	}
	
	/**
	 * Team kills count.
	 * @param  teamName the team name
	 * @return          the int
	 */
	public static int teamKillsCount(final String teamName)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return -1;
		}
		
		return teamPointsCount.get(index);
	}
	
	/**
	 * Sets the team kills count.
	 * @param teamName       the team name
	 * @param teamKillsCount the team kills count
	 */
	public static void setTeamKillsCount(final String teamName, final int teamKillsCount)
	{
		final int index = tvtTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamPointsCount.set(index, teamKillsCount);
	}
	
	public static void kickPlayerFromTvt(final L2PcInstance playerToKick)
	{
		if (playerToKick == null)
		{
			return;
		}
		
		if (joining)
		{
			playersShuffle.remove(playerToKick);
			players.remove(playerToKick);
			playerToKick.inEventTvT = false;
			playerToKick.teamNameTvT = "";
			playerToKick.countTvTkills = 0;
		}
		
		if (started || teleport)
		{
			playersShuffle.remove(playerToKick);
			// playerToKick._inEventTvT = false;
			removePlayer(playerToKick);
			if (playerToKick.isOnline())
			{
				playerToKick.getAppearance().setNameColor(playerToKick.originalNameColorTvT);
				playerToKick.setKarma(playerToKick.originalKarmaTvT);
				playerToKick.setTitle(playerToKick.originalTitleTvT);
				playerToKick.broadcastUserInfo();
				playerToKick.sendMessage("You have been kicked from the TvT.");
				playerToKick.teleToLocation(npcX, npcY, npcZ, false);
				playerToKick.teleToLocation(npcX + Rnd.get(201) - 100, npcY + Rnd.get(201) - 100, npcZ, false);
			}
		}
	}
	
	/**
	 * Find best killer.
	 * @param  players the players
	 * @return         the l2 pc instance
	 */
	public static L2PcInstance findBestKiller(final Vector<L2PcInstance> players)
	{
		if (players == null)
		{
			return null;
		}
		
		L2PcInstance bestKiller = null;
		
		for (L2PcInstance player : players)
		{
			if ((bestKiller == null) || (bestKiller.countTvTkills < player.countTvTkills))
			{
				bestKiller = player;
			}
		}
		
		return bestKiller;
	}
	
	/**
	 * Find looser.
	 * @param  players the players
	 * @return         the l2 pc instance
	 */
	public static L2PcInstance findLooser(final Vector<L2PcInstance> players)
	{
		if (players == null)
		{
			return null;
		}
		
		L2PcInstance looser = null;
		
		for (L2PcInstance player : players)
		{
			if ((looser == null) || (looser.countTvTdies < player.countTvTdies))
			{
				looser = player;
			}
		}
		
		return looser;
	}
	
	public static class TvTTeam
	{
		private int killCount = -1;
		private String name = null;
		
		/**
		 * Instantiates a new tv t team.
		 * @param name      the name
		 * @param killCount the kill count
		 */
		TvTTeam(String name, int killCount)
		{
			this.killCount = killCount;
			this.name = name;
		}
		
		public int getKillCount()
		{
			return killCount;
		}
		
		public void setKillCount(int killCount)
		{
			this.killCount = killCount;
		}
		
		public String getName()
		{
			return name;
		}
		
		/**
		 * @param name Team name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}
	
	private static void closeFortDoors()
	{
		DoorTable.getInstance().getDoor(23170004).closeMe();
		DoorTable.getInstance().getDoor(23170005).closeMe();
		DoorTable.getInstance().getDoor(23170002).closeMe();
		DoorTable.getInstance().getDoor(23170003).closeMe();
		DoorTable.getInstance().getDoor(23170006).closeMe();
		DoorTable.getInstance().getDoor(23170007).closeMe();
		DoorTable.getInstance().getDoor(23170008).closeMe();
		DoorTable.getInstance().getDoor(23170009).closeMe();
		DoorTable.getInstance().getDoor(23170010).closeMe();
		DoorTable.getInstance().getDoor(23170011).closeMe();
	}
	
	private static void openFortDoors()
	{
		DoorTable.getInstance().getDoor(23170004).openMe();
		DoorTable.getInstance().getDoor(23170005).openMe();
		DoorTable.getInstance().getDoor(23170002).openMe();
		DoorTable.getInstance().getDoor(23170003).openMe();
		DoorTable.getInstance().getDoor(23170006).openMe();
		DoorTable.getInstance().getDoor(23170007).openMe();
		DoorTable.getInstance().getDoor(23170008).openMe();
		DoorTable.getInstance().getDoor(23170009).openMe();
		DoorTable.getInstance().getDoor(23170010).openMe();
		DoorTable.getInstance().getDoor(23170011).openMe();
	}
	
	private static void closeAdenColosseumDoors()
	{
		DoorTable.getInstance().getDoor(24190002).closeMe();
		DoorTable.getInstance().getDoor(24190003).closeMe();
	}
	
	private static void openAdenColosseumDoors()
	{
		DoorTable.getInstance().getDoor(24190002).openMe();
		DoorTable.getInstance().getDoor(24190003).openMe();
	}
}