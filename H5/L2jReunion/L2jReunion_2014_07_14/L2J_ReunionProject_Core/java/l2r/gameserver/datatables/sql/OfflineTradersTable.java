/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.LoginServerThread;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.L2ManufactureItem;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.L2GameClient.GameClientState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfflineTradersTable
{
	private static Logger _log = LoggerFactory.getLogger(OfflineTradersTable.class);
	
	// SQL DEFINITIONS
	private static final String SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)";
	private static final String SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`) VALUES (?,?,?,?)";
	private static final String CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade";
	private static final String CLEAR_OFFLINE_TABLE_PLAYER = "DELETE FROM character_offline_trade WHERE `charId`=?";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items";
	private static final String CLEAR_OFFLINE_TABLE_ITEMS_PLAYER = "DELETE FROM character_offline_trade_items WHERE `charId`=?";
	private static final String LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade";
	private static final String LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?";
	
	public void storeOffliners()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
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
					if ((pc.getPrivateStoreType() != PrivateStoreType.NONE) && ((pc.getClient() == null) || pc.getClient().isDetached()))
					{
						stm3.setInt(1, pc.getObjectId()); // Char Id
						stm3.setLong(2, pc.getOfflineStartTime());
						stm3.setInt(3, pc.getPrivateStoreType().getId()); // store type
						String title = null;
						
						switch (pc.getPrivateStoreType())
						{
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
					_log.warn(getClass().getSimpleName() + ": Error while saving offline trader: " + pc.getObjectId() + " " + e, e);
				}
			}
			_log.info(getClass().getSimpleName() + ": Offline traders stored.");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while saving offline traders: " + e, e);
		}
	}
	
	public void restoreOfflineTraders()
	{
		_log.info(getClass().getSimpleName() + ": Loading offline traders...");
		int nTraders = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
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
					_log.warn(getClass().getSimpleName() + ": PrivateStoreType with id " + rs.getInt("type") + " could not be found.");
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
					player.sitDown();
					if (Config.OFFLINE_SET_NAME_COLOR)
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
					_log.warn(getClass().getSimpleName() + ": Error loading trader: " + player, e);
					if (player != null)
					{
						player.deleteMe();
					}
				}
			}
			
			_log.info(getClass().getSimpleName() + ": Loaded: " + nTraders + " offline trader(s)");
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error while loading offline traders: ", e);
		}
	}
	
	public static synchronized void onTransaction(L2PcInstance trader, boolean finished, boolean firstCall)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			String title = null;
			
			PreparedStatement st = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS_PLAYER);
			st.setInt(1, trader.getObjectId()); // Char Id
			st.execute();
			st.close();
			
			// Trade is done - clear info
			if (finished)
			{
				st = con.prepareStatement(CLEAR_OFFLINE_TABLE_PLAYER);
				st.setInt(1, trader.getObjectId()); // Char Id
				st.execute();
				st.close();
			}
			else
			{
				try
				{
					if ((trader.getClient() == null) || trader.getClient().isDetached())
					{
						PreparedStatement st1 = con.prepareStatement(SAVE_ITEMS);
						switch (trader.getPrivateStoreType())
						{
							case BUY:
								if (firstCall)
								{
									title = trader.getBuyList().getTitle();
								}
								for (TradeItem i : trader.getBuyList().getItems())
								{
									st1.setInt(1, trader.getObjectId());
									st1.setInt(2, i.getItem().getId());
									st1.setLong(3, i.getCount());
									st1.setLong(4, i.getPrice());
									st1.executeUpdate();
									st1.clearParameters();
								}
								break;
							case SELL:
							case PACKAGE_SELL:
								if (firstCall)
								{
									title = trader.getSellList().getTitle();
								}
								for (TradeItem i : trader.getSellList().getItems())
								{
									st1.setInt(1, trader.getObjectId());
									st1.setInt(2, i.getObjectId());
									st1.setLong(3, i.getCount());
									st1.setLong(4, i.getPrice());
									st1.executeUpdate();
									st1.clearParameters();
								}
								break;
							case MANUFACTURE:
								
								if (firstCall)
								{
									title = trader.getStoreName();
								}
								for (L2ManufactureItem i : trader.getManufactureItems().values())
								{
									st1.setInt(1, trader.getObjectId());
									st1.setInt(2, i.getRecipeId());
									st1.setLong(3, 0);
									st1.setLong(4, i.getCost());
									st1.executeUpdate();
									st1.clearParameters();
								}
						}
						st1.close();
						if (firstCall)
						{
							PreparedStatement st2 = con.prepareStatement(SAVE_OFFLINE_STATUS);
							st2.setInt(1, trader.getObjectId()); // Char Id
							st2.setLong(2, trader.getOfflineStartTime());
							st2.setInt(3, trader.getPrivateStoreType().getId()); // store type
							st2.setString(4, title);
							st2.executeUpdate();
							st2.clearParameters();
							st2.close();
						}
					}
				}
				catch (Exception e)
				{
					_log.warn("OfflineTradersTable[storeTradeItems()]: Error while saving offline trader: " + trader.getObjectId() + " " + e, e);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("OfflineTradersTable[storeTradeItems()]: Error while saving offline traders: " + e, e);
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
