/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.instancemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Duel;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class DuelManager
{
	private final Map<Integer, Duel> _duels = new ConcurrentHashMap<>();
	private final AtomicInteger _currentDuelId = new AtomicInteger();
	
	DuelManager()
	{
		// Hide constructor
	}
	
	public Duel getDuel(int duelId)
	{
		return _duels.get(duelId);
	}
	
	public void addDuel(L2PcInstance playerA, L2PcInstance playerB, boolean partyDuel)
	{
		if ((playerA == null) || (playerB == null))
		{
			return;
		}
		
		final int duelId = _currentDuelId.incrementAndGet();
		_duels.put(duelId, new Duel(playerA, playerB, partyDuel, duelId));
	}
	
	public void removeDuel(Duel duel)
	{
		_duels.remove(duel.getId());
	}
	
	public void doSurrender(L2PcInstance player)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		duel.doSurrender(player);
	}
	
	/**
	 * Updates player states.
	 * @param player - the dying player
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onPlayerDefeat(player);
		}
	}
	
	/**
	 * Broadcasts a packet to the team opposing the given player.
	 * @param player
	 * @param packet
	 */
	public void broadcastToOppositTeam(L2PcInstance player, L2GameServerPacket packet)
	{
		if ((player == null) || !player.isInDuel())
		{
			return;
		}
		final Duel duel = getDuel(player.getDuelId());
		
		if (duel == null)
		{
			return;
		}
		if (duel.getTeamA().contains(player))
		{
			duel.broadcastToTeam2(packet);
		}
		else
		{
			duel.broadcastToTeam1(packet);
		}
	}
	
	/**
	 * Checks if this player might join / start a duel.<br>
	 * @param player
	 * @param target
	 * @param partyDuel
	 * @return true if the player might join/start a duel.
	 */
	public static boolean canDuel(L2PcInstance player, L2PcInstance target, boolean partyDuel)
	{
		SystemMessageId reason = null;
		if (target.isInCombat() || target.isJailed())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
		}
		else if (target.isTransformed())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_WHILE_POLYMORPHED;
		}
		else if (target.isDead() || target.isDead() || ((target.getCurrentHp() < (target.getMaxHp() / 2)) || (target.getCurrentMp() < (target.getMaxMp() / 2))))
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
		}
		else if (target.isInDuel())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
		}
		else if (target.isInOlympiadMode())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
		}
		else if (target.isCursedWeaponEquipped() || (target.getKarma() > 0) || (target.getPvpFlag() > 0))
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
		}
		else if (target.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
		}
		else if (target.isMounted() || target.isInBoat())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER;
		}
		else if (target.isFishing())
		{
			reason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
		}
		else if ((!partyDuel && (target.isInsideZone(ZoneId.PEACE) || target.isInsideZone(ZoneId.WATER))) || target.isInsideZone(ZoneId.PVP) || target.isInsideZone(ZoneId.SIEGE))
		{
			reason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
		}
		
		if (reason != null)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(reason);
			msg.addString(target.getName());
			player.sendPacket(msg);
			return false;
		}
		
		return true;
	}
	
	public static final DuelManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		static final DuelManager _instance = new DuelManager();
	}
}