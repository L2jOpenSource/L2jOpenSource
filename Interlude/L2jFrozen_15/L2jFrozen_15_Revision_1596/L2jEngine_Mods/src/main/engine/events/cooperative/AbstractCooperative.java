package main.engine.events.cooperative;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.datatables.csv.DoorTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.skills.effects.EffectTemplate;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.concurrent.ThreadPool;
import main.data.memory.ObjectData;
import main.data.memory.WorldData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.enums.TeamType;
import main.holders.IntIntHolder;
import main.holders.WorldHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.ItemHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilMessage;
import main.util.UtilSpawn;

/**
 * @author fissban
 */
public abstract class AbstractCooperative extends AbstractMod
{
	public enum EventState
	{
		NONE,
		PREPARE_TO_TELEPORT,
		PREPARE_TO_FIGHT,
		START,
		END,
		TELEPORT_TO_BACK,
	}
	
	/** World id */
	private static final int WORLD_ID = 999;
	/** World */
	private static WorldHolder world = null;
	/** Task with the deaths of the players */
	protected static Map<Integer, Future<?>> deathTasks = new ConcurrentHashMap<>();
	/** List of characters within the event. */
	protected static List<Integer> playersInEvent = new CopyOnWriteArrayList<>();
	
	public AbstractCooperative()
	{
		// Recorded the event in our main engine.
		registerMod(false);
		// Recorded the event on the manager.
		EventCooperativeManager.registerEvent(this);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
			{
				break;
			}
			case END:
			{
				// Initialize the state of the event.
				eventState = EventState.NONE;
				// Initialize the list of characters in the event.
				playersInEvent.clear();
				// Cancel the start task
				start.cancel(false);
				start = null;
				// Initialize time
				time = 0;
				// Initialize the state of the event.
				eventState = EventState.NONE;
				// Initialize the afk listings.
				playersAfk.clear();
				playersAfkLocs.clear();
				
				break;
			}
			default:
				break;
			
		}
	}
	
	public NpcHolder addSpawnNpc(int npcId, int x, int y, int z, int heading, int randomOffset, long despawnDelay, TeamType teamType)
	{
		return UtilSpawn.npc(npcId, x, y, z, heading, randomOffset, despawnDelay, teamType, WORLD_ID);
	}
	
	public void addSpawnDoor(int id)
	{
		DoorTable.getInstance().getDoor(id).closeMe();
		// create door instance
		// UtilSpawn.door(id, true, WORLD_ID);
	}
	
	// XXX THREAD START EVENT ------------------------------------------------------------------------------------------
	
	protected static Future<?> start = null;
	private int time = 0;
	
	/**
	 * Launch the events and their times.
	 */
	public void start(List<Integer> list)
	{
		playersInEvent.addAll(list);
		
		start = ThreadPool.scheduleAtFixedRate(() ->
		{
			try
			{
				switch (eventState)
				{
					case NONE:
					{
						setEventState(EventState.PREPARE_TO_TELEPORT);
						break;
					}
					case PREPARE_TO_TELEPORT:
					{
						world = WorldData.create(WORLD_ID, true);
						// Create doors
						addSpawnDoor(24190001);
						addSpawnDoor(24190002);
						addSpawnDoor(24190003);
						addSpawnDoor(24190004);
						
						// -------------------------------------------------------------------
						// Prepare the characters to be sent to the event and fight!
						// -------------------------------------------------------------------
						getPlayersInEvent().forEach(ph ->
						{
							removeAllEffectsAndRevive(ph);
							// Stop any action in progress.
							stopAnyAction(ph);
							// Leave the party.
							if (ph.getInstance().isInParty())
							{
								ph.getInstance().getParty().removePartyMember(ph.getInstance(), false);
							}
							// Removed the summon from the player
							if (ph.getInstance().getPet() != null)
							{
								ph.getInstance().getPet().unSummon(ph.getInstance());
							}
							// Remove the player from the Olympiad.
							if (Olympiad.getInstance().isRegistered(ph.getInstance()) || (ph.getInstance().getOlympiadGameId() != -1))
							{
								Olympiad.getInstance().removeDisconnectedCompetitor(ph.getInstance());
							}
							// Update abnormal effect
							ph.getInstance().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_ROOT); // FIXME es necesario?
							ph.getInstance().startRooted();
							ph.getInstance().setIsParalyzed(true);
							ph.getInstance().setIsInvul(true);
							ph.getInstance().updateAbnormalEffect();
							
							giveBuff(ph);
						});
						
						// The characters are sent to a custom world
						getPlayersInEvent().forEach(ph -> world.add(ph));
						
						// Create teams.
						createTeams();
						// Create partys.
						createParty();
						
						// Define the next state
						setEventState(EventState.PREPARE_TO_FIGHT);
						break;
					}
					case PREPARE_TO_FIGHT:
					{
						time++;
						
						// Canceled the paralysis
						if (time >= 5)// HARDCODE 5 seconds
						{
							// Update abnormal effect
							getPlayersInEvent().forEach(ph ->
							{
								ph.getInstance().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_ROOT);// FIXME es necesario?
								ph.getInstance().stopRooting(null);
								ph.getInstance().setIsParalyzed(false);
								ph.getInstance().setIsInvul(false);
								ph.getInstance().updateAbnormalEffect();
							});
							// Initialize time
							time = 0;
							// Define the next state
							setEventState(EventState.START);
						}
						
						break;
					}
					case START:
					{
						if (time == 0)
						{
							// Authorized the listeners to this event
							super.startMod();
							
							onStart();
						}
						
						time++;
						
						announceFinishEvent((ConfigData.COOPERATIVE_EVENT_DURATION * 60) - time);
						
						// How long the event will last in minutes
						if (time >= (ConfigData.COOPERATIVE_EVENT_DURATION * 60))
						{
							setEventState(EventState.END);
							break;
						}
						
						// Check the afk
						if (ConfigData.COOPERATIVE_AFK_CHECK)
						{
							getPlayersInEvent().forEach(ph ->
							{
								if (checkIsAfk(ph))
								{
									// Remove them from the event and send it to their last location before the event.
									// ALL create some sort of penalty? So that you do not enter future events?
									removePlayerFromEvent(ph);
								}
							});
						}
						
						break;
					}
					case END:
					{
						giveRewards();
						
						removeDeathTasks();
						
						onEnd();
						
						getPlayersInEvent().forEach(ph ->
						{
							// Stop any action in progress.
							stopAnyAction(ph);
							// Remove all buffs
							removeAllEffectsAndRevive(ph);
							// Remove the team effect
						});
						
						setEventState(EventState.TELEPORT_TO_BACK);
						break;
					}
					case TELEPORT_TO_BACK:
					{
						getPlayersInEvent().forEach(ph -> removePlayerFromEvent(ph));
						
						playersInEvent.clear();
						// Cancel the authorization of the listeners to this event
						super.endMod();
						break;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}, 1000, 1000);
	}
	
	private void announceFinishEvent(int time)
	{
		switch (time)
		{
			case 300: // 5 min
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 5 minutes!");
				break;
			case 60: // 1 min
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 1 minutes!");
				break;
			case 10: // 10 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 10 segundos!");
				break;
			case 5: // 5 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 5!");
				break;
			case 4: // 4 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 4!");
				break;
			case 3: // 3 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 3!");
				break;
			case 2: // 2 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 2!");
				break;
			case 1: // 1 seg
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event end in 1!");
		}
	}
	
	public void createTeams()
	{
		//
	}
	
	/**
	 * Deliver event buffs to the characters
	 * @param ph
	 */
	public void giveBuff(PlayerHolder ph)
	{
		for (IntIntHolder bsh : ph.getInstance().isMageClass() ? ConfigData.COOPERATIVE_BUFF_MAGE : ConfigData.COOPERATIVE_BUFF_WARRIOR)
		{
			L2Skill skill = bsh.getSkill();
			if (skill != null)
			{
				skill.getEffects(ph.getInstance(), ph.getInstance());
			}
		}
	}
	
	public void giveRewards()
	{
		//
	}
	
	public void createParty()
	{
		//
	}
	
	public abstract void onStart();
	
	public abstract void onEnd();
	
	// XXX ANTI AFK -----------------------------------------------------------------------------------------------------
	/** Save the amount of seconds q does not move a player */
	private static Map<Integer, Integer> playersAfk = new ConcurrentHashMap<>();
	/** Saved the last location where the player was stopped */
	private static Map<Integer, Point3D> playersAfkLocs = new ConcurrentHashMap<>();
	
	/**
	 * Check if a player is AFK more than {@link ConfigData#COOPERATIVE_AFK_SECONDS}
	 * @param  ph
	 * @return
	 */
	protected static boolean checkIsAfk(PlayerHolder ph)
	{
		if (!playersAfk.containsKey(ph.getObjectId()))
		{
			// Initialize the data
			initAfk(ph);
		}
		
		int seconds = playersAfk.get(ph.getObjectId());
		
		// Those with -1 are because they were previously found as afk
		if (seconds == -1)
		{
			return true;
		}
		// Check how many seconds you have not moved
		if (seconds > ConfigData.COOPERATIVE_AFK_SECONDS)
		{
			playersAfk.put(ph.getObjectId(), -1);
			return true;
		}
		
		// Now we check if it moved
		if (playersAfkLocs.get(ph.getObjectId()).equals(ph.getInstance().getPosition()))
		{
			playersAfk.put(ph.getObjectId(), ++seconds);
		}
		else
		{
			// Initialize the data
			initAfk(ph);
		}
		
		return false;
	}
	
	private static void initAfk(PlayerHolder ph)
	{
		playersAfk.put(ph.getObjectId(), 1);
		playersAfkLocs.put(ph.getObjectId(), ph.getInstance().getPosition().getWorldPosition());
	}
	
	// XXX STATE ----------------------------------------------------------------------------------------------------
	// Event Status
	private static EventState eventState = EventState.NONE;
	
	/**
	 * A new event status is set.
	 * @param state
	 */
	public static void setEventState(EventState state)
	{
		eventState = state;
	}
	
	/**
	 * Event Status
	 * @return
	 */
	public EventState getEventState()
	{
		return eventState;
	}
	
	// XXX VOTES ------------------------------------------------------------------------------------------------------
	private volatile int votes = 0;
	
	/**
	 * Increase the number of votes for the event by 1.
	 */
	public void increaseVote()
	{
		votes++;
	}
	
	/**
	 * The number of votes for the event is reset.
	 */
	public void initVotes()
	{
		votes = 0;
	}
	
	/**
	 * Get the number of votes from the event.
	 * @return
	 */
	public int getVotes()
	{
		return votes;
	}
	
	// XXX MISC ------------------------------------------------------------------------------------------------------
	
	/**
	 * Rustic method to get a list of all the characters within the event. <br>
	 * -> We walk the list playersInEvent and will be removed: <br>
	 * -> The characters are not inside the game <br>
	 * -> Those who are not participating. (Missing) <br>
	 * @return
	 */
	protected synchronized static List<PlayerHolder> getPlayersInEvent()
	{
		List<PlayerHolder> players = new ArrayList<>();
		
		Iterator<Integer> it = playersInEvent.iterator();
		
		while (it.hasNext())
		{
			int objId = it.next();
			// Characters that are not in the game are removed.
			PlayerHolder ph = ObjectData.get(PlayerHolder.class, objId);
			
			if (ph.getInstance() == null)
			{
				// prevent memory leak
				ph.setTeam(TeamType.NONE);
				// remove from custom world
				world.remove(ph);
				// sql query from new xyz
				removeOfflinePlayer(objId);
				// remove from list
				it.remove();
			}
			else
			{
				players.add(ph);
			}
		}
		
		return players;
	}
	
	/**
	 * -> Remove the character from the list playersInEvent <br>
	 * -> Indicate in the memory of our engine that the character no longer participates in any event <br>
	 * -> Update in the DB the coordinates of the character with the last ones before entering the event.
	 * @param objId
	 */
	public static void removeOfflinePlayer(int objId)
	{
		// Remove the character from the playersInEvent list
		playersInEvent.remove(Integer.valueOf(objId));
		// Update in the db the coordinates of the character and we send it to its last location before the event.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE characters SET x=?,y=?,z=? WHERE obj_id=?");)
		{
			ps.setInt(1, ObjectData.get(PlayerHolder.class, objId).getLastLoc().getX());
			ps.setInt(2, ObjectData.get(PlayerHolder.class, objId).getLastLoc().getY());
			ps.setInt(3, ObjectData.get(PlayerHolder.class, objId).getLastLoc().getZ());
			ps.setInt(4, objId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected static void removePlayerFromEvent(PlayerHolder ph)
	{
		try
		{
			ph.setTeam(TeamType.NONE);
			// Remove the character from the playersInEvent list
			playersInEvent.remove(Integer.valueOf(ph.getObjectId()));
			// Teleport the character to his last location before the event
			ph.getInstance().teleToLocation(ph.getLastLoc(), false);
			world.remove(ph);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Check if one or more characters are inside the event.
	 * @param  player
	 * @return
	 */
	public boolean playerInEvent(CharacterHolder... players)
	{
		for (CharacterHolder p : players)
		{
			if (!playersInEvent.contains(p.getActingPlayer().getObjectId()))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * -> Stop all effects of the character<br>
	 * -> Stop all character cubics<br>
	 * -> Revive the Character<br>
	 * -> Heal the character in full<br>
	 * @param ph
	 */
	protected void removeAllEffectsAndRevive(PlayerHolder ph)
	{
		// Stop all effects
		ph.getInstance().stopAllEffects();
		// Stop all cubics
		ph.getInstance().unsummonAllCubics();
		// Revive
		if (ph.getInstance().isDead())
		{
			ph.getInstance().doRevive();
		}
		// Heal
		healToMax(ph);
	}
	
	/**
	 * Heal to the maximum the HP, MP and CP of a character.
	 * @param ph
	 */
	protected static void healToMax(PlayerHolder ph)
	{
		ph.getInstance().setCurrentHpMp(ph.getInstance().getStat().getMaxHp(), ph.getInstance().getStat().getMaxMp());
		ph.getInstance().setCurrentCp(ph.getInstance().getStat().getMaxCp());
	}
	
	/**
	 * -> Stop any attack in progress.<br>
	 * -> Stop any cast in progress.<br>
	 * -> Cancel the targets of the character.<br>
	 * -> Stop any movement in progress.<br>
	 * @param ph
	 */
	protected void stopAnyAction(PlayerHolder ph)
	{
		// Stop any attack in progress
		ph.getInstance().abortAttack();
		// Stop any cast in progress
		ph.getInstance().abortCast();
		// Cancel the targets of the character
		ph.getInstance().setTarget(null);
		// Stop any movement in progress
		ph.getInstance().stopMove(null);
	}
	
	/**
	 * Get the name of the event.
	 * @return
	 */
	public String getName()
	{
		return getClass().getSimpleName();
	}
	
	/**
	 * Get the time of the event
	 */
	public int getTimeEvent()
	{
		return time;
	}
	
	// XXX LISTENERS --------------------------------------------------------------------------------------------------
	
	@Override
	public boolean onUseItem(PlayerHolder ph, ItemHolder item)
	{
		return true;
	}
	
	@Override
	public boolean onUseSkill(PlayerHolder ph, L2Skill skill)
	{
		if (skill.hasEffects())
		{
			for (EffectTemplate e : skill.getEffectTemplates())
			{
				// TODO chequear esto q en frozen no se como se maneja.
				if (e.function.getName().equals("Resurrect"))
				{
					UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[System]", "You can not use this skill here");
					return false;
				}
			}
		}
		switch (skill.getSkillType())
		{
			case RECALL:
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[System]", "You can not use this skill here");
				return false;
		}
		
		return true;
	}
	
	@Override
	public boolean onExitWorld(PlayerHolder ph)
	{
		if (deathTasks.containsKey(ph.getObjectId()))
		{
			Future<?> task = deathTasks.get(ph.getObjectId());
			if ((task != null) && !task.isDone())
			{
				task.cancel(true);
				deathTasks.remove(ph.getObjectId());
			}
		}
		
		if (ph.getInstance() != null)
		{
			removePlayerFromEvent(ph);
		}
		else
		{
			// prevent memory leak
			ph.setTeam(TeamType.NONE);
			// remove from custom world
			world.remove(ph);
			//
			removeOfflinePlayer(ph.getObjectId());
		}
		
		return false;
	}
	
	// XX death Task
	private static void removeDeathTasks()
	{
		// Canceled all the "revive" tasks.
		for (Future<?> task : deathTasks.values())
		{
			if ((task != null) && !task.isDone())
			{
				task.cancel(true);
				task = null;
			}
		}
		
		deathTasks.clear();
	}
}
