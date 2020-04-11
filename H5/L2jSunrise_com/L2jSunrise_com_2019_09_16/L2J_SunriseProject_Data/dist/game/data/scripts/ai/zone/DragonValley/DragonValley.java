/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package ai.zone.DragonValley;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * Dragon Valley AI.
 * @author St3eT, vGodFather
 */
public final class DragonValley extends AbstractNpcAI
{
	// NPC
	private static final int GEM_DRAGON = 22830;
	private static final int GEM_DRAGON_HATCHLING = 22837;
	private static final int DRAKOS_ASSASSIN = 22823;
	private static final int[] SUMMON_NPC =
	{
		22824, // Drakos Guardian
		22862, // Drakos Hunter
	};
	private static final int[] SPAWN_ANIMATION =
	{
		22826, // Scorpion Bones
		22823, // Drakos Assassin
		22828, // Parasitic Leech
		
	};
	private static final int[] REACT_MONSTER =
	{
		22822, // Drakos Warrior
		22823, // Drakos Assassin
		22824, // Drakos Guardian
		22825, // Giant Scorpion Bones
		22826, // Scorpion Bones
		22827, // Batwing Drake
		22828, // Parasitic Leech
		22829, // Emerald Drake
		22830, // Gem Dragon
		22831, // Dragon Tracker of the Valley
		22832, // Dragon Scout of the Valley
		22833, // Sand Drake Tracker
		22834, // Dust Dragon Tracker
		22860, // Hungry Parasitic Leech
		22861, // Hard Scorpion Bones
		22862, // Drakos Hunter
	};
	
	// Items
	private static final int GREATER_HERB_OF_MANA = 8604;
	private static final int SUPERIOR_HERB_OF_MANA = 8605;
	
	// Skills
	private static final SkillHolder MORALE_BOOST1 = new SkillHolder(6885, 1);
	private static final SkillHolder MORALE_BOOST2 = new SkillHolder(6885, 2);
	private static final SkillHolder MORALE_BOOST3 = new SkillHolder(6885, 3);
	private static final SkillHolder VITALITY_BUFF = new SkillHolder(6883, 1);
	
	// Misc
	private final int SPAWN_CHANCE = 100; // Retail 100%
	private static final int MIN_DISTANCE = 1500;
	private static final int MIN_MEMBERS = 1;
	private static final int MIN_LVL = 1;
	private static final int CLASS_LVL = 0;
	private static final EnumMap<ClassId, Integer> CLASS_POINTS = new EnumMap<>(ClassId.class);
	private final int RESET_TIMER = 55 * 1000;
	private final String RESET = "RESET";
	
	{
		CLASS_POINTS.put(ClassId.adventurer, 0);
		CLASS_POINTS.put(ClassId.arcanaLord, 22);
		CLASS_POINTS.put(ClassId.archmage, 1);
		CLASS_POINTS.put(ClassId.cardinal, 0);
		CLASS_POINTS.put(ClassId.dominator, 4);
		CLASS_POINTS.put(ClassId.doombringer, 3);
		CLASS_POINTS.put(ClassId.doomcryer, 0);
		CLASS_POINTS.put(ClassId.dreadnought, 17);
		CLASS_POINTS.put(ClassId.duelist, 5);
		CLASS_POINTS.put(ClassId.elementalMaster, 20);
		CLASS_POINTS.put(ClassId.evaSaint, 0);
		CLASS_POINTS.put(ClassId.evaTemplar, 14);
		CLASS_POINTS.put(ClassId.femaleSoulhound, 11);
		CLASS_POINTS.put(ClassId.fortuneSeeker, 17);
		CLASS_POINTS.put(ClassId.ghostHunter, 8);
		CLASS_POINTS.put(ClassId.ghostSentinel, 0);
		CLASS_POINTS.put(ClassId.grandKhavatari, 0);
		CLASS_POINTS.put(ClassId.hellKnight, 2);
		CLASS_POINTS.put(ClassId.hierophant, 18);
		CLASS_POINTS.put(ClassId.judicator, 23);
		CLASS_POINTS.put(ClassId.moonlightSentinel, 0);
		CLASS_POINTS.put(ClassId.maestro, 19);
		CLASS_POINTS.put(ClassId.maleSoulhound, 11);
		CLASS_POINTS.put(ClassId.mysticMuse, 0);
		CLASS_POINTS.put(ClassId.phoenixKnight, 0);
		CLASS_POINTS.put(ClassId.sagittarius, 0);
		CLASS_POINTS.put(ClassId.shillienSaint, 0);
		CLASS_POINTS.put(ClassId.shillienTemplar, 10);
		CLASS_POINTS.put(ClassId.soultaker, 0);
		CLASS_POINTS.put(ClassId.spectralDancer, 0);
		CLASS_POINTS.put(ClassId.spectralMaster, 24);
		CLASS_POINTS.put(ClassId.stormScreamer, 0);
		CLASS_POINTS.put(ClassId.swordMuse, 0);
		CLASS_POINTS.put(ClassId.titan, 1);
		CLASS_POINTS.put(ClassId.trickster, 5);
		CLASS_POINTS.put(ClassId.windRider, 2);
	}
	
