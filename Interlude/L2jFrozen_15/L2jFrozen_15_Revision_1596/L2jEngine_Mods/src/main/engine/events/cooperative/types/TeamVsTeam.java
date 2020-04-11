package main.engine.events.cooperative.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.cooperative.AbstractCooperative;
import main.enums.TeamType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;

/**
 * @author fissban
 */
public class TeamVsTeam extends AbstractCooperative
{
	/** Points of each player */
	private static Map<String, Integer> playerPoints = new ConcurrentHashMap<>();
	/** Points of blue team */
	private static volatile int pointsBlue = 0;
	/** Points of red team */
	private static volatile int pointsRed = 0;
	
	public TeamVsTeam()
	{
		super();
	}
	
	@Override
	public void onStart()
	{
		// Send message on screen with the points of each team
		sendPointsToAllParticipant();
		// init all players in event
		getPlayersInEvent().forEach(ph -> playerPoints.put(ph.getName(), 0));
	}
	
	@Override
	public void onEnd()
	{
		// Cancel Partys
		getPlayersInEvent().stream().filter(ph -> ph.getInstance().getParty() != null).forEach(ph -> ph.getInstance().getParty().removePartyMember(ph.getInstance()));
		
		// Generate the ranking
		HashMap<String, Integer> pointsOrdered = new LinkedHashMap<>();
		
		// Send html showing the points of each player
		// Order the list according to your scores
		int LIMIT = 10;
		playerPoints.entrySet().stream().sorted(Entry.<String, Integer> comparingByValue().reversed()).limit(LIMIT).forEach(e ->
		{
			pointsOrdered.put(e.getKey(), e.getValue());
		});
		
		// Generate the html of the ranking
		HtmlBuilder hb = Html.eventRanking(pointsOrdered);
		// Send the html to each character in the event
		sendHtml(null, hb, getPlayersInEvent());
		
		// Clear
		playerPoints.clear();
		pointsBlue = 0;
		pointsRed = 0;
	}
	
	@Override
	public void createTeams()
	{
		TeamType lastTeam = TeamType.BLUE;
		
		for (PlayerHolder ph : getPlayersInEvent())
		{
			// Save the character's location before being sent to the event.
			ph.setLastLoc(ph.getInstance().getX(), ph.getInstance().getY(), ph.getInstance().getZ());
			
			switch (lastTeam)
			{
				case RED:
					// Teleport
					ph.teleportTo(ConfigData.TVT_SPAWN_TEAM_BLUE, ConfigData.TVT_RADIUS_SPAWN);
					// Set team
					ph.setTeam(TeamType.BLUE);
					// Set last Team
					lastTeam = TeamType.BLUE;
					break;
				case BLUE:
					// Teleport
					ph.teleportTo(ConfigData.TVT_SPAWN_TEAM_RED, ConfigData.TVT_RADIUS_SPAWN);
					// Set team
					ph.setTeam(TeamType.RED);
					// Set last team
					lastTeam = TeamType.RED;
					break;
				default:
					// Never happens!
					break;
			}
		}
	}
	
	@Override
	public void giveRewards()
	{
		// Check the winning team.
		TeamType teamWinner = TeamType.NONE;
		
		// Tied teams
		if (pointsBlue == pointsRed)
		{
			//
		}
		else if (pointsBlue > pointsRed)
		{
			teamWinner = TeamType.BLUE;
		}
		else
		{
			teamWinner = TeamType.RED;
		}
		
		if (teamWinner == TeamType.NONE)
		{
			UtilMessage.sendAnnounceMsg("The event ends in a draw!", getPlayersInEvent());
		}
		else
		{
			// A message is sent to all participants informing the winning team.
			UtilMessage.sendAnnounceMsg("The team " + teamWinner.name() + " was the winner!", getPlayersInEvent());
			
			for (PlayerHolder ph : getPlayersInEvent())
			{
				// Prizes are awarded to the winning team.
				if (ph.getTeam() == teamWinner)
				{
					ConfigData.TVT_REWARDS.forEach(rh -> UtilInventory.giveItems(ph, rh.getRewardId(), rh.getRewardCount(), 0));
				}
			}
		}
	}
	
