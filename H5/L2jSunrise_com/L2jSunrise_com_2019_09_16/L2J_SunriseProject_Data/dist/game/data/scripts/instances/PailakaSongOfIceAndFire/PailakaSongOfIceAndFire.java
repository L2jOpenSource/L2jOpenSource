/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package instances.PailakaSongOfIceAndFire;

import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.util.Rnd;

import instances.AbstractInstance;

/**
 * Pailaka Song of Ice and Fire Instance zone.
 * @author Gnacik, St3eT
 */
public final class PailakaSongOfIceAndFire extends AbstractInstance
{
	protected class PSoIWorld extends InstanceWorld
	{
	
	}
	
	// NPCs
	private static final int ADLER1 = 32497;
	private static final int GARGOS = 18607;
	private static final int BLOOM = 18616;
	private static final int BOTTLE = 32492;
	private static final int BRAZIER = 32493;
	// Items
	private static final int FIRE_ENHANCER = 13040;
	private static final int WATER_ENHANCER = 13041;
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	// Location
	private static final Location TELEPORT = new Location(-52875, 188232, -4696);
	// Misc
	private static final int TEMPLATE_ID = 43;
	private static final int ZONE = 20108;
	
	//@formatter:off
	
	private static final int[] MONSTERS =
	{
		18611, 18612, 18613, 18614, 18615
	};
	
	private static final int[][] DROPLIST =
	{
		// must be sorted by npcId !
		// npcId, itemId, chance
		{ BLOOM, SHIELD_POTION, 30 },
		{ BLOOM, HEAL_POTION, 80 },
		{ BOTTLE, SHIELD_POTION, 10 },
		{ BOTTLE, WATER_ENHANCER, 40 },
		{ BOTTLE, HEAL_POTION, 80 },
		{ BRAZIER, SHIELD_POTION, 10 },
		{ BRAZIER, FIRE_ENHANCER, 40 },
		{ BRAZIER, HEAL_POTION, 80 }
	};
	
	private static final int[][] HP_HERBS_DROPLIST =
	{
		// itemId, count, chance
		{ 8602, 1, 10 },
		{ 8601, 1, 40 },
		{ 8600, 1, 70 }
	};
	
	private static final int[][] MP_HERBS_DROPLIST =
	{
		// itemId, count, chance
		{ 8605, 1, 10 },
		{ 8604, 1, 40 },
		{ 8603, 1, 70 }
	};
	//@formatter:on
	
	public PailakaSongOfIceAndFire()
	{
		super(PailakaSongOfIceAndFire.class.getSimpleName());
		addStartNpc(ADLER1);
		addTalkId(ADLER1);
		addAttackId(BOTTLE, BRAZIER);
		addExitZoneId(ZONE);
		addSeeCreatureId(GARGOS);
		addSpawnId(BLOOM);
		addKillId(BLOOM);
		
		addKillId(MONSTERS);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, TELEPORT, world.getInstanceId());
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "enter":
			{
				enterInstance(player, new PSoIWorld(), "PailakaSongOfIceAndFire.xml", TEMPLATE_ID);
				break;
			}
			case "GARGOS_LAUGH":
			{
				broadcastNpcSay(npc, Say2.NPC_SHOUT, NpcStringId.OHH_OH_OH);
				break;
			}
			case "TELEPORT":
			{
				teleportPlayer(player, TELEPORT, player.getInstanceId());
				break;
			}
			case "DELETE":
			{
				if (npc != null)
				{
					npc.deleteMe();
				}
				break;
			}
			case "BLOOM_TIMER":
			{
				startQuestTimer("BLOOM_TIMER2", getRandom(2, 4) * 60 * 1000, npc, null);
				break;
			}
			case "BLOOM_TIMER2":
			{
				npc.setInvisible(!npc.isInvisible());
				startQuestTimer("BLOOM_TIMER", 5000, npc, null);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if ((damage > 0) && npc.isScriptValue(0))
		{
			switch (getRandom(6))
			{
				case 0:
				{
					if (npc.getId() == BOTTLE)
					{
						npc.dropItem(player, WATER_ENHANCER, getRandom(1, 6));
					}
					break;
				}
				case 1:
				{
					if (npc.getId() == BRAZIER)
					{
						npc.dropItem(player, FIRE_ENHANCER, getRandom(1, 6));
					}
					break;
				}
				case 2:
				case 3:
				{
					npc.dropItem(player, SHIELD_POTION, getRandom(1, 10));
					break;
				}
				case 4:
				case 5:
				{
					npc.dropItem(player, HEAL_POTION, getRandom(1, 10));
					break;
				}
			}
			npc.setScriptValue(1);
			startQuestTimer("DELETE", 3000, npc, null);
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		npc.dropItem(player, getRandomBoolean() ? SHIELD_POTION : HEAL_POTION, getRandom(1, 7));
		
		switch (npc.getId())
		{
			case BOTTLE:
			case BRAZIER:
			case BLOOM:
				dropItem(npc, player);
				break;
			default:
				dropHerb(npc, player, HP_HERBS_DROPLIST);
				dropHerb(npc, player, MP_HERBS_DROPLIST);
				break;
		}
		
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if ((character.isPlayer()) && !character.isDead() && !character.isTeleporting() && ((L2PcInstance) character).isOnline())
		{
			final InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
			{
				startQuestTimer("TELEPORT", 1000, null, (L2PcInstance) character);
			}
		}
		return super.onExitZone(character, zone);
	}
	
	@Override
	public String onSeeCreature(L2Npc npc, L2Character creature, boolean isSummon)
	{
		if (npc.isScriptValue(0) && creature.isPlayer())
		{
			npc.setScriptValue(1);
			startQuestTimer("GARGOS_LAUGH", 1000, npc, creature.getActingPlayer());
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setInvisible(true);
		startQuestTimer("BLOOM_TIMER", 1000, npc, null);
		return super.onSpawn(npc);
	}
	
	private static final void dropItem(L2Npc mob, L2PcInstance player)
	{
		final int npcId = mob.getId();
		final int chance = Rnd.get(100);
		for (int[] drop : DROPLIST)
		{
			if (npcId == drop[0])
			{
				if (chance < drop[2])
				{
					((L2MonsterInstance) mob).dropItem(player, drop[1], Rnd.get(1, 6));
					return;
				}
			}
			if (npcId < drop[0])
			{
				return; // not found
			}
		}
	}
	
	private static final void dropHerb(L2Npc mob, L2PcInstance player, int[][] drop)
	{
		final int chance = Rnd.get(100);
		for (int[] element : drop)
		{
			if (chance < element[2])
			{
				((L2MonsterInstance) mob).dropItem(player, element[0], element[1]);
				return;
			}
		}
	}
}