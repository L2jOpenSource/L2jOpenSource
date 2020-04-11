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

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * Raid Boss Cancel AI.
 * @author Adry_85
 */
public final class RaidBossCancel extends AbstractNpcAI
{
	// Raid Bosses
	private static final int[] RAID_BOSSES =
	{
		25019, // Pan Dryad
		25050, // Verfa
		25063, // Chertuba of Great Soul
		25088, // Crazy Mechanic Golem
		25098, // Sejarr's Servitor
		25102, // Shacram
		25118, // Guilotine, Warden of the Execution Grounds
		25125, // Fierce Tiger King Angel
		25126, // Longhorn Golkonda
		25127, // Langk Matriarch Rashkos
		25158, // King Tarlk
		25162, // Giant Marpanak
		25163, // Roaring Skylancer
		25169, // Ragraman
		25188, // Apepi
		25198, // Fafurion's Herald Lokness
		25229, // Storm Winged Naga
		25233, // Spirit of Andras, the Betrayer
		25234, // Ancient Drake
		25244, // Last Lesser Giant Olkuth
		25248, // Doom Blade Tanatos
		25255, // Gargoyle Lord Tiphon
		25259, // Zaken's Butcher Krantz
		25272, // Partisan Leader Talakin
		25276, // Death Lord Ipos
		25280, // Pagan Watcher Cerberon
		25281, // Anakim's Nemesis Zakaron
		25282, // Death Lord Shax
		25305, // Ketra's Chief Brakki
		25315, // Varka's Chief Horus
		25333, // Anakazel
		25334, // Anakazel
		25335, // Anakazel
		25336, // Anakazel
		25337, // Anakazel
		25338, // Anakazel
		25365, // Patriarch Kuroboros
		25372, // Discarded Guardian
		25391, // Nurka's Messenger
		25394, // Premo Prime
		25437, // Timak Orc Gosmos
		25512, // Gigantic Chaos Golem
		25523, // Plague Golem
		25527, // Uruka
		25552, // Soul Hunter Chakundel
		25553, // Durango the Crusher
		25578, // Jakard
		25588, // Immortal Muus
		25592, // Commander Koenig
		25616, // Lost Warden
		25617, // Lost Warden
		25618, // Lost Warden
		25619, // Lost Warden
		25620, // Lost Warden
		25621, // Lost Warden
		25622, // Lost Warden
		25680, // Giant Marpanak
		25709, // Lost Warden
		25753, // Guillotine Warden
		25766, // Ancient Drake
		29036, // Fenril Hound Uruz
		29040, // Wings of Flame, Ixion
		29060, // Captain of the Ice Queen's Royal Guard
		29065, // Sailren
		29095, // Gordon
	};
	
	public RaidBossCancel()
	{
		super(RaidBossCancel.class.getSimpleName(), "ai/group_template");
		addAttackId(RAID_BOSSES);
		addSkillSeeId(RAID_BOSSES);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final SkillHolder selfRangeCancel = npc.getTemplate().getParameters().getObject("SelfRangeCancel_a", SkillHolder.class);
		if (Util.checkIfInRange(150, npc, attacker, true) && (getRandom(750) < 1))
		{
			addSkillCastDesire(npc, attacker, selfRangeCancel, 1000000L);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, Skill skill, L2Object[] targets, boolean isSummon)
	{
		final SkillHolder selfRangeCancel = npc.getTemplate().getParameters().getObject("SelfRangeCancel_a", SkillHolder.class);
		if (Util.checkIfInRange(150, npc, player, true) && (getRandom(750) < 1))
		{
			addSkillCastDesire(npc, player, selfRangeCancel, 1000000L);
		}
		return super.onSkillSee(npc, player, skill, targets, isSummon);
	}
	
	public static void main(String[] args)
	{
		new RaidBossCancel();
	}
}