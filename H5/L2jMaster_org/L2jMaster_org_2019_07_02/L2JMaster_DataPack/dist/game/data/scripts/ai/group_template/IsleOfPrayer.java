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

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemChanceHolder;

import ai.npc.AbstractNpcAI;

/**
 * Isle of Prayer AI.
 * @author Zoey76, Zephyr
 */
public final class IsleOfPrayer extends AbstractNpcAI
{
	// Items
	private static final int YELLOW_SEED_OF_EVIL_SHARD = 9593;
	private static final int GREEN_SEED_OF_EVIL_SHARD = 9594;
	private static final int BLUE_SEED_OF_EVIL_SHARD = 9595;
	private static final int RED_SEED_OF_EVIL_SHARD = 9596;
	private static final int SPIRIT_OF_LAKE = 9689;
	// Monsters
	private static final Map<Integer, ItemChanceHolder> MONSTERS_DROP = new HashMap<>();
	static
	{
		MONSTERS_DROP.put(22257, new ItemChanceHolder(YELLOW_SEED_OF_EVIL_SHARD, 2087)); // Island Guardian
		MONSTERS_DROP.put(22258, new ItemChanceHolder(YELLOW_SEED_OF_EVIL_SHARD, 2147)); // White Sand Mirage
		MONSTERS_DROP.put(22259, new ItemChanceHolder(YELLOW_SEED_OF_EVIL_SHARD, 2642)); // Muddy Coral
		MONSTERS_DROP.put(22260, new ItemChanceHolder(YELLOW_SEED_OF_EVIL_SHARD, 2292)); // Kleopora
		MONSTERS_DROP.put(22261, new ItemChanceHolder(GREEN_SEED_OF_EVIL_SHARD, 1171)); // Seychelles
		MONSTERS_DROP.put(22262, new ItemChanceHolder(GREEN_SEED_OF_EVIL_SHARD, 1173)); // Naiad
		MONSTERS_DROP.put(22263, new ItemChanceHolder(GREEN_SEED_OF_EVIL_SHARD, 1403)); // Sonneratia
		MONSTERS_DROP.put(22264, new ItemChanceHolder(GREEN_SEED_OF_EVIL_SHARD, 1207)); // Castalia
		MONSTERS_DROP.put(22265, new ItemChanceHolder(RED_SEED_OF_EVIL_SHARD, 575)); // Chrysocolla
		MONSTERS_DROP.put(22266, new ItemChanceHolder(RED_SEED_OF_EVIL_SHARD, 493)); // Pythia
		MONSTERS_DROP.put(22267, new ItemChanceHolder(RED_SEED_OF_EVIL_SHARD, 770)); // Dark Water Dragon
		MONSTERS_DROP.put(22268, new ItemChanceHolder(BLUE_SEED_OF_EVIL_SHARD, 987)); // Shade
		MONSTERS_DROP.put(22269, new ItemChanceHolder(BLUE_SEED_OF_EVIL_SHARD, 995)); // Shade
		MONSTERS_DROP.put(22270, new ItemChanceHolder(BLUE_SEED_OF_EVIL_SHARD, 1008)); // Water Dragon Detractor
		MONSTERS_DROP.put(22271, new ItemChanceHolder(BLUE_SEED_OF_EVIL_SHARD, 1008)); // Water Dragon Detractor
		MONSTERS_DROP.put(22270, new ItemChanceHolder(SPIRIT_OF_LAKE, 10000)); // Water Dragon Detractor
		MONSTERS_DROP.put(22271, new ItemChanceHolder(SPIRIT_OF_LAKE, 10000)); // Water Dragon Detractor
	}
	
	private static final int[] MONSTERS_SILHOUETTE =
	{
		22257, // Island Guardian
		22258, // White Sand Mirage
		22259, // Muddy Coral
		22260, // Kleopora
		22261, // Seychelles
		22262, // Naiad
		22263, // Sonneratia
		22264, // Castalia
		22265, // Chrysocolla
		22266, // Pythia
	};
	
	private IsleOfPrayer()
	{
		super(IsleOfPrayer.class.getSimpleName(), "ai/group_template");
		addKillId(MONSTERS_DROP.keySet());
		addAttackId(MONSTERS_SILHOUETTE);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final ItemChanceHolder holder = MONSTERS_DROP.get(npc.getId());
		if (getRandom(10000) <= holder.getChance())
		{
			npc.dropItem(killer, holder);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((attacker.getParty() != null) && (attacker.getParty().getMembers().size() > 2) && !npc.getVariables().getBoolean("silhouetteSpawned", false))
		{
			int silhouetteId = npc.getTemplate().getParameters().getInt("silhouette");
			spawnSilhouettes(attacker, npc, silhouetteId, silhouetteId);
			npc.getVariables().set("silhouetteSpawned", true);
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	private void spawnSilhouettes(L2PcInstance attacker, L2Npc npc, int... silhouetteIds)
	{
		for (int silhouetteId : silhouetteIds)
		{
			L2Attackable silhouette = (L2Attackable) addSpawn(silhouetteId, npc.getLocation(), true, 0);
			silhouette.setRunning();
			silhouette.addDamageHate(attacker, 0, 999);
			silhouette.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}
	
	public static void main(String[] args)
	{
		new IsleOfPrayer();
	}
}