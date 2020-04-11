/*
+ * This program is free software: you can redistribute it and/or modify it under
+ * the terms of the GNU General Public License as published by the Free Software
+ * Foundation, either version 3 of the License, or (at your option) any later
+ * version.
+ * 
+ * This program is distributed in the hope that it will be useful, but WITHOUT
+ * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
+ * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
+ * details.
+ * 
+ * You should have received a copy of the GNU General Public License along with
+ * this program. If not, see <http://www.gnu.org/licenses/>.
+ */
package l2r.gameserver.scripts.instances;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.PlaySound;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

public class Bloodshed extends Quest
{
	private static final String qn = "Bloodshed";
	
	// Items
	private final int E_APIGA = 14720;
	private final int STONE = 9576;
	private final int SCROLLW = 6577;
	private final int SCROLLA = 6578;
	
	// Npcs
	private final int ROSE = 40000;
	private final int CHEST = 40001;
	
	// Monsters
	private final int NAGLFAR = 40002;
	private final int SENTRY1 = 40003;
	private final int SENTRY2 = 40004;
	private final int HOUND = 40005;
	
	// Miscs
	private final int TIMELIMIT = 86400000;
	private final int INSTANCEID = 500000;
	
	// Doors
	private final int DOOR1 = 12240001;
	private final int DOOR2 = 12240002;
	
	public class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	private class BSWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		
		public BSWorld()
		{
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			player.sendMessage("You may not enter with a party.");
			return false;
		}
		if (player.getLevel() < 83)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.broadcastPacket(sm);
			return false;
		}
		final Long reentertime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), INSTANCEID);
		if (System.currentTimeMillis() < reentertime)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(2100);
			sm.addPcName(player);
			player.broadcastPacket(sm);
			return false;
		}
		
		return true;
	}
	
	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}
	
	protected int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof BSWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.getInstanceId();
			teleportplayer(player, teleto);
			return instanceId;
		}
		// New instance
		if (!checkConditions(player))
		{
			return 0;
		}
		final L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new BSWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCEID);
		world.setStatus(0);
		((BSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Blood Sheed Event " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		// runTumors((BSPWorld) world);
		// teleport players
		teleto.instanceId = instanceId;
		
		if (player.getParty() == null)
		{
			teleportplayer(player, teleto);
			// removeBuffs(player);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (final L2PcInstance partyMember : party.getMembers())
			{
				teleportplayer(partyMember, teleto);
				// removeBuffs(partyMember);
				world.addAllowed(partyMember.getObjectId());
			}
		}
		return instanceId;
	}
	
	protected void exitInstance(L2PcInstance player, teleCoord tele, int tId)
	{
		player.setInstanceId(0);
		if (TIMELIMIT > 0)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, System.currentTimeMillis() + TIMELIMIT);
		}
		player.teleToLocation(tele.x, tele.y, tele.z);
		final L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getId();
		if (npcId == ROSE)
		{
			final teleCoord tele = new teleCoord();
			tele.x = -238599;
			tele.y = 219983;
			tele.z = -10144;
			enterInstance(player, "Bloodshed.xml", tele);
			return "";
		}
		else if (npcId == CHEST)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			player.sendPacket(new ExShowScreenMessage("Solo Instance Event (83+): Completed!", 8000));
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = 83279;
			tele.y = 148011;
			tele.z = -3404;
			player.addItem("Event", 57, 10000000, player, true);
			if (Rnd.get(100) < 10)
			{
				player.addItem("Event", STONE, 1, player, true);
			}
			if (Rnd.get(100) < 15)
			{
				player.addItem("Event", SCROLLW, 10, player, true);
			}
			if (Rnd.get(100) < 15)
			{
				player.addItem("Event", SCROLLA, 10, player, true);
			}
			
			exitInstance(player, tele, 0);
			
			npc.decayMe();
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			inst.setEmptyDestroyTime(0);
		}
		return "";
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getId();
		switch (npcId)
		{
			case SENTRY1:
				openDoor(DOOR1, player.getInstanceId());
			case SENTRY2:
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Master, Forgive Me!"));
				player.addItem("Solo Instance Event", E_APIGA, 2, npc, true);
				openDoor(DOOR2, player.getInstanceId());
				break;
			case HOUND:
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				player.sendPacket(new ExShowScreenMessage("Demonic Lord Naglfar Has Appeared!", 8000));
				addSpawn(NAGLFAR, -242754, 219982, -9985, 306, false, 0, false, player.getInstanceId());
				player.addItem("Solo Instance Event", E_APIGA, 1, npc, true);
				player.sendPacket(new Earthquake(240826, 219982, -9985, 20, 10));
				break;
			case NAGLFAR:
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Ugh.... Defeated.. How!?"));
				player.sendPacket(new ExShowScreenMessage("Congratulations! You Have Defeated Demonic Lord Naglfar.", 12000));
				player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
				player.addItem("Solo Instance Event", E_APIGA, 10, npc, true);
				addSpawn(CHEST, -242754, 219982, -9985, 306, false, 0, false, player.getInstanceId());
				InstanceManager.getInstance().getInstance(player.getInstanceId()).setDuration(1 * 60 * 1000);
				break;
		}
		
		return "";
	}
	
	public Bloodshed(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addKillId(SENTRY1);
		addKillId(SENTRY2);
		addKillId(HOUND);
		addKillId(NAGLFAR);
		addStartNpc(ROSE);
		addTalkId(ROSE);
		addTalkId(CHEST);
	}
	
	public static void main(String[] args)
	{
		new Bloodshed(-1, qn, "events");
		_log.info("blood shed loaded.");
	}
}