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
public class NoMovingNpcs extends AbstractNpcAI
{
	// @formatter:off
	private final static int[] NO_MOVING_NPCS_LIST =
	{
		//Fort Support Unit Captain
		35680,35716,35749,35785,35818,35849,35885,35918,35954,35992,36025,36061,36099,36132,36163,36199,36237,36275,36308,36344,36382,
		//Fort Archer Captain
		35683,35719,35752,35788,35821,35852,35888,35921,35957,35995,36028,36064,36102,36135,36166,36202,36240,36278,36311,36347,36385,
		//Brazier Of Purufy && Machine
		18805,18806,
		// 7 Sign 196 quest
		27384,
		//Plains of Lizermen
		18867, 18868,
		//Krateis Cube
		18601,18602,
		// Chests quest clan level 4
		27173, 27174, 27175, 27176, 27177,
		// Doorman Zombie (Pagan guard)
		18343,
		18949,
		// Pailaka injured dragon
		18660,
		// Penance Guard, Chapel Guard (Pagan)
		22137, 22138,
		// Triol''s Revelation
		32058,32059,32060,32061,32062,32063,32064,32065,32066,32067,32068,
		// Queen Ant Larva
		29002,
		//Pillar
		18506,
		//Lock of Naia
		18491,
		// Pavel Safety Device
		18917,
		// Ancient Egg, Sprigant
		18344, 18345, 18346
	};
	// @formatter:on
	
	public NoMovingNpcs()
	{
		super(NoMovingNpcs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NO_MOVING_NPCS_LIST);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}
}
