/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.events.LegionSquad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemChanceHolder;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * @author MaGa
 */
public class LegionSquad extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final String[] _LEGION_COMMAND =
	{
		"legion",
		"legionF"
	};
	
	private static final int BUTCHER = 1024;
	private static final int[] GUARDS =
	{
		1025,
		1026,
		1027
	};
	private static final int _butcherAmount = 1;
	private final int _guardsAmount = 9;
	private int _butcherSpawned = _butcherAmount;
	
	private final Map<Integer, List<ItemChanceHolder>> DROPS = new HashMap<>();
	private final Map<L2Npc, ArrayList<L2Npc>> _butcherGroup = new ConcurrentHashMap<>();
	private ArrayList<L2Npc> _guardsNpc;
	
	private final ArrayList<Integer> _locUsed = new ArrayList<>();
	
	private static final int[][] LOCATIONS =
	{
		{
			// White Sands Fortress
			114040,
			199902,
			-3731
		},
		{
			// Southern Fortress
			-14833,
			214471,
			-3697
		},
		{
			// Giran Castle
			106211,
			138221,
			-3512
		},
		{
			// Goddard Castle
			154781,
			-46928,
			-3768
		},
		{
			// Western Fortress
			107653,
			-1398,
			-3132
		},
		{
			// Valley Fortress
			122781,
			120063,
			-3163
		},
		{
			// Auru Fortress
			65521,
			178210,
			-2407
		},
		{
			// Fortress of Resistance
			48355,
			101056,
			-320
		},
		{
			// Rune Castle
			30308,
			-53158,
			-2061
		},
	};
	
	private static final String[] LOCATIONS_NAMES =
	{
		"Near White Sands Fortress",
		"Near Southern Fortress",
		"Near Giran Castle",
		"Near Goddard Castle",
		"Near Western Fortress",
		"Near Valley Fortress",
		"Near Auru Fortress",
		"Near Fortress of Resistance",
		"Near Rune Castle"
	};
	
	{
		DROPS.put(BUTCHER, Arrays.asList( // Butcher
			new ItemChanceHolder(736, 2703, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 2365, 4), // Major Healing Potion
			new ItemChanceHolder(737, 3784, 4), // Scroll of Resurrection
			new ItemChanceHolder(5593, 2365, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1136, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10131, 4919, 1), // Transformation Scroll: Onyx Beast
			new ItemChanceHolder(10132, 4919, 1), // Transformation Scroll: Doom Wraith
			new ItemChanceHolder(10133, 4919, 1), // Transformation Scroll: Grail Apostle
			new ItemChanceHolder(1538, 3279, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1230, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(21749, 320, 1))); // Great Adventurer's Treasure Sack
	}
	
	public LegionSquad()
	{
		super(LegionSquad.class.getSimpleName(), "events/LegionSquad");
		addAttackId(DROPS.keySet());
		addKillId(GUARDS);
		addKillId(BUTCHER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "SPAWN":
				int butcherAmount = _butcherAmount;
				int guardsAmount = _guardsAmount;
				List<Integer> mobSpawnForButcher = new ArrayList<>(); // spawn for each Butcher
				
				_butcherGroup.clear();
				
				while (butcherAmount > 0)
				{
					_guardsNpc = new ArrayList<>();
					butcherAmount--;
					while (guardsAmount > 0)
					{
						guardsAmount--;
						try
						{
							mobSpawnForButcher.add(Rnd.get(1025, 1027));
						}
						catch (SecurityException | ClassCastException e)
						{
							e.printStackTrace();
						}
					}
					guardsAmount = _guardsAmount;
					try
					{
						int loc = Rnd.get(0, LOCATIONS.length - 1);
						
						while (_locUsed.contains(loc))
						{
							loc = Rnd.get(0, LOCATIONS.length - 1);
						}
						_locUsed.add(loc);
						
						L2Npc butcher = new L2Npc(BUTCHER);
						
						for (Integer g : mobSpawnForButcher)
						{
							L2Npc guard = addSpawn(g, new Location(LOCATIONS[loc][0] + Rnd.get(150), LOCATIONS[loc][1] + Rnd.get(150), LOCATIONS[loc][2]), false, (30 * 60 * 1000), true, 0);
							_guardsNpc.add(guard);
						}
						_butcherGroup.put(butcher, _guardsNpc);
					}
					catch (SecurityException | ClassCastException e)
					{
						e.printStackTrace();
					}
					mobSpawnForButcher.clear();
					guardsAmount = _guardsAmount;
				}
				startQuestTimer("ANNOUNCE", 1000, npc, player);
				break;
			case "ANNOUNCE":
				for (int index : _locUsed)
				{
					Broadcast.toAllOnlinePlayers("Confrontation with the Dark Legion reached the scale of a full-blown war!");
					Broadcast.toAllOnlinePlayers("Legion seeks to weaken the enemy, capturing the most important defense facilities throughout the kingdom. Stay alert and to repulse the enemy located in " + LOCATIONS_NAMES[index] + " You have 30 minute(s) to kill them");
				}
				break;
			default:
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public synchronized String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == BUTCHER)
		{
			final List<ItemChanceHolder> items = DROPS.get(npc.getId());
			for (ItemChanceHolder item : items)
			{
				if (getRandom(10000) < item.getChance())
				{
					npc.dropItem(killer, item.getId(), item.getCount());
				}
			}
			Broadcast.toAllOnlinePlayers("Congratulations! The forces of the squadron of the legion were neutralized ");
		}
		else if ((npc.getId() == GUARDS[0]) || (npc.getId() == GUARDS[1]) || (npc.getId() == GUARDS[2]))
		{
			if (_butcherGroup != null)
			{
				for (L2Npc c : _butcherGroup.keySet())
				{
					if (_butcherGroup.get(c).contains(npc))
					{
						Location loc = npc.getLocation();
						_butcherGroup.get(c).remove(npc);
						if (_butcherGroup.get(c).isEmpty())
						{
							_butcherSpawned--;
							addSpawn(BUTCHER, loc, false, 30 * 60 * 1000, true, 0);
							if (_butcherSpawned == 0)
							{
								initialize();
							}
						}
						loc = null;
					}
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private void initialize()
	{
		_butcherSpawned = _butcherAmount;
		_butcherGroup.clear();
		_locUsed.clear();
		if (_guardsNpc != null)
		{
			_guardsNpc.clear();
		}
		try
		{
			this.finalize();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("legion") && activeChar.isGM())
		{
			onAdvEvent("SPAWN", null, activeChar);
			return true;
		}
		if (command.equalsIgnoreCase("legionF") && activeChar.isGM())
		{
			if (!_butcherGroup.isEmpty())
			{
				for (L2Npc c : _butcherGroup.keySet())
				{
					for (L2Npc g : _butcherGroup.get(c))
					{
						g.deleteMe();
					}
					c.deleteMe();
				}
			}
			this.initialize();
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _LEGION_COMMAND;
	}
	
	public static LegionSquad getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LegionSquad INSTANCE = new LegionSquad();
	}
}