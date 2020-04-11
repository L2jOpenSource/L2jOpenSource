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
public class FlyingNpcs extends AbstractNpcAI
{
	// @formatter:off
	private final static int[] flyingNpcs =
	{
		// Flying Monsters
		13148, 22602, 22603, 22604, 22605, 22606, 22607, 22608,
		22609, 22610, 22611, 22612, 22613, 22614, 22615, 25629,
		25630, 25633,
		
		// Flying Npcs
		18684, 18685, 18686, 18687, 18688, 18689, 18690, 18691, 18692,
		
		// Flying Raid Bosses
		25623, 25624, 25625, 25626,
	};
	// @formatter:on
	
	public FlyingNpcs()
	{
		super(FlyingNpcs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(flyingNpcs);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsFlying(true);
		return super.onSpawn(npc);
	}
}
