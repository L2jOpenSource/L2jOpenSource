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

package l2r.gameserver.scripts.custom;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author TeMeRuT
 */
public class LuckyPig extends AbstractNpcAI
{
	private final int LUCKY_PIG_NPC = 18666;
	private final int LUCKY_PIG_MOB_PINK = 2502;
	private final int LUCKY_PIG_MOB_YELLOW = 2503;
	
	private final Map<Integer, List<Long>> _ADENAS;
	
	private final int[] _MOBS =
	{
		// TODO: Add Correct Monsters
		22862,
		22823,
	};
	
	public LuckyPig()
	{
		super(LuckyPig.class.getSimpleName(), "custom");
		
		_ADENAS = new FastMap<Integer, List<Long>>().shared();
		registerMobs(_MOBS, QuestEventType.ON_KILL);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("checkForAdena".equalsIgnoreCase(event))
		{
			try
			{
				for (L2Object object : L2World.getInstance().getVisibleObjects(npc, 500))
				{
					if (!(object instanceof L2ItemInstance))
					{
						continue;
					}
					L2ItemInstance item = (L2ItemInstance) object;
					if (item.getId() == Inventory.ADENA_ID)
					{
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(item.getX(), item.getY(), item.getZ(), 0));
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						if (_ADENAS.containsKey(npc.getObjectId()))
						{
							_ADENAS.get(npc.getObjectId()).add(item.getCount());
							
							if (_ADENAS.get(npc.getObjectId()).size() > 9)
							{
								long totalAdena = 0;
								for (long adena : _ADENAS.get(npc.getObjectId()))
								{
									totalAdena += adena;
								}
								
								if (totalAdena < 10000000)
								{
									npc.deleteMe();
								}
								else if (totalAdena < 100000000)
								{
									int x = npc.getX();
									int y = npc.getY();
									int z = npc.getZ();
									npc.deleteMe();
									addSpawn(LUCKY_PIG_MOB_PINK, x, y, z, 0, true, 5 * 60 * 1000, true);
								}
								else if (totalAdena >= 100000000)
								{
									int x = npc.getX();
									int y = npc.getY();
									int z = npc.getZ();
									npc.deleteMe();
									addSpawn(LUCKY_PIG_MOB_YELLOW, x, y, z, 0, true, 5 * 60 * 1000, true);
								}
								
								cancelQuestTimer("checkForAdena", npc, null);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.warn(e.getMessage(), e);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (Util.contains(_MOBS, npc.getId()))
		{
			if (Rnd.chance(50))
			{
				L2Npc mob = addSpawn(LUCKY_PIG_NPC, npc.getX() + 50, npc.getY() + 50, npc.getZ(), npc.getHeading(), true, 10 * 60 * 1000, true);
				onSpawn(mob);
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		switch (npc.getId())
		{
			case LUCKY_PIG_NPC:
			{
				List<Long> _adena = new FastList<>();
				_ADENAS.put(npc.getObjectId(), _adena);
				startQuestTimer("checkForAdena", 1000, npc, null, true);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), "I am hungry please give me some adenas!"));
				break;
			}
			case LUCKY_PIG_MOB_PINK:
			case LUCKY_PIG_MOB_YELLOW:
			{
				npc.setIsInvul(true);
			}
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new LuckyPig();
	}
}
