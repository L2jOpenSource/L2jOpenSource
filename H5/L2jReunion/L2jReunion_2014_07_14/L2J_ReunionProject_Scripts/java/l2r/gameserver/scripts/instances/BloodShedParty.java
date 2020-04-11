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

import javolution.util.FastMap;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

public class BloodShedParty extends Quest
{
	
	private static final String qn = "Bloodshedparty";
	
	private static final int INSTANCEID = 555555;
	
	// Items
	private static final int E_APIGA = 14720;
	private static final int ADENA = 57;
	private static final int APIGA = 14721;
	private static final int STONE82 = 10486;
	private static final int STONE84 = 14169;
	private static final int SCROLLW = 6578;
	private static final int SCROLLA = 6577;
	private static final int GOLDDRAGON = 3481;
	
	// NPCs
	private static final int ROSE = 2009001;
	private static final int CHEST = 2010010;
	private static final int MOBS = 2010001;
	// FIRST CHAMBER MOBS
	private static final int PROTECTOR = 2010005;
	
	private static final int BELETH = 2010007;
	private static final int BAYLOR = 2010008;
	private static final int TIAT = 2010009;
	
	// Doors
	private static final int[] DOOR =
	{
		20240001
	};
	
	private final FastMap<Integer, Integer> _mobs;
	
	public class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	private class BSPWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0,
			0
		};
		
		public BSPWorld()
		{
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			player.sendPacket(new ExShowScreenMessage("You need to be in party to enter.", 3000));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			player.sendPacket(new ExShowScreenMessage("Only party leader can enter", 3000));
			return false;
		}
		if (party.getMemberCount() < 9)
		{
			player.sendPacket(new ExShowScreenMessage("Your party must have at least 9 players", 3000));
			return false;
		}
		for (final L2PcInstance partyMember : party.getMembers())
		{
			if (partyMember.getLevel() < 78)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			final Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
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
			if (!(world instanceof BSPWorld))
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
		world = new BSPWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCEID);
		world.setStatus(0);
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			if (Util.contains(DOOR, door.getId()))
			{
				door.setIsAttackableDoor(true);
			}
		}
		((BSPWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Blood Sheed Party Event " + template + " Instance: " + instanceId + " created by player: " + player.getName());
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
		if (tId > 0)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), tId, System.currentTimeMillis() * 60000 * 60 * 24);
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
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getId();
		if (npcId == ROSE)
		{
			final teleCoord tele = new teleCoord();
			tele.x = 16345;
			tele.y = 209051;
			tele.z = -9357;
			enterInstance(player, "Bloodshedparty.xml", tele);
			return "";
		}
		else if (npcId == CHEST)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			player.sendPacket(new ExShowScreenMessage("This world was destroied: Completed", 8000));
			player.sendPacket(new ExShowScreenMessage("Baylor: You fools, We will meet in your world soon.....Aha ha ha", 15000));
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = 82200;
			tele.y = 148347;
			tele.z = -3467;
			final L2Party party = player.getParty();
			if (party != null)
			{
				for (final L2PcInstance partyMember : party.getMembers())
				{
					partyMember.addItem("Event", ADENA, 20000000, player, true);
					if (Rnd.get(100) < 10)
					{
						partyMember.addItem("Event", STONE84, 10, player, true);
					}
					if (Rnd.get(100) < 15)
					{
						partyMember.addItem("Event", STONE82, 10, player, true);
					}
					if (Rnd.get(100) < 25)
					{
						partyMember.addItem("Event", APIGA, 50, player, true);
					}
					if (Rnd.get(100) < 25)
					{
						partyMember.addItem("Event", SCROLLA, 20, player, true);
					}
					if (Rnd.get(100) < 50)
					{
						partyMember.addItem("Event", SCROLLW, 10, player, true);
					}
					
					exitInstance(partyMember, tele, world.getTemplateId());
				}
			}
			else
			{
				exitInstance(player, tele, 0);
			}
			
			npc.decayMe();
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			inst.setEmptyDestroyTime(0);
		}
		return "";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final BSPWorld world = (BSPWorld) tmpworld;
		
		if (npc.getId() == MOBS)
		{
			if (GetKilledMobs(MOBS) == 6)
			{
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Master, Forgive Me! I failed"));
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "You fools, here's your end"));
				player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Ooo...It is a good day to die"));
				addSpawn(PROTECTOR, 16658, 211498, -9357, 0, false, 0, false, world.getInstanceId());
				openDoor(20240001, world.getInstanceId());
			}
		}
		if (npc.getId() == PROTECTOR)
		{
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Master, Forgive Me!"));
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "I failed, give me another chance to protect you"));
			player.sendPacket(new ExShowScreenMessage("Fools, here's your end!", 8000));
			// newNpc = self.addSpawn(int npcId,x,y,z,heading,randomOffset,despawnDelay,isSummonSpawn,instanceId);
			addSpawn(BELETH, 16344, 213091, -9356, 0, false, 0, false, world.getInstanceId());
			player.sendPacket(new Earthquake(16344, 213091, -9356, 20, 5));
		}
		if (npc.getId() == BELETH)
		{
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "My world....."));
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Baylorrrrr"));
			player.addItem("Event", E_APIGA, 5, player, true);
			player.sendPacket(new ExShowScreenMessage("Baylor:My Brother, I will avenge you!", 12000));
			// newNpc = self.addSpawn(int npcId,x,y,z,heading,randomOffset,despawnDelay,isSummonSpawn,instanceId)
			addSpawn(BAYLOR, 16344, 213091, -9356, 0, false, 0, false, world.getInstanceId());
			player.sendPacket(new Earthquake(16344, 213091, -9356, 20, 5));
		}
		if (npc.getId() == BAYLOR)
		{
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "My death is nothing, your end is near"));
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "You fools ....Aha ha ha ha"));
			player.addItem("Event", E_APIGA, 5, player, true);
			player.sendPacket(new ExShowScreenMessage("..............kill them all!", 8000));
			// newNpc = self.addSpawn(int npcId,x,y,z,heading,randomOffset,despawnDelay,isSummonSpawn,instanceId);
			addSpawn(TIAT, 16344, 213091, -9356, 0, false, 0, false, world.getInstanceId());
			player.sendPacket(new Earthquake(16344, 213091, -9356, 80, 5));
		}
		if (npc.getId() == TIAT)
		{
			player.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Ugh.... Defeated.. How!?"));
			player.sendPacket(new ExShowScreenMessage("Congratulations! You Have Defeated Demonic Lord.", 12000));
			player.addItem("Event", E_APIGA, 10, player, true);
			player.addItem("Event", GOLDDRAGON, 4, player, true);
			// newNpc = self.addSpawn(int npcId,x,y,z,heading,randomOffset,despawnDelay,isSummonSpawn,instanceId)
			addSpawn(CHEST, 16225, 213040, -9357, 0, false, 0, false, world.getInstanceId());
		}
		return null;
	}
	
	private int GetKilledMobs(int npcId)
	{
		int t;
		if (_mobs.containsKey(npcId))
		{
			t = _mobs.get(npcId) + 1;
		}
		else
		{
			t = 1;
		}
		
		_mobs.put(npcId, t);
		return t;
	}
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public BloodShedParty(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(ROSE);
		addTalkId(ROSE);
		addTalkId(CHEST);
		
		addKillId(MOBS);
		addKillId(PROTECTOR);
		addKillId(BELETH);
		addKillId(BAYLOR);
		addKillId(TIAT);
		
		_mobs = new FastMap<>();
		
	}
	
	public static void main(String[] args)
	{
		new BloodShedParty(-1, qn, "Bloodshedparty");
	}
	
}