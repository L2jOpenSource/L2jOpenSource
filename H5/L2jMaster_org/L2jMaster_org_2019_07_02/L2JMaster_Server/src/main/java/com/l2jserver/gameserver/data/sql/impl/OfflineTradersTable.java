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
package com.l2jserver.gameserver.data.sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.LoginServerThread;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.model.L2ManufactureItem;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.L2GameClient.GameClientState;

import SellbuffOffline.SellbuffOfflineSQL;

public class OfflineTradersTable
{
	private static Logger LOGGER = Logger.getLogger(OfflineTradersTable.class.getName());
	
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`) VALUES (?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	
	public void storeOffliners()
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement stm1 = con.prepareStatement(CLEAR_OFFLINE_TABLE);
			PreparedStatement stm2 = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS);
			PreparedStatement stm3 = con.prepareStatement(SAVE_OFFLINE_STATUS);
			PreparedStatement stm_items = con.prepareStatement(SAVE_ITEMS))
		{
			stm1.execute();
			stm2.execute();
			con.setAutoCommit(false); // avoid halfway done
			
			for (L2PcInstance pc : L2World.getInstance().getPlayers())
			{
				try
				{
					if ((pc.getPrivateStoreType() != PrivateStoreType.NONE) && pc.isInOfflineMode())
					{
						stm3.setInt(1, pc.getObjectId()); // Char Id
						stm3.setLong(2, pc.getOfflineStartTime());
						stm3.setInt(3, pc.getPrivateStoreType().getId()); // store type
						String title = null;
						
						switch (pc.getPrivateStoreType())
						{
							case SELL_BUFF:
								SellbuffOfflineSQL.getInstance().storeSellbuffPlayersShutDown(pc.getCharId(), pc.getAppearance().getNameColor(), pc.getTitle(), pc.getAppearance().getTitleColor(), pc.getBuffPrize());
								break;
							case BUY:
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getBuyList().getTitle();
								for (TradeItem i : pc.getBuyList().getItems())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getItem().getId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case SELL:
							case PACKAGE_SELL:
								if (!Config.OFFLINE_TRADE_ENABLE)
								{
									continue;
								}
								title = pc.getSellList().getTitle();
								for (TradeItem i : pc.getSellList().getItems())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getObjectId());
									stm_items.setLong(3, i.getCount());
									stm_items.setLong(4, i.getPrice());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
								break;
							case MANUFACTURE:
								if (!Config.OFFLINE_CRAFT_ENABLE)
								{
									continue;
								}
								title = pc.getStoreName();
								for (L2ManufactureItem i : pc.getManufactureItems().values())
								{
									stm_items.setInt(1, pc.getObjectId());
									stm_items.setInt(2, i.getRecipeId());
									stm_items.setLong(3, 0);
									stm_items.setLong(4, i.getCost());
									stm_items.executeUpdate();
									stm_items.clearParameters();
								}
						}
						stm3.setString(4, title);
						stm3.executeUpdate();
						stm3.clearParameters();
						con.commit(); // flush
					}
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + " " + e, e);
				}
			}
			LOGGER.info(getClass().getSimpleName() + ": Offline traders stored.");
			LOGGER.info(getClass().getSimpleName() + ": SellBuff traders stored.");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while saving offline traders: " + e, e);
		}
	}
	
	public void restoreOfflineTraders()
	{
		LOGGER.info(getClass().getSimpleName() + ": Loading offline traders...");
		int nTraders = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement stm = con.createStatement();
			ResultSet rs = stm.executeQuery(LOAD_OFFLINE_STATUS))
		{
			while (rs.next())
			{
				long time = rs.getLong("time");
				if (Config.OFFLINE_MAX_DAYS > 0)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(time);
					cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS);
					if (cal.getTimeInMillis() <= System.currentTimeMillis())
					{
						continue;
					}
				}
				
				PrivateStoreType type = PrivateStoreType.findById(rs.getInt("type"));
				if (type == null)
				{
					LOGGER.warning(getClass().getSimpleName() + ": PrivateStoreType with id " + rs.getInt("type") + " could not be found.");
					continue;
				}
				
				if (type == PrivateStoreType.NONE)
				{
					continue;
				}
				
				L2PcInstance player = null;
				
				try
				{
					L2GameClient client = new L2GameClient(null);
					client.setDetached(true);
					player = L2PcInstance.load(rs.getInt("charId"));
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					client.setAccountName(player.getAccountNamePlayer());
					L2World.getInstance().addPlayerToWorld(player);
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.setOfflineStartTime(time);
					player.spawnMe(player.getX(), player.getY(), player.getZ());
					LoginServerThread.getInstance().addGameServerLogin(player.getAccountName(), client);
					try (PreparedStatement stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS))
					{
						stm_items.setInt(1, player.getObjectId());
						try (ResultSet items = stm_items.executeQuery())
						{
							switch (type)
							{
								case SELL_BUFF:
									SellbuffOfflineSQL.getInstance().restoreSellbuffPlayerShutDown(player);
									break;
								case BUY:
									while (items.next())
									{
										if (player.getBuyList().addItemByItemId(items.getInt(2), items.getLong(3), items.getLong(4)) == null)
										{
											throw new NullPointerException();
										}
									}
									player.getBuyList().setTitle(rs.getString("title"));
									break;
								case SELL:
								case PACKAGE_SELL:
									while (items.next())
									{
										if (player.getSellList().addItem(items.getInt(2), items.getLong(3), items.getLong(4)) == null)
										{
											throw new NullPointerException();
										}
									}
									player.getSellList().setTitle(rs.getString("title"));
									player.getSellList().setPackaged(type == PrivateStoreType.PACKAGE_SELL);
									break;
								case MANUFACTURE:
									while (items.next())
									{
										player.getManufactureItems().put(items.getInt(2), new L2ManufactureItem(items.getInt(2), items.getLong(4)));
									}
									player.setStoreName(rs.getString("title"));
									break;
							}
						}
					}
					if (type == PrivateStoreType.SELL_BUFF)
					{
						player.setSellBuff(true);
					}
					player.sitDown();
					if (Config.OFFLINE_SET_NAME_COLOR && (type != PrivateStoreType.SELL_BUFF))
					{
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					
					player.setPrivateStoreType(type);
					player.setOnlineStatus(true, true);
					player.restoreEffects();
					player.broadcastUserInfo();
					nTraders++;
				}
				catch (Exception e)
				{
					LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error loading trader: " + player, e);
					if (player != null)
					{
						player.deleteMe();
					}
				}
			}
			
			LOGGER.info(getClass().getSimpleName() + ": Loaded: " + nTraders + " offline trader(s)");
			
			try (Statement stm1 = con.createStatement())
			{
				stm1.execute(CLEAR_OFFLINE_TABLE);
				stm1.execute(CLEAR_OFFLINE_TABLE_ITEMS);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Error while loading offline traders: ", e);
		}
	}
	
	/**
	 * Gets the single instance of OfflineTradersTable.
	 * @return single instance of OfflineTradersTable
	 */
	public static OfflineTradersTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OfflineTradersTable _instance = new OfflineTradersTable();
	}
}
