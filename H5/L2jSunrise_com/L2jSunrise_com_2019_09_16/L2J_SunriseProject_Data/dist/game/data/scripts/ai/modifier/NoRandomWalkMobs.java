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
package ai.modifier;

import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class NoRandomWalkMobs extends AbstractNpcAI
{
	// @formatter:off
	private final static int[] NO_RND_WALK_MOBS_LIST =
	{
		18328, 18329, 18330, 18331, 18332, 18333, 18334, 18335, 18336, 18337, 18338, 18339,
		18347, 18348, 18349, 18350, 18351, 18352, 18353, 18354, 18355, 18356, 18357, 18358,
		18359, 18360, 18361, 18362, 18363, 18367, 18368, 18467, 18478, 18484, 18554, 18555,
		18556, 18557, 18558, 18559, 18560, 18561, 18562, 18563, 18564, 18565, 18566, 18567,
		18568, 18569, 18570, 18571, 18572, 18573, 18574, 18575, 18576, 18577, 18578, 18607,
		18608, 18609, 18610, 18611, 18612, 18613, 18614, 18615, 18616, 18620, 18622, 18623,
		18628, 18629, 18630, 18631, 18632, 18633, 18634, 22199, 22217, 22272, 22273,
		22274, 22275, 22276, 22277, 22278, 22279, 22280, 22281, 22282, 22283, 22284, 22285,
		22286, 22287, 22288, 22289, 22290, 22291, 22292, 22293, 22294, 22295, 22296, 22297,
		22298, 22299, 22301, 22302, 22303, 22304, 22305, 22306, 22307, 22308, 22309, 22310,
		22311, 22312, 22313, 22314, 22315, 22316, 22317, 22326, 22341, 22342, 22343, 22344,
		22345, 22346, 22347, 22355, 22356, 22357, 22358, 22359, 22360, 22361, 22365, 22366,
		23367, 22368, 22369, 22370, 22400, 22401, 22402, 22416, 22417, 22418, 22419, 22420,
		22422, 22449, 22485, 22486, 22487, 22488, 22489, 22490, 22491, 22492, 22493, 22494,
		22495, 22496, 22497, 22498, 22499, 22500, 22501, 22502, 22503, 22504, 22505, 25528,
		25529, 25530, 25531, 25532, 25533, 25534, 25616, 25617, 25618, 25619, 25620, 25621,
		25622, 27165, 27166, 27167, 29045, 29046, 29047, 29048, 29049, 29050, 29051, 29065,
		29099, 29103, 29104, 29118, 29119, 29129, 29130, 29131, 29132, 29133, 29134, 29135,
		29136, 29137, 29138, 29139, 29140, 29141, 29142, 29143, 29144, 29145, 29146, 29147,
		29148, 29149, 32492, 32493, 32495, 32367,
		// Sel mahums training grounds
		22780, 22781, 22782, 22783, 22784, 22785, 22775, 22776, 22777, 22778, 22787,
		// 4th Seven Sign Epic Quest
		18834, 18835, 27351,
		// 8th Seven Sing Epic Quest
		27424, 27422,
		// Solina Tomb Epic Quest
		18952, 18953, 18954, 18955, 18956, 18957, 18958, 18959, 27403, 27404,
		22860, // Hungry Parasitic Leech
		18256, // Four Sepulchers - Halishas chest
		// Four Sepulchers - Statue of Protection
		18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243,
		// Krateis Cube
		18601, 18602,
		// Ekimus Feral Hound
		29151,
		// Lair of antharas mobs
		22848, 22849, 22850, 22851, 22857,
		// Dragon Valley
		22859, 22820,
		// Pagan temple
		22175, 32038,
		// Grandbosses no instances
		29006, 29014, 29020, 29028, 29065, 29068, 29118, 29119,
		// Freya instances
		29179, 25699, 29177, 25700, 29180, 29178,
		// Zaken instances
		29022, 29176, 29181,
		// Gordon
		29095,
		// Atomic Foundry
		22398, 22399
	};
	// @formatter:on
	
	public NoRandomWalkMobs()
	{
		super(NoRandomWalkMobs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NO_RND_WALK_MOBS_LIST);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
}
