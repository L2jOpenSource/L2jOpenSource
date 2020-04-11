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
package l2r.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.multisell.Entry;
import l2r.gameserver.model.multisell.Ingredient;
import l2r.gameserver.model.multisell.ListContainer;
import l2r.gameserver.model.multisell.PreparedListContainer;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2r.gameserver.network.serverpackets.MultiSellList;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.util.file.filter.XMLFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MultiSell
{
	private static final Logger _log = LoggerFactory.getLogger(MultiSell.class);
	
	public static final int PAGE_SIZE = 40;
	
	public static final int PC_BANG_POINTS = -100;
	public static final int CLAN_REPUTATION = -200;
	public static final int FAME = -300;
	
	private final Map<Integer, ListContainer> _entries = new HashMap<>();
	
	protected MultiSell()
	{
		load();
	}
	
	public final void reload()
	{
		_entries.clear();
		load();
	}
	
	/**
	 * This will generate the multisell list for the items.<br>
	 * There exist various parameters in multisells that affect the way they will appear:
	 * <ol>
	 * <li>Inventory only:
	 * <ul>
	 * <li>If true, only show items of the multisell for which the "primary" ingredients are already in the player's inventory. By "primary" ingredients we mean weapon and armor.</li>
	 * <li>If false, show the entire list.</li>
	 * </ul>
	 * </li>
	 * <li>Maintain enchantment: presumably, only lists with "inventory only" set to true should sometimes have this as true. This makes no sense otherwise...
	 * <ul>
	 * <li>If true, then the product will match the enchantment level of the ingredient.<br>
	 * If the player has multiple items that match the ingredient list but the enchantment levels differ, then the entries need to be duplicated to show the products and ingredients for each enchantment level.<br>
	 * For example: If the player has a crystal staff +1 and a crystal staff +3 and goes to exchange it at the mammon, the list should have all exchange possibilities for the +1 staff, followed by all possibilities for the +3 staff.</li>
	 * <li>If false, then any level ingredient will be considered equal and product will always be at +0</li>
	 * </ul>
	 * </li>
	 * <li>Apply taxes: Uses the "taxIngredient" entry in order to add a certain amount of adena to the ingredients.
	 * <li>
	 * <li>Additional product and ingredient multipliers.</li>
	 * </ol>
	 * @param listId
	 * @param player
	 * @param npc
	 * @param inventoryOnly
	 * @param productMultiplier
	 * @param ingredientMultiplier
	 */
	public final void separateAndSend(int listId, L2PcInstance player, L2Npc npc, boolean inventoryOnly, double productMultiplier, double ingredientMultiplier)
	{
		ListContainer template = _entries.get(listId);
		if (template == null)
		{
			if (player.isAioMultisell())
			{
				_log.warn("AIOItem " + getClass().getSimpleName() + ": Cannot find list: " + listId + " requested by player: " + player.getName());
			}
			else
			{
				_log.warn(getClass().getSimpleName() + ": can't find list id: " + listId + " requested by player: " + player.getName() + ", npcId:" + (npc != null ? npc.getId() : 0));
			}
			return;
		}
		
		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		
		// Pass through this only when multipliers are different from 1
		if ((productMultiplier != 1) || (ingredientMultiplier != 1))
		{
			list.getEntries().forEach(entry ->
			{
				// Math.max used here to avoid dropping count to 0
				entry.getProducts().forEach(product -> product.setItemCount((long) Math.max(product.getItemCount() * productMultiplier, 1)));
				
				// Math.max used here to avoid dropping count to 0
				entry.getIngredients().forEach(ingredient -> ingredient.setItemCount((long) Math.max(ingredient.getItemCount() * ingredientMultiplier, 1)));
			});
		}
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());
		
		player.setMultiSell(list);
	}
	
	public final void separateAndSend(int listId, L2PcInstance player, L2Npc npc, boolean inventoryOnly)
	{
		separateAndSend(listId, player, npc, inventoryOnly, 1, 1);
	}
	
	public static final boolean checkSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case PC_BANG_POINTS:
				if (player.getPcBangPoints() < amount)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SHORT_OF_ACCUMULATED_POINTS));
					break;
				}
				return true;
			case CLAN_REPUTATION:
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
					break;
				}
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					break;
				}
				if (player.getClan().getReputationScore() < amount)
				{
					player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					break;
				}
				return true;
			case FAME:
				if (player.getFame() < amount)
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS);
					break;
				}
				return true;
		}
		return false;
	}
	
	public static final boolean getSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case PC_BANG_POINTS: // PcBang points
				final int cost = player.getPcBangPoints() - (int) (amount);
				player.setPcBangPoints(cost);
				SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
				smsgpc.addInt((int) amount);
				player.sendPacket(smsgpc);
				player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) amount, false, false, 1));
				return true;
			case CLAN_REPUTATION:
				player.getClan().takeReputationScore((int) amount, true);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
				smsg.addLong(amount);
				player.sendPacket(smsg);
				return true;
			case FAME:
				player.setFame(player.getFame() - (int) amount);
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExBrExtraUserInfo(player));
				return true;
		}
		return false;
	}
	
	public static final void addSpecialProduct(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
				player.getClan().addReputationScore((int) amount, true);
				break;
			case FAME:
				player.setFame((int) (player.getFame() + amount));
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExBrExtraUserInfo(player));
				break;
		}
	}
	
	private final void load()
	{
		Document doc = null;
		int id = 0;
		List<File> files = new FastList<>();
		hashFiles("data/multisell", files);
		if (Config.CUSTOM_MULTISELL_LOAD)
		{
			hashFiles("data/multisell/custom", files);
		}
		
		for (File f : files)
		{
			try
			{
				id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.error(getClass().getSimpleName() + ": Error loading file " + f, e);
				continue;
			}
			
			try
			{
				ListContainer list = parseDocument(doc);
				list.setListId(id);
				_entries.put(id, list);
			}
			catch (Exception e)
			{
				_log.error(getClass().getSimpleName() + ": Error in file " + f, e);
			}
		}
		verify();
		_log.info(getClass().getSimpleName() + ": Loaded " + _entries.size() + " lists.");
	}
	
	private final ListContainer parseDocument(Document doc)
	{
		int entryId = 1;
		Node attribute;
		ListContainer list = new ListContainer();
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				attribute = n.getAttributes().getNamedItem("applyTaxes");
				if (attribute == null)
				{
					list.setApplyTaxes(false);
				}
				else
				{
					list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				
				attribute = n.getAttributes().getNamedItem("useRate");
				if (attribute != null)
				{
					try
					{
						
						list.setUseRate(Double.valueOf(attribute.getNodeValue()));
						if (list.getUseRate() <= 1e-6)
						{
							throw new NumberFormatException("The value cannot be 0"); // threat 0 as invalid value
						}
					}
					catch (NumberFormatException e)
					{
						
						try
						{
							list.setUseRate(Config.class.getField(attribute.getNodeValue()).getDouble(Config.class));
						}
						catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | DOMException e1)
						{
							_log.warn(e.getMessage() + doc.getLocalName());
							list.setUseRate(1.0);
						}
						
					}
					catch (DOMException e)
					{
						_log.warn(e.getMessage() + doc.getLocalName());
					}
				}
				
				attribute = n.getAttributes().getNamedItem("maintainEnchantment");
				if (attribute == null)
				{
					list.setMaintainEnchantment(false);
				}
				else
				{
					list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						Entry e = parseEntry(d, entryId++, list);
						list.getEntries().add(e);
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				Entry e = parseEntry(n, entryId++, list);
				list.getEntries().add(e);
			}
		}
		
		return list;
	}
	
	private final Entry parseEntry(Node n, int entryId, ListContainer list)
	{
		Node attribute;
		Node first = n.getFirstChild();
		final Entry entry = new Entry(entryId);
		
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				int enchantmentLevel = 0;
				if (n.getAttributes().getNamedItem("enchantmentLevel") != null)
				{
					enchantmentLevel = Integer.parseInt(n.getAttributes().getNamedItem("enchantmentLevel").getNodeValue());
				}
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				long count = Long.parseLong(n.getAttributes().getNamedItem("count").getNodeValue());
				boolean isTaxIngredient, mantainIngredient;
				
				attribute = n.getAttributes().getNamedItem("isTaxIngredient");
				if (attribute != null)
				{
					isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}
				else
				{
					isTaxIngredient = false;
				}
				
				attribute = n.getAttributes().getNamedItem("maintainIngredient");
				if (attribute != null)
				{
					mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}
				else
				{
					mantainIngredient = false;
				}
				
				entry.addIngredient(new Ingredient(id, count, enchantmentLevel, isTaxIngredient, mantainIngredient));
			}
			else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				int enchantmentLevel = 0;
				if (n.getAttributes().getNamedItem("enchantmentLevel") != null)
				{
					enchantmentLevel = Integer.parseInt(n.getAttributes().getNamedItem("enchantmentLevel").getNodeValue());
				}
				
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				long count = (long) (Long.parseLong(n.getAttributes().getNamedItem("count").getNodeValue()) * list.getUseRate());
				
				entry.addProduct(new Ingredient(id, count, enchantmentLevel, false, false));
			}
		}
		
		return entry;
	}
	
	private final void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.warn(getClass().getSimpleName() + ": Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		File[] files = dir.listFiles(new XMLFilter());
		for (File f : files)
		{
			hash.add(f);
		}
	}
	
	private final void verify()
	{
		ListContainer list;
		final Iterator<ListContainer> iter = _entries.values().iterator();
		while (iter.hasNext())
		{
			list = iter.next();
			
			for (Entry ent : list.getEntries())
			{
				for (Ingredient ing : ent.getIngredients())
				{
					if (!verifyIngredient(ing))
					{
						_log.warn(getClass().getSimpleName() + ": can't find ingredient with itemId: " + ing.getId() + " in list: " + list.getListId());
					}
				}
				for (Ingredient ing : ent.getProducts())
				{
					if (!verifyIngredient(ing))
					{
						_log.warn(getClass().getSimpleName() + ": can't find product with itemId: " + ing.getId() + " in list: " + list.getListId());
					}
				}
			}
		}
	}
	
	private final boolean verifyIngredient(Ingredient ing)
	{
		switch (ing.getId())
		{
			case PC_BANG_POINTS:
			case CLAN_REPUTATION:
			case FAME:
				return true;
			default:
				if (ing.getTemplate() != null)
				{
					return true;
				}
		}
		
		return false;
	}
	
	public static MultiSell getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MultiSell _instance = new MultiSell();
	}
}
