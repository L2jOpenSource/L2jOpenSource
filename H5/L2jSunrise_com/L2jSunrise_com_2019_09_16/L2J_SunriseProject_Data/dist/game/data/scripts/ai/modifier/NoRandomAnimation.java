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
package ai.modifier;

import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class NoRandomAnimation extends AbstractNpcAI
{
	// @formatter:off
	private final static int[] NO_ANIMATION_MOBS_LIST =
	{
		13148, 18635, 18636, 18638, 18639, 18640, 18641, 18642, 18644, 18645, 18646, 18648,
		18649, 18650, 18652, 18653, 18654, 18655, 18656, 18657, 18658, 18659, 18660, 18704,
		18705, 18706, 18708, 18709, 18710, 18711, 18805, 18806, 18811, 22136, 29045, 29046,
		29047, 29048, 29049, 29050, 29051, 29099, 29103, 29150, 29151, 29152, 29161, 29163,
		29173, 29174, 29175, 30675, 30761, 30762, 30763, 30980, 31074, 31665, 32746, 31752,
		32015, 32568, 32556, 32568,22786,22787,
		22839, 22841, //LoA client not have social
		// TIAT Mobs
		18759, 18772, 18778, 22536, 22537, 22539, 22540, 22541, 22542, 22543, 22544, 22546,
		22548, 22549, 22550, 22551, 22552, 22593, 22596, 22597, 29162,
		// Frintezza Mobs
		18329, 18330, 18331, 18333, 18334, 18335, 18336, 18337, 18338,
		// Secret ritual of the priests
		18834,
	};
	// @formatter:on
	
	public NoRandomAnimation()
	{
		super(NoRandomAnimation.class.getSimpleName(), "ai/modifiers");
		addSpawnId(NO_ANIMATION_MOBS_LIST);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setRandomAnimationEnabled(false);
		return super.onSpawn(npc);
	}
}
