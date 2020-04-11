package main.engine.mods;

import java.util.List;
import java.util.logging.Level;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2ManufactureItem;
import com.l2jfrozen.gameserver.model.L2ManufactureList;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.TradeList.TradeItem;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;

import main.EngineModsManager;
import main.concurrent.ThreadPool;
import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilPlayer;

/**
 * @author fissban
 */
public class OfflineShop extends AbstractMod
{
	public OfflineShop()
	{
		if (ConfigData.OFFLINE_TRADE_ENABLE || ConfigData.OFFLINE_SELLBUFF_ENABLE)
		{
			registerMod(true);
		}
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
			{
				loadValuesFromDb();
				loadOfflineShops();
				clearValueDB();
				break;
			}
			case END:
			{
				break;
			}
		}
	}
	
	@Override
	public void onShutDown()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			try
			{
				boolean saveValue = false;
				// saved state in memory
				String title = "";
				String storeItems = "";
				String storeType = "";
				
				if (player.isInStoreMode() && ConfigData.OFFLINE_TRADE_ENABLE)
				{
					switch (player.getPrivateStoreType())
					{
						case 3:
							storeType = "BUY";
							title = player.getBuyList().getTitle();
							for (TradeItem item : player.getBuyList().getItems())
							{
								// ItemId,count,price;
								storeItems += item.getItem().getItemId() + "," + item.getCount() + "," + item.getPrice() + "," + item.getEnchant() + ";";
							}
							break;
						case 5:
							storeType = "MANUFACTURE";
							title = player.getCreateList().getStoreName();
							for (L2ManufactureItem item : player.getCreateList().getList())
							{
								// recipeId,cost;
								storeItems += item.getRecipeId() + "," + item.getCost() + ";";
							}
							break;
						case 1:
							storeType = "SELL";
							title = player.getSellList().getTitle();
							for (TradeItem item : player.getSellList().getItems())
							{
								// objectId,count,price
								storeItems += item.getObjectId() + "," + item.getCount() + "," + item.getPrice() + ";";
							}
							break;
						default:
							System.out.println("NPE ->" + storeItems);
							return;
					}
					
					saveValue = true;
				}
				else if (ObjectData.get(PlayerHolder.class, player).isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE)
				{
					title = "SellBuff"; // TODO You could add to save this data as something custom.
					
					PlayerHolder ph = ObjectData.get(PlayerHolder.class, player);
					
					for (Integer id : player.getSkills().keySet())
					{
						Integer price = ph.getSellBuffPrice(id);
						
						if (price > -1)
						{
							storeItems += id + "," + price + ";";
						}
					}
					storeType = "SELL_BUFF";
					saveValue = true;
				}
				
				if (saveValue)
				{
					// saved state and items in memory
					setValueDB(player.getObjectId(), "offlineShop", storeType + "#" + (title == null || title.length() == 0 ? "null" : title.replaceAll("#", " ")) + "#" + storeItems);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		if (ph.isOffline())
		{
			ph.setOffline(false);
		}
	}
	
	@Override
	public boolean onExitWorld(PlayerHolder ph)
	{
		if (ph.getInstance().isInStoreMode() && ConfigData.OFFLINE_TRADE_ENABLE || ph.isSellBuff() && ConfigData.OFFLINE_SELLBUFF_ENABLE)
		{
			if (!ph.getInstance().isInsideZone(L2Character.ZONE_PEACE))
			{
				ph.getInstance().sendMessage("Estas fuera de la zona de paz!");
				return true;
			}
			
			if (ph.getInstance().isInOlympiadMode() || ph.getInstance().isFestivalParticipant() || ph.getInstance().isInJail())
			{
				return true;
			}
			
			// If a party is in progress, leave it
			if (ph.getInstance().isInParty())
			{
				ph.getInstance().getParty().removePartyMember(ph.getInstance(), true);
			}
			
			// If the Player has Pet, unsummon it
			if (ph.getInstance().getPet() != null)
			{
				ph.getInstance().getPet().unSummon(ph.getInstance());
			}
			
			// Handle removal from olympiad game
			if (Olympiad.getInstance().isRegistered(ph.getInstance()) || ph.getInstance().getOlympiadGameId() != -1)
			{
				Olympiad.getInstance().removeDisconnectedCompetitor(ph.getInstance());
			}
			
			ThreadPool.schedule(() ->
			{
				if (ConfigData.OFFLINE_SET_NAME_COLOR)
				{
					ph.getInstance().getAppearance().setNameColor(ConfigData.OFFLINE_NAME_COLOR);
				}
			}, 5000);
			
			ph.setOffline(true);
		}
		return false;
	}
	
	/**
	 * all players in "trade" mode is read from the db
	 */
	private void loadOfflineShops()
	{
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			String shop = getValueDB(ph, "offlineShop").getString();
			// Don't has value in db
			if (shop == null)
			{
				continue;
			}
			
			L2PcInstance player = null;
			
			try
			{
				// restore players
				String shopType = shop.split("#")[0];
				String shopTitle = shop.split("#")[1];
				String shopItems = shop.split("#")[2];
				
				player = UtilPlayer.spawnPlayer(ph.getObjectId());
				
				player.sitDown();
				player.setIsInvul(true);
				ph.setOffline(true);
				
				if (shopType.equals("SELL_BUFF"))
				{
					for (String e : shopItems.split(";"))
					{
						EngineModsManager.onEvent(player, "SellBuffs set " + e.split(",")[0] + " " + e.split(",")[1]); // shopItems -> price
					}
				}
				else
				{
					String store = shopType;
					switch (store)
					{
						case "BUY":
							player.setPrivateStoreType(3);
							for (String list : shopItems.split(";"))
							{
								List<Integer> items = Util.parseInt(list, ",");
								
								if (player.getBuyList().addItemByItemId(items.get(0), items.get(1), items.get(2), items.get(3)) == null)
								{
									throw new NullPointerException();
								}
							}
							player.getBuyList().setTitle(shopTitle.equals("null") ? "" : shopTitle);
							break;
						
						case "MANUFACTURE":
							player.setPrivateStoreType(5);
							L2ManufactureList createList = new L2ManufactureList();
							for (String list : shopItems.split(";"))
							{
								List<Integer> items = Util.parseInt(list, ",");
								createList.add(new L2ManufactureItem(items.get(0), items.get(1)));
							}
							player.setCreateList(createList);
							player.getCreateList().setStoreName(shopTitle.equals("null") ? "" : shopTitle);
							break;
						
						case "SELL":
							player.setPrivateStoreType(1);
							for (String list : shopItems.split(";"))
							{
								List<Integer> items = Util.parseInt(list, ",");
								if (player.getSellList().addItem(items.get(0), items.get(1), items.get(2)) == null)
								{
									throw new NullPointerException();
								}
							}
							player.getSellList().setTitle(shopTitle.equals("null") ? "" : shopTitle);
							// player.getSellList().setPackaged(store == StoreType.PACKAGE_SELL);
							break;
						default:
							System.out.println("Wrong store type " + store);
							player.deleteMe();
							break;
					}
					
				}
				if (ConfigData.OFFLINE_SET_NAME_COLOR)
				{
					player.getAppearance().setNameColor(ConfigData.OFFLINE_NAME_COLOR);
				}
				
				// player.setInOfflineMode();
				// player.restoreEffects();
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				LOG.log(Level.WARNING, getClass().getSimpleName() + ": Error loading trader: " + player, e);
				e.printStackTrace();
				if (player != null)
				{
					player.deleteMe();
				}
				ph.setOffline(false);
			}
		}
	}
}