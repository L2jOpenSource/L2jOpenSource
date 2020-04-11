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
package events.SquashEvent;

import java.util.Arrays;
import java.util.List;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.event.LongTimeEvent;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.util.Rnd;

public class SquashEvent extends LongTimeEvent
{
	private static final int MANAGER = 31860;
	private static final int NECTAR_SKILL = 2005;
	
	private static final List<Integer> SQUASH_LIST = Arrays.asList(12774, 12775, 12776, 12777, 12778, 12779, 13016, 13017);
	private static final List<Integer> CHRONO_SQUASH_LIST = Arrays.asList(12777, 12778, 12779, 13017);
	private static final List<Integer> CHRONO_LIST = Arrays.asList(4202, 5133, 5817, 7058, 8350);
	
	//@formatter:off
	private static final String[] _NOCHRONO_TEXT =
	{
		"You cannot kill me without Chrono",
		"Hehe...keep trying...",
		"Nice try...",
		"Tired ?",
		"Go go ! haha..."
	};
	private static final String[] _CHRONO_TEXT =
	{
		"Arghh... Chrono weapon...",
		"My end is coming...",
		"Please leave me!",
		"Heeellpppp...",
		"Somebody help me please..."
	};
	private static final String[] _NECTAR_TEXT =
	{
		"Yummie... Nectar...",
		"Plase give me more...",
		"Hmmm.. More.. I need more...",
		"I would like you more, if you give me more...",
		"Hmmmmmmm...",
		"My favourite..."
	};
	private static final int ADENA = 57;
	private static final int CRYSTAL_A_GRADE = 1461;
	private static final int CRYSTAL_S_GRADE = 1462;
	private static final int BSOE = 1538;
	private static final int QUICK_HEALING_POTION = 1540;
	private static final int GEMS_A_GRADE = 2133;
	private static final int GEMS_S_GRADE = 2134;
	private static final int LUNARGENT = 6029;
	private static final int HELLFIRE_OIL = 6033;
	private static final int FIREWORK = 6406;
	private static final int LARGE_FIREWORK = 6407;
	private static final int GIANT_CODEX = 6622;
	private static final int HIGH_GRADE_LS76 = 8752;
	private static final int TOP_GRADE_LS76 = 8762;
	private static final int ENERGY_GINSENG = 20004;
	private static final int BAGUETTE_HERB_1 = 20272;
	private static final int BAGUETTE_HERB_2 = 20273;
	private static final int BAGUETTE_HERB_3 = 20274;
	private static final int LARGE_LUCKY_CUB = 22005;
	private static final int ANCIENT_ENCHANT_WEAPON_A = 22015;
	private static final int ANCIENT_ENCHANT_ARMOR_A = 22017;
	
	private static final int[][] DROPLIST =
	{
		// High Quality Young Squash
		{ 12775, BAGUETTE_HERB_2, 100 },
		{ 12775, ENERGY_GINSENG, 100 },
		{ 12775, GEMS_S_GRADE, 80 },
		{ 12775, CRYSTAL_S_GRADE, 80 },
		{ 12775, QUICK_HEALING_POTION, 75 },
		{ 12775, LARGE_LUCKY_CUB, 50 },
		{ 12775, ADENA, 100 },
		
		// Low Quality Young Squash
		{ 12776, BAGUETTE_HERB_3, 100 },
		{ 12776, QUICK_HEALING_POTION, 70 },
		{ 12776, GEMS_A_GRADE, 80 },
		{ 12776, CRYSTAL_A_GRADE, 100 },
		{ 12776, HIGH_GRADE_LS76, 10 },
		{ 12776, ADENA, 100 },
		
		// High Quality Large Squash
		{ 12778, BAGUETTE_HERB_2, 100 },
		{ 12778, ADENA, 100 },
		{ 12778, TOP_GRADE_LS76, 35 },
		{ 12778, GIANT_CODEX, 10 },
		{ 12778, QUICK_HEALING_POTION, 60 },
		{ 12778, GEMS_S_GRADE, 80 },
		{ 12778, CRYSTAL_S_GRADE, 100 },
		{ 12778, ENERGY_GINSENG, 100 },
		{ 12778, LARGE_LUCKY_CUB, 60 },
		
		// Low Quality Large Squash
		{ 12779, BAGUETTE_HERB_3, 100 },
		{ 12779, ADENA, 100 },
		{ 12779, HIGH_GRADE_LS76, 10 },
		{ 12779, QUICK_HEALING_POTION, 60 },
		{ 12779, ENERGY_GINSENG, 70 },
		{ 12779,GEMS_A_GRADE, 100 },
		
		// King Squash
		{ 13016, ADENA, 100 },
		{ 13016, QUICK_HEALING_POTION, 100 },
		{ 13016, CRYSTAL_S_GRADE, 10 },
		{ 13016, GIANT_CODEX, 10 },
		{ 13016, LUNARGENT, 15 },
		{ 13016, HIGH_GRADE_LS76, 50 },
		{ 13016, FIREWORK, 100 },
		{ 13016, BAGUETTE_HERB_1, 100 },
		{ 13016, BAGUETTE_HERB_2, 80 },
		{ 13016, BSOE, 80 },
		{ 13016, ENERGY_GINSENG, 100 },
		{ 13016, ANCIENT_ENCHANT_WEAPON_A, 5 },
		
		// Emperor Squash
		{ 13017, ADENA, 100 },
		{ 13017, TOP_GRADE_LS76, 10 },
		{ 13017, GEMS_S_GRADE, 100 },
		{ 13017, CRYSTAL_S_GRADE, 100 },
		{ 13017, HELLFIRE_OIL, 5 },
		{ 13017, ENERGY_GINSENG, 100 },
		{ 13017, BSOE, 70 },
		{ 13017, GIANT_CODEX, 30 },
		{ 13017, LARGE_FIREWORK, 100 },
		{ 13017, ANCIENT_ENCHANT_ARMOR_A, 10 },
		{ 13017, BAGUETTE_HERB_2, 100 },
		{ 13017, BAGUETTE_HERB_3, 100 }
	};
	//@formatter:on
	
