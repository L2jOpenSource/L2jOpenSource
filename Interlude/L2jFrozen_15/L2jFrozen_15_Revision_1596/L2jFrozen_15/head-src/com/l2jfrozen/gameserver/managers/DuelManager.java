package com.l2jfrozen.gameserver.managers;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Duel;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;

public class DuelManager
{
	private static DuelManager instance;
	private final List<Duel> duels;
	private int currentDuelId = 0x90;
	
	private DuelManager()
	{
		duels = new ArrayList<>();
	}
	
	public static final DuelManager getInstance()
	{
		if (instance == null)
		{
			instance = new DuelManager();
		}
		return instance;
	}
	
	private int getNextDuelId()
	{
		currentDuelId++;
		// In case someone wants to run the server forever :)
		if (currentDuelId >= 2147483640)
		{
			currentDuelId = 1;
		}
		
		return currentDuelId;
	}
	
	public Duel getDuel(final int duelId)
	{
		for (Duel duel : duels)
		{
			if (duel.getId() == duelId)
			{
				return duel;
			}
		}
		
		return null;
	}
	
	public void addDuel(final L2PcInstance playerA, final L2PcInstance playerB, final int partyDuel)
	{
		if (playerA == null || playerB == null)
		{
			return;
		}
		
		// return if a player has PvPFlag
		String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
		if (partyDuel == 1)
		{
			boolean playerInPvP = false;
			for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				if (temp.getPvpFlag() != 0)
				{
					playerInPvP = true;
					break;
				}
			}
			if (!playerInPvP)
			{
				for (final L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					if (temp.getPvpFlag() != 0)
					{
						playerInPvP = true;
						break;
					}
				}
			}
			// A player has PvP flag
			if (playerInPvP)
			{
				for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				for (final L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				return;
			}
		}
		else
		{
			if (playerA.getPvpFlag() != 0 || playerB.getPvpFlag() != 0)
			{
				playerA.sendMessage(engagedInPvP);
				playerB.sendMessage(engagedInPvP);
				return;
			}
		}
		
		engagedInPvP = null;
		
		Duel duel = new Duel(playerA, playerB, partyDuel, getNextDuelId());
		duels.add(duel);
		
		duel = null;
	}
	
	public void removeDuel(final Duel duel)
	{
		duels.remove(duel);
	}
	
	public void doSurrender(final L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		duel.doSurrender(player);
		duel = null;
	}
	
	/**
	 * Updates player states.
	 * @param player - the dieing player
	 */
	public void onPlayerDefeat(final L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onPlayerDefeat(player);
		}
		duel = null;
	}
	
	/**
	 * Registers a debuff which will be removed if the duel ends
	 * @param player
	 * @param buff
	 */
	public void onBuff(final L2PcInstance player, final L2Effect buff)
	{
		if (player == null || !player.isInDuel() || buff == null)
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onBuff(player, buff);
		}
		duel = null;
	}
	
	/**
	 * Removes player from duel.
	 * @param player - the removed player
	 */
	public void onRemoveFromParty(final L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onRemoveFromParty(player);
		}
		duel = null;
	}
	
	/**
	 * Broadcasts a packet to the team opposing the given player.
	 * @param player
	 * @param packet
	 */
	public void broadcastToOppositTeam(final L2PcInstance player, final L2GameServerPacket packet)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		
		if (duel == null)
		{
			return;
		}
		if (duel.getPlayerA() == null || duel.getPlayerB() == null)
		{
			return;
		}
		
		if (duel.getPlayerA() == player)
		{
			duel.broadcastToTeam2(packet);
		}
		else if (duel.getPlayerB() == player)
		{
			duel.broadcastToTeam1(packet);
		}
		else if (duel.isPartyDuel())
		{
			if (duel.getPlayerA().getParty() != null && duel.getPlayerA().getParty().getPartyMembers().contains(player))
			{
				duel.broadcastToTeam2(packet);
			}
			else if (duel.getPlayerB().getParty() != null && duel.getPlayerB().getParty().getPartyMembers().contains(player))
			{
				duel.broadcastToTeam1(packet);
			}
		}
		duel = null;
	}
}
