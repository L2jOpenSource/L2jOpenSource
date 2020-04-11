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
package ai.group_template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.NpcSay;

import ai.npc.AbstractNpcAI;

/**
 * Lucky Pig AI.
 * @author Zephyr, Sacrifice
 * @since 2.6.0.0
 */
public final class LuckyPig extends AbstractNpcAI
{
	// Lucky Pig Id's
	private static final int LUCKY_PIG_52 = 18664;
	private static final int LUCKY_PIG_70 = 18665;
	private static final int LUCKY_PIG_80 = 2501;
	private static final int GOLDEN_WINGLESS_LUCKY_PIG = 2503;
	private static final int WINGLESS_LUCKY_PIG = 2502;
	
	// Wingless Lucky Pig Drop Chance
	private static final int WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE = 50;
	private static final int WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE = 50;
	private static final int WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE = 50;
	
	// Golden Wingless Lucky Pig Drop Chance
	private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE = 70;
	private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE = 70;
	private static final int GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE = 70;
	
	// Adena required for Golden Wingless Lucky Pig
	private static final long GOLDEN_WINGLESS_LUCKY_PIG_ADENA_REQUIRED = 50000000;
	
	// Ranges
	private static final int CHECK_ADENA_RANGE = 200;
	private static final int MESSAGES_RANGE = 2000;
	
	// Timer values
	private static final int LUCKY_PIG_REFRESH_SPAWN = 600000; // 10 minutes}
	
	private static final NpcStringId FULL_MESSAGE = NpcStringId.OH_MY_WINGS_DISAPPEARED_ARE_YOU_GONNA_HIT_ME_IF_YOU_HIT_ME_ILL_THROW_UP_EVERYTHING_THAT_I_ATE;
	
	private static final NpcStringId SPAWN_MESSAGE = NpcStringId.NOW_ITS_TIME_TO_EAT;
	
	private static final NpcStringId[] PIG_ACTIVE_MESSAGES =
	{
		NpcStringId.IM_STILL_NOT_FULL,
		NpcStringId.IM_STILL_HUNGRY,
		NpcStringId.I_ALSO_NEED_A_DESSERT,
		NpcStringId.YUM_YUM_YUM_YUM
	};
	
	private static final NpcStringId[] PIG_STANDBY_MESSAGES =
	{
		NpcStringId.MY_STOMACH_IS_EMPTY,
		NpcStringId.IM_HUNGRY_IM_HUNGRY,
		NpcStringId.I_FEEL_A_LITTLE_WOOZY,
		NpcStringId.GIVE_ME_SOMETHING_TO_EAT,
		NpcStringId.I_HAVENT_EATEN_ANYTHING_IM_SO_WEAK
	};
	
	// Lucky Pig Droplist items Id's
	// @formatter:off
	private static final int[] DROP_GOLDEN_WINGLESS_LUCKY_PIG_52 = {14678}; // Neolithic Crystal - B
	private static final int[] DROP_GOLDEN_WINGLESS_LUCKY_PIG_70 = {14679}; // Neolithic Crystal - A
	private static final int[] DROP_GOLDEN_WINGLESS_LUCKY_PIG_80 = {14680}; // Neolithic Crystal - S
	
	// @formatter:off
	private static final int[] DROP_WINGLESS_LUCKY_PIG_52 = {8755}; // Top-Grade Life Stone - Level 52
	//@formatter:on
	
	private static final int[] DROP_WINGLESS_LUCKY_PIG_70 =
	{
		5577, // Red Soul Crystal - Stage 11
		5578, // Green Soul Crystal - Stage 11
		5579 // Blue Soul Crystal - Stage 11
	};
	
	private static final int[] DROP_WINGLESS_LUCKY_PIG_80 =
	{
		9552, // Fire Crystal
		9553, // Water Crystal
		9554, // Earth Crystal
		9555, // Wind Crystal
		9556, // Dark Crystal
		9557 // Holy Crystal
	};
	
	// Adena deposited
	private final Map<Integer, List<Long>> _adenaDeposited = new ConcurrentHashMap<>();
	
