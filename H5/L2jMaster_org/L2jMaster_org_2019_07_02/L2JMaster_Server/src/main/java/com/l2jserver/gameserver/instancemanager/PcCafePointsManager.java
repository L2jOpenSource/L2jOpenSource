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

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

public final class PcCafePointsManager
{
	public PcCafePointsManager()
	{
	}
	
	public void givePcCafePoint(final L2PcInstance player, final long exp)
	{
		if (!Config.PC_BANG_ENABLED)
		{
			return;
		}
		
		if (player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.PVP) || player.isInsideZone(ZoneId.SIEGE) || (player.isOnlineInt() == 0) || player.isJailed())
		{
			return;
		}
		
		if (player.getPcBangPoints() >= Config.PC_BANG_MAX_POINTS)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EXCEEDED_MAX_OF_PC_CAFE_POINTS);
			player.sendPacket(sm);
			return;
		}
		
		int points = (int) (exp * 0.0001 * Config.PC_BANG_POINT_RATE);
		
		if (Config.PC_BANG_RANDOM_POINT)
		{
			points = Rnd.get(points / 2, points);
		}
		
		if ((points == 0) && (exp > 0) && Config.PC_BANG_REWARD_LOW_EXP_KILLS && (Rnd.get(100) < Config.PC_BANG_LOW_EXP_KILLS_CHANCE))
		{
			points = 1; // minimum points
		}
		
		SystemMessage message = null;
		if (points > 0)
		{
			if (Config.PC_BANG_ENABLE_DOUBLE_POINTS && (Rnd.get(100) < Config.PC_BANG_DOUBLE_POINTS_CHANCE))
			{
				points *= 2;
				message = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
			}
			else
			{
				message = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT);
			}
			if ((player.getPcBangPoints() + points) > Config.PC_BANG_MAX_POINTS)
			{
				points = Config.PC_BANG_MAX_POINTS - player.getPcBangPoints();
			}
			message.addLong(points);
			player.sendPacket(message);
			player.setPcBangPoints(player.getPcBangPoints() + points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), points, 1));
		}
	}
	
	/**
	 * Gets the single instance of {@code PcCafePointsManager}.
	 * @return single instance of {@code PcCafePointsManager}
	 */
	public static final PcCafePointsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PcCafePointsManager _instance = new PcCafePointsManager();
	}
}