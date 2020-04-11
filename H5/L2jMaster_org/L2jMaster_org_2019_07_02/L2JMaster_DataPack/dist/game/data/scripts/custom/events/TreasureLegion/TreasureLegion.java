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
package custom.events.TreasureLegion;

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
 * @author WhiteDev
 */
public class TreasureLegion extends AbstractNpcAI implements IVoicedCommandHandler
{
	private static final String[] _TREASURE_COMMAND =
	{
		"treasure",
		"treasureF"
	};
	
	private static final int CHEST = 1020;
	private static final int[] GUARDS =
	{
		1021,
		1022,
		1023
	};
	private static final int _chestAmount = 3;
	private final int _guardsAmount = 9;
	private int _chestSpawned = _chestAmount;
	
	private final Map<Integer, List<ItemChanceHolder>> DROPS = new HashMap<>();
	private final Map<L2Npc, ArrayList<L2Npc>> _chestGroup = new ConcurrentHashMap<>();
	private ArrayList<L2Npc> _guardsNpc;
	
	private final ArrayList<Integer> _locUsed = new ArrayList<>();
	
	private static final int[][] LOCATIONS =
	{
		{
			// Orc Barracks
			-87500,
			100229,
			-3551
		},
		{
			// Wasteland
			-12846,
			191226,
			-4213
		},
		{
			// Forest Of The Dead
			49784,
			-36122,
			-1592
		},
		{
			// Blazing Swamp
			152474,
			-11219,
			-4495
		},
		{
			// Plunderous Plains
			109110,
			-151167,
			-2233
		},
		{
			// The Forest of Mirrors
			161241,
			72781,
			-3520
		},
		{
			// Primeval Isle
			25470,
			-10550,
			-2475
		},
		{
			// Cruma Tower
			24893,
			113818,
			-3717
		},
		{
			// Wall of Argos
			169002,
			-39218,
			-3543
		},
		{
			// Swamp of Screams
			86500,
			-49584,
			-3084
		},
		{
			// Forest of Evil
			95143,
			10419,
			-3651
		},
		{
			// Alligator Island
			116589,
			182379,
			-3772
		},
		{
			// Spine Mountains
			162666,
			-183233,
			-783
		}
	};
	
	private static final String[] LOCATIONS_NAMES =
	{
		"Orc Barracks",
		"Wasteland",
		"Forest Of The Dead",
		"Blazing Swamp",
		"Plunderous Plains",
		"The Forest of Mirrors",
		"Primeval Isle",
		"Cruma Tower",
		"Wall of Argos",
		"Swamp of Screams",
		"Forest of Evil",
		"Alligator Island",
		"Spine Mountains"
	};
	
	{
		DROPS.put(CHEST, Arrays.asList( // Treasure Chest
			new ItemChanceHolder(736, 2703, 7), // Scroll of Escape
			new ItemChanceHolder(1061, 2365, 4), // Major Healing Potion
			new ItemChanceHolder(737, 3784, 4), // Scroll of Resurrection
			new ItemChanceHolder(10268, 1136, 1), // Wind Walk Juice
			new ItemChanceHolder(5593, 2365, 6), // SP Scroll (Low-grade)
			new ItemChanceHolder(5594, 1136, 1), // SP Scroll (Mid-grade)
			new ItemChanceHolder(10131, 4919, 1), // Transformation Scroll: Onyx Beast
			new ItemChanceHolder(10132, 4919, 1), // Transformation Scroll: Doom Wraith
			new ItemChanceHolder(10133, 4919, 1), // Transformation Scroll: Grail Apostle
			new ItemChanceHolder(1538, 3279, 1), // Blessed Scroll of Escape
			new ItemChanceHolder(3936, 1230, 1), // Blessed Scroll of Resurrection
			new ItemChanceHolder(21749, 320, 1))); // Novice Adventurer's Treasure Sack
	}
	
	public TreasureLegion()
	{
		super(TreasureLegion.class.getSimpleName(), "events/TreasureLegion");
		addAttackId(DROPS.keySet());
		addKillId(GUARDS);
		addKillId(CHEST);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "SPAWN":
				int chestAmount = _chestAmount;
				int guardsAmount = _guardsAmount;
				List<Integer> mobSpawnForChest = new ArrayList<>(); // spawn for each chest
				
				_chestGroup.clear();
				
				while (chestAmount > 0)
				{
					_guardsNpc = new ArrayList<>();
					chestAmount--;
					while (guardsAmount > 0)
					{
						guardsAmount--;
						try
						{
							mobSpawnForChest.add(Rnd.get(1021, 1023));
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
						
						L2Npc chest = new L2Npc(CHEST);
						
						for (Integer g : mobSpawnForChest)
						{
							L2Npc guard = addSpawn(g, new Location(LOCATIONS[loc][0] + Rnd.get(150), LOCATIONS[loc][1] + Rnd.get(150), LOCATIONS[loc][2]), false, (30 * 60 * 1000), true, 0);
							_guardsNpc.add(guard);
						}
						_chestGroup.put(chest, _guardsNpc);
					}
					catch (SecurityException | ClassCastException e)
					{
						e.printStackTrace();
					}
					mobSpawnForChest.clear();
					guardsAmount = _guardsAmount;
				}
				startQuestTimer("ANNOUNCE", 1000, npc, player);
				break;
			case "ANNOUNCE":
				for (int index : _locUsed)
				{
					Broadcast.toAllOnlinePlayers("Run! Dark Legion spawn on " + LOCATIONS_NAMES[index] + " kill them & discover their treasure");
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
		if (npc.getId() == CHEST)
		{
			int locIndex = 0;
			for (int loc : _locUsed)
			{
				if (npc.isInsideRadius(LOCATIONS[loc][0], LOCATIONS[loc][1], LOCATIONS[loc][2], 1000, false, false))
				{
					locIndex = loc;
					break;
				}
			}
			Broadcast.toAllOnlinePlayers("Treasure discovered in " + LOCATIONS_NAMES[locIndex]);
		}
		else if ((npc.getId() == GUARDS[0]) || (npc.getId() == GUARDS[1]) || (npc.getId() == GUARDS[2]))
		{
			if (_chestGroup != null)
			{
				for (L2Npc c : _chestGroup.keySet())
				{
					if (_chestGroup.get(c).contains(npc))
					{
						Location loc = npc.getLocation();
						_chestGroup.get(c).remove(npc);
						if (_chestGroup.get(c).isEmpty())
						{
							_chestSpawned--;
							addSpawn(CHEST, loc, false, 20000, true, 0);
							if (_chestSpawned == 0)
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
		_chestSpawned = _chestAmount;
		_chestGroup.clear();
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
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		npc.doDie(attacker);
		final List<ItemChanceHolder> items = DROPS.get(npc.getId());
		for (ItemChanceHolder item : items)
		{
			if (getRandom(10000) < item.getChance())
			{
				npc.dropItem(attacker, item.getId(), item.getCount());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("treasure") && activeChar.isGM())
		{
			onAdvEvent("SPAWN", null, activeChar);
			return true;
		}
		if (command.equalsIgnoreCase("treasureF") && activeChar.isGM())
		{
			if (!_chestGroup.isEmpty())
			{
				for (L2Npc c : _chestGroup.keySet())
				{
					for (L2Npc g : _chestGroup.get(c))
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
		return _TREASURE_COMMAND;
	}
	
	public static TreasureLegion getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TreasureLegion INSTANCE = new TreasureLegion();
	}
}