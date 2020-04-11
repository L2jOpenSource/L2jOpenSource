/*
 * Copyright (C) 2004-2013 L2J Server
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
package ai.group_template.extra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public final class LuckyPig extends AbstractNpcAI
{
	// Lucky Pig IDs
	private final int Lucky_Pig = 2501;
	private final int Wingless_Lucky_Pig = 2502;
	private final int Golden_Wingless_Lucky_Pig = 2503;
	// Misc
	private final Map<Integer, List<Long>> Adena = new ConcurrentHashMap<>();
	// Lucky Pig Spawn Chances %
	private final float Lucky_Pig_Level_52_Spawn_Chance = 0.3f;
	private final float Lucky_Pig_Level_70_Spawn_Chance = 0.3f;
	private final float Lucky_Pig_Level_80_Spawn_Chance = 0.3f;
	private final int despawnTime = 10; // in minutes
	
	//@formatter:off
	// Monsters IDs
	private final int Lucky_Pig_Level_52[] =
	{
		// Enchanted Valley
		20589, 20590, 20591, 20592, 20593, 20594, 20595, 20596,
		20597, 20598, 20599
	};
	
	private final int Lucky_Pig_Level_70[] =
	{
		// Forest of the Dead
		18119, 21555, 21556, 21547, 21553, 21548, 21557, 21559,
		21560, 21561, 21562, 21563, 21564, 21565, 21566, 21568,
		21567, 21596, 21572, 21573, 21571, 21570, 21574, 21576,
		21599, 21580, 21581, 21579, 21582, 21578, 21586, 21587,
		21583, 21585, 21590, 21593, 21588,
		// Valley of Saints
		21520, 21521, 21524, 21523, 21526, 21529, 21541, 21531,
		21530, 21533, 21532, 21536, 21535, 21537, 21539, 21544
	};
	
	private final int Lucky_Pig_Level_80[] =
	{
		// Beast Farm
		18873, 18880, 18887, 18894, 18906, 18907, 18874, 18875,
		18876, 18877, 18878, 18879, 18881, 18882, 18883, 18884,
		18885, 18886, 18888, 18889, 18890, 18891, 18892, 18893,
		18895, 18896, 18897, 18898, 18899, 18900,
		// Plains of the Lizardmen
		22768, 22769, 22773, 22772, 22771, 22770, 22774,
		// Sel Mahum Training Grounds
		18908, 22780, 22782, 22784, 22781, 22783, 22785, 22776,
		22786, 22787, 22788, 22775, 22777, 22778,
		// Fields of Silence & Fields of Whispers
		22651, 22654, 22650, 22655, 22652, 22658, 22659,
		// Crypts of Disgrace
		22704, 22703, 22705,
		// Den of Evil
		22701, 22691, 22698, 22695, 22694, 22696, 22692, 22693,
		22699, 22698, 22697, 18807, 22702,
		// Primeval Island
		22196, 22197, 22198, 22218, 22223, 22203, 22204, 22205,
		22220, 22225, 22743, 22745, 22200, 22201, 22202, 22219,
		22224, 22742, 22744, 22199, 22212, 22213, 22222, 22211,
		22227, 22208, 22209, 22210, 22221, 22226, 22214,
		// Dragon Valley
		22815, 22822, 22823, 22824, 22862, 22818, 22819, 22860,
		22829, 22858, 22830, 22828, 22827, 22826, 22861, 22825
	};
	
	// Lucky Pig Droplist items IDs
	private final int Wingless_Lucky_Pig_Level_52_Drop_Id = 8755;
	private final int Wingless_Lucky_Pig_Level_70_Drop_Id[] =
	{
		5577, 5578,5579
	};
	
	private final int Wingless_Lucky_Pig_Level_80_Drop_Id[] =
	{
		9552, 9553, 9554, 9555, 9556, 9557
	};
	//@formatter:on
	
	public LuckyPig()
	{
		super(LuckyPig.class.getSimpleName(), "ai/group_template/extra");
		addKillId(Wingless_Lucky_Pig, Golden_Wingless_Lucky_Pig);
		addKillId(Lucky_Pig_Level_52);
		addKillId(Lucky_Pig_Level_70);
		addKillId(Lucky_Pig_Level_80);
		addSpawnId(Lucky_Pig, Wingless_Lucky_Pig, Golden_Wingless_Lucky_Pig);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("checkForAdena"))
		{
			for (L2Object object : L2World.getInstance().getVisibleObjects(npc, 600))
			{
				if (!object.isItem())
				{
					continue;
				}
				
				final L2ItemInstance item = (L2ItemInstance) object;
				if (item.getId() == Inventory.ADENA_ID)
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(item.getX(), item.getY(), item.getZ(), 0));
					L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					L2World.getInstance().removeObject(item);
					
					startQuestTimer("startTalking", 500, npc, null);
					
					if (Adena.containsKey(npc.getObjectId()))
					{
						Adena.get(npc.getObjectId()).add(item.getCount());
						
						if (npc.getVariables().getInt("feedCount", 0) == 0)
						{
							npc.getVariables().set("feedCount", Rnd.get(3, 10));
						}
						
						if (Adena.get(npc.getObjectId()).size() > npc.getVariables().getInt("feedCount", 0))
						{
							long adenaCount = 0;
							for (long adena : Adena.get(npc.getObjectId()))
							{
								adenaCount += adena;
							}
							
							if ((adenaCount > 0) && (adenaCount < 50000000))
							{
								L2Npc pig = null;
								int luckyLvl = npc.getVariables().getInt("isLucky", 0);
								switch (luckyLvl)
								{
									case 52:
									case 70:
									case 80:
										pig = addSpawn(Wingless_Lucky_Pig, npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ(), despawnTime * 60 * 1000, (byte) luckyLvl);
										pig.getVariables().set("isLucky", luckyLvl);
										break;
								}
								npc.deleteMe();
							}
							else if (adenaCount >= 50000000)
							{
								L2Npc pig = null;
								int luckyLvl = npc.getVariables().getInt("isLucky", 0);
								switch (luckyLvl)
								{
									case 52:
									case 70:
									case 80:
										pig = addSpawn(Golden_Wingless_Lucky_Pig, npc.getLocation().getX(), npc.getLocation().getY(), npc.getLocation().getZ(), despawnTime * 60 * 1000, (byte) luckyLvl);
										pig.getVariables().set("isLucky", luckyLvl);
										break;
								}
								npc.deleteMe();
							}
							
							cancelQuestTimer("checkForAdena", npc, null);
							break;
						}
					}
				}
			}
		}
		else if (event.equals("startTalking"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), getRandomBoolean() ? "Yum-yum, yum-yum" : "I'm still hungry~"));
			cancelQuestTimer("startTalking", npc, null);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (Util.contains(Lucky_Pig_Level_52, npc.getId()) && (Rnd.get(1000) < (Lucky_Pig_Level_52_Spawn_Chance * 10)))
		{
			L2Npc pig = addSpawn(Lucky_Pig, npc.getX() + 50, npc.getY() + 50, npc.getZ(), npc.getHeading(), true, despawnTime * 60 * 1000, true);
			pig.getVariables().set("isLucky", 52);
		}
		else if (Util.contains(Lucky_Pig_Level_70, npc.getId()) && (Rnd.get(1000) < (Lucky_Pig_Level_70_Spawn_Chance * 10)))
		{
			L2Npc pig = addSpawn(Lucky_Pig, npc.getX() + 50, npc.getY() + 50, npc.getZ(), npc.getHeading(), true, despawnTime * 60 * 1000, true);
			pig.getVariables().set("isLucky", 70);
		}
		else if (Util.contains(Lucky_Pig_Level_80, npc.getId()) && (Rnd.get(1000) < (Lucky_Pig_Level_80_Spawn_Chance * 10)))
		{
			L2Npc pig = addSpawn(Lucky_Pig, npc.getX() + 50, npc.getY() + 50, npc.getZ(), npc.getHeading(), true, despawnTime * 60 * 1000, true);
			pig.getVariables().set("isLucky", 80);
		}
		
		switch (npc.getId())
		{
			case Wingless_Lucky_Pig:
				if (Rnd.get(1000) < 500)
				{
					Random rnd = new Random();
					int randomQuantity = 0;
					int randomDrop = 0;
					switch (npc.getVariables().getInt("isLucky", 0))
					{
						case 52:
							randomQuantity = getRandom(2);
							npc.dropItem(player, Wingless_Lucky_Pig_Level_52_Drop_Id, randomQuantity);
							break;
						case 70:
							randomDrop = rnd.nextInt(Wingless_Lucky_Pig_Level_70_Drop_Id.length);
							randomQuantity = getRandom(2);
							npc.dropItem(player, Wingless_Lucky_Pig_Level_70_Drop_Id[randomDrop], randomQuantity);
							break;
						case 80:
							randomDrop = rnd.nextInt(Wingless_Lucky_Pig_Level_80_Drop_Id.length);
							randomQuantity = getRandom(2);
							npc.dropItem(player, Wingless_Lucky_Pig_Level_80_Drop_Id[randomDrop], randomQuantity);
							break;
					}
				}
				break;
			case Golden_Wingless_Lucky_Pig:
			{
				if (Rnd.get(1000) < 700)
				{
					switch (npc.getVariables().getInt("isLucky", 0))
					{
						case 52:
							npc.dropItem(player, 14678, 1);
							break;
						case 70:
							npc.dropItem(player, 14679, 1);
							break;
						case 80:
							npc.dropItem(player, 14680, 1);
							break;
					}
				}
				break;
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			// we will force lucky pigs to stop attack
			case Wingless_Lucky_Pig:
			case Golden_Wingless_Lucky_Pig:
				npc.disableCoreAI(true);
				npc.setIsImmobilized(true);
				break;
			case Lucky_Pig:
				Adena.put(npc.getObjectId(), new ArrayList<>());
				// Feed check must be made every 10 seconds.
				startQuestTimer("checkForAdena", 10000, npc, null, true);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "Now it's time to eat~"));
				break;
		}
		
		return super.onSpawn(npc);
	}
	
	private static L2Npc addSpawn(int npcId, int x, int y, int z, long despawnDelay, byte level)
	{
		try
		{
			if ((x == 0) && (y == 0))
			{
				return null;
			}
			
			int offset = Rnd.get(50, 100);
			if (Rnd.nextBoolean())
			{
				offset *= -1;
			}
			x += offset;
			
			offset = Rnd.get(50, 100);
			if (Rnd.nextBoolean())
			{
				offset *= -1;
			}
			y += offset;
			
			final L2Spawn spawn = new L2Spawn(npcId);
			spawn.setInstanceId(0);
			spawn.setHeading(0);
			spawn.setX(x);
			spawn.setY(y);
			spawn.setZ(z);
			spawn.stopRespawn();
			
			final L2Npc npc = spawn.spawnOne(true);
			npc.getTemplate().setLevel(level);
			if (despawnDelay > 0)
			{
				npc.scheduleDespawn(despawnDelay);
			}
			
			return npc;
		}
		catch (Exception e)
		{
		
		}
		
		return null;
	}
}