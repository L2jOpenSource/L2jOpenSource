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
package l2r.gameserver.scripts.instances;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

public class LibraryOfSages extends Quest
{
	private static final String qn = "LibraryOfSages";
	// Values
	private static final int TEMPLATE_ID = 156;
	// NPC's
	private static final int Sophia = 32596;
	private static final int Sophia2 = 32861;
	private static final int Sophia3 = 32863;
	private static final int Elcadia_Support = 32785;
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int HidenRoom = 2;
	private static final int[][] TELEPORTS =
	{
		{
			37063,
			-49813,
			-1128
		},
		{
			37063,
			-49813,
			-1128
		},
		{
			37355,
			-50065,
			-1127
		}
	// books
	};
	
	private static final NpcStringId[] spam =
	{
		NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK,
		NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT,
		NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES,
		NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};
	private final FastMap<Integer, InstanceHolder> instanceWorlds = new FastMap<>();
	
	public static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}
	
	private class LibraryOfSagesWorld extends InstanceWorld
	{
		public LibraryOfSagesWorld()
		{
		}
	}
	
	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if ((holder == null) && (instanceId > 0))
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		if (holder != null)
		{
			for (L2Npc h : holder.mobs)
			{
				h.deleteMe();
			}
			holder.mobs.clear();
		}
		if (instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
			if (holder != null)
			{
				holder.mobs.add(support);
			}
			startQuestTimer("check_follow", 3000, support, player);
		}
	}
	
	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof LibraryOfSagesWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.getInstanceId());
			}
			return;
		}
		final int instanceId = InstanceManager.getInstance().createDynamicInstance("LibraryOfSages.xml");
		
		world = new LibraryOfSagesWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(TEMPLATE_ID);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		
		world.addAllowed(player.getObjectId());
		
		teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);
		return;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (event.equalsIgnoreCase("check_follow"))
		{
			cancelQuestTimer("check_follow", npc, player);
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), spam[Rnd.get(0, spam.length - 1)]));
			startQuestTimer("check_follow", 20000, npc, player);
			return "";
		}
		else if (npc.getId() == Sophia)
		{
			if (event.equalsIgnoreCase("tele1"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if (npc.getId() == Sophia2)
		{
			if (event.equalsIgnoreCase("tele2"))
			{
				teleportPlayer(npc, player, TELEPORTS[HidenRoom], player.getInstanceId());
				return null;
			}
			else if (event.equalsIgnoreCase("tele3"))
			{
				InstanceHolder holder = instanceWorlds.get(player.getInstanceId());
				if (holder != null)
				{
					for (L2Npc h : holder.mobs)
					{
						h.deleteMe();
					}
					holder.mobs.clear();
				}
				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				return null;
			}
		}
		else if (npc.getId() == Sophia3)
		{
			if (event.equalsIgnoreCase("tele4"))
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], player.getInstanceId());
				return null;
			}
		}
		return htmltext;
	}
	
	public LibraryOfSages(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(Sophia);
		addStartNpc(Sophia2);
		addTalkId(Sophia);
		addTalkId(Sophia2);
		addTalkId(Sophia3);
		addTalkId(Elcadia_Support);
	}
	
	public static void main(String[] args)
	{
		new LibraryOfSages(-1, qn, "instances");
	}
}
