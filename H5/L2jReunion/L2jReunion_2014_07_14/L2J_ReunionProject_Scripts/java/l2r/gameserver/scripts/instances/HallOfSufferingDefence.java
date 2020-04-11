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

import java.util.Calendar;
import java.util.Map;

import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

public class HallOfSufferingDefence extends Quest
{
	
	private class DHSWorld extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new FastMap<>();
		public L2Npc klodekus = null;
		public L2Npc klanikus = null;
		public boolean isBossesAttacked = false;
		public long[] storeTime =
		{
			0,
			0
		}; // 0: instance start, 1: finish time
		
		public DHSWorld()
		{
			// InstanceManager.getInstance().super();
		}
	}
	
	private static final String qn = "HallOfSufferingDefence";
	private static final int INSTANCEID = 116; // this is the client number
	private static final boolean debug = false;
	
	// Items
	
	// NPCs
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int TEPIOS = 32530;
	
	// mobs
	private static final int KLODEKUS = 25665;
	private static final int KLANIKUS = 25666;
	private static final int TUMOR_ALIVE = 18704;
	// private static final int TUMOR_DEAD = 0;
	private static final int[] TUMOR_MOBIDS =
	{
		22509,
		22510,
		22511,
		22512,
		22513,
		22514,
		22515
	};
	private static final int[] TWIN_MOBIDS =
	{
		22509,
		22510,
		22511,
		22512,
		22513
	};
	
	// Doors/Walls/Zones
	
	// Doors/Walls/Zones
	private static final int[][] ROOM_1_MOBS =
	{
		{
			22509,
			-173712,
			217838,
			-9559
		},
		{
			22509,
			-173489,
			218281,
			-9557
		},
		{
			22509,
			-173824,
			218389,
			-9558
		},
		{
			22510,
			-174018,
			217970,
			-9559
		},
		{
			22510,
			-173382,
			218198,
			-9547
		}
	};
	private static final int[][] ROOM_2_MOBS =
	{
		{
			22511,
			-173456,
			217976,
			-9556
		},
		{
			22511,
			-173673,
			217951,
			-9547
		},
		{
			22509,
			-173622,
			218233,
			-9547
		},
		{
			22510,
			-173775,
			218218,
			-9545
		},
		{
			22510,
			-173660,
			217980,
			-9542
		},
		{
			22510,
			-173712,
			217838,
			-9559
		}
	};
	private static final int[][] ROOM_3_MOBS =
	{
		{
			22512,
			-173489,
			218281,
			-9557
		},
		{
			22512,
			-173824,
			218389,
			-9558
		},
		{
			22512,
			-174018,
			217970,
			-9559
		},
		{
			22509,
			-173382,
			218198,
			-9547
		},
		{
			22511,
			-173456,
			217976,
			-9556
		},
		{
			22511,
			-173673,
			217951,
			-9547
		},
		{
			22510,
			-173622,
			218233,
			-9547
		},
		{
			22510,
			-173775,
			218218,
			-9545
		}
	};
	private static final int[][] ROOM_4_MOBS =
	{
		{
			22514,
			-173660,
			217980,
			-9542
		},
		{
			22514,
			-173712,
			217838,
			-9559
		},
		{
			22514,
			-173489,
			218281,
			-9557
		},
		{
			22513,
			-173824,
			218389,
			-9558
		},
		{
			22513,
			-174018,
			217970,
			-9559
		},
		{
			22511,
			-173382,
			218198,
			-9547
		},
		{
			22511,
			-173456,
			217976,
			-9556
		},
		{
			22512,
			-173673,
			217951,
			-9547
		},
		{
			22512,
			-173622,
			218233,
			-9547
		}
	};
	private static final int[][] ROOM_5_MOBS =
	{
		{
			22512,
			-173775,
			218218,
			-9545
		},
		{
			22512,
			-173660,
			217980,
			-9542
		},
		{
			22512,
			-173712,
			217838,
			-9559
		},
		{
			22513,
			-173489,
			218281,
			-9557
		},
		{
			22513,
			-173824,
			218389,
			-9558
		},
		{
			22514,
			-174018,
			217970,
			-9559
		},
		{
			22514,
			-173382,
			218198,
			-9547
		},
		{
			22514,
			-173456,
			217976,
			-9556
		},
		{
			22515,
			-173673,
			217951,
			-9547
		},
		{
			22515,
			-173622,
			218233,
			-9547
		}
	};
	private static final int[][] TUMOR_SPAWNS =
	{
		{
			-173727,
			218109,
			-9536
		},
		{
			-173727,
			218109,
			-9536
		},
		{
			-173727,
			218109,
			-9536
		},
		{
			-173727,
			218109,
			-9536
		},
		{
			-173727,
			218109,
			-9536
		}
	};
	private static final int[][] TWIN_SPAWNS =
	{
		{
			25665,
			-173727,
			218169,
			-9536
		},
		{
			25666,
			-173727,
			218049,
			-9536
		}
	};
	private static final int[] TEPIOS_SPAWN =
	{
		-173727,
		218109,
		-9536
	};
	
	// etc
	private static final int BOSS_INVUL_TIME = 30000; // in milisex
	private static final int BOSS_MINION_SPAWN_TIME = 60000; // in milisex
	private static final int BOSS_RESSURECT_TIME = 20000; // in milisex
	// Instance reenter time
	// default: 24h
	private static final int INSTANCEPENALTY = 24;
	
	public class teleCoord
	{
		int instanceId;
		int x;
		int y;
		int z;
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		if (debug)
		{
			return true;
		}
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2101));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(2185));
			return false;
		}
		for (L2PcInstance partyMember : party.getMembers())
		{
			if ((partyMember.getLevel() < 75) || (partyMember.getLevel() > 82))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2097);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
			{
				continue;
			}
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
			{
				continue;
			}
			e.exit();
		}
		if (ch.getSummon() != null)
		{
			for (L2Effect e : ch.getSummon().getAllEffects())
			{
				if (e == null)
				{
					continue;
				}
				L2Skill skill = e.getSkill();
				if (skill.isDebuff() || skill.isStayAfterDeath())
				{
					continue;
				}
				e.exit();
			}
		}
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
			if (!(world instanceof DHSWorld))
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
		L2Party party = player.getParty();
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new DHSWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCEID);
		world.setStatus(0);
		((DHSWorld) world).storeTime[0] = System.currentTimeMillis();
		InstanceManager.getInstance().addWorld(world);
		_log.info("Hall Of Suffering started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		runTumors((DHSWorld) world);
		// teleport players
		teleto.instanceId = instanceId;
		
		if (player.getParty() == null)
		{
			teleportplayer(player, teleto);
			removeBuffs(player);
			world.addAllowed(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportplayer(partyMember, teleto);
				removeBuffs(partyMember);
				world.addAllowed(partyMember.getObjectId());
			}
		}
		return instanceId;
	}
	
	protected void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
		L2Summon pet = player.getSummon();
		if (pet != null)
		{
			pet.setInstanceId(0);
			pet.teleToLocation(tele.x, tele.y, tele.z);
		}
	}
	
	protected boolean checkKillProgress(L2Npc mob, DHSWorld world)
	{
		if (world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for (boolean isDead : world.npcList.values())
		{
			if (!isDead)
			{
				return false;
			}
		}
		return true;
	}
	
	protected int[][] getRoomSpawns(int room)
	{
		switch (room)
		{
			case 0:
				return ROOM_1_MOBS;
			case 1:
				return ROOM_2_MOBS;
			case 2:
				return ROOM_3_MOBS;
			case 3:
				return ROOM_4_MOBS;
			case 4:
				return ROOM_5_MOBS;
		}
		_log.warn("");
		return new int[][] {};
	}
	
	protected void runTumors(DHSWorld world)
	{
		for (int[] mob : getRoomSpawns(world.getStatus()))
		{
			L2Npc npc = addSpawn(mob[0], mob[1], mob[2], mob[3], 0, false, 0, false, world.getInstanceId());
			world.npcList.put(npc, false);
		}
		L2Npc mob = addSpawn(TUMOR_ALIVE, TUMOR_SPAWNS[world.getStatus()][0], TUMOR_SPAWNS[world.getStatus()][1], TUMOR_SPAWNS[world.getStatus()][2], 0, false, 0, false, world.getInstanceId());
		mob.disableCoreAI(true);
		mob.setIsImmobilized(true);
		mob.setCurrentHp(mob.getMaxHp() * 0.5);
		world.npcList.put(mob, false);
		world.setStatus(world.getStatus() + 1);
	}
	
	protected void runTwins(DHSWorld world)
	{
		world.setStatus(world.getStatus() + 1);
		world.klodekus = addSpawn(TWIN_SPAWNS[0][0], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus = addSpawn(TWIN_SPAWNS[1][0], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, world.getInstanceId());
		world.klanikus.setIsMortal(false);
		world.klodekus.setIsMortal(false);
	}
	
	protected void bossSimpleDie(L2Npc boss)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (boss.isDead())
			{
				return;
			}
			// now reset currentHp to zero
			boss.setCurrentHp(0);
			boss.setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		boss.setTarget(null);
		
		// Stop movement
		boss.stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		boss.getStatus().stopHpMpRegeneration();
		
		boss.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		boss.broadcastStatusUpdate();
		
		// Notify L2Character AI
		try
		{
			boss.getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		}
		catch (Exception e)
		{
			if (Config.DEBUG_SCRIPT_NOTIFIES)
			{
				_log.warn("HallOfSufferingDefence[notifyEvent]1 failed");
			}
		}
		
		if (boss.getWorldRegion() != null)
		{
			boss.getWorldRegion().onDeath(boss);
		}
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.hasEffectType(L2EffectType.REBALANCE_HP, L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
		{
			int hate = 2 * skill.getAggroPoints();
			if (hate < 2)
			{
				hate = 1000;
			}
			((L2Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			DHSWorld world = (DHSWorld) tmpworld;
			if (event.equalsIgnoreCase("spawnBossGuards"))
			{
				L2Npc mob = addSpawn(TWIN_MOBIDS[Rnd.get(TWIN_MOBIDS.length)], TWIN_SPAWNS[0][1], TWIN_SPAWNS[0][2], TWIN_SPAWNS[0][3], 0, false, 0, false, npc.getInstanceId());
				((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				if (Rnd.get(100) < 33)
				{
					mob = addSpawn(TWIN_MOBIDS[Rnd.get(TWIN_MOBIDS.length)], TWIN_SPAWNS[1][1], TWIN_SPAWNS[1][2], TWIN_SPAWNS[1][3], 0, false, 0, false, npc.getInstanceId());
					((L2Attackable) mob).addDamageHate(((L2Attackable) npc).getMostHated(), 0, 1);
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
			}
			else if (event.equalsIgnoreCase("isTwinSeparated"))
			{
				if (Util.checkIfInRange(500, world.klanikus, world.klodekus, false))
				{
					world.klanikus.setIsInvul(false);
					world.klodekus.setIsInvul(false);
				}
				else
				{
					world.klanikus.setIsInvul(true);
					world.klodekus.setIsInvul(true);
				}
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("ressurectTwin"))
			{
				L2Skill skill = SkillData.getInstance().getInfo(5824, 1);
				L2Npc aliveTwin = (world.klanikus == npc ? world.klodekus : world.klanikus);
				npc.doRevive();
				npc.doCast(skill);
				npc.setCurrentHp(aliveTwin.getCurrentHp());
				
				// get most hated of other boss
				L2Character hated = ((L2MonsterInstance) aliveTwin).getMostHated();
				if (hated != null)
				{
					try
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, hated, 1000);
					}
					catch (Exception e)
					{
						if (Config.DEBUG_SCRIPT_NOTIFIES)
						{
							_log.warn("HallOfSufferingDefence[notifyEvent]2 failed");
						}
					}
				}
				
				aliveTwin.setIsInvul(true); // make other boss invul
				startQuestTimer("uninvul", BOSS_INVUL_TIME, aliveTwin, null);
			}
			else if (event.equals("uninvul"))
			{
				npc.setIsInvul(false);
			}
		}
		return "";
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			if (!((DHSWorld) tmpworld).isBossesAttacked)
			{
				((DHSWorld) tmpworld).isBossesAttacked = true;
				Calendar reenter = Calendar.getInstance();
				reenter.add(Calendar.HOUR, INSTANCEPENALTY);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(tmpworld.getTemplateId()));
				
				// set instance reenter time for all allowed players
				for (int objectId : tmpworld.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if ((player != null) && player.isOnline())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, tmpworld.getTemplateId(), reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
				startQuestTimer("spawnBossGuards", BOSS_MINION_SPAWN_TIME, npc, null);
				startQuestTimer("isTwinSeparated", 10000, npc, null);
			}
			else if (damage >= npc.getCurrentHp())
			{
				if (((DHSWorld) tmpworld).klanikus.isDead())
				{
					((DHSWorld) tmpworld).klanikus.setIsDead(false);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
				}
				else if (((DHSWorld) tmpworld).klodekus.isDead())
				{
					((DHSWorld) tmpworld).klodekus.setIsDead(false);
					((DHSWorld) tmpworld).klodekus.doDie(attacker);
					((DHSWorld) tmpworld).klanikus.doDie(attacker);
				}
				else
				{
					bossSimpleDie(npc);
					startQuestTimer("ressurectTwin", BOSS_RESSURECT_TIME, npc, null);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof DHSWorld)
		{
			DHSWorld world = (DHSWorld) tmpworld;
			if (world.getStatus() < 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTumors(world);
				}
			}
			else if (world.getStatus() == 5)
			{
				if (checkKillProgress(npc, world))
				{
					runTwins(world);
				}
			}
			else if ((world.getStatus() == 6) && ((npc.getId() == KLODEKUS) || (npc.getId() == KLANIKUS)))
			{
				if (world.klanikus.isDead() && world.klodekus.isDead())
				{
					world.setStatus(world.getStatus() + 1);
					// instance end
					world.storeTime[1] = System.currentTimeMillis();
					world.klanikus = null;
					world.klodekus = null;
					this.cancelQuestTimers("ressurectTwin");
					this.cancelQuestTimers("spawnBossGuards");
					this.cancelQuestTimers("isTwinSeparated");
					addSpawn(TEPIOS, TEPIOS_SPAWN[0], TEPIOS_SPAWN[1], TEPIOS_SPAWN[2], 0, false, 0, false, world.getInstanceId());
				}
			}
		}
		return "";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == MOUTHOFEKIMUS)
		{
			teleCoord tele = new teleCoord();
			tele.x = -174701;
			tele.y = 218109;
			tele.z = -9592;
			enterInstance(player, "HallOfSufferingDefence.xml", tele);
			return "";
		}
		else if (npcId == TEPIOS)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			Long finishDiff = ((DHSWorld) world).storeTime[1] - ((DHSWorld) world).storeTime[0];
			if (finishDiff < 1260000)
			{
				st.giveItems(13777, 1);
			}
			else if (finishDiff < 1380000)
			{
				st.giveItems(13778, 1);
			}
			else if (finishDiff < 1500000)
			{
				st.giveItems(13779, 1);
			}
			else if (finishDiff < 1620000)
			{
				st.giveItems(13780, 1);
			}
			else if (finishDiff < 1740000)
			{
				st.giveItems(13781, 1);
			}
			else if (finishDiff < 1860000)
			{
				st.giveItems(13782, 1);
			}
			else if (finishDiff < 1980000)
			{
				st.giveItems(13783, 1);
			}
			else if (finishDiff < 2100000)
			{
				st.giveItems(13784, 1);
			}
			else if (finishDiff < 2220000)
			{
				st.giveItems(13785, 1);
			}
			else
			{
				st.giveItems(13786, 1);
			}
			world.removeAllowed(player.getObjectId());
			teleCoord tele = new teleCoord();
			tele.instanceId = 0;
			tele.x = -183292;
			tele.y = 206063;
			tele.z = -12888;
			exitInstance(player, tele);
		}
		return "";
	}
	
	public HallOfSufferingDefence(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
		addKillId(TUMOR_ALIVE);
		addKillId(KLODEKUS);
		addKillId(KLANIKUS);
		addAttackId(KLODEKUS);
		addAttackId(KLANIKUS);
		for (int mobId : TUMOR_MOBIDS)
		{
			addSkillSeeId(mobId);
			addKillId(mobId);
		}
	}
	
	public static void main(String[] args)
	{
		new HallOfSufferingDefence(-1, HallOfSufferingDefence.class.getSimpleName(), "instances");
	}
}