	public DragonValley()
	{
		super(DragonValley.class.getSimpleName(), "ai/zone/DragonValley");
		addAttackId(SUMMON_NPC);
		addAttackId(REACT_MONSTER);
		addKillId(REACT_MONSTER);
		addKillId(GEM_DRAGON);
		addSpawnId(REACT_MONSTER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case RESET:
			{
				npc.setScriptValue(0);
			}
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			manageMoraleBoost(attacker, npc);
			
			if ((npc.getCurrentHp() < (npc.getMaxHp() / 2)) && (getRandom(100) < 5))
			{
				final int rnd = getRandom(3, 5);
				for (int i = 0; i < rnd; i++)
				{
					if (Rnd.get(1000) <= (SPAWN_CHANCE * 10))
					{
						final L2Playable playable = isSummon ? attacker.getSummon() : attacker;
						final L2Npc minion = addSpawn(DRAKOS_ASSASSIN, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), true, 0, true);
						addAttackDesire(minion, playable);
					}
				}
			}
			
			startQuestTimer(RESET, RESET_TIMER, npc, null);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == GEM_DRAGON) // Gem Dragon
		{
			if (getRandom(100) < 10)
			{
				final L2Attackable gemHatchling = (L2Attackable) addSpawn(GEM_DRAGON_HATCHLING, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
				attackPlayer(gemHatchling, killer);
			}
		}
		else if (((L2Attackable) npc).isSweepActive())
		{
			npc.dropItem(killer, getRandom(GREATER_HERB_OF_MANA, SUPERIOR_HERB_OF_MANA), 1);
		}
		
		if (getRandom(1000) > 5)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new BuffAfterDeath(killer, VITALITY_BUFF), 2000);
		}
		
		cancelQuestTimer(RESET, npc, null);
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (Util.contains(SPAWN_ANIMATION, npc.getId()))
		{
			npc.setShowSummonAnimation(true);
		}
		return super.onSpawn(npc);
	}
	
	private void manageMoraleBoost(L2PcInstance player, L2Npc npc)
	{
		double points = 0;
		int threshold = 25;
		int max_threshold = 140;
		int category_weight = 15;
		int moraleBoostLv = 0;
		
		if (player.isInParty() && (player.getParty().getMemberCount() >= MIN_MEMBERS) && (npc != null))
		{
			for (L2PcInstance member : player.getParty().getMembers())
			{
				if (!CLASS_POINTS.containsKey(member.getClassId()))
				{
					continue;
				}
				
				if ((member.getLevel() >= MIN_LVL) && (member.getClassId().level() >= CLASS_LVL) && (Util.calculateDistance(npc, member, true, false) < MIN_DISTANCE))
				{
					points += CLASS_POINTS.get(member.getClassId());
				}
			}
			
			for (L2PcInstance member : player.getParty().getMembers())
			{
				if ((member.getLevel() >= MIN_LVL) && (member.getClassId().level() >= CLASS_LVL) && (Util.calculateDistance(npc, member, true, false) < MIN_DISTANCE))
				{
					if ((member.getClassId().getId() == 90) || (member.getClassId().getId() == 91) || (member.getClassId().getId() == 99) || (member.getClassId().getId() == 106))
					{
						points += category_weight;
					}
					
					if ((member.getClassId().getId() == 95) || (member.getClassId().getId() == 96) || (member.getClassId().getId() == 104) || (member.getClassId().getId() == 111))
					{
						points += category_weight;
					}
					
					if ((member.getClassId().getId() == 94) || (member.getClassId().getId() == 103) || (member.getClassId().getId() == 110))
					{
						points += 3;
					}
					
					if ((member.getClassId().getId() == 92) || (member.getClassId().getId() == 102) || (member.getClassId().getId() == 109) || (member.getClassId().getId() == 134))
					{
						points += 3;
					}
					
					if ((member.getClassId().getId() == 93) || (member.getClassId().getId() == 101) || (member.getClassId().getId() == 108))
					{
						points += 3;
					}
					
					if ((member.getClassId().getId() == 97) || (member.getClassId().getId() == 105) || (member.getClassId().getId() == 112) || (member.getClassId().getId() == 115))
					{
						points += 1;
					}
					
					if ((member.getClassId().getId() == 98) || (member.getClassId().getId() == 100) || (member.getClassId().getId() == 107) || (member.getClassId().getId() == 116))
					{
						// nothing.
					}
					
					if ((member.getClassId().getId() == 88) || (member.getClassId().getId() == 89) || (member.getClassId().getId() == 113) || (member.getClassId().getId() == 114) || (member.getClassId().getId() == 131) || (member.getClassId().getId() == 132) || (member.getClassId().getId() == 133) || (member.getClassId().getId() == 117))
					{
						// nothing.
					}
				}
			}
			
			if (points > threshold)
			{
				if (points > (max_threshold * 0.450000))
				{
					moraleBoostLv = 3;
				}
				else if (points > (max_threshold * 0.300000))
				{
					moraleBoostLv = 2;
				}
				else
				{
					moraleBoostLv = 1;
				}
			}
			
			switch (moraleBoostLv)
			{
				case 1:
					addSkillCastDesire(npc, player, MORALE_BOOST1, 99900000000L);
					break;
				case 2:
					addSkillCastDesire(npc, player, MORALE_BOOST2, 99900000000L);
					break;
				case 3:
					addSkillCastDesire(npc, player, MORALE_BOOST3, 99900000000L);
					break;
			}
		}
	}
	
	public static class BuffAfterDeath implements Runnable
	{
		private final L2Character _killer;
		private final L2Skill _skill;
		
		public BuffAfterDeath(L2Character killer, SkillHolder skill)
		{
			_killer = killer;
			_skill = skill.getSkill();
		}
		
		@Override
		public void run()
		{
			if (_skill == null)
			{
				return;
			}
			
			if ((_killer != null) && _killer.isPlayer() && !_killer.isDead())
			{
				List<L2Character> targets = new ArrayList<>();
				targets.add(_killer);
				_killer.broadcastPacket(new MagicSkillUse(_killer, _killer, _skill.getId(), _skill.getLevel(), 0, 0));
				for (L2Character target : targets)
				{
					_skill.applyEffects(_killer, target);
				}
			}
		}
	}
}