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
package l2r.gameserver.script.faenor;

import java.util.List;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.Announcements;
import l2r.gameserver.datatables.EventDroplist;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.model.L2DropCategory;
import l2r.gameserver.model.L2DropData;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.script.DateRange;
import l2r.gameserver.script.EngineInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{
	protected static final Logger _log = LoggerFactory.getLogger(FaenorInterface.class);
	
	public static FaenorInterface getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public List<?> getAllPlayers()
	{
		return null;
	}
	
	/**
	 * Adds a new Quest Drop to an NPC
	 */
	@Override
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcID);
		if (npc == null)
		{
			throw new NullPointerException();
		}
		L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		drop.setQuestID(questID);
		drop.addStates(states);
		addDrop(npc, drop, false);
	}
	
	/**
	 * Adds a new Drop to an NPC
	 * @param npcID
	 * @param itemID
	 * @param min
	 * @param max
	 * @param sweep
	 * @param chance
	 * @throws NullPointerException
	 */
	public void addDrop(int npcID, int itemID, int min, int max, boolean sweep, int chance) throws NullPointerException
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcID);
		if (npc == null)
		{
			if (Config.DEBUG)
			{
				_log.warn("Npc doesnt Exist");
			}
			throw new NullPointerException();
		}
		L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		
		addDrop(npc, drop, sweep);
	}
	
	/**
	 * Adds a new drop to an NPC. If the drop is sweep, it adds it to the NPC's Sweep category If the drop is non-sweep, it creates a new category for this drop.
	 * @param npc
	 * @param drop
	 * @param sweep
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
	{
		if (sweep)
		{
			addDrop(npc, drop, -1);
		}
		else
		{
			int maxCategory = -1;
			
			for (L2DropCategory cat : npc.getDropData())
			{
				if (maxCategory < cat.getCategoryType())
				{
					maxCategory = cat.getCategoryType();
				}
			}
			maxCategory++;
			npc.addDropData(drop, maxCategory);
		}
	}
	
	/**
	 * Adds a new drop to an NPC, in the specified category. If the category does not exist, it is created.
	 * @param npc
	 * @param drop
	 * @param category
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
	{
		npc.addDropData(drop, category);
	}
	
	public List<L2DropData> getQuestDrops(int npcID)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcID);
		if (npc == null)
		{
			return null;
		}
		List<L2DropData> questDrops = new FastList<>();
		for (L2DropCategory cat : npc.getDropData())
		{
			for (L2DropData drop : cat.getAllDrops())
			{
				if (drop.getQuestID() != null)
				{
					questDrops.add(drop);
				}
			}
		}
		return questDrops;
	}
	
	@Override
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int) (chance * L2DropData.MAX_CHANCE), range);
	}
	
	@Override
	public void onPlayerLogin(String[] message, DateRange validDateRange)
	{
		Announcements.getInstance().addEventAnnouncement(validDateRange, message);
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorInterface _instance = new FaenorInterface();
	}
}
