/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.modifier.dropEngine;

import java.util.Arrays;
import java.util.List;

import l2r.Config;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public class FortressReward extends AbstractNpcAI
{
	private static final int KE = 9912;
	
	// List contains all fortresses npcs
	private static final List<Integer> NPCS = Arrays.asList(
	// ARCHER
	35789, 35822, 36167, 35853, 36203, 36241, 36279, 35889, 36136, 36029, 35720, 35753, 36065, 36103, 36312, 35922, 36348, 35958, 35996, 35684, 36386,
	// ARCHCAP
	36028, 35719, 35752, 36064, 36102, 36311, 35921, 36347, 35957, 35995, 35683, 36385, 35788, 35821, 36166, 35852, 36202, 36240, 36278, 35888, 36135,
	// CGUARD
	35673, 35705, 35811, 36156, 36050, 36088, 35842, 36125, 36333, 35943, 35981, 36018, 35742, 36371, 35774, 36226, 36264, 36301, 36188, 35911, 35874,
	// DECORATED
	35909, 35872, 35671, 35703, 35809, 36154, 36048, 36086, 35840, 36123, 36331, 35979, 36016, 35740, 36369, 35941, 35772, 36224, 36262, 36299, 36186,
	// DGSERG1
	35711, 36056, 36094, 36339, 35949, 35987, 36377, 35780, 36232, 36270, 36194, 35880,
	// DGSERG2
	35814, 36159, 35845, 36128, 36021, 35745, 36304, 35914, 35676,
	// GENERAL
	36066, 36349, 35959, 35997, 36387, 35790, 36204, 36242, 36280, 35890, 35721,
	// GUARD
	35944, 36198, 35982, 36236, 36019, 36274, 35743, 36372, 35848, 35884, 35775, 36131, 36227, 36265, 36302, 36189, 36024, 35715, 35748, 35912, 35875, 35674, 36060, 36098, 35706, 36307, 35812, 36157, 35917, 36051, 36089, 36343, 35843, 35953, 35991, 35679, 36381, 35784, 36126, 35817, 36162, 36334,
	// GUARDCAP
	36058, 36096, 36305, 35915, 36341, 35951, 35989, 35677, 36379, 35782, 35815, 36160, 36196, 36234, 36272, 35846, 35882, 36129, 36022, 35713, 35746,
	// HEALER
	36346, 35956, 35994, 35682, 36384, 35787, 35820, 36165, 35851, 36201, 36239, 36277, 35887, 36134, 36027, 35718, 35751, 36063, 36101, 36310, 35920,
	// MINISTER
	36205, 36243, 36281, 35891, 35722, 36067, 36350, 35960, 35998, 36388, 35791,
	// REBELCOM
	36122, 36330, 35978, 36015, 35739, 36368, 35940, 35771, 36223, 36261, 36298, 36185, 35908, 35871, 35670, 35702, 35808, 36153, 36047, 36085, 35839,
	// REBELPRIV
	36332, 35980, 36017, 35741, 36370, 35942, 35773, 36225, 36263, 36300, 36187, 35910, 35873, 35672, 35704, 35810, 36155, 36049, 36087, 35841, 36124,
	// SGOLEM
	35781, 36233, 36271, 36195, 35881, 35712, 36057, 36095, 36340, 35950, 35988, 36378,
	// SBOXES
	35665, 35697, 35803, 35834, 35734, 35766,
	// SUPPORT
	36308, 35918, 36344, 35954, 35992, 35680, 36382, 35785, 35818, 36163, 36199, 36237, 36275, 35849, 35885, 36132, 36025, 35716, 35749, 36061, 36099,
	// WIZARD
	36133, 36026, 35717, 35750, 36062, 36100, 36309, 35919, 36345, 35955, 35993, 35681, 36383, 35786, 35819, 36164, 35850, 36200, 36238, 36276, 35886);
	
	// @formatter:off
	private static final int[] ARCHER         = { 35789, 35822, 36167, 35853, 36203, 36241, 36279, 35889, 36136, 36029, 35720, 35753, 36065, 36103, 36312, 35922, 36348, 35958, 35996, 35684, 36386 };
	private static final int[] ARCHER_DROP    = { 4, 16, 100 };
	private static final int[] ARCHCAP        = { 36028, 35719, 35752, 36064, 36102, 36311, 35921, 36347, 35957, 35995, 35683, 36385, 35788, 35821, 36166, 35852, 36202, 36240, 36278, 35888, 36135 };
	private static final int[] ARCHCAP_DROP   = { 140, 300, 100 };
	private static final int[] CGUARD         = { 35673, 35705, 35811, 36156, 36050, 36088, 35842, 36125, 36333, 35943, 35981, 36018, 35742, 36371, 35774, 36226, 36264, 36301, 36188, 35911, 35874 };
	private static final int[] CGUARD_DROP    = { 4, 12, 100 };
	private static final int[] DECORATED      = { 35909, 35872, 35671, 35703, 35809, 36154, 36048, 36086, 35840, 36123, 36331, 35979, 36016, 35740, 36369, 35941, 35772, 36224, 36262, 36299, 36186 };
	private static final int[] DECORATED_DROP = { 4, 16, 100 };
	private static final int[] DGSERG1        = { 35711, 36056, 36094, 36339, 35949, 35987, 36377, 35780, 36232, 36270, 36194, 35880 };
	private static final int[] DGSERG_DROP1   = { 24, 72, 4 };
	private static final int[] DGSERG2        = { 35814, 36159, 35845, 36128, 36021, 35745, 36304, 35914, 35676 };
	private static final int[] DGSERG_DROP2   = { 4, 12, 100 };
	private static final int[] GENERAL        = { 36066, 36349, 35959, 35997, 36387, 35790, 36204, 36242, 36280, 35890, 35721 };
	private static final int[] GENERAL_DROP   = { 40, 154, 100 };
	private static final int[] GUARD          = { 35944, 36198, 35982, 36236, 36019, 36274, 35743, 36372, 35848, 35884, 35775, 36131, 36227, 36265, 36302, 36189, 36024, 35715, 35748, 35912, 35875, 35674, 36060, 36098, 35706, 36307, 35812, 36157, 35917, 36051, 36089, 36343, 35843, 35953, 35991, 35679, 36381, 35784, 36126, 35817, 36162, 36334 };
	private static final int[] GUARD_DROP     = { 4, 12, 100 };
	private static final int[] GUARDCAP       = { 36058, 36096, 36305, 35915, 36341, 35951, 35989, 35677, 36379, 35782, 35815, 36160, 36196, 36234, 36272, 35846, 35882, 36129, 36022, 35713, 35746 };
	private static final int[] GUARDCAP_DROP  = { 140, 300, 100 };
	private static final int[] HEALER         = { 36346, 35956, 35994, 35682, 36384, 35787, 35820, 36165, 35851, 36201, 36239, 36277, 35887, 36134, 36027, 35718, 35751, 36063, 36101, 36310, 35920 };
	private static final int[] HEALER_DROP    = { 4, 16, 100 };
	private static final int[] MINISTER       = { 36205, 36243, 36281, 35891, 35722, 36067, 36350, 35960, 35998, 36388, 35791 };
	private static final int[] MINISTER_DROP  = { 40, 154, 100 };
	private static final int[] REBELCOM       = { 36122, 36330, 35978, 36015, 35739, 36368, 35940, 35771, 36223, 36261, 36298, 36185, 35908, 35871, 35670, 35702, 35808, 36153, 36047, 36085, 35839 };
	private static final int[] REBELCOM_DROP  = { 40, 200, 100 };
	private static final int[] REBELPRIV      = { 36332, 35980, 36017, 35741, 36370, 35942, 35773, 36225, 36263, 36300, 36187, 35910, 35873, 35672, 35704, 35810, 36155, 36049, 36087, 35841, 36124 };
	private static final int[] REBELPRIV_DROP = { 4, 12, 100 };
	private static final int[] SGOLEM         = { 35781, 36233, 36271, 36195, 35881, 35712, 36057, 36095, 36340, 35950, 35988, 36378 };
	private static final int[] SGOLEM_DROP    = { 4, 6, 100 };
	private static final int[] SBOX1          = { 35665 };
	private static final int[] SBOX_DROP1     = { 40, 80, 100 };
	private static final int[] SBOX2          = { 35697, };
	private static final int[] SBOX_DROP2     = { 80, 160, 100 };
	private static final int[] SBOX3          = { 35803 };
	private static final int[] SBOX_DROP3     = { 200, 400, 100 };
	private static final int[] SBOX4          = { 35834 };
	private static final int[] SBOX_DROP4     = { 240, 480, 100 };
	private static final int[] SBOX5          = { 35734 };
	private static final int[] SBOX_DROP5     = { 120, 240, 100 };
	private static final int[] SBOX6          = { 35766 };
	private static final int[] SBOX_DROP6     = { 160, 320, 100 };
	private static final int[] SUPPORT        = { 36308, 35918, 36344, 35954, 35992, 35680, 36382, 35785, 35818, 36163, 36199, 36237, 36275, 35849, 35885, 36132, 36025, 35716, 35749, 36061, 36099 };
	private static final int[] SUPPORT_DROP   = { 140, 300, 100 };
	private static final int[] WIZARD         = { 36133, 36026, 35717, 35750, 36062, 36100, 36309, 35919, 36345, 35955, 35993, 35681, 36383, 35786, 35819, 36164, 35850, 36200, 36238, 36276, 35886 };
	private static final int[] WIZARD_DROP    = { 4, 16, 100 };
	// @formatter:on
	
	public FortressReward()
	{
		super(FortressReward.class.getSimpleName(), "ai/modifiers/dropEngine");
		
		addKillId(NPCS);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		int npcid = npc.getId();
		Fort fort = npc.getFort();
		L2Clan clan = player.getClan();
		if ((fort != null) && (clan != null) && (fort.getOwnerClan() != clan))
		{
			FortSiege siege = fort.getSiege();
			if ((siege != null) && siege.checkIsAttacker(clan))
			{
				if (Util.contains(CGUARD, npcid))
				{
					rewardPlayer(player, npc, CGUARD_DROP);
				}
				else if (Util.contains(GUARD, npcid))
				{
					rewardPlayer(player, npc, GUARD_DROP);
				}
				else if (Util.contains(ARCHER, npcid))
				{
					rewardPlayer(player, npc, ARCHER_DROP);
				}
				else if (Util.contains(HEALER, npcid))
				{
					rewardPlayer(player, npc, HEALER_DROP);
				}
				else if (Util.contains(WIZARD, npcid))
				{
					rewardPlayer(player, npc, WIZARD_DROP);
				}
				else if (Util.contains(SUPPORT, npcid))
				{
					rewardPlayer(player, npc, SUPPORT_DROP);
				}
				else if (Util.contains(ARCHCAP, npcid))
				{
					rewardPlayer(player, npc, ARCHCAP_DROP);
				}
				else if (Util.contains(GUARDCAP, npcid))
				{
					rewardPlayer(player, npc, GUARDCAP_DROP);
				}
				else if (Util.contains(REBELPRIV, npcid))
				{
					rewardPlayer(player, npc, REBELPRIV_DROP);
				}
				else if (Util.contains(DECORATED, npcid))
				{
					rewardPlayer(player, npc, DECORATED_DROP);
				}
				else if (Util.contains(REBELCOM, npcid))
				{
					rewardPlayer(player, npc, REBELCOM_DROP);
				}
				else if (Util.contains(DGSERG1, npcid))
				{
					rewardPlayer(player, npc, DGSERG_DROP1);
				}
				else if (Util.contains(DGSERG2, npcid))
				{
					rewardPlayer(player, npc, DGSERG_DROP2);
				}
				else if (Util.contains(GENERAL, npcid))
				{
					rewardPlayer(player, npc, GENERAL_DROP);
				}
				else if (Util.contains(MINISTER, npcid))
				{
					rewardPlayer(player, npc, MINISTER_DROP);
				}
				else if (Util.contains(SGOLEM, npcid))
				{
					rewardPlayer(player, npc, SGOLEM_DROP);
				}
				else if (Util.contains(SBOX1, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP1);
				}
				else if (Util.contains(SBOX2, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP2);
				}
				else if (Util.contains(SBOX3, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP3);
				}
				else if (Util.contains(SBOX4, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP4);
				}
				else if (Util.contains(SBOX5, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP5);
				}
				else if (Util.contains(SBOX6, npcid))
				{
					rewardPlayer(player, npc, SBOX_DROP6);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void rewardPlayer(L2PcInstance player, L2Npc npc, int[] drop)
	{
		if (getRandom(100) < (drop[2] - (player.getLevel() - npc.getLevel())))
		{
			final QuestState st = player.getQuestState(getName());
			st.giveItems(KE, (long) (getRandom(drop[0], drop[1]) * Config.RATE_DROP_ITEMS));
			playSound(player, Sound.ITEMSOUND_QUEST_ITEMGET);
		}
	}
}