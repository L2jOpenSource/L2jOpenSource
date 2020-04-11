package main.engine.community;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.templates.L2EtcItem;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;

import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.data.xml.IconData;
import main.engine.AbstractMod;
import main.holders.AuctionItemHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilInventory;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class MemoCommunityBoard extends AbstractMod
{
	public MemoCommunityBoard()
	{
		registerMod(ConfigData.ENABLE_BBS_MEMO);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				loadValuesFromDb();
				loadAllItems();
				break;
			
			case END:
				break;
		}
	}
	
	private void loadAllItems()
	{
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			try
			{
				// read sold items
				getCollectionValuesDB(ph, "auctionSold").forEach((s, d) -> ph.addAuctionSold(Integer.parseInt(s), new AuctionItemHolder(d.getString())));
				getCollectionValuesDB(ph, "auctionSell").forEach((s, d) -> ph.addAuctionSell(Integer.parseInt(s), new AuctionItemHolder(d.getString())));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Remove sold items from memory and db
	 * @param ph
	 * @param key
	 */
	private void removeSold(PlayerHolder ph, int key)
	{
		removeValueDB(ph.getObjectId(), "auctionSold " + key);
		ph.removeAuctionSold(key);
	}
	
	/**
	 * Remove sell items from memory and db
	 * @param ph
	 * @param id
	 */
	private void removeSell(PlayerHolder ph, int id)
	{
		removeValueDB(ph.getObjectId(), "auctionSell " + id);
		ph.removeAuctionSell(id);
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		for (AuctionItemHolder ih : ph.getAuctionsSold().values())
		{
			// remove from db and memory
			removeSold(ph, ih.getkey());
			
			giveAuctionSold(ph.getInstance(), ih);
		}
	}
	
	@Override
	public boolean onCommunityBoard(PlayerHolder ph, String command)
	{
		if (command.startsWith("_bbsmemo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			// bbsmemo
			st.nextToken();
			// bypass
			String bypass = st.hasMoreTokens() ? st.nextToken() : "allAuctions";
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
			hb.append(Html.START);
			hb.append("<br>");
			hb.append("<center>");
			hb.append(marcButton(bypass));
			hb.append("<table border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td><button value=\"ALL AUCTIONS\" action=\"bypass _bbsmemo allAuctions\" width=100 height=32 back=L2UI_CH3.br_party1_back2 fore=L2UI_CH3.br_party1_back2></td>");
			hb.append("<td><button value=\"MY AUCTION\" action=\"bypass _bbsmemo myAuctions\" width=100 height=32 back=L2UI_CH3.br_party1_back2 fore=L2UI_CH3.br_party1_back2></td>");
			hb.append("<td><button value=\"AUCTION ITEM\" action=\"bypass _bbsmemo setAuction\" width=100 height=32 back=L2UI_CH3.br_party1_back2 fore=L2UI_CH3.br_party1_back2></td>");
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(marcButton(bypass));
			hb.append("<br>");
			
			hb.append("<br>");
			
			switch (bypass)
			{
				case "buyItemConfirm":
				{
					try
					{
						PlayerHolder phOwner = ObjectData.get(PlayerHolder.class, Integer.parseInt(st.nextToken()));
						int key = Integer.parseInt(st.nextToken());
						
						hb.append(buyItemConfirm(ph, phOwner, key));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				}
				case "buyItem":
				{
					try
					{
						PlayerHolder phOwner = ObjectData.get(PlayerHolder.class, Integer.parseInt(st.nextToken()));
						int key = Integer.parseInt(st.nextToken());
						
						hb.append(buyItem(ph, phOwner, key));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					break;
				}
				case "sellItem":
				{
					int itemObjId = 0;
					int itemCount = 1;
					int itemPriceCount = 0;
					int itemPriceId = 57;
					
					try
					{
						itemObjId = Integer.parseInt(st.nextToken());
						itemCount = Integer.parseInt(st.nextToken());
						itemPriceCount = Integer.parseInt(st.nextToken());
						
						// Add each case for each new item added
						switch (st.nextToken())
						{
							case "Adena":
								itemPriceId = 57;
								break;
							case "NCoins":
								itemPriceId = -1;
								break;
						}
						
						hb.append(sellItem(ph, itemObjId, itemCount, itemPriceCount, itemPriceId));
					}
					catch (Exception e)
					{
						// hb.append("bypass? olvidate!");
						// e.printStackTrace();
						// es solo por si dejan algun espacio vacio
						// De todas maneras falta generar la pag para confirmar la venta
						break;
					}
					
					// mostrar sus auctions
					hb.append(getMyAuctions(ph));
					break;
				}
				case "cancelSell":
				{
					if (st.hasMoreTokens())
					{
						int id = Integer.parseInt(st.nextToken());
						
						AuctionItemHolder ih = ph.getAuctionsSell().get(id);
						
						if (ih != null)
						{
							// Remove from memory and db
							removeSell(ph, id);
							
							// Create item
							UtilInventory.giveItems(ph, ih.getItemId(), ih.getItemCount(), ih.getItemEnchantLevel());
						}
					}
					else
					{
						hb.append("bypass? xD");
					}
					
					// mostrar sus auctions
					hb.append(getMyAuctions(ph));
					break;
				}
				case "myAuctions":
				{
					hb.append(getMyAuctions(ph));
					break;
				}
				// It shows the list of items that could sell
				case "setAuction":
				{
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
					hb.append(getSellList(ph, page));
					break;
				}
				case "allAuctions":
				{
					// item type
					String itemType = st.hasMoreTokens() ? st.nextToken() : "all";
					// grade
					String grade = st.hasMoreTokens() ? st.nextToken() : "ALL";
					// page
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
					
					hb.append(allAuctions(bypass, grade, page, itemType));
					break;
				}
				
			}
			hb.append("</center>");
			hb.append(Html.END);
			sendCommunity(ph, hb.toString());
			return true;
		}
		
		return false;
		
	}
	
	// XXX HTML -----------------------------------------------------
	private static String getMyAuctions(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		int color = 0;
		
		hb.append("<br1>");
		hb.append("If you cancel the sale of an item it will not be returned<br1>");
		hb.append("the initial commission we charge you<br>");
		
		hb.append(Html.image(L2UI.SquareGray, 600, 1));
		// It traverses all the inventory items that can be traded.
		for (Entry<Integer, AuctionItemHolder> entry : ph.getAuctionsSell().entrySet())
		{
			AuctionItemHolder holder = entry.getValue();
			int id = entry.getKey();
			
			if (ph.getObjectId() != holder.getOwnerId())
			{
				continue;
			}
			
			L2ItemInstance item = UtilInventory.createDummyItem(holder.getItemId());
			
			hb.append("<table ", (color % 2) == 0 ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=600 height=56>");
			hb.append("<tr>");
			hb.append("<td width=32>", Html.image(IconData.getIconByItemId(item.getItemId()), 32, 32), "</td>");
			hb.append("<td width=489>");
			
			hb.append("<table cellspacing=0 cellpadding=0 width=489 height=56>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Name: "), item.getItemName(), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Grade: "), getCrystalType(item), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Enchant: "), "+", holder.getItemEnchantLevel(), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "count: "), "+", holder.getItemCount(), "</td></tr>");
			hb.append("</table");
			
			hb.append("</td>");
			hb.append("<td width=75><button value=CANCEL action=\"bypass _bbsmemo cancelSell ", id, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(Html.image(L2UI.SquareGray, 600, 1));
			color++;
		}
		
		return hb.toString();
	}
	
	private static String allAuctions(String bypass, String grade, int page, String itemType)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		// Grade of the items you want to filter
		hb.append("<table width=600 height=24 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=18 align=right>", grade.equals("ALL") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=ALL action=\"bypass _bbsmemo ", bypass, " ", itemType, " ALL ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("NONE") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=NO action=\"bypass _bbsmemo ", bypass, " ", itemType, " NONE ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("D") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=D action=\"bypass _bbsmemo ", bypass, " ", itemType, " D ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("C") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=C action=\"bypass _bbsmemo ", bypass, " ", itemType, " C ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("B") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=B action=\"bypass _bbsmemo ", bypass, " ", itemType, " B ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("A") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=A action=\"bypass _bbsmemo ", bypass, " ", itemType, " A ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("<td width=18 align=right>", grade.equals("S") ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td><td width=64><button value=S action=\"bypass _bbsmemo ", bypass, " ", itemType, " S ", page, "\" width=64 height=22 back=", L2UI_CH3.herochat_tab2, " fore=", L2UI_CH3.herochat_tab2_over, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		// menu left -------------------------------------------------------------------------------------------------------
		hb.append("<td align=center>");
		
		hb.append("<table width=126 height=22 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackLeft, 16, 22), "</td>");
		hb.append("<td width=94 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackMid, 94, 22), "</td>");
		hb.append("<td width=16 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackRight, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("<table cellspacing=3 cellpadding=0>");
		hb.append(menuItemType(bypass + " all " + grade + " " + page, "all", itemType));
		hb.append(menuItemType(bypass + " weapon " + grade + " " + page, "weapon", itemType));
		hb.append(menuItemType(bypass + " chest " + grade + " " + page, "chest", itemType));
		hb.append(menuItemType(bypass + " legs " + grade + " " + page, "legs", itemType));
		hb.append(menuItemType(bypass + " head " + grade + " " + page, "head", itemType));
		hb.append(menuItemType(bypass + " glove " + grade + " " + page, "glove", itemType));
		hb.append(menuItemType(bypass + " boot " + grade + " " + page, "boot", itemType));
		hb.append(menuItemType(bypass + " shield " + grade + " " + page, "shield", itemType));
		hb.append(menuItemType(bypass + " jewel " + grade + " " + page, "jewel", itemType));
		hb.append(menuItemType(bypass + " scroll " + grade + " " + page, "scroll", itemType));
		hb.append(menuItemType(bypass + " misc " + grade + " " + page, "misc", itemType));
		hb.append("</table>");
		
		hb.append("</td>");
		// menu right -------------------------------------------------------------------------------------------------------
		hb.append("<td align=center>");
		
		hb.append("<table width=474 height=22 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackLeft, 16, 22), "</td>");
		hb.append("<td width=442 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackMid, 442, 22), "</td>");
		hb.append("<td width=16 height=22 valign=top align=center>", Html.image(L2UI_CH3.FrameBackRight, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append(getAuctionItems(bypass, itemType, grade, page));
		
		hb.append("</td>");
		// --------------------------------------------------------------------------------------------------------------------
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	private String sellItem(PlayerHolder ph, int itemObjId, int itemCount, int itemPriceCount, int itemPriceId)
	{
		// Negative numbers are prevented
		if (itemPriceCount < 0)
		{
			itemPriceCount = 0;
		}
		
		if (!UtilInventory.takeItems(ph, ConfigData.COMMISION_FOR_START_SELL_ID, ConfigData.COMMISION_FOR_START_SELL))
		{
			return "<br><br><br>You do not have enough for the initial commission";
		}
		
		L2ItemInstance itemToDestroy = ph.getInstance().getInventory().getItemByObjectId(itemObjId);
		
		if (itemToDestroy.getCount() < itemCount)
		{
			return "<br><br><br>You do not have so many items to sell";
		}
		
		// prevenimos posible bypass
		if (ph.getInstance().destroyItem("auction", itemToDestroy, ph.getInstance(), true))
		{
			// se busca un key no usado para los personajes
			int id = getNewKey(ph.getAuctionsSell());
			if (id == -1)
			{
				if (id == -1)
				{
					return "<br><br><br>ufff un error al obtener el nuevo id del item a vender";
				}
			}
			
			AuctionItemHolder ash = new AuctionItemHolder(id, ph.getObjectId(), itemObjId, itemToDestroy.getItemId(), itemCount, itemToDestroy.getEnchantLevel(), itemPriceCount, itemPriceId);
			// save db and memory
			ph.addAuctionSell(id, ash);
			setValueDB(ph, "auctionSell " + id, ash.toString());
		}
		
		return "";
	}
	
	private String buyItem(PlayerHolder ph, PlayerHolder phOwner, int key)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		if (!phOwner.getAuctionsSell().containsKey(key))
		{
			// possible bypass
			hb.append("<br><br><br>");
			hb.append("bypass? xD");
		}
		else
		{
			AuctionItemHolder ih = phOwner.getAuctionsSell().get(key);
			hb.append("<br><br><br>");
			hb.append("<table cellspacing=0 cellpadding=0 height=80 width=600>");
			hb.append("<tr><td height=40 align=center>");
			hb.append(Html.fontColor("LEVEL", "Estas seguro que deseas comprar???"));
			hb.append("</td></tr>");
			
			hb.append("<tr><td height=60 align=center>");
			L2ItemInstance item = UtilInventory.createDummyItem(ih.getItemId());
			
			hb.append("<table bgcolor=000000 cellspacing=0 cellpadding=0 width=474>");
			hb.append("<tr>");
			hb.append("<td fixwidth=32>", Html.image(IconData.getIconByItemId(item.getItemId()), 32, 32), "</td>");
			hb.append("<td align=center>");
			
			String priceName = "";
			
			switch (ih.getItemPriceId())
			{
				case -1:
					priceName = "NCoins";
					break;
				default:
					ItemTable.getInstance().getTemplate(ih.getItemPriceId()).getName();
					break;
			}
			
			hb.append("<table cellspacing=0 cellpadding=0 width=337 height=85>");
			hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Name: "), item.getItemName(), " - ", getCrystalType(item), "</td></tr>");
			hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Enchant: "), "+", ih.getItemEnchantLevel(), "</td></tr>");
			hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Cantidad: "), "", ih.getItemCount(), "</td></tr>");
			hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Price: "), Html.formatAdena(ih.getItemPriceCount()), " ", priceName, "</td></tr>");
			hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Owner: "), phOwner.getName(), "</td></tr>");
			hb.append("</table>");
			
			hb.append("</td></tr>");
			hb.append("</table>");
			
			hb.append("</td></tr>");
			hb.append("<tr><td align=center>");
			hb.append("<br>");
			hb.append("<table cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td><button value=\"Yes\" action=\"bypass _bbsmemo buyItemConfirm ", phOwner.getObjectId(), " ", key, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
			hb.append("<td><button value=\"No\" action=\"bypass _bbsmemo allAuctions\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
			hb.append("</tr>");
			hb.append("</table>");
			
			hb.append("</td></tr>");
			hb.append("</table>");
		}
		
		return hb.toString();
	}
	
	private String buyItemConfirm(PlayerHolder ph, PlayerHolder phOwner, int key)
	{
		if (!phOwner.getAuctionsSell().containsKey(key))
		{
			// Possible bypass
			return "<br><br><br>bypass??? xD";
		}
		else
		{
			// The value of the memory is obtained
			AuctionItemHolder ih = phOwner.getAuctionsSell().get(key);
			
			// Check that the buyer has enough to buy the item
			if (ph.getInstance().getInventory().destroyItemByItemId("auction buy", ih.getItemPriceId(), ih.getItemPriceCount(), ph.getInstance(), ph.getInstance()) == null)
			{
				return "<br><br><br>Sorry, you are not able to buy this item";
			}
			
			// Deliver the item to the buyer.
			UtilInventory.giveItems(ph, ih.getItemId(), ih.getItemCount(), ih.getItemEnchantLevel());
			
			L2PcInstance owner = L2World.getInstance().getPlayer(ih.getOwnerId());
			
			// The adena of sale is delivered to the owner.
			if (owner == null)
			{
				// se busca un key no usado para el personaje
				int id = getNewKey(phOwner.getAuctionsSold());
				
				if (id == -1)
				{
					return "<br><br><br>ufff un error al obtener el nuevo id del item ";
				}
				
				ih.setKey(id);
				
				// se incrementa la cantidad de items vendidos a entregar cuando se conecte
				phOwner.addAuctionSold(ih.getkey(), ih);
				setValueDB(phOwner.getObjectId(), "auctionSold " + ih.getkey(), ih.toString());
			}
			else
			{
				// remove from db and memory
				removeSold(phOwner, ih.getkey());
				
				giveAuctionSold(owner, ih);
			}
			
			// remove from memory and db
			// removeSell(phOwner, ih.getkey());
			removeSold(phOwner, ih.getkey());
			// TODO podriamos crear la parte del historial no?
		}
		
		return "<br><br><br>Congratulations, your purchase was successful.";
	}
	
	private static String getSellList(PlayerHolder ph, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		hb.append("<br1>");
		hb.append("Ten en cuenta que por poner un item a la venta<br1>");
		hb.append("se te va a cobrar ", Html.formatAdena(ConfigData.COMMISION_FOR_START_SELL), " de ", ItemTable.getInstance().getTemplate(ConfigData.COMMISION_FOR_START_SELL_ID).getName(), "<br1>");
		hb.append("y al momento de la venta nos quedamos con<br1>");
		hb.append("el ", ConfigData.COMMISION_FOR_END_SELL, "% del total.<br>");
		
		hb.append(Html.image(L2UI.SquareGray, 600, 1));
		// It traverses all the inventory items that can be traded.
		int MAX_PER_PAGE = 6;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		int color = 0;
		
		L2ItemInstance[] list = ph.getInstance().getInventory().getAvailableItems(false);
		
		for (L2ItemInstance item : list)
		{
			// min
			if (count < searchPage)
			{
				count++;
				continue;
			}
			// max
			if (count >= (searchPage + MAX_PER_PAGE))
			{
				continue;
			}
			
			// TODO si se quiere filtrar los items que se pueden vender aqui es el lugar!
			
			// De los etcItems solo dejaremos que vendan los enchants
			// if (item.getItemType() instanceof EtcItemType)
			// {
			// if (item.getItemType() != EtcItemType.SCRL_ENCHANT_WP && item.getItemType() != EtcItemType.SCRL_ENCHANT_AM && item.getItemType() != EtcItemType.BLESS_SCRL_ENCHANT_AM && item.getItemType() != EtcItemType.BLESS_SCRL_ENCHANT_WP)
			// {
			// continue;
			// }
			// }
			
			hb.append("<table ", (color % 2) == 0 ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=600 height=56>");
			hb.append("<tr>");
			hb.append("<td width=32>", Html.image(IconData.getIconByItemId(item.getItemId()), 32, 32), "</td>");
			hb.append("<td width=300>");
			
			hb.append("<table cellspacing=0 cellpadding=0 width=300 height=56>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Name: "), item.getItemName(), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "count: "), "", item.getCount(), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Grade: "), getCrystalType(item), "</td></tr>");
			hb.append("<tr><td>", Html.fontColor("FF8000", "Enchant: "), "+", item.getEnchantLevel(), "</td></tr>");
			hb.append("</table");
			
			hb.append("</td>");
			
			hb.append("<td width=50>Count:</td>");
			hb.append("<td width=50><edit var=\"count", color, "\" width=40></td>");
			
			hb.append("<td width=60>Price:</td>");
			hb.append("<td width=40><edit var=\"price", color, "\" width=40></td>");
			hb.append("<td width=50><combobox width=50 var=item list=Adena;NCoins;></td>");
			hb.append("<td width=40><button value=SELL action=\"bypass _bbsmemo sellItem ", item.getObjectId(), " $count", color, " $price", color, " $item\" width=50 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(Html.image(L2UI.SquareGray, 600, 1));
			color++;
			count++;
		}
		
		hb.append(Html.image(L2UI.SquareWhite, 474, 1));
		hb.append("<table>");
		hb.append("<tr>");
		int currentPage = 1;
		int size = list.length;
		for (int i = 0; i < size; i++)
		{
			if ((i % MAX_PER_PAGE) == 0)
			{
				if (currentPage == page)
				{
					hb.append("<td width=20>", Html.fontColor("LEVEL", currentPage), "</td>");
				}
				else
				{
					hb.append("<td width=20><a action=\"bypass _bbsmemo setAuction ", currentPage, "\">", currentPage, "</a></td>");
				}
				currentPage++;
			}
		}
		
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	private static String getAuctionItems(String bypass, String itemType, String grade, int page)
	{
		List<AuctionItemHolder> itemList = new ArrayList<>();
		
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			for (AuctionItemHolder holder : ph.getAuctionsSell().values())
			{
				L2ItemInstance it = UtilInventory.createDummyItem(holder.getItemId());
				
				switch (grade)
				{
					case "ALL":
					{
						if (searchItemByType(itemType, it))
						{
							itemList.add(holder);
						}
						
						break;
					}
					default:
					{
						int crystal = 0;
						switch (grade)
						{
							case "NONE":
								crystal = 0;
								break;
							case "D":
								crystal = 1;
								break;
							case "C":
								crystal = 2;
								break;
							case "B":
								crystal = 3;
								break;
							case "A":
								crystal = 4;
								break;
							case "S":
								crystal = 5;
								break;
						}
						// CrystalType cType = CrystalType.valueOf(grade);
						
						if (it.getItem().getCrystalType() == crystal)
						{
							if (searchItemByType(itemType, it))
							{
								itemList.add(holder);
							}
						}
						
						break;
					}
				}
			}
		}
		
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		if (!itemList.isEmpty())
		{
			int MAX_PER_PAGE = 6;
			int searchPage = MAX_PER_PAGE * (page - 1);
			int count = 0;
			int color = 0;
			
			hb.append(Html.image(L2UI.SquareGray, 474, 1));
			hb.append("<table width=474 height=294 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td valign=top>");
			
			for (AuctionItemHolder ih : itemList)
			{
				L2ItemInstance item = UtilInventory.createDummyItem(ih.getItemId());
				
				// min
				if (count < searchPage)
				{
					count++;
					continue;
				}
				// max
				if (count >= (searchPage + MAX_PER_PAGE))
				{
					continue;
				}
				
				hb.append("<table ", (color % 2) == 0 ? "bgcolor=000000 " : "", "cellspacing=0 cellpadding=0 width=474 height=42>");
				hb.append("<tr>");
				hb.append("<td fixwidth=32 height=42>", Html.image(IconData.getIconByItemId(item.getItemId()), 32, 32), "</td>");
				hb.append("<td>");
				
				String gradeItem = getCrystalType(item);
				if (gradeItem.equals("NONE"))
				{
					gradeItem = "No Grade";
				}
				
				String priceName = "";
				switch (ih.getItemPriceId())
				{
					case -1:
						priceName = "NCoins";
						break;
					default:
						ItemTable.getInstance().getTemplate(ih.getItemPriceId()).getName();
						break;
				}
				
				hb.append("<table cellspacing=0 cellpadding=0 width=337 height=42>");
				hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Name: "), item.getItemName(), " - ", gradeItem, " - <font color=LEVEL>(", ih.getItemCount(), ")</font></td></tr>");
				hb.append("<tr><td>", Html.fontColor("FF8000", "Enchant: "), "+", ih.getItemEnchantLevel(), "</td></tr>");
				hb.append("<tr><td>", Html.fontColor("FF8000", "Price: "), Html.formatAdena(ih.getItemPriceCount()), " ", priceName, "</td></tr>");
				// hb.append("<tr><td fixwidth=337>", Html.fontColor("FF8000", "Owner: "), ObjectData.get(PlayerHolder.class, holder.getOwnerId()).getName(), "</td></tr>");
				hb.append("</table>");
				
				hb.append("</td>");
				hb.append("<td width=105><button value=BUY action=\"bypass _bbsmemo buyItem ", ih.getOwnerId(), " ", ih.getkey(), "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
				hb.append("</tr>");
				hb.append("</table>");
				hb.append(Html.image(L2UI.SquareGray, 474, 1));
				
				color++;
				count++;
			}
			
			hb.append("</td>");
			hb.append("</tr>");
			hb.append("</table>");
			
			hb.append("<br>");
			hb.append(Html.image(L2UI.SquareWhite, 474, 1));
			hb.append("<table bgcolor=000000 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			
			int currentPage = 1;
			int size = itemList.size();
			for (int i = 0; i < size; i++)
			{
				if ((i % MAX_PER_PAGE) == 0)
				{
					if (currentPage == page)
					{
						hb.append("<td width=20>", Html.fontColor("LEVEL", currentPage), "</td>");
					}
					else
					{
						hb.append("<td width=20><a action=\"bypass _bbsmemo ", bypass, " ", itemType, " ", grade, " ", currentPage, "\">", currentPage, "</a></td>");
					}
					currentPage++;
				}
			}
			
			hb.append("</tr>");
			hb.append("</table>");
			hb.append(Html.image(L2UI.SquareWhite, 474, 1));
			hb.append("<br>");
		}
		else
		{
			hb.append("<center>No items for sale</center>");
		}
		
		hb.append("<button value=\"Refresh\" action=\"bypass _bbsmemo ", bypass, " ", itemType, " ", grade, " ", page, "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		
		return hb.toString();
	}
	
	private static String menuItemType(String bypass, String itemType, String actualItemType)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<tr>");
		hb.append("<td width=16 height=22>", actualItemType.equals(itemType) ? Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16) : "", "</td>");
		hb.append("<td><button value=\"", itemType, "\" action=\"bypass _bbsmemo ", bypass, "\" width=95 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
		hb.append("</tr>");
		return hb.toString();
	}
	
	private static String marcButton(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td height=2>", Html.image(bypass.equals("allAuctions") ? L2UI_CH3.fishing_bar1 : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
		hb.append("<td height=2>", Html.image(bypass.equals("myAuctions") ? L2UI_CH3.fishing_bar1 : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
		hb.append("<td height=2>", Html.image(bypass.equals("setAuction") ? L2UI_CH3.fishing_bar1 : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	// XXX MISC --------------------------------------------------------------------------------------------------------
	private static boolean searchItemByType(String itemType, L2ItemInstance it)
	{
		switch (itemType)
		{
			case "all":
			{
				return true;
			}
			case "weapon":
			{
				if (it.getItem() instanceof L2Weapon)
				{
					return true;
				}
				break;
			}
			case "chest":
			{
				if ((it.getItem().getBodyPart() == L2Item.SLOT_CHEST) || (it.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR))
				{
					return true;
				}
				break;
			}
			case "legs":
			{
				if (it.getItem().getBodyPart() == L2Item.SLOT_LEGS)
				{
					return true;
				}
				break;
			}
			case "head":
			{
				if (it.getItem().getBodyPart() == L2Item.SLOT_HEAD)
				{
					return true;
				}
				break;
			}
			case "glove":
			{
				if (it.getItem().getBodyPart() == L2Item.SLOT_GLOVES)
				{
					return true;
				}
				break;
			}
			case "boot":
			{
				if (it.getItem().getBodyPart() == L2Item.SLOT_FEET)
				{
					return true;
				}
				break;
			}
			case "shield":
			{
				if (it.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
				{
					return true;
				}
				break;
			}
			case "jewel":
			{
				switch (it.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_EAR:
					case L2Item.SLOT_L_EAR:
					case L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR:
					case L2Item.SLOT_NECK:
					case L2Item.SLOT_R_FINGER:
					case L2Item.SLOT_L_FINGER:
					case L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER:
						return true;
					default:
						break;
				}
				break;
			}
			case "scroll":
			{
				if (it.getItem().getItemType() == L2EtcItemType.SCROLL)
				{
					return true;
				}
				break;
			}
			case "misc":
			{
				if (it.getItem() instanceof L2EtcItem)
				{
					if (it.getItem().getItemType() == L2EtcItemType.SCROLL)
					{
						return false;
					}
					
					return true;
				}
				break;
			}
		}
		
		return false;
	}
	
	private static void giveAuctionSold(L2PcInstance owner, AuctionItemHolder ih)
	{
		// template del item
		L2Item item = ItemTable.getInstance().getTemplate(ih.getItemId());
		// mensajes
		owner.sendPacket(new CreatureSay(owner.getObjectId(), Say2.TELL, "Auction", "Has vendido el item " + item.getName()));
		// owner.sendPacket(new ExShowScreenMessage("Has vendido el item " + item.getName(), 10000, SMPOS.TOP_CENTER, false));
		// give item
		owner.getInventory().addItem("Auction", ih.getItemPriceId(), ih.getItemPriceCount(), owner, null);
	}
	
	private static int getNewKey(Map<Integer, AuctionItemHolder> map)
	{
		// se busca un key no usado para el personaje
		int id = -1;
		for (int i = 1; i <= 100; i++)
		{
			if (!map.containsKey(i))
			{
				id = i;
				break;
			}
		}
		
		return id;
	}
	
	private static String getCrystalType(L2ItemInstance item)
	{
		switch (item.getItem().getCrystalType())
		{
			case 1:
				return "D";
			case 2:
				return "C";
			case 3:
				return "B";
			case 4:
				return "A";
			case 5:
				return "S";
		}
		
		return "NONE";
	}
}
