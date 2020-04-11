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
package l2r.gameserver.scripts.ai.modifier;

import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

/**
 * @author Kazumi
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
		22839, 22841 //LoA client not have social
	};
	// @formatter:on
	
	public NoRandomAnimation(String name, String descr)
	{
		super(name, descr);
		
		for (L2Object obj : L2World.getInstance().getVisibleObjects())
		{
			if ((obj instanceof L2Npc) && contains(NO_ANIMATION_MOBS_LIST, ((L2Npc) obj).getId()))
			{
				((L2Npc) obj).setRandomAnimationEnabled(false);
			}
		}
		
		registerMobs(NO_ANIMATION_MOBS_LIST, QuestEventType.ON_SPAWN);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (contains(NO_ANIMATION_MOBS_LIST, npc.getId()))
		{
			npc.setRandomAnimationEnabled(false);
		}
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new NoRandomAnimation("NoRandomAnimation", "ai");
	}
}