	// Wingless Lucky Pig spawned
	private final Map<Integer, Integer> _winglessLuckyPigSpawned = new ConcurrentHashMap<>();
	
	private LuckyPig()
	{
		super(LuckyPig.class.getSimpleName(), "ai/group_template");
		addSpawnId(LUCKY_PIG_52, LUCKY_PIG_70, LUCKY_PIG_80);
		addKillId(WINGLESS_LUCKY_PIG, GOLDEN_WINGLESS_LUCKY_PIG);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "standByMessage":
				npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, PIG_STANDBY_MESSAGES[getRandom(PIG_STANDBY_MESSAGES.length - 1)]), MESSAGES_RANGE);
				break;
			case "checkForAdena":
				onCheckForAdena(npc);
				break;
			case "despawnLuckyPig":
				onDespawnLuckyPig(npc);
				break;
			case "despawnWinglessLuckyPig":
				onDespawnWinglessLuckyPig(npc);
				break;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!_winglessLuckyPigSpawned.containsKey(npc.getObjectId()))
		{
			return super.onKill(npc, player, isPet);
		}
		
		final int level = _winglessLuckyPigSpawned.remove(npc.getObjectId());
		final int[] drop;
		final int dropChance;
		final boolean isGolden = npc.getId() == GOLDEN_WINGLESS_LUCKY_PIG;
		
		switch (level)
		{
			case 52:
				drop = isGolden ? DROP_GOLDEN_WINGLESS_LUCKY_PIG_52 : DROP_WINGLESS_LUCKY_PIG_52;
				dropChance = isGolden ? GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE : WINGLESS_LUCKY_PIG_LEVEL_52_DROP_CHANCE;
				break;
			case 70:
				drop = isGolden ? DROP_GOLDEN_WINGLESS_LUCKY_PIG_70 : DROP_WINGLESS_LUCKY_PIG_70;
				dropChance = isGolden ? GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE : WINGLESS_LUCKY_PIG_LEVEL_70_DROP_CHANCE;
				break;
			case 80:
				drop = isGolden ? DROP_GOLDEN_WINGLESS_LUCKY_PIG_80 : DROP_WINGLESS_LUCKY_PIG_80;
				dropChance = isGolden ? GOLDEN_WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE : WINGLESS_LUCKY_PIG_LEVEL_80_DROP_CHANCE;
				break;
			default:
				return super.onKill(npc, player, isPet);
		}
		
		if (getRandom(0, 100) <= dropChance)
		{
			npc.dropItem(player, drop[getRandom(drop.length - 1)], isGolden ? 1 : getRandom(1, 2));
		}
		cancelQuestTimer("despawnWinglessLuckyPig", npc, null);
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case LUCKY_PIG_52:
				_adenaDeposited.put(npc.getObjectId(), new ArrayList<>());
				npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, SPAWN_MESSAGE), MESSAGES_RANGE);
				startQuestTimer("standByMessage", 60000, npc, null, true);
				startQuestTimer("checkForAdena", 5000, npc, null, true);
				startQuestTimer("despawnLuckyPig", LUCKY_PIG_REFRESH_SPAWN, npc, null);
				startQuestTimer("spawnLuckyPig52", LUCKY_PIG_REFRESH_SPAWN, null, null);
				break;
			case LUCKY_PIG_70:
				_adenaDeposited.put(npc.getObjectId(), new ArrayList<>());
				npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, SPAWN_MESSAGE), MESSAGES_RANGE);
				startQuestTimer("standByMessage", 60000, npc, null, true);
				startQuestTimer("checkForAdena", 5000, npc, null, true);
				startQuestTimer("despawnLuckyPig", LUCKY_PIG_REFRESH_SPAWN, npc, null);
				startQuestTimer("spawnLuckyPig70", LUCKY_PIG_REFRESH_SPAWN, null, null);
				break;
			case LUCKY_PIG_80:
				_adenaDeposited.put(npc.getObjectId(), new ArrayList<>());
				npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, SPAWN_MESSAGE), MESSAGES_RANGE);
				startQuestTimer("standByMessage", 60000, npc, null, true);
				startQuestTimer("checkForAdena", 5000, npc, null, true);
				startQuestTimer("despawnLuckyPig", LUCKY_PIG_REFRESH_SPAWN, npc, null);
				startQuestTimer("spawnLuckyPig80", LUCKY_PIG_REFRESH_SPAWN, null, null);
				break;
			case WINGLESS_LUCKY_PIG:
			case GOLDEN_WINGLESS_LUCKY_PIG:
				startQuestTimer("despawnWinglessLuckyPig", LUCKY_PIG_REFRESH_SPAWN, npc, null);
				break;
		}
		return super.onSpawn(npc);
	}
	
	private void onCheckForAdena(L2Npc npc)
	{
		for (L2Object object : L2World.getInstance().getVisibleObjects(npc, CHECK_ADENA_RANGE))
		{
			if (!object.isItem() || (object.getId() != Inventory.ADENA_ID))
			{
				continue;
			}
			
			final L2ItemInstance item = (L2ItemInstance) object;
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(item.getX(), item.getY(), item.getZ(), 0));
			L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
			L2World.getInstance().removeObject(item);
			cancelQuestTimer("standByMessage", npc, null);
			
			if (_adenaDeposited.containsKey(npc.getObjectId()))
			{
				_adenaDeposited.get(npc.getObjectId()).add(item.getCount());
				
				final int feedTimes = getRandom(3, 10);
				
				if (_adenaDeposited.get(npc.getObjectId()).size() > feedTimes)
				{
					// Get the total
					long adenaCount = 0;
					
					for (long adena : _adenaDeposited.get(npc.getObjectId()))
					{
						adenaCount += adena;
					}
					
					final int winglessPigId = adenaCount < GOLDEN_WINGLESS_LUCKY_PIG_ADENA_REQUIRED ? WINGLESS_LUCKY_PIG : GOLDEN_WINGLESS_LUCKY_PIG;
					final int x = npc.getX();
					final int y = npc.getY();
					final int z = npc.getZ();
					final int level = npc.getLevel();
					npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, FULL_MESSAGE), MESSAGES_RANGE);
					onDespawnLuckyPig(npc);
					final L2Npc winglessLuckyPig = addSpawn(winglessPigId, x, y, z, 0, true, 0, true);
					_winglessLuckyPigSpawned.put(winglessLuckyPig.getObjectId(), level);
				}
				else
				{
					cancelQuestTimer("despawnLuckyPig", npc, null);
					startQuestTimer("despawnLuckyPig", LUCKY_PIG_REFRESH_SPAWN / 2, npc, null, false);
					npc.broadcastPacket(new NpcSay(npc, Say2.NPC_ALL, PIG_ACTIVE_MESSAGES[getRandom(PIG_ACTIVE_MESSAGES.length - 1)]), MESSAGES_RANGE);
				}
				return;
			}
		}
	}
	
	private void onDespawnLuckyPig(L2Npc npc)
	{
		switch (npc.getId())
		{
			case LUCKY_PIG_52:
			case LUCKY_PIG_70:
			case LUCKY_PIG_80:
				_adenaDeposited.remove(npc.getObjectId());
				npc.deleteMe();
				break;
		}
		cancelQuestTimer("standByMessage", npc, null);
		cancelQuestTimer("checkForAdena", npc, null);
		cancelQuestTimer("despawnLuckyPig", npc, null);
	}
	
	private void onDespawnWinglessLuckyPig(L2Npc npc)
	{
		switch (npc.getId())
		{
			case WINGLESS_LUCKY_PIG:
			case GOLDEN_WINGLESS_LUCKY_PIG:
				_winglessLuckyPigSpawned.remove(npc.getObjectId());
				npc.deleteMe();
				break;
		}
		cancelQuestTimer("despawnWinglessLuckyPig", npc, null);
	}
	
	public static void main(String[] args)
	{
		new LuckyPig();
	}
}