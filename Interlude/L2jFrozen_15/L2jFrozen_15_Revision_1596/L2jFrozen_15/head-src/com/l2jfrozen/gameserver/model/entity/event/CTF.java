package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Radar;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.entity.event.manager.EventTask;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

import javolution.text.TextBuilder;

public class CTF implements EventTask
{
	protected static final Logger LOGGER = Logger.getLogger(CTF.class);
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
	protected static long intervalBetweenMatches = 0;
	private String startEventTime;
	protected static boolean teamEvent = true;
	public static Vector<L2PcInstance> players = new Vector<>();
	private static String topTeam = new String();
	public static Vector<L2PcInstance> playersShuffle = new Vector<>();
	public static Vector<String> ctfTeams = new Vector<>();
	public static Vector<String> savePlayers = new Vector<>();
	public static Vector<String> savePlayerTeams = new Vector<>();
	public static Vector<Integer> ctfTeamPlayersCount = new Vector<>();
	public static Vector<Integer> teamColors = new Vector<>();
	public static Vector<Integer> teamsX = new Vector<>();
	public static Vector<Integer> teamsY = new Vector<>();
	public static Vector<Integer> teamsZ = new Vector<>();
	public static Vector<Integer> teamPointsCount = new Vector<>();
	public static int topScore = 0;
	public static int eventCenterX = 0;
	public static int eventCenterY = 0;
	public static int eventCenterZ = 0;
	public static int eventOffset = 0;
	private static int flagNPC = 35062;
	private static int FLAG_IN_HAND_ITEM_ID = 6718;
	public static Vector<Integer> flagIds = new Vector<>();
	public static Vector<Integer> flagsX = new Vector<>();
	public static Vector<Integer> flagsY = new Vector<>();
	public static Vector<Integer> flagsZ = new Vector<>();
	public static Vector<L2Spawn> flagSpawns = new Vector<>();
	public static Vector<L2Spawn> throneSpawns = new Vector<>();
	public static Vector<Boolean> ctfFlagsTaken = new Vector<>();
	
	/**
	 * Gets the new instance.
	 * @return the new instance
	 */
	public static CTF getNewInstance()
	{
		return new CTF();
	}
	
	public static String getEventName()
	{
		return eventName;
	}
	
	/**
	 * Set_event name.
	 * @param  eventName the eventName to set
	 * @return           true, if successful
	 */
	public static boolean setEventName(String eventName)
	{
		if (!isInProgress())
		{
			CTF.eventName = eventName;
			return true;
		}
		return false;
	}
	
	public static String getEventDescription()
	{
		return eventDesc;
	}
	
	/**
	 * Set_event desc.
	 * @param  eventDesc the eventDesc to set
	 * @return           true, if successful
	 */
	public static boolean setEventDescription(String eventDesc)
	{
		if (!isInProgress())
		{
			CTF.eventDesc = eventDesc;
			return true;
		}
		return false;
	}
	
	public static String getJoiningLocationName()
	{
		return joiningLocationName;
	}
	
	/**
	 * Set_joining location name.
	 * @param  joiningLocationName the joiningLocationName to set
	 * @return                     true, if successful
	 */
	public static boolean setJoiningLocationName(String joiningLocationName)
	{
		if (!isInProgress())
		{
			CTF.joiningLocationName = joiningLocationName;
			return true;
		}
		return false;
	}
	
	public static int getNpcId()
	{
		return npcId;
	}
	
	/**
	 * Set_npc id.
	 * @param  npcId the npcId to set
	 * @return       true, if successful
	 */
	public static boolean setNpcId(int npcId)
	{
		if (!isInProgress())
		{
			CTF.npcId = npcId;
			return true;
		}
		return false;
	}
	
	public static Location getNpcLocation()
	{
		Location npc_loc = new Location(npcX, npcY, npcZ, npcHeading);
		return npc_loc;
	}
	
	public static int get_rewardId()
	{
		return rewardId;
	}
	
	/**
	 * @param  rewardId the rewardId to set
	 * @return          true, if successful
	 */
	public static boolean setRewardId(int rewardId)
	{
		if (!isInProgress())
		{
			CTF.rewardId = rewardId;
			return true;
		}
		return false;
	}
	
	public static int getRewardAmount()
	{
		return rewardAmount;
	}
	
	/**
	 * @param  rewardAmount the rewardAmount to set
	 * @return              true, if successful
	 */
	public static boolean setRewardAmount(int rewardAmount)
	{
		if (!isInProgress())
		{
			CTF.rewardAmount = rewardAmount;
			return true;
		}
		return false;
	}
	
	public static int getMinLvl()
	{
		return minlvl;
	}
	
	/**
	 * @param  minlvl the minlvl to set
	 * @return        true, if successful
	 */
	public static boolean setMinLvl(int minlvl)
	{
		if (!isInProgress())
		{
			CTF.minlvl = minlvl;
			return true;
		}
		return false;
	}
	
	public static int getMaxLvl()
	{
		return maxlvl;
	}
	
	/**
	 * Set_maxlvl.
	 * @param  maxlvl the maxlvl to set
	 * @return        true, if successful
	 */
	public static boolean setMaxLvl(int maxlvl)
	{
		if (!isInProgress())
		{
			CTF.maxlvl = maxlvl;
			return true;
		}
		return false;
	}
	
	public static int getJoinTime()
	{
		return joinTime;
	}
	
	/**
	 * @param  joinTime the joinTime to set
	 * @return          true, if successful
	 */
	public static boolean setJoinTime(int joinTime)
	{
		if (!isInProgress())
		{
			CTF.joinTime = joinTime;
			return true;
		}
		return false;
	}
	
	public static int getEventTime()
	{
		return eventTime;
	}
	
	/**
	 * @param  eventTime the eventTime to set
	 * @return           true, if successful
	 */
	public static boolean setEventTime(int eventTime)
	{
		if (!isInProgress())
		{
			CTF.eventTime = eventTime;
			return true;
		}
		return false;
	}
	
	public static int getMinPlayers()
	{
		return minPlayers;
	}
	
	/**
	 * @param  minPlayers the minPlayers to set
	 * @return            true, if successful
	 */
	public static boolean setMinPlayers(int minPlayers)
	{
		if (!isInProgress())
		{
			CTF.minPlayers = minPlayers;
			return true;
		}
		return false;
	}
	
