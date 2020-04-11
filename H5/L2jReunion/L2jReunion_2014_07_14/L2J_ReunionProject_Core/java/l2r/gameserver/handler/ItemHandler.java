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
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.items.L2EtcItem;
import l2r.gameserver.scripts.handlers.itemhandlers.AioItemBuff;
import l2r.gameserver.scripts.handlers.itemhandlers.AioItemNpcs;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSoulShot;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSpice;
import l2r.gameserver.scripts.handlers.itemhandlers.BeastSpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.BlessedSpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.Book;
import l2r.gameserver.scripts.handlers.itemhandlers.Bypass;
import l2r.gameserver.scripts.handlers.itemhandlers.Calculator;
import l2r.gameserver.scripts.handlers.itemhandlers.ChristmasTree;
import l2r.gameserver.scripts.handlers.itemhandlers.Disguise;
import l2r.gameserver.scripts.handlers.itemhandlers.Elixir;
import l2r.gameserver.scripts.handlers.itemhandlers.EnchantAttribute;
import l2r.gameserver.scripts.handlers.itemhandlers.EnchantScrolls;
import l2r.gameserver.scripts.handlers.itemhandlers.EnergyStarStone;
import l2r.gameserver.scripts.handlers.itemhandlers.EventItem;
import l2r.gameserver.scripts.handlers.itemhandlers.ExtractableItems;
import l2r.gameserver.scripts.handlers.itemhandlers.FishShots;
import l2r.gameserver.scripts.handlers.itemhandlers.Harvester;
import l2r.gameserver.scripts.handlers.itemhandlers.ItemSkills;
import l2r.gameserver.scripts.handlers.itemhandlers.ItemSkillsTemplate;
import l2r.gameserver.scripts.handlers.itemhandlers.ManaPotion;
import l2r.gameserver.scripts.handlers.itemhandlers.Maps;
import l2r.gameserver.scripts.handlers.itemhandlers.MercTicket;
import l2r.gameserver.scripts.handlers.itemhandlers.NicknameColor;
import l2r.gameserver.scripts.handlers.itemhandlers.PaganKeys;
import l2r.gameserver.scripts.handlers.itemhandlers.PetFood;
import l2r.gameserver.scripts.handlers.itemhandlers.QuestItems;
import l2r.gameserver.scripts.handlers.itemhandlers.Recipes;
import l2r.gameserver.scripts.handlers.itemhandlers.RollingDice;
import l2r.gameserver.scripts.handlers.itemhandlers.ScrollOfResurrection;
import l2r.gameserver.scripts.handlers.itemhandlers.Seed;
import l2r.gameserver.scripts.handlers.itemhandlers.SevenSignsRecord;
import l2r.gameserver.scripts.handlers.itemhandlers.SoulShots;
import l2r.gameserver.scripts.handlers.itemhandlers.SpecialXMas;
import l2r.gameserver.scripts.handlers.itemhandlers.SpiritShot;
import l2r.gameserver.scripts.handlers.itemhandlers.SummonItems;
import l2r.gameserver.scripts.handlers.itemhandlers.TeleportBookmark;
import gr.reunion.configsEngine.AioBufferConfigs;
import gr.reunion.configsEngine.AioItemsConfigs;

/**
 * This class manages handlers of items
 * @author UnAfraid
 */
public class ItemHandler implements IHandler<IItemHandler, L2EtcItem>
{
	private final Map<String, IItemHandler> _datatable;
	
	/**
	 * Constructor of ItemHandler
	 */
	protected ItemHandler()
	{
		_datatable = new HashMap<>();
		
		if (AioBufferConfigs.ENABLE_AIO_BUFFER)
		{
			registerHandler(new AioItemBuff());
		}
		if (AioItemsConfigs.ENABLE_AIO_NPCS)
		{
			registerHandler(new AioItemNpcs());
		}
		registerHandler(new BeastSoulShot());
		registerHandler(new BeastSpice());
		registerHandler(new BeastSpiritShot());
		registerHandler(new BlessedSpiritShot());
		registerHandler(new Book());
		registerHandler(new Bypass());
		registerHandler(new Calculator());
		registerHandler(new ChristmasTree());
		registerHandler(new Disguise());
		registerHandler(new Elixir());
		registerHandler(new EnchantAttribute());
		registerHandler(new EnchantScrolls());
		registerHandler(new EnergyStarStone());
		registerHandler(new EventItem());
		registerHandler(new ExtractableItems());
		registerHandler(new FishShots());
		registerHandler(new Harvester());
		registerHandler(new ItemSkills());
		registerHandler(new ItemSkillsTemplate());
		registerHandler(new ManaPotion());
		registerHandler(new Maps());
		registerHandler(new MercTicket());
		registerHandler(new NicknameColor());
		registerHandler(new PaganKeys());
		registerHandler(new PetFood());
		registerHandler(new QuestItems());
		registerHandler(new Recipes());
		registerHandler(new RollingDice());
		registerHandler(new ScrollOfResurrection());
		registerHandler(new Seed());
		registerHandler(new SevenSignsRecord());
		registerHandler(new SoulShots());
		registerHandler(new SpecialXMas());
		registerHandler(new SpiritShot());
		registerHandler(new SummonItems());
		registerHandler(new TeleportBookmark());
	}
	
	/**
	 * Adds handler of item type in <I>datatable</I>.<BR>
	 * <BR>
	 * <B><I>Concept :</I></U><BR>
	 * This handler is put in <I>datatable</I> Map &lt;String ; IItemHandler &gt; for each ID corresponding to an item type (existing in classes of package itemhandlers) sets as key of the Map.
	 * @param handler (IItemHandler)
	 */
	@Override
	public void registerHandler(IItemHandler handler)
	{
		_datatable.put(handler.getClass().getSimpleName(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IItemHandler handler)
	{
		_datatable.remove(handler.getClass().getSimpleName());
	}
	
	/**
	 * Returns the handler of the item
	 * @param item
	 * @return IItemHandler
	 */
	@Override
	public IItemHandler getHandler(L2EtcItem item)
	{
		if ((item == null) || (item.getHandlerName() == null))
		{
			return null;
		}
		return _datatable.get(item.getHandlerName());
	}
	
	/**
	 * Returns the number of elements contained in datatable
	 * @return int : Size of the datatable
	 */
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}
