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

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class NoChampionMobs extends AbstractNpcAI
{
	// @formatter:off
	private final static int[] NoChampionList =
	{
		// Nornil's Garden mobs
		18347, 18348, 18349, 18350, 18351, 18352, 18353, 18354, 18355, 18356, 18357, 18358,
		18359, 18360, 18361, 18362, 18363, 18367, 18368,
		
		// Cruma Event Mobs
		18544, 18545, 18546, 18547, 18548, 18549, 18550, 18551, 18552, 18553,
		
		// Kamaloka Mobs/Bosses
		18554, 18555, 18556, 18558, 18559, 18560, 18562, 18563, 18564, 18566, 18567, 18568,
		18569, 18571, 18572, 18573, 18574, 18577, 18578,
		
		// Pailaka Mobs/Boss
		18607, 18608, 18609, 18609, 18610, 18611, 18612, 18613, 18614, 18615, 18616, 18620,
		18622, 18623, 18624, 18625, 18626, 18627, 18628, 18629, 18630, 18631, 18632, 18633,
		18634, 18635, 18636, 18637, 18638, 18639, 18640, 18641, 18642, 18643, 18644, 18645,
		18646, 18647, 18648, 18649, 18650, 18651, 18652, 18653, 18654, 18655, 18656, 18657,
		18658, 18659, 18660,
		
		// Queen ant minions
		29002, 29003, 29004, 29005,
		
		// Antharas minions
		29069, 29070, 29190,
		
		// Hellbound Market Town mobs
		22359, 22360, 22361, 22449,
		
		// Steel Citadel: Base Tower mobs
		22362, 22363, 22364, 22365, 22366, 22367, 22368, 22369, 22370, 22371, 22372,
		
		// Solo Kamaloka Mobs
		22452, 22453, 22454, 22455, 22456, 22457, 22458, 22459, 22460, 22461, 22462, 22463,
		22464, 22465, 22466, 22467, 22468, 22468, 22470, 22471, 22472, 22473, 22474, 22475,
		22476, 22477, 22478, 22479, 22480, 22481, 22482, 22483, 22484,
		
		// 4 Sepulchers mobs
		18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131,
		18132, 18133, 18134, 18135, 18136, 18137, 18138, 18139, 18140, 18141, 18142, 18143,
		18144, 18145, 18146, 18147, 18148, 18149, 18150, 18151, 18152, 18153, 18154, 18155,
		18156, 18157, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18665, 18166, 18167,
		18168, 18169, 18170, 18171, 18172, 18173, 18174, 18175, 18176, 18177, 17178, 18179,
		18180, 18181, 18182, 18183, 18184, 18185, 18186, 18187, 18188, 18189, 18190, 18191,
		18192, 18193, 18194, 18195, 18196, 18197, 18198, 18199, 18200, 18201, 18202, 18203,
		18204, 18205, 18206, 18207, 18208, 18209, 18210, 18211, 18212, 18213, 18214, 18215,
		18216, 18217, 18218, 18219, 18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227,
		18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239,
		18240, 18241, 18242, 18243, 18244, 18245, 18246, 18247, 18248, 18249, 18250, 18251,
		18252, 18253, 18254, 18255, 18256, 25339, 25342, 25346, 25349,
		
		// Ballista
		35685, 35723, 35754, 35792, 35823, 35854, 35892, 35923, 35961, 35999, 36030, 36068,
		36106, 36137, 36168,36206, 36244, 36282, 36313, 36351,36389,
	};
	// @formatter:on
	
	public NoChampionMobs()
	{
		super(NoChampionMobs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NoChampionList);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.isAttackable())
		{
			((L2Attackable) npc).setChampion(false);
		}
		return super.onSpawn(npc);
	}
}