	public static int getMaxPlayers()
	{
		return maxPlayers;
	}
	
	/**
	 * Set_max players.
	 * @param  maxPlayers the maxPlayers to set
	 * @return            true, if successful
	 */
	public static boolean setMaxPlayers(int maxPlayers)
	{
		if (!isInProgress())
		{
			CTF.maxPlayers = maxPlayers;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the interval between matchs.
	 * @return the intervalBetweenMatches
	 */
	public static long getIntervalBetweenMatches()
	{
		return intervalBetweenMatches;
	}
	
	/**
	 * @param  intervalBetweenMatches the intervalBetweenMatches to set
	 * @return                        true, if successful
	 */
	public static boolean setIntervalBetweenMatches(long intervalBetweenMatches)
	{
		if (!isInProgress())
		{
			CTF.intervalBetweenMatches = intervalBetweenMatches;
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
	public boolean setStartEventTime(String startEventTime)
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
	public static boolean checkMaxLevel(int maxlvl)
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
	public static boolean checkMinLevel(int minlvl)
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
	public static boolean checkMinPlayers(int players)
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
	public static boolean checkMaxPlayers(int players)
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
		if (ctfTeams.size() < 2 || teamsX.contains(0) || teamsY.contains(0) || teamsZ.contains(0))
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
		try
		{
			if (flagsX.contains(0) || flagsY.contains(0) || flagsZ.contains(0) || flagIds.contains(0))
			{
				return false;
			}
			if (flagsX.size() < ctfTeams.size() || flagsY.size() < ctfTeams.size() || flagsZ.size() < ctfTeams.size() || flagIds.size() < ctfTeams.size())
			{
				return false;
			}
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			return false;
		}
		
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
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(npcId);
		
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
			npcSpawn.getLastSpawn().isEventMobCTF = true;
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
		if (Config.CTF_ANNOUNCE_REWARD && ItemTable.getInstance().getTemplate(rewardId) != null)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Reward: " + rewardAmount + " " + ItemTable.getInstance().getTemplate(rewardId).getName());
		}
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Recruiting levels: " + minlvl + " to " + maxlvl);
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Joinable in " + joiningLocationName + ".");
		
		if (Config.CTF_COMMAND)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Commands .ctfjoin .ctfleave .ctfinfo!");
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
		
		if (teamEvent)
		{
			
			if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(playersShuffle.size()))
			{
				shuffleTeams();
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(playersShuffle.size()))
			{
				Announcements.getInstance().gameAnnounceToAll(eventName + ": Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + playersShuffle.size());
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info(eventName + ":Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + playersShuffle.size());
				}
				
				return false;
			}
			
		}
		else
		{
			
			if (!checkMinPlayers(players.size()))
			{
				Announcements.getInstance().gameAnnounceToAll(eventName + ": Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + players.size());
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info(eventName + ":Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + players.size());
				}
				return false;
			}
			
		}
		
		joining = false;
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Teleport to team spot in 20 seconds!");
		
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			sit();
			afterTeleportOperations();
			
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					if (Config.CTF_ON_START_UNSUMMON_PET)
					{
						// Remove Summon's buffs
						if (player.getPet() != null)
						{
							final L2Summon summon = player.getPet();
							for (final L2Effect e1 : summon.getAllEffects())
							{
								if (e1 != null)
								{
									e1.exit(true);
								}
							}
							
							if (summon instanceof L2PetInstance)
							{
								summon.unSummon(player);
							}
						}
					}
					
					if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
					{
						for (final L2Effect e2 : player.getAllEffects())
						{
							if (e2 != null)
							{
								e2.exit(true);
							}
						}
					}
					
					// Remove player from his party
					if (player.getParty() != null)
					{
						final L2Party party = player.getParty();
						party.removePartyMember(player);
					}
					
					if (teamEvent)
					{
						final int offset = Config.CTF_SPAWN_OFFSET;
						player.teleToLocation(teamsX.get(ctfTeams.indexOf(player.teamNameCTF)) + Rnd.get(offset), teamsY.get(ctfTeams.indexOf(player.teamNameCTF)) + Rnd.get(offset), teamsZ.get(ctfTeams.indexOf(player.teamNameCTF)));
						
					}
				}
			}
			
		}, 20000);
		teleport = true;
		return true;
	}
	
	protected static void afterTeleportOperations()
	{
		spawnAllFlags();
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
		
		afterStartOperations();
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": Started. Go Capture the Flags!");
		started = true;
		
		return true;
	}
	
	/**
	 * After start operations.
	 */
	private static void afterStartOperations()
	{
		for (L2PcInstance player : players)
		{
			if (player != null)
			{
				player.teamNameHaveFlagCTF = null;
				player.haveFlagCTF = false;
			}
		}
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
		final long delay = intervalBetweenMatches;
		
		Announcements.getInstance().gameAnnounceToAll(eventName + ": joining period will be avaible again in " + intervalBetweenMatches + " minute(s)!");
		
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
			
			if (topScore != 0)
			{
				
				playKneelAnimation(topTeam);
				
				if (Config.CTF_ANNOUNCE_TEAM_STATS)
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + " Team Statistics:");
					for (final String team : ctfTeams)
					{
						final int flags_ = teamPointsCount(team);
						Announcements.getInstance().gameAnnounceToAll(eventName + ": Team: " + team + " - Flags taken: " + flags_);
					}
				}
				
				if (topTeam != null)
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + ": Team " + topTeam + " wins the match, with " + topScore + " flags taken!");
				}
				else
				{
					Announcements.getInstance().gameAnnounceToAll(eventName + ": The event finished with a TIE: " + topScore + " flags taken by each team!");
				}
				rewardTeam(topTeam);
				
				if (Config.CTF_STATS_LOGGER)
				{
					
					LOGGER.info("**** " + eventName + " ****");
					LOGGER.info(eventName + " Team Statistics:");
					for (final String team : ctfTeams)
					{
						final int flagsCount = teamPointsCount(team);
						LOGGER.info("Team: " + team + " - Flags taken: " + flagsCount);
					}
					
					LOGGER.info(eventName + ": Team " + topTeam + " wins the match, with " + topScore + " flags taken!");
					
				}
				
			}
			else
			{
				
				Announcements.getInstance().gameAnnounceToAll(eventName + ": The event finished with a TIE: No team wins the match(nobody took flags)!");
				
				if (Config.CTF_STATS_LOGGER)
				{
					LOGGER.info(eventName + ": No team win the match(nobody took flags).");
				}
				
				rewardTeam(topTeam);
				
			}
			
		}
		teleportFinish();
	}
	
	private static void afterFinishOperations()
	{
		unspawnAllFlags();
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
			cleanCTF();
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
	
	private static void afterFinish()
	{
		unspawnAllFlags();
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
			
			sit();
			cleanCTF();
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
							
							LOGGER.info(eventName + ": waiting.....delay for restart event  " + intervalBetweenMatches + " minutes.");
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
	
	/**
	 * Removes the offline players.
	 */
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
					L2PcInstance player = playersShuffle.get(0);
					
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
		
		if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			if (ctfTeamPlayersCount.contains(0))
			{
				return false;
			}
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
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
		if (checkShufflePlayers(eventPlayer) || eventPlayer.inEventCTF)
		{
			eventPlayer.sendMessage("You already participated in the event!");
			return false;
		}
		
		if (eventPlayer.inEventTvT || eventPlayer.inEventDM)
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
					
					if (player != null && player.inEventCTF)
					{
						eventPlayer.sendMessage("You already participated in event with another char!");
						return false;
					}
				}
			}
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
		
		if (CTF.savePlayers.contains(eventPlayer.getName()))
		{
			eventPlayer.sendMessage("You already participated in another event!");
			return false;
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("NO"))
		{
			return true;
		}
		else if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			
			for (final int playersCount : ctfTeamPlayersCount)
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
			
			for (final int teamPlayerCount : ctfTeamPlayersCount)
			{
				if (teamPlayerCount < countBefore)
				{
					countBefore = teamPlayerCount;
				}
			}
			
			final Vector<String> joinableTeams = new Vector<>();
			
			for (final String team : ctfTeams)
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
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			return true;
		}
		
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}
	
	/**
	 * Sets the user data.
	 */
	public static void setUserData()
	{
		for (final L2PcInstance player : players)
		{
			player.originalNameColorCTF = player.getAppearance().getNameColor();
			player.originalKarmaCTF = player.getKarma();
			player.originalTitleCTF = player.getTitle();
			player.getAppearance().setNameColor(teamColors.get(ctfTeams.indexOf(player.teamNameCTF)));
			player.setKarma(0);
			if (Config.CTF_AURA)
			{
				if (ctfTeams.size() >= 2)
				{
					player.setTeam(ctfTeams.indexOf(player.teamNameCTF) + 1);
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
		
		for (final String team : ctfTeams)
		{
			LOGGER.info(team + " Flags Taken :" + teamPointsCount.get(ctfTeams.indexOf(team)));
		}
		
		if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
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
				LOGGER.info("Name: " + player.getName() + "   Team: " + player.teamNameCTF + "  Flags :" + player.countCTFflags);
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
		
		dumpLocalEventInfo();
	}
	
	private static void dumpLocalEventInfo()
	{
		LOGGER.info("**********==CTF==************");
		LOGGER.info("CTF._teamPointsCount:" + teamPointsCount.toString());
		LOGGER.info("CTF._flagIds:" + flagIds.toString());
		LOGGER.info("CTF._flagSpawns:" + flagSpawns.toString());
		LOGGER.info("CTF._throneSpawns:" + throneSpawns.toString());
		LOGGER.info("CTF._flagsTaken:" + ctfFlagsTaken.toString());
		LOGGER.info("CTF._flagsX:" + flagsX.toString());
		LOGGER.info("CTF._flagsY:" + flagsY.toString());
		LOGGER.info("CTF._flagsZ:" + flagsZ.toString());
		LOGGER.info("************EOF**************\n");
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
		ctfTeams = new Vector<>();
		savePlayerTeams = new Vector<>();
		playersShuffle = new Vector<>();
		ctfTeamPlayersCount = new Vector<>();
		teamPointsCount = new Vector<>();
		teamColors = new Vector<>();
		teamsX = new Vector<>();
		teamsY = new Vector<>();
		teamsZ = new Vector<>();
		
		throneSpawns = new Vector<>();
		flagSpawns = new Vector<>();
		ctfFlagsTaken = new Vector<>();
		flagIds = new Vector<>();
		flagsX = new Vector<>();
		flagsY = new Vector<>();
		flagsZ = new Vector<>();
		
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
		topScore = 0;
		minlvl = 0;
		maxlvl = 0;
		joinTime = 0;
		eventTime = 0;
		minPlayers = 0;
		maxPlayers = 0;
		intervalBetweenMatches = 0;
		
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("Select * from ctf");
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
				intervalBetweenMatches = rs.getLong("delayForNextEvent");
			}
			DatabaseUtils.close(statement);
			
			int index = -1;
			if (teams > 0)
			{
				index = 0;
			}
			while (index < teams && index > -1)
			{
				statement = con.prepareStatement("Select * from ctf_teams where teamId = ?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
				{
					ctfTeams.add(rs.getString("teamName"));
					ctfTeamPlayersCount.add(0);
					teamPointsCount.add(0);
					teamColors.add(0);
					teamsX.add(0);
					teamsY.add(0);
					teamsZ.add(0);
					teamsX.set(index, rs.getInt("teamX"));
					teamsY.set(index, rs.getInt("teamY"));
					teamsZ.set(index, rs.getInt("teamZ"));
					teamColors.set(index, rs.getInt("teamColor"));
					
					flagsX.add(0);
					flagsY.add(0);
					flagsZ.add(0);
					flagsX.set(index, rs.getInt("flagX"));
					flagsY.set(index, rs.getInt("flagY"));
					flagsZ.set(index, rs.getInt("flagZ"));
					flagSpawns.add(null);
					flagIds.add(flagNPC);
					ctfFlagsTaken.add(false);
					
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
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(playersShuffle.size()))
			{
				if (!started)
				{
					replyMSG.append("Currently participated: <font color=\"00FF00\">" + playersShuffle.size() + ".</font><br>");
					replyMSG.append("Max players: <font color=\"00FF00\">" + maxPlayers + "</font><br><br>");
					replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
				}
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.CTF_JOIN_CURSED)
			{
				replyMSG.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
			}
			else if (!started && joining && eventPlayer.getLevel() >= minlvl && eventPlayer.getLevel() <= maxlvl)
			{
				if (players.contains(eventPlayer) || playersShuffle.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer.teamNameCTF + "</font><br><br>");
					}
					else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
					}
					
					replyMSG.append("<center>Joined Players: <font color=\"00FF00\">" + playersShuffle.size() + "</font></center><br>");
					
					replyMSG.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
					replyMSG.append("<center><button value=\"Remove\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
				}
				else
				{
					replyMSG.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
					replyMSG.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">" + minlvl + "</font></center></td><br>");
					replyMSG.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">" + maxlvl + "</font></center></td><br><br>");
					replyMSG.append("<center><font color=\"3366CC\">Teams:</font></center><br>");
					
					if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (final String team : ctfTeams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
						}
						replyMSG.append("</table></center>");
					}
					else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center>");
						
						for (final String team : ctfTeams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font> &nbsp;</td>");
						}
						
						replyMSG.append("</center><br>");
						
						replyMSG.append("<center><button value=\"Join Event\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
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
		
		if (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE"))
		{
			player.teamNameCTF = teamName;
			players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
		{
			playersShuffle.add(player);
		}
		
		player.inEventCTF = true;
		player.countCTFflags = 0;
		player.sendMessage(eventName + ": You successfully registered for the event.");
	}
	
	/**
	 * Removes the player.
	 * @param player the player
	 */
	public static void removePlayer(final L2PcInstance player)
	{
		if (player.inEventCTF)
		{
			if (!joining)
			{
				player.getAppearance().setNameColor(player.originalNameColorCTF);
				player.setTitle(player.originalTitleCTF);
				player.setKarma(player.originalKarmaCTF);
				if (Config.CTF_AURA)
				{
					if (ctfTeams.size() >= 2)
					{
						player.setTeam(0);// clear aura :P
					}
				}
				player.broadcastUserInfo();
			}
			
			// after remove, all event data must be cleaned in player
			player.originalNameColorCTF = 0;
			player.originalTitleCTF = null;
			player.originalKarmaCTF = 0;
			player.teamNameCTF = new String();
			player.countCTFflags = 0;
			player.inEventCTF = false;
			
			if ((Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE")) && players.contains(player))
			{
				setTeamPlayersCount(player.teamNameCTF, teamPlayersCount(player.teamNameCTF) - 1);
				players.remove(player);
			}
			else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (!playersShuffle.isEmpty() && playersShuffle.contains(player)))
			{
				playersShuffle.remove(player);
			}
			
			player.sendMessage("Your participation in the CTF event has been removed.");
		}
	}
	
	/**
	 * Clean ctf.
	 */
	public static void cleanCTF()
	{
		for (L2PcInstance player : players)
		{
			if (player != null)
			{
				
				cleanEventPlayer(player);
				
				removePlayer(player);
				if (savePlayers.contains(player.getName()))
				{
					savePlayers.remove(player.getName());
				}
				player.inEventCTF = false;
			}
		}
		
		if (playersShuffle != null && !playersShuffle.isEmpty())
		{
			for (final L2PcInstance player : playersShuffle)
			{
				if (player != null)
				{
					player.inEventCTF = false;
				}
			}
		}
		
		topScore = 0;
		topTeam = new String();
		players = new Vector<>();
		
		playersShuffle = new Vector<>();
		savePlayers = new Vector<>();
		savePlayerTeams = new Vector<>();
		
		teamPointsCount = new Vector<>();
		ctfTeamPlayersCount = new Vector<>();
		
		cleanLocalEventInfo();
		
		inProgress = false;
		
		loadData();
	}
	
	/**
	 * Clean local event info.
	 */
	private static void cleanLocalEventInfo()
	{
		
		flagSpawns = new Vector<>();
		ctfFlagsTaken = new Vector<>();
		
	}
	
	/**
	 * Clean event player.
	 * @param player the player
	 */
	private static void cleanEventPlayer(final L2PcInstance player)
	{
		
		if (player.haveFlagCTF)
		{
			removeFlagFromPlayer(player);
		}
		else
		{
			player.getInventory().destroyItemByItemId("", CTF.FLAG_IN_HAND_ITEM_ID, 1, player, null);
		}
		player.haveFlagCTF = false;
		
	}
	
	/**
	 * Adds the disconnected player.
	 * @param player the player
	 */
	public static synchronized void addDisconnectedPlayer(final L2PcInstance player)
	{
		if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE") && (teleport || started)) || (Config.CTF_EVEN_TEAMS.equals("NO") || Config.CTF_EVEN_TEAMS.equals("BALANCE") && (teleport || started)))
		{
			if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
			{
				player.stopAllEffects();
			}
			
			player.teamNameCTF = savePlayerTeams.get(savePlayers.indexOf(player.getName()));
			
			for (final L2PcInstance p : players)
			{
				if (p == null)
				{
					continue;
				}
				// check by name incase player got new objectId
				else if (p.getName().equals(player.getName()))
				{
					player.originalNameColorCTF = player.getAppearance().getNameColor();
					player.originalTitleCTF = player.getTitle();
					player.originalKarmaCTF = player.getKarma();
					player.inEventCTF = true;
					player.countCTFflags = p.countCTFflags;
					players.remove(p); // removing old object id from vector
					players.add(player); // adding new objectId to vector
					break;
				}
			}
			
			player.getAppearance().setNameColor(teamColors.get(ctfTeams.indexOf(player.teamNameCTF)));
			player.setKarma(0);
			if (Config.CTF_AURA)
			{
				if (ctfTeams.size() >= 2)
				{
					player.setTeam(ctfTeams.indexOf(player.teamNameCTF) + 1);
				}
			}
			player.broadcastUserInfo();
			
			final int offset = Config.CTF_SPAWN_OFFSET;
			player.teleToLocation(teamsX.get(ctfTeams.indexOf(player.teamNameCTF)) + Rnd.get(offset), teamsY.get(ctfTeams.indexOf(player.teamNameCTF)) + Rnd.get(offset), teamsZ.get(ctfTeams.indexOf(player.teamNameCTF)));
			
			afterAddDisconnectedPlayerOperations(player);
			
		}
	}
	
	/**
	 * After add disconnected player operations.
	 * @param player the player
	 */
	private static void afterAddDisconnectedPlayerOperations(final L2PcInstance player)
	{
		
		player.teamNameHaveFlagCTF = null;
		player.haveFlagCTF = false;
		checkRestoreFlags();
		
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
			players.get(playersCount).teamNameCTF = ctfTeams.get(teamCount);
			savePlayers.add(players.get(playersCount).getName());
			savePlayerTeams.add(ctfTeams.get(teamCount));
			playersCount++;
			
			if (teamCount == ctfTeams.size() - 1)
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
		for (L2PcInstance player : players)
		{
			if (player != null)
			{
				if (!player.teamNameCTF.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
				}
				else if (player.teamNameCTF.equals(teamName))
				{
					player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				}
			}
		}
	}
	
	/**
	 * Reward team.
	 * @param teamName the team name
	 */
	public static void rewardTeam(final String teamName)
	{
		for (L2PcInstance player : players)
		{
			if (player != null && (player.isOnline()) && (player.inEventCTF))
			{
				if (teamName != null && (player.teamNameCTF.equals(teamName)))
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
					if (topScore != 0)
					{
						minus_reward = rewardAmount / 2;
					}
					else
					{
						// nobody took flags
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
		for (final String team : ctfTeams)
		{
			if (teamPointsCount(team) == topScore && topScore > 0)
			{
				topTeam = null;
			}
			
			if (teamPointsCount(team) > topScore)
			{
				topTeam = team;
				topScore = teamPointsCount(team);
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
		
		ctfTeams.add(teamName);
		ctfTeamPlayersCount.add(0);
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
		
		addOrSet(ctfTeams.indexOf(teamName), null, false, flagNPC, 0, 0, 0);
		
	}
	
	/**
	 * Removes the team.
	 * @param teamName the team name
	 */
	public static void removeTeam(final String teamName)
	{
		if (isInProgress() || ctfTeams.isEmpty())
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
		
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamsZ.remove(index);
		teamsY.remove(index);
		teamsX.remove(index);
		teamColors.remove(index);
		teamPointsCount.remove(index);
		ctfTeamPlayersCount.remove(index);
		ctfTeams.remove(index);
		
		removeTeamEventItems(teamName);
		
	}
	
	/**
	 * Removes the team event items.
	 * @param teamName the team name
	 */
	private static void removeTeamEventItems(final String teamName)
	{
		
		final int index = ctfTeams.indexOf(teamName);
		
		flagSpawns.remove(index);
		ctfFlagsTaken.remove(index);
		flagIds.remove(index);
		flagsX.remove(index);
		flagsY.remove(index);
		flagsZ.remove(index);
		
	}
	
	/**
	 * Sets the team pos.
	 * @param teamName   the team name
	 * @param activeChar the active char
	 */
	public static void setTeamPos(final String teamName, final L2PcInstance activeChar)
	{
		final int index = ctfTeams.indexOf(teamName);
		
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
		final int index = ctfTeams.indexOf(teamName);
		
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
		
		final int index = ctfTeams.indexOf(teamName);
		
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
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return -1;
		}
		
		return ctfTeamPlayersCount.get(index);
	}
	
	/**
	 * Sets the team players count.
	 * @param teamName         the team name
	 * @param teamPlayersCount the team players count
	 */
	public static void setTeamPlayersCount(final String teamName, final int teamPlayersCount)
	{
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		ctfTeamPlayersCount.set(index, teamPlayersCount);
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
					eventPlayer.inEventCTF = false;
					continue;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.inEventCTF = true;
					eventPlayer.countCTFflags = 0;
					return true;
				}
				
				// This 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					playersShuffle.remove(player);
					playersShuffle.add(eventPlayer);
					eventPlayer.inEventCTF = true;
					eventPlayer.countCTFflags = 0;
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
	
	/**
	 * just an announcer to send termination messages.
	 */
	public static void sendFinalMessages()
	{
		if (!started && !aborted)
		{
			Announcements.getInstance().gameAnnounceToAll(eventName + ": Thank you For Participating At, " + eventName + " Event.");
		}
	}
	
	/**
	 * returns the interval between each event.
	 * @return the interval between matches
	 */
	public static int getIntervalBetweenMatchs()
	{
		final long actualTime = System.currentTimeMillis();
		final long totalTime = actualTime + intervalBetweenMatches;
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
		
		if (player.inEventCTF)
		{
			removePlayer(player);
			player.teleToLocation(npcX, npcY, npcZ);
		}
		
	}
	
	/**
	 * Team points count.
	 * @param  teamName the team name
	 * @return          the int
	 */
	public static int teamPointsCount(final String teamName)
	{
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return -1;
		}
		
		return teamPointsCount.get(index);
	}
	
	/**
	 * Sets the team points count.
	 * @param teamName       the team name
	 * @param teamPointCount the team point count
	 */
	public static void setTeamPointsCount(final String teamName, final int teamPointCount)
	{
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		
		teamPointsCount.set(index, teamPointCount);
	}
	
	public static int getEventOffset()
	{
		return eventOffset;
	}
	
	/**
	 * @param  eventOffset the eventOffset to set
	 * @return             true, if successful
	 */
	public static boolean setEventOffset(int eventOffset)
	{
		if (!isInProgress())
		{
			CTF.eventOffset = eventOffset;
			return true;
		}
		return false;
	}
	
	/**
	 * Show flag html.
	 * @param eventPlayer the event player
	 * @param objectId    the object id
	 * @param teamName    the team name
	 */
	public static void showFlagHtml(final L2PcInstance eventPlayer, final String objectId, final String teamName)
	{
		if (eventPlayer == null)
		{
			return;
		}
		
		try
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			final TextBuilder replyMSG = new TextBuilder("<html><head><body><center>");
			replyMSG.append("CTF Flag<br><br>");
			replyMSG.append("<font color=\"00FF00\">" + teamName + "'s Flag</font><br1>");
			if (eventPlayer.teamNameCTF != null && eventPlayer.teamNameCTF.equals(teamName))
			{
				replyMSG.append("<font color=\"LEVEL\">This is your Flag</font><br1>");
			}
			else
			{
				replyMSG.append("<font color=\"LEVEL\">Enemy Flag!</font><br1>");
			}
			if (started)
			{
				processInFlagRange(eventPlayer);
			}
			else
			{
				replyMSG.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");
			}
			replyMSG.append("</center></body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			LOGGER.info("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
		}
	}
	
	/**
	 * Check restore flags.
	 */
	public static void checkRestoreFlags()
	{
		Vector<Integer> teamsTakenFlag = new Vector<>();
		try
		{
			for (final L2PcInstance player : players)
			{
				if (player != null)
				{
					if (!player.isOnline() && player.haveFlagCTF)
					{ // logged off with a flag in his hands
						Announcements.getInstance().gameAnnounceToAll(eventName + ": " + player.getName() + " logged off with a CTF flag!");
						player.haveFlagCTF = false;
						if (ctfTeams.indexOf(player.teamNameHaveFlagCTF) >= 0)
						{
							if (ctfFlagsTaken.get(ctfTeams.indexOf(player.teamNameHaveFlagCTF)))
							{
								ctfFlagsTaken.set(ctfTeams.indexOf(player.teamNameHaveFlagCTF), false);
								spawnFlag(player.teamNameHaveFlagCTF);
								Announcements.getInstance().gameAnnounceToAll(eventName + ": " + player.teamNameHaveFlagCTF + " flag now returned to place.");
							}
						}
						removeFlagFromPlayer(player);
						player.teamNameHaveFlagCTF = null;
						return;
					}
					else if (player.haveFlagCTF)
					{
						teamsTakenFlag.add(ctfTeams.indexOf(player.teamNameHaveFlagCTF));
					}
				}
			}
			
			// Go over the list of ALL teams
			for (final String team : ctfTeams)
			{
				if (team == null)
				{
					continue;
				}
				final int index = ctfTeams.indexOf(team);
				if (!teamsTakenFlag.contains(index))
				{
					if (ctfFlagsTaken.get(index))
					{
						ctfFlagsTaken.set(index, false);
						spawnFlag(team);
						Announcements.getInstance().gameAnnounceToAll(eventName + ": " + team + " flag returned due to player error.");
					}
				}
			}
			// Check if a player ran away from the event holding a flag:
			for (final L2PcInstance player : players)
			{
				if (player != null && player.haveFlagCTF)
				{
					if (isOutsideCTFArea(player))
					{
						Announcements.getInstance().gameAnnounceToAll(eventName + ": " + player.getName() + " escaped from the event holding a flag!");
						player.haveFlagCTF = false;
						if (ctfTeams.indexOf(player.teamNameHaveFlagCTF) >= 0)
						{
							if (ctfFlagsTaken.get(ctfTeams.indexOf(player.teamNameHaveFlagCTF)))
							{
								ctfFlagsTaken.set(ctfTeams.indexOf(player.teamNameHaveFlagCTF), false);
								spawnFlag(player.teamNameHaveFlagCTF);
								Announcements.getInstance().gameAnnounceToAll(eventName + ": " + player.teamNameHaveFlagCTF + " flag now returned to place.");
							}
						}
						removeFlagFromPlayer(player);
						player.teamNameHaveFlagCTF = null;
						player.teleToLocation(teamsX.get(ctfTeams.indexOf(player.teamNameCTF)), teamsY.get(ctfTeams.indexOf(player.teamNameCTF)), teamsZ.get(ctfTeams.indexOf(player.teamNameCTF)));
						player.sendMessage("You have been returned to your team spawn");
						return;
					}
				}
			}
			
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.info("CTF.restoreFlags() Error:" + e.toString());
			return;
		}
	}
	
	/**
	 * Adds the flag to player.
	 * @param player the player
	 */
	public static void addFlagToPlayer(final L2PcInstance player)
	{
		// Remove items from the player hands (right, left, both)
		// This is NOT a BUG, I don't want them to see the icon they have 8D
		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LRHAND);
			}
		}
		else
		{
			player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_RHAND);
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if (wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LHAND);
			}
		}
		// Add the flag in his hands
		player.getInventory().equipItem(ItemTable.getInstance().createItem("", CTF.FLAG_IN_HAND_ITEM_ID, 1, player, null));
		player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow
		player.haveFlagCTF = true;
		player.broadcastUserInfo();
		final CreatureSay cs = new CreatureSay(player.getObjectId(), 15, ":", "You got it! Run back! ::"); // 8D
		player.sendPacket(cs);
	}
	
	/**
	 * Removes the flag from player.
	 * @param player the player
	 */
	public static void removeFlagFromPlayer(final L2PcInstance player)
	{
		final L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		player.haveFlagCTF = false;
		if (wpn != null)
		{
			final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			player.getInventory().destroyItemByItemId("", CTF.FLAG_IN_HAND_ITEM_ID, 1, player, null);
			final InventoryUpdate iu = new InventoryUpdate();
			for (final L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			player.sendPacket(new ItemList(player, true)); // Get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
		else
		{
			player.getInventory().destroyItemByItemId("", CTF.FLAG_IN_HAND_ITEM_ID, 1, player, null);
			player.sendPacket(new ItemList(player, true)); // Get your weapon back now ...
			player.abortAttack();
			player.broadcastUserInfo();
		}
	}
	
	/**
	 * Sets the team flag.
	 * @param teamName   the team name
	 * @param activeChar the active char
	 */
	public static void setTeamFlag(final String teamName, final L2PcInstance activeChar)
	{
		final int index = ctfTeams.indexOf(teamName);
		
		if (index == -1)
		{
			return;
		}
		addOrSet(ctfTeams.indexOf(teamName), null, false, flagNPC, activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	/**
	 * Spawn all flags.
	 */
	public static void spawnAllFlags()
	{
		while (flagSpawns.size() < ctfTeams.size())
		{
			flagSpawns.add(null);
		}
		while (throneSpawns.size() < ctfTeams.size())
		{
			throneSpawns.add(null);
		}
		for (final String team : ctfTeams)
		{
			final int index = ctfTeams.indexOf(team);
			final L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(flagIds.get(index));
			final L2NpcTemplate throne = NpcTable.getInstance().getTemplate(32027);
			try
			{
				// Spawn throne
				throneSpawns.set(index, new L2Spawn(throne));
				throneSpawns.get(index).setLocx(flagsX.get(index));
				throneSpawns.get(index).setLocy(flagsY.get(index));
				throneSpawns.get(index).setLocz(flagsZ.get(index) - 10);
				throneSpawns.get(index).setAmount(1);
				throneSpawns.get(index).setHeading(0);
				throneSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(throneSpawns.get(index), false);
				throneSpawns.get(index).init();
				throneSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				throneSpawns.get(index).getLastSpawn().decayMe();
				throneSpawns.get(index).getLastSpawn().spawnMe(throneSpawns.get(index).getLastSpawn().getX(), throneSpawns.get(index).getLastSpawn().getY(), throneSpawns.get(index).getLastSpawn().getZ());
				throneSpawns.get(index).getLastSpawn().setTitle(team + " Throne");
				throneSpawns.get(index).getLastSpawn().broadcastPacket(new MagicSkillUser(throneSpawns.get(index).getLastSpawn(), throneSpawns.get(index).getLastSpawn(), 1036, 1, 5500, 1));
				throneSpawns.get(index).getLastSpawn().isCTF_throneSpawn = true;
				// Spawn flag
				flagSpawns.set(index, new L2Spawn(tmpl));
				flagSpawns.get(index).setLocx(flagsX.get(index));
				flagSpawns.get(index).setLocy(flagsY.get(index));
				flagSpawns.get(index).setLocz(flagsZ.get(index));
				flagSpawns.get(index).setAmount(1);
				flagSpawns.get(index).setHeading(0);
				flagSpawns.get(index).setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(flagSpawns.get(index), false);
				flagSpawns.get(index).init();
				flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
				flagSpawns.get(index).getLastSpawn().setTitle(team + "'s Flag");
				flagSpawns.get(index).getLastSpawn().cTF_FlagTeamName = team;
				flagSpawns.get(index).getLastSpawn().decayMe();
				flagSpawns.get(index).getLastSpawn().spawnMe(flagSpawns.get(index).getLastSpawn().getX(), flagSpawns.get(index).getLastSpawn().getY(), flagSpawns.get(index).getLastSpawn().getZ());
				flagSpawns.get(index).getLastSpawn().isCTF_Flag = true;
				calculateOutSideOfCTF(); // Sets event boundaries so players don't run with the flag.
			}
			catch (final Exception e)
			{
				LOGGER.info("CTF Engine[spawnAllFlags()]: exception: ");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Unspawn all flags.
	 */
	public static void unspawnAllFlags()
	{
		try
		{
			if (throneSpawns == null || flagSpawns == null || ctfTeams == null)
			{
				return;
			}
			for (final String team : ctfTeams)
			{
				final int index = ctfTeams.indexOf(team);
				if (throneSpawns.get(index) != null)
				{
					throneSpawns.get(index).getLastSpawn().deleteMe();
					throneSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(throneSpawns.get(index), true);
				}
				if (flagSpawns.get(index) != null)
				{
					flagSpawns.get(index).getLastSpawn().deleteMe();
					flagSpawns.get(index).stopRespawn();
					SpawnTable.getInstance().deleteSpawn(flagSpawns.get(index), true);
				}
			}
			throneSpawns.removeAllElements();
		}
		catch (final Exception e)
		{
			LOGGER.info("CTF Engine[unspawnAllFlags()]: exception: ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Unspawn flag.
	 * @param teamName the team name
	 */
	private static void unspawnFlag(final String teamName)
	{
		final int index = ctfTeams.indexOf(teamName);
		
		flagSpawns.get(index).getLastSpawn().deleteMe();
		flagSpawns.get(index).stopRespawn();
		SpawnTable.getInstance().deleteSpawn(flagSpawns.get(index), true);
	}
	
	/**
	 * Spawn flag.
	 * @param teamName the team name
	 */
	public static void spawnFlag(final String teamName)
	{
		final int index = ctfTeams.indexOf(teamName);
		final L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(flagIds.get(index));
		
		try
		{
			flagSpawns.set(index, new L2Spawn(tmpl));
			
			flagSpawns.get(index).setLocx(flagsX.get(index));
			flagSpawns.get(index).setLocy(flagsY.get(index));
			flagSpawns.get(index).setLocz(flagsZ.get(index));
			flagSpawns.get(index).setAmount(1);
			flagSpawns.get(index).setHeading(0);
			flagSpawns.get(index).setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(flagSpawns.get(index), false);
			
			flagSpawns.get(index).init();
			flagSpawns.get(index).getLastSpawn().getStatus().setCurrentHp(999999999);
			flagSpawns.get(index).getLastSpawn().setTitle(teamName + "'s Flag");
			flagSpawns.get(index).getLastSpawn().cTF_FlagTeamName = teamName;
			flagSpawns.get(index).getLastSpawn().isCTF_Flag = true;
			flagSpawns.get(index).getLastSpawn().decayMe();
			flagSpawns.get(index).getLastSpawn().spawnMe(flagSpawns.get(index).getLastSpawn().getX(), flagSpawns.get(index).getLastSpawn().getY(), flagSpawns.get(index).getLastSpawn().getZ());
		}
		catch (final Exception e)
		{
			LOGGER.info("CTF Engine[spawnFlag(" + teamName + ")]: exception: ");
			e.printStackTrace();
		}
	}
	
	/**
	 * In range of flag.
	 * @param  player    the player
	 * @param  flagIndex the flag index
	 * @param  offset    the offset
	 * @return           true, if successful
	 */
	public static boolean InRangeOfFlag(final L2PcInstance player, final int flagIndex, final int offset)
	{
		if (player.getX() > CTF.flagsX.get(flagIndex) - offset && player.getX() < CTF.flagsX.get(flagIndex) + offset && player.getY() > CTF.flagsY.get(flagIndex) - offset && player.getY() < CTF.flagsY.get(flagIndex) + offset && player.getZ() > CTF.flagsZ.get(flagIndex) - offset && player.getZ() < CTF.flagsZ.get(flagIndex) + offset)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Process in flag range.
	 * @param player the player
	 */
	public static void processInFlagRange(final L2PcInstance player)
	{
		try
		{
			checkRestoreFlags();
			for (final String team : ctfTeams)
			{
				if (team.equals(player.teamNameCTF))
				{
					final int indexOwn = ctfTeams.indexOf(player.teamNameCTF);
					
					// If player is near his team flag holding the enemy flag
					if (InRangeOfFlag(player, indexOwn, 100) && !ctfFlagsTaken.get(indexOwn) && player.haveFlagCTF)
					{
						final int indexEnemy = ctfTeams.indexOf(player.teamNameHaveFlagCTF);
						// Return enemy flag to place
						ctfFlagsTaken.set(indexEnemy, false);
						spawnFlag(player.teamNameHaveFlagCTF);
						// Remove the flag from this player
						player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow
						player.broadcastUserInfo();
						player.broadcastPacket(new SocialAction(player.getObjectId(), 3)); // Victory
						player.broadcastUserInfo();
						removeFlagFromPlayer(player);
						teamPointsCount.set(indexOwn, teamPointsCount(team) + 1);
						Announcements.getInstance().gameAnnounceToAll(eventName + ": " + player.getName() + " scores for " + player.teamNameCTF + ".");
					}
				}
				else
				{
					final int indexEnemy = ctfTeams.indexOf(team);
					// If the player is near a enemy flag
					if (InRangeOfFlag(player, indexEnemy, 100) && !ctfFlagsTaken.get(indexEnemy) && !player.haveFlagCTF && !player.isDead())
					{
						ctfFlagsTaken.set(indexEnemy, true);
						unspawnFlag(team);
						player.teamNameHaveFlagCTF = team;
						addFlagToPlayer(player);
						player.broadcastUserInfo();
						player.haveFlagCTF = true;
						Announcements.getInstance().gameAnnounceToAll(eventName + ": " + team + " flag taken by " + player.getName() + "...");
						pointTeamTo(player, team);
						break;
					}
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Point team to.
	 * @param hasFlag the has flag
	 * @param ourFlag the our flag
	 */
	public static void pointTeamTo(final L2PcInstance hasFlag, final String ourFlag)
	{
		try
		{
			for (final L2PcInstance player : players)
			{
				if (player != null && player.isOnline())
				{
					if (player.teamNameCTF.equals(ourFlag))
					{
						player.sendMessage(hasFlag.getName() + " took your flag!");
						if (player.haveFlagCTF)
						{
							player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
							player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
						}
						else
						{
							player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
							final L2Radar rdr = new L2Radar(player);
							final L2Radar.RadarOnPlayer radar = rdr.new RadarOnPlayer(hasFlag, player);
							ThreadPoolManager.getInstance().scheduleGeneral(radar, 10000 + Rnd.get(30000));
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Adds the or set.
	 * @param listSize   the list size
	 * @param flagSpawn  the flag spawn
	 * @param flagsTaken the flags taken
	 * @param flagId     the flag id
	 * @param flagX      the flag x
	 * @param flagY      the flag y
	 * @param flagZ      the flag z
	 */
	private static void addOrSet(final int listSize, final L2Spawn flagSpawn, final boolean flagsTaken, final int flagId, final int flagX, final int flagY, final int flagZ)
	{
		while (flagsX.size() <= listSize)
		{
			flagSpawns.add(null);
			ctfFlagsTaken.add(false);
			flagIds.add(flagNPC);
			flagsX.add(0);
			flagsY.add(0);
			flagsZ.add(0);
		}
		flagSpawns.set(listSize, flagSpawn);
		ctfFlagsTaken.set(listSize, flagsTaken);
		flagIds.set(listSize, flagId);
		flagsX.set(listSize, flagX);
		flagsY.set(listSize, flagY);
		flagsZ.set(listSize, flagZ);
	}
	
	/**
	 * Used to calculate the event CTF area, so that players don't run off with the flag. Essential, since a player may take the flag just so other teams can't score points. This function is Only called upon ONE time on BEGINING OF EACH EVENT right after we spawn the flags.
	 */
	private static void calculateOutSideOfCTF()
	{
		if (ctfTeams == null || flagSpawns == null || teamsX == null || teamsY == null || teamsZ == null)
		{
			return;
		}
		final int division = ctfTeams.size() * 2;
		int pos = 0;
		final int[] locX = new int[division], locY = new int[division], locZ = new int[division];
		// Get all coordinates inorder to create a polygon:
		for (final L2Spawn flag : flagSpawns)
		{
			if (flag == null)
			{
				continue;
			}
			
			locX[pos] = flag.getLocx();
			locY[pos] = flag.getLocy();
			locZ[pos] = flag.getLocz();
			pos++;
			if (pos > division / 2)
			{
				break;
			}
		}
		for (int x = 0; x < ctfTeams.size(); x++)
		{
			locX[pos] = teamsX.get(x);
			locY[pos] = teamsY.get(x);
			locZ[pos] = teamsZ.get(x);
			pos++;
			if (pos > division)
			{
				break;
			}
		}
		// Find the polygon center, note that it's not the mathematical center of the polygon,
		// Rather than a point which centers all coordinates:
		int centerX = 0, centerY = 0, centerZ = 0;
		for (int x = 0; x < pos; x++)
		{
			centerX += (locX[x] / division);
			centerY += (locY[x] / division);
			centerZ += (locZ[x] / division);
		}
		// Now let's find the furthest distance from the "center" to the egg shaped sphere
		// Surrounding the polygon, size x1.5 (for maximum logical area to wander...):
		int maxX = 0, maxY = 0, maxZ = 0;
		for (int x = 0; x < pos; x++)
		{
			if (maxX < 2 * Math.abs(centerX - locX[x]))
			{
				maxX = (2 * Math.abs(centerX - locX[x]));
			}
			if (maxY < 2 * Math.abs(centerY - locY[x]))
			{
				maxY = (2 * Math.abs(centerY - locY[x]));
			}
			if (maxZ < 2 * Math.abs(centerZ - locZ[x]))
			{
				maxZ = (2 * Math.abs(centerZ - locZ[x]));
			}
		}
		
		// CenterX,centerY,centerZ are the coordinates of the "event center".
		// So let's save those coordinates to check on the players:
		eventCenterX = centerX;
		eventCenterY = centerY;
		eventCenterZ = centerZ;
		eventOffset = maxX;
		if (eventOffset < maxY)
		{
			eventOffset = maxY;
		}
		if (eventOffset < maxZ)
		{
			eventOffset = maxZ;
		}
	}
	
	/**
	 * Checks if is outside ctf area.
	 * @param  player the player
	 * @return        true, if is outside ctf area
	 */
	public static boolean isOutsideCTFArea(final L2PcInstance player)
	{
		if (player == null || !player.isOnline())
		{
			return true;
		}
		if (!(player.getX() > eventCenterX - eventOffset && player.getX() < eventCenterX + eventOffset && player.getY() > eventCenterY - eventOffset && player.getY() < eventCenterY + eventOffset && player.getZ() > eventCenterZ - eventOffset && player.getZ() < eventCenterZ + eventOffset))
		{
			return true;
		}
		return false;
	}
	
}