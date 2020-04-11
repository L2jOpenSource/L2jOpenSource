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
package l2r.gameserver.scripts.ai.group_template;

import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2ChestInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * Chest AI implementation.
 * @author Fulminus
 */
public class Chests extends AbstractNpcAI
{
	private static final int _SKILL_UNLOCK_ID = 27;
	private static final int _SKILL_MAESTRO_KEY_ID = 22271;
	// TARGET_AURA skill with suicide(Treasure Bomb)
	private static final int _SKILL_SUICIDE_ID = 4143;
	
	private static final int _NPCID_REWARD_CHESTS_MIN_ID = 18265;
	private static final int _NPCID_REWARD_CHESTS_MAX_ID = 18286;
	
	private static final int[] _SKILL_UNLOCK_MAX_CHANCES =
	{
		98,
		84,
		99,
		84,
		88,
		90,
		89,
		88,
		86,
		90,
		87,
		89,
		89,
		89,
		89
	};
	
	private static void selfDestructChest(L2ChestInstance chest)
	{
		chest.doCast(SkillData.getInstance().getInfo(_SKILL_SUICIDE_ID, chest.getLevel() / 10));
	}
	
	public static void main(String[] args)
	{
		Chests chestsAi = new Chests();
		
		for (int chestNpcId = _NPCID_REWARD_CHESTS_MIN_ID; chestNpcId <= _NPCID_REWARD_CHESTS_MAX_ID; ++chestNpcId)
		{
			chestsAi.addSpawnId(chestNpcId);
			chestsAi.addAttackId(chestNpcId);
			chestsAi.addSkillSeeId(chestNpcId);
			chestsAi.addSpellFinishedId(chestNpcId);
			
			// setup already spawned chests :o
			for (L2Spawn chestSpawn : SpawnTable.getInstance().getSpawns(chestNpcId))
			{
				if (chestSpawn.getLastSpawn() != null)
				{
					chestsAi.onSpawn(chestSpawn.getLastSpawn());
				}
			}
		}
	}
	
	private Chests()
	{
		super(Chests.class.getSimpleName(), "ai/group_template");
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		L2ChestInstance chest = (L2ChestInstance) npc;
		chest.disableCoreAI(true);
		chest.setIsNoRndWalk(true);
		chest.setMustRewardExpSp(false);
		chest.enableItemDrop(false);
		chest.setIsInvul(true);
		chest.resetInteract();
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		L2ChestInstance chest = (L2ChestInstance) npc;
		if (!chest.tryInteract())
		{
			return null;
		}
		
		selfDestructChest(chest);
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		// only trigger behavior when the npc seeing this also is in target list
		if (!Util.contains(targets, npc))
		{
			return null;
		}
		
		L2ChestInstance chest = (L2ChestInstance) npc;
		if (!chest.tryInteract())
		{
			return null;
		}
		
		// Not required, we only register valid chests
		// @formatter:off
/*		if ((chest.getId() < _NPCID_REWARD_CHESTS_MIN_ID) || (chest.getId() > _NPCID_REWARD_CHESTS_MAX_ID))
		{
			// consider this to be a trap chest
			selfDestructChest(chest);
			return null;
		}*/
		// @formatter:on
		
		int openChance = 0;
		
		switch (skill.getId())
		{
			case _SKILL_UNLOCK_ID:
			{
				int maxChance = 0;
				try
				{
					maxChance = _SKILL_UNLOCK_MAX_CHANCES[skill.getLevel() - 1];
				}
				catch (RuntimeException e)
				{
					// do nothing, most likely a IndexOutOfBoundsException which should not happen anyway
				}
				
				openChance = Math.min(maxChance, maxChance - ((chest.getLevel() - (skill.getLevel() * 4) - 16) * 6));
			}
			case _SKILL_MAESTRO_KEY_ID:
			{
				if (((caster.getLevel() <= 77) && (Math.abs(chest.getLevel() - caster.getLevel()) > 6)) || ((caster.getLevel() >= 78) && (Math.abs(chest.getLevel() - caster.getLevel()) > 5)))
				{
					openChance = 0;
				}
				else
				{
					openChance = 100;
				}
			}
		}
		
		if (Rnd.get(100) < openChance)
		{
			chest.enableItemDrop(true);
			chest.setIsInvul(false);
			chest.reduceCurrentHp(chest.getMaxHp(), caster, null);
		}
		else
		{
			selfDestructChest(chest);
		}
		
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		// after self descruction casting, chest is deleted and decayed directly
		npc.deleteMe();
		return null;
	}
}
