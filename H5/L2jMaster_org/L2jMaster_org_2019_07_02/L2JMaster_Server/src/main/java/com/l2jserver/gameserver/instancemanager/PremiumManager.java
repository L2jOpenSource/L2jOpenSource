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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class PremiumManager
{
	private long endDate = 0;
	
	private static final String GETPREM = "SELECT premium_service,enddate FROM account_premium WHERE account_name=?";
	private static final String UPDATEPREM = "UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?";
	private static final String REMOVEPREM = "INSERT INTO account_premium (account_name,premium_service,enddate) values(?,?,?) ON DUPLICATE KEY UPDATE premium_service = ?, enddate = ?";
	
	public long getPremiumEndDate(String accountName)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GETPREM))
		{
			statement.setString(1, accountName);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (Config.PREMIUM_SYSTEM_ENABLED)
				{
					endDate = rset.getLong("enddate");
					if (endDate <= System.currentTimeMillis())
					{
						endDate = 0;
						removePremiumStatus(accountName);
					}
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
		}
		
		return endDate;
	}
	
	public void updatePremiumData(int months, String accountName)
	{
		long remainingTime = getPremiumEndDate(accountName);
		if (remainingTime > 0)
		{
			remainingTime -= System.currentTimeMillis();
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATEPREM))
		
		{
			Calendar endDate = Calendar.getInstance();
			endDate.setTimeInMillis(System.currentTimeMillis() + remainingTime);
			endDate.set(Calendar.SECOND, 0);
			endDate.add(Calendar.MONTH, months);
			
			statement.setInt(1, 1);
			statement.setLong(2, endDate.getTimeInMillis());
			statement.setString(3, accountName);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.getAccountNamePlayer().equalsIgnoreCase(accountName))
			{
				player.setPremiumStatus(getPremiumEndDate(accountName) > 0 ? true : false);
			}
		}
	}
	
	public void removePremiumStatus(String accountName)
	{
		// TODO: Add check if account exists. XD
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REMOVEPREM))
		{
			statement.setString(1, accountName);
			statement.setInt(2, 0);
			statement.setLong(3, 0);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.getAccountNamePlayer().equalsIgnoreCase(accountName))
			{
				player.setPremiumStatus(false);
			}
		}
	}
	
	public static final PremiumManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PremiumManager _instance = new PremiumManager();
	}
}