	public SquashEvent()
	{
		super(SquashEvent.class.getSimpleName(), "events");
		
		addAttackId(SQUASH_LIST);
		addKillId(SQUASH_LIST);
		addSpawnId(SQUASH_LIST);
		addSpawnId(CHRONO_SQUASH_LIST);
		addSkillSeeId(SQUASH_LIST);
		
		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		npc.disableCoreAI(true);
		if (CHRONO_SQUASH_LIST.contains(npc.getId()))
		{
			npc.setIsInvul(true);
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (CHRONO_SQUASH_LIST.contains(npc.getId()))
		{
			if ((attacker.getActiveWeaponItem() != null) && CHRONO_LIST.contains(attacker.getActiveWeaponItem().getId()))
			{
				ChronoText(npc);
				npc.setIsInvul(false);
				npc.getStatus().reduceHp(10, attacker);
			}
			else
			{
				noChronoText(npc);
				npc.setIsInvul(true);
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, Skill skill, L2Object[] targets, boolean isPet)
	{
		if (SQUASH_LIST.contains(npc.getId()) && (skill.getId() == NECTAR_SKILL))
		{
			switch (npc.getId())
			{
				case 12774: // Young Squash
					randomSpawn(13016, 12775, 12776, npc, true);
					break;
				case 12777: // Large Young Squash
					randomSpawn(13017, 12778, 12779, npc, true);
					break;
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (SQUASH_LIST.contains(npc.getId()))
		{
			dropItem(npc, killer);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
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
					if (drop[1] > 6000)
					{
						int adenaCount = Rnd.get(50000, 150000);
						if (drop[1] == 57)
						{
							((L2MonsterInstance) mob).dropItem(player, drop[1], adenaCount);
						}
						else
						{
							((L2MonsterInstance) mob).dropItem(player, drop[1], 1);
						}
					}
					else
					{
						int adenaCount = Rnd.get(50000, 200000);
						if (drop[1] == 57)
						{
							((L2MonsterInstance) mob).dropItem(player, drop[1], adenaCount);
						}
						else
						{
							((L2MonsterInstance) mob).dropItem(player, drop[1], Rnd.get(2, 6));
						}
					}
					continue;
				}
			}
			if (npcId < drop[0])
			{
				return;
			}
		}
	}
	
	private void randomSpawn(int low, int medium, int high, L2Npc npc, boolean delete)
	{
		final int _random = Rnd.get(100);
		if (_random < 5)
		{
			spawnNext(low, npc);
		}
		if (_random < 10)
		{
			spawnNext(medium, npc);
		}
		else if (_random < 30)
		{
			spawnNext(high, npc);
		}
		else
		{
			nectarText(npc);
		}
	}
	
	private void ChronoText(L2Npc npc)
	{
		if (Rnd.get(100) < 20)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _CHRONO_TEXT[Rnd.get(_CHRONO_TEXT.length)]));
		}
	}
	
	private void noChronoText(L2Npc npc)
	{
		if (Rnd.get(100) < 20)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _NOCHRONO_TEXT[Rnd.get(_NOCHRONO_TEXT.length)]));
		}
	}
	
	private void nectarText(L2Npc npc)
	{
		if (Rnd.get(100) < 30)
		{
			npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _NECTAR_TEXT[Rnd.get(_NECTAR_TEXT.length)]));
		}
	}
	
	private void spawnNext(int npcId, L2Npc npc)
	{
		addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 60000);
		npc.deleteMe();
	}
	
	public static void main(String[] args)
	{
		new SquashEvent();
	}
}