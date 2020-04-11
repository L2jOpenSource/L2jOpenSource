package main.engine.events.cooperative.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.cooperative.AbstractCooperative;
import main.enums.TeamType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractCooperative
{
	/** FlagItem */
	private static final int FLAG_ITEM = 6718;
	/** Points of blue team */
	private static volatile int pointsBlue = 0;
	/** Points of red team */
	private static volatile int pointsRed = 0;
	/** Points of each player */
	private static Map<String, Integer> playerPoints = new ConcurrentHashMap<>();
	/** Flags and holders instances */
	private static Map<Integer, NpcHolder> flagAndHolders = new HashMap<>();
	
	public CaptureTheFlag()
	{
		super();
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
					ph.teleportTo(ConfigData.CTF_SPAWN_TEAM_BLUE, ConfigData.CTF_RADIUS_SPAWN);
					// Set team
					ph.setTeam(TeamType.BLUE);
					// Set last team
					lastTeam = TeamType.BLUE;
					break;
				case BLUE:
					// Teleport
					ph.teleportTo(ConfigData.CTF_SPAWN_TEAM_RED, ConfigData.CTF_RADIUS_SPAWN);
					// Set el team
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
			UtilMessage.sendAnnounceMsg("The team " + teamWinner.name() + " was the winner!", getPlayersInEvent());
			
			for (PlayerHolder ph : getPlayersInEvent())
			{
				// Prizes are awarded to the winning team.
				if (ph.getTeam() == teamWinner)
				{
					ConfigData.CTF_REWARDS.forEach(rh -> UtilInventory.giveItems(ph, rh.getRewardId(), rh.getRewardCount(), 0));
				}
			}
		}
	}
	
	// LISTENERS -----------------------------------------------------------------------------------------
	
	/**
	 * @param  ph
	 * @return
	 */
	@Override
	public boolean onExitWorld(PlayerHolder ph)
	{
		if (ph.hasFlag())
		{
			unequipFlag(ph, true);
		}
		
		return super.onExitWorld(ph);
	}
	
	@Override
	public void onDeath(CharacterHolder character)
	{
		// Check if the character is inside the event
		if (!playerInEvent(character))
		{
			return;
		}
		
		PlayerHolder ph = character.getActingPlayer();
		
		if (ph.hasFlag())
		{
			unequipFlag(ph, true);
		}
		
		ScheduledFuture<?> death = ThreadPool.schedule(() ->
		{
			deathTasks.remove(character.getObjectId());
			
			// Revive
			revivePlayer(ph);
			// Heal
			healToMax(ph);
			// Buff
			giveBuff(ph);
			
		}, 10 * 1000); // 10 sec
		
		deathTasks.put(character.getObjectId(), death);
	}
	
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
					pointsBlue += ConfigData.CTF_POINT_KILL_PLAYER;
				}
				else
				{
					pointsRed += ConfigData.CTF_POINT_KILL_PLAYER;
				}
				
				// Send all the characters of the event the amount of points.
				sendPointsToAllParticipant();
				// Increase the points of each player
				increasePlayerPoint(killerPc, ConfigData.CTF_POINT_KILL_PLAYER);
			}
		}
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (!Util.areObjectType(L2NpcInstance.class, character))
		{
			return false;
		}
		
		// Check if the player participates in the event.
		if (!playerInEvent(ph))
		{
			return false;
		}
		
		NpcHolder nh = (NpcHolder) character;
		
		if (nh.getId() == ConfigData.CTF_FLAG_ID)
		{
			if (ph.hasFlag())
			{
				return false;
			}
			if (ph.getTeam() != nh.getTeam())
			{
				equipFlag(ph, nh);
				return true;
			}
			return false;
		}
		
		if (nh.getId() == ConfigData.CTF_HOLDER_ID)
		{
			if (ph.hasFlag())
			{
				if (ph.getTeam() == nh.getTeam())
				{
					unequipFlag(ph, false);
					
					increasePlayerPoint(ph, ConfigData.CTF_POINT_DELIVER_FLAG);
					
					switch (ph.getTeam())
					{
						case BLUE:
							pointsBlue += ConfigData.CTF_POINT_DELIVER_FLAG;
							break;
						case RED:
							pointsRed += ConfigData.CTF_POINT_DELIVER_FLAG;
							break;
					}
					
					sendPointsToAllParticipant();
					return true;
				}
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void onUnequip(CharacterHolder player)
	{
		if (player instanceof PlayerHolder)
		{
			PlayerHolder ph = (PlayerHolder) player;
			L2ItemInstance flag = ph.getInstance().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND); // TODO RHAND?
			
			if ((flag == null) && ph.hasFlag())
			{
				// drop flag
				unequipFlag(ph, true);
			}
		}
	}
	
	// XXX MISC ------------------------------------------------------------------------------------------------------------
	
	@Override
	public void onStart()
	{
		// init all players in event
		getPlayersInEvent().forEach(ph -> playerPoints.put(ph.getName(), 0));
		// get xyz flag for red team
		int xRed = ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getX();
		int yRed = ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getY();
		int zRed = ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getZ();
		// get xyz flag for blue team
		int xBlue = ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getX();
		int yBlue = ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getY();
		int zBlue = ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getZ();
		
		// Flag spawn
		addSpawnNpc(ConfigData.CTF_FLAG_ID, xBlue, yBlue, zBlue, 0, 0, 0, TeamType.BLUE);
		addSpawnNpc(ConfigData.CTF_FLAG_ID, xRed, yRed, zRed, 0, 0, 0, TeamType.RED);
		// Holder spawn
		addSpawnNpc(ConfigData.CTF_HOLDER_ID, xBlue - 100, yBlue, zBlue, 0, 0, 0, TeamType.BLUE);// FIXME chequear el -100 si queda bien
		addSpawnNpc(ConfigData.CTF_HOLDER_ID, xRed - 100, yRed, zRed, 0, 0, 0, TeamType.RED);// FIXME chequear el -100 si queda bien
	}
	
	@Override
	public void onEnd()
	{
		// Cancel party
		getPlayersInEvent().stream().filter(ph -> ph.getInstance().getParty() != null).forEach(ph -> ph.getInstance().getParty().removePartyMember(ph.getInstance()));
		
		// Unspawn all flags and holders
		flagAndHolders.values().stream().filter(npc -> npc.getInstance() != null).forEach(npc -> npc.getInstance().deleteMe());
		
		// Generate ranking
		LinkedHashMap<String, Integer> pointsOrdered = new LinkedHashMap<>();
		
		// Send html showing the points of each player
		// order the list according to your scores
		int LIMIT = 10;
		playerPoints.entrySet().stream().sorted(Entry.<String, Integer> comparingByValue().reversed()).limit(LIMIT).forEach(e -> pointsOrdered.put(e.getKey(), e.getValue()));
		
		// Generate the html of the ranking
		HtmlBuilder hb = Html.eventRanking(pointsOrdered);
		// Send the html to each character in the event
		sendHtml(null, hb, getPlayersInEvent());
		
		// Clear
		flagAndHolders.clear();
		playerPoints.clear();
		pointsBlue = 0;
		pointsRed = 0;
	}
	
	/**
	 * Revive a character and send it to the spawn point according to his team
	 * @param ph
	 */
	private void revivePlayer(PlayerHolder ph)
	{
		if (!ph.getInstance().isDead())
		{
			return;
		}
		
		ph.getInstance().doRevive();
		
		switch (ph.getTeam())
		{
			case BLUE:
				ph.teleportTo(ConfigData.CTF_SPAWN_TEAM_BLUE, ConfigData.CTF_RADIUS_SPAWN);
				break;
			case RED:
				ph.teleportTo(ConfigData.CTF_SPAWN_TEAM_RED, ConfigData.CTF_RADIUS_SPAWN);
				break;
		}
	}
	
	/**
	 * Increase points of a character
	 * @param ph
	 */
	private static void increasePlayerPoint(PlayerHolder ph, int p)
	{
		if (!playerPoints.containsKey(ph.getName()))
		{
			playerPoints.put(ph.getName(), 0);
		}
		
		int points = playerPoints.get(ph.getName()) + p;
		playerPoints.put(ph.getName(), points);
	}
	
	/**
	 * Equip a character with a flag.
	 * @param ph
	 * @param flagTeam
	 */
	private static void equipFlag(PlayerHolder ph, NpcHolder npc)
	{
		TeamType teamType = ph.getTeam();
		
		// get flag
		NpcHolder flag = flagAndHolders.remove(npc.getObjectId());
		// remove flag from world
		flag.getInstance().deleteMe();
		// give flag
		UtilInventory.giveItems(ph, FLAG_ITEM, 1);
		// equip flag
		L2ItemInstance flagItem = ph.getInstance().getInventory().getItemByItemId(FLAG_ITEM);
		if (flagItem != null)
		{
			ph.getInstance().getInventory().equipItemAndRecord(flagItem);
		}
		// add in memory
		ph.setHasFlag(true);
		// send Message
		UtilMessage.sendAnnounceMsg("The " + teamType.name().toLowerCase() + " team has taken the flag!", getPlayersInEvent());
	}
	
	/**
	 * Remove the flag of a character.
	 * @param ph
	 */
	private void unequipFlag(PlayerHolder ph, boolean drop)
	{
		// create new spawn
		switch (ph.getTeam())
		{
			case BLUE:
				int xRed = drop ? ph.getInstance().getX() : ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getX();
				int yRed = drop ? ph.getInstance().getY() : ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getY();
				int zRed = drop ? ph.getInstance().getZ() : ConfigData.CTF_FLAG_SPAWN_TEAM_RED.getZ();
				addSpawnNpc(ConfigData.CTF_FLAG_ID, xRed, yRed, zRed, 0, 0, 0, TeamType.RED);
				break;
			case RED:
				int xBlue = drop ? ph.getInstance().getX() : ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getX();
				int yBlue = drop ? ph.getInstance().getY() : ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getY();
				int zBlue = drop ? ph.getInstance().getZ() : ConfigData.CTF_FLAG_SPAWN_TEAM_BLUE.getZ();
				addSpawnNpc(ConfigData.CTF_FLAG_ID, xBlue, yBlue, zBlue, 0, 0, 0, TeamType.BLUE);
				break;
		}
		
		// remove from memory
		ph.setHasFlag(false);
		
		// take flag
		UtilInventory.takeItems(ph, FLAG_ITEM, 1);
		
		if (drop)
		{
			UtilMessage.sendAnnounceMsg("The " + ph.getTeam().name().toLowerCase() + " has drop flag!", getPlayersInEvent());
		}
		else
		{
			UtilMessage.sendAnnounceMsg("The " + ph.getTeam().name().toLowerCase() + " team has delivered the flag!", getPlayersInEvent());
		}
	}
	
	@Override
	public NpcHolder addSpawnNpc(int npcId, int x, int y, int z, int heading, int randomOffset, long despawnDelay, TeamType teamType)
	{
		NpcHolder npc = super.addSpawnNpc(npcId, x, y, z, heading, randomOffset, despawnDelay, teamType);
		npc.getInstance().setTitle(teamType.name());
		// npc.getInstance().getSpawn().stopRespawn();
		npc.getInstance().broadcastPacket(new NpcInfo(npc.getInstance(), npc.getInstance()));
		flagAndHolders.put(npc.getObjectId(), npc);
		return npc;
	}
	
	/**
	 * Send all the characters a message on the screen showing the points of each team
	 */
	private static void sendPointsToAllParticipant()
	{
		UtilMessage.sendAnnounceMsg("BLUE " + pointsBlue + " | " + pointsRed + " RED", getPlayersInEvent());
	}
}