	@Override
	public void createParty()
	{
		// Leader Party Blue
		PlayerHolder lpb = null;
		// Leader Party Red
		PlayerHolder lpr = null;
		
		if (getPlayersInEvent().size() <= 3)
		{
			return;
		}
		
		for (PlayerHolder ph : getPlayersInEvent())
		{
			if (ph == null)
			{
				continue;
			}
			
			// Remove from old party
			if (ph.getInstance().getParty() != null)
			{
				ph.getInstance().getParty().removePartyMember(ph.getInstance());
			}
			
			PlayerHolder leader = null;
			
			switch (ph.getTeam())
			{
				case BLUE:
					leader = lpb;
					break;
				case RED:
					leader = lpr;
					break;
			}
			
			// If there is no leader yet, we assign one
			if (leader == null)
			{
				leader = ph;
				continue;
			}
			
			// If the party exceeds the limit of members we start the creation of a new one
			if ((leader != null) && (leader.getInstance().getParty() != null) && (leader.getInstance().getParty().getMemberCount() >= 7))
			{
				switch (ph.getTeam())
				{
					case BLUE:
						lpb = ph;
						continue;
					case RED:
						lpr = ph;
						continue;
				}
			}
			
			// Created the party
			if (leader.getInstance().getParty() == null)
			{
				L2Party party = new L2Party(leader.getInstance(), 0);
				leader.getInstance().setParty(party);
				party.addPartyMember(ph.getInstance());
			}
			// Added a new party member
			else
			{
				leader.getInstance().getParty().addPartyMember(ph.getInstance());
			}
		}
	}
	
	// LISTENERS -----------------------------------------------------------------------------------------
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (Util.areObjectType(L2PlayableInstance.class, killer, victim))
		{
			PlayerHolder killerPc = killer.getActingPlayer();
			PlayerHolder victimPc = victim.getActingPlayer();
			
			if (playerInEvent(killerPc, victimPc))
			{
				// Increase the points of each team
				if (killerPc.getTeam() == TeamType.BLUE)
				{
					pointsBlue++;
				}
				else
				{
					pointsRed++;
				}
				
				// Send all the characters of the event the amount of points.
				sendPointsToAllParticipant();
				// Increase the points of each player
				increasePlayerPoint(killerPc);
			}
		}
	}
	
	@Override
	public boolean canAttack(CharacterHolder attacker, CharacterHolder victim)
	{
		if (Util.areObjectType(L2PlayableInstance.class, attacker, victim))
		{
			// Check if the 2 players are participating in the event.
			if (playerInEvent(attacker, victim))
			{
				// Check the team of each character.
				if (attacker.getTeam() != victim.getTeam())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void onDeath(CharacterHolder player)
	{
		// Check if the character is inside the event
		if (!playerInEvent(player))
		{
			return;
		}
		
		ScheduledFuture<?> death = ThreadPool.schedule(() ->
		{
			deathTasks.remove(player.getObjectId());
			
			PlayerHolder pc = (PlayerHolder) player;
			// Revival
			revivePlayer(pc);
			// Heal
			healToMax(pc);
			// Buff
			giveBuff(pc);
			
		}, 10 * 1000);// 10 sec
		
		deathTasks.put(player.getObjectId(), death);
	}
	
	// XXX MISC ------------------------------------------------------------------------------------------------------------
	
	/**
	 * Revive a character and send it to the spawn point according to his team
	 * @param ph
	 */
	private static void revivePlayer(PlayerHolder ph)
	{
		if (!ph.getInstance().isDead())
		{
			return;
		}
		
		ph.getInstance().doRevive();
		
		switch (ph.getTeam())
		{
			case BLUE:
				ph.teleportTo(ConfigData.TVT_SPAWN_TEAM_BLUE, ConfigData.TVT_RADIUS_SPAWN);
				break;
			case RED:
				ph.teleportTo(ConfigData.TVT_SPAWN_TEAM_RED, ConfigData.TVT_RADIUS_SPAWN);
				break;
		}
	}
	
	/**
	 * Send all the characters a message on the screen showing the points of each team
	 */
	private static void sendPointsToAllParticipant()
	{
		UtilMessage.sendAnnounceMsg("BLUE " + pointsBlue + " | " + pointsRed + " RED", getPlayersInEvent());
	}
	
	/**
	 * Increase by 1 the number of points of a character
	 * @param ph
	 */
	private static void increasePlayerPoint(PlayerHolder ph)
	{
		if (!playerPoints.containsKey(ph.getName()))
		{
			playerPoints.put(ph.getName(), 0);
		}
		
		int points = playerPoints.get(ph.getName());
		playerPoints.put(ph.getName(), ++points);
	}
}
