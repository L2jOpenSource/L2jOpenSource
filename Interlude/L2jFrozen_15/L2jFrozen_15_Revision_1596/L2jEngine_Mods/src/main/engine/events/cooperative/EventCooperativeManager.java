package main.engine.events.cooperative;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.cooperative.types.AllVsAll;
import main.engine.events.cooperative.types.CaptureTheFlag;
import main.engine.events.cooperative.types.Survive;
import main.engine.events.cooperative.types.TeamVsTeam;
import main.holders.objects.PlayerHolder;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class EventCooperativeManager
{
	private static final int TIME_EVENT_DURATION = ConfigData.COOPERATIVE_EVENT_DURATION * 60;// 10 minutes default
	/** Time between each event */
	private static int TIME_BETWEEN_EACH_EVENT = ConfigData.COOPERATIVE_BETWEEN_EACH_EVENT * 60;// 20 minutes default
	/** Variable responsible for keeping track of time */
	private static int time = TIME_BETWEEN_EACH_EVENT;
	
	/** List with different events */
	private static final Map<String, AbstractCooperative> EVENTS = new HashMap<>();
	
	public EventCooperativeManager()
	{
		// All events start
		try
		{
			new TeamVsTeam();
			new Survive();
			new AllVsAll();
			new CaptureTheFlag();
			// new 1vs1();
			// new TeamVsTeamDeathMatch();
			
			startTimerToStart();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Register new events
	 * @param event
	 */
	public static void registerEvent(AbstractCooperative event)
	{
		EVENTS.put(event.getClass().getSimpleName(), event);
	}
	
	/**
	 * List of enabled events
	 * @return
	 */
	public static Collection<AbstractCooperative> getAllEvents()
	{
		return EVENTS.values();
	}
	
	/**
	 * The number of votes for an event is increased by one
	 * @param eventName
	 */
	public static void increaseVote(String eventName)
	{
		EVENTS.get(eventName).increaseVote();
	}
	
	/**
	 * It executes the main thread that will start the different enabled events.
	 */
	public static void startTimerToStart()
	{
		ThreadPool.scheduleAtFixedRate(() ->
		{
			time--;
			
			switch (time)
			{
				case 1800: // 30 min
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 30 minutes!");
					break;
				case 900: // 15 min
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 15 minutes!");
					break;
				case 600: // 10 min
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 10 minutes!");
					break;
				case 300: // 5 min
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 5 minutes!");
					break;
				case 60: // 1 min
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 1 minutes!");
					break;
				case 10: // 10 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 10 segundos!");
					break;
				case 5: // 5 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 5!");
					break;
				case 4: // 4 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 4!");
					break;
				case 3: // 3 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 3!");
					break;
				case 2: // 2 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 2!");
					break;
				case 1: // 1 seg
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event starts in 1!");
					break;
				case 0: // 0 seg
				case -1:
					UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The event will start in a moment!");
					
					// The time between events plus 1 min is expected by the times of each state of the events.
					time = TIME_BETWEEN_EACH_EVENT + TIME_EVENT_DURATION + 60;
					
					if (getRegisterPlayersCount() < ConfigData.COOPERATIVE_MIN_PLAYERS)
					{
						UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Event Canceled due to lack of participants!");
						UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Next event in " + (time / 60) + " minutes!");
						
						clear();
						break;
					}
					
					// It ensures that all registered characters are inside the game
					checkPlayerRegister();
					// The event with the most votes starts.
					EVENTS.get(getEventMoreVotes()).start(registerPlayers);
					
					clear();
					break;
			}
		}, 10 * 1000, 1000);
		
	}
	
	/**
	 * Clear values.
	 * <li>The vote for all events is initialized</li>
	 * <li>Player registration is initialized</li>
	 * <li>Initiate the vote of the characters in the events</li>
	 */
	private static void clear()
	{
		// The vote for all events is initialized.
		EVENTS.values().forEach(e -> e.initVotes());
		// Player registration is initialized
		registerPlayers.clear();
		// Initiate the vote of the characters in the events
		votePlayers.clear();
	}
	
	// XXX REGISTER --------------------------------------------------------------------------------------------------------------
	
	/** Players registers in events */
	private static volatile List<Integer> registerPlayers = new CopyOnWriteArrayList<>();
	
	/**
	 * You get the number of characters registered to the events
	 * @return
	 */
	public static int getRegisterPlayersCount()
	{
		checkPlayerRegister();
		
		return registerPlayers.size();
	}
	
	/**
	 * You get if you have reached the maximum limit of characters that can be registered to events.
	 * <li>Checked offline and removed from state before returning value</li>
	 * <li>To adjust the limit go to {@link ConfigData#COOPERATIVE_MAX_PLAYERS}</li>
	 * @return
	 */
	public static boolean isMaxPlayerRegisters()
	{
		checkPlayerRegister();
		
		return getRegisterPlayersCount() >= ConfigData.COOPERATIVE_MAX_PLAYERS;
	}
	
	/**
	 * A player is registered if he fulfills all the conditions
	 * <li>It is checked that all registered players are online.</li>
	 * <li>It is checked that they can register.</li>
	 * <li>It is checked that it is no longer registered.</li>
	 * @param  ph
	 * @return
	 */
	public static boolean register(PlayerHolder ph)
	{
		checkPlayerRegister();
		
		if (registerPlayers.contains(ph.getObjectId()))
		{
			return false;
		}
		
		if (isRegisterPlayerIp(ph, false))
		{
			return false;
		}
		
		registerPlayers.add(ph.getObjectId());
		
		return true;
	}
	
	public static boolean unRegister(PlayerHolder ph)
	{
		if (registerPlayers.contains(ph.getObjectId()))
		{
			registerPlayers.remove(Integer.valueOf(ph.getObjectId()));
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check if a player is already registered
	 * <li>Check that all registered players are online.</li>
	 * <li>Check if another character is already registered with the character's ip</li>
	 * @param  ph
	 * @return
	 */
	public static boolean isRegisterPlayer(PlayerHolder ph)
	{
		checkPlayerRegister();
		
		if (isRegisterPlayerIp(ph, false))
		{
			return true;
		}
		
		return registerPlayers.contains(ph.getObjectId());
	}
	
	/**
	 * Check if a character's ip is already registered to the event.
	 * <li>We check that all registered players are online.</li>
	 * <li>Check the player's ip with the others registered.</li>
	 * @param  ph
	 * @param  removeOffline -> remove offline players from {@link #registerPlayers}
	 * @return
	 */
	public static boolean isRegisterPlayerIp(PlayerHolder ph, boolean removeOffline)
	{
		if (removeOffline)
		{
			checkPlayerRegister();
		}
		
		if (!ConfigData.COOPERATIVE_CHECK_PLAYER_IP)
		{
			return false;
		}
		
		for (int objId : registerPlayers)
		{
			// Do not check the ip of the same player in case it was already registered
			if (ph.getObjectId() == objId)
			{
				continue;
			}
			
			L2PcInstance pc = L2World.getInstance().getPlayer(objId);
			
			if (pc.getClient().getConnection().getInetAddress().equals(ph.getInstance().getClient().getConnection().getInetAddress()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * It is checked that all registered players are online
	 */
	private static synchronized void checkPlayerRegister()
	{
		// Check the characters offline
		List<Integer> aux = registerPlayers.stream().filter(objId -> L2World.getInstance().getPlayer(objId) == null).collect(Collectors.toList());
		// Remove offline players from the registry
		aux.forEach(objId -> registerPlayers.remove(objId));
	}
	
	// XXX VOTE -------------------------------------------------------------------------------------------------------------------
	
	/** List that contains the votes of the characters */
	private static List<Integer> votePlayers = new CopyOnWriteArrayList<>();
	
	/**
	 * The character's vote is recorded to an event
	 * @param ph
	 */
	public static void vote(PlayerHolder ph)
	{
		votePlayers.add(ph.getObjectId());
	}
	
	/**
	 * Check if the character votes for an event.
	 * @param  ph
	 * @return
	 */
	public static boolean hasVote(PlayerHolder ph)
	{
		return votePlayers.contains(ph.getObjectId());
	}
	
	// XXX MISC -------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Checked if you can register or vote for an event
	 * @return
	 */
	public static boolean canRegisterOrVote()
	{
		return getCurrentEvent() == null;
	}
	
	/**
	 * You get the current running event, with no event running it will return "null
	 * @return
	 */
	public static AbstractCooperative getCurrentEvent()
	{
		return EVENTS.values().stream().filter(e -> e.isStarting()).findFirst().orElse(null);
	}
	
	/**
	 * The event with the most votes is obtained, by default the TvT
	 * @return
	 */
	private static String getEventMoreVotes()
	{
		String event = "TeamVsTeam";
		int topVotes = 0;
		
		for (AbstractCooperative e : EVENTS.values())
		{
			if (e.getVotes() > topVotes)
			{
				event = e.getClass().getSimpleName();
				topVotes = e.getVotes();
			}
		}
		
		return event;
	}
	
	/**
	 * Total votes are obtained at all events.
	 * @return
	 */
	public static int getTotalVotes()
	{
		int aux = 0;
		
		for (AbstractCooperative e : EVENTS.values())
		{
			aux += e.getVotes();
		}
		return aux;
	}
}
