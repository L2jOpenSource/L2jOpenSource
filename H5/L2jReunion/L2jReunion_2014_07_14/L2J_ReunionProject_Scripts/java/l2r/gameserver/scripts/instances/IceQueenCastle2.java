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

import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExChangeNpcState;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage2;
import l2r.gameserver.network.serverpackets.OnEventTrigger;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;
import l2r.gameserver.scripts.quests.Q10286_ReunionWithSirra;
import l2r.gameserver.util.Broadcast;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

public class IceQueenCastle2 extends AbstractNpcAI
{
	private static final int TEMPLATE_ID = 139;
	
	// Npc
	public static int _sirra = 32762;
	public static int Jinia = 32781;
	// Mobs
	public static int glacier = 18853;
	private static int archery_breathe = 18854;
	public static int archery_knight = 18855;
	// Boss
	private static int Glakias = 25699;
	private static int freyaOnThrone = 29177;
	private static int freyaSpelling = 29178;
	private static int freyaStand = 29179;
	// Door
	private static int door = 23140101;
	
	public static int freyaStand_hard = 29180;
	public static int archery_knight_hard = 18856;
	public static int Glakias_hard = 25700;
	public boolean _isHard = false;
	
	private static int[] emmiters =
	{
		23140202,
		23140204,
		23140206,
		23140208,
		23140212,
		23140214,
		23140216
	};
	private static int decoration = 0;
	
	public static final int[] archery_blocked_status =
	{
		11,
		19,
		22,
		29,
		39
	};
	
	public static final int[] glacier_blocked_status =
	{
		11,
		19,
		29,
		39
	};
	
	public static final int[][] frozeKnightsSpawn =
	{
		{
			113845,
			-116091,
			-11168,
			8264
		},
		{
			113381,
			-115622,
			-11168,
			8264
		},
		{
			113380,
			-113978,
			-11168,
			-8224
		},
		{
			113845,
			-113518,
			-11168,
			-8224
		},
		{
			115591,
			-113516,
			-11168,
			-24504
		},
		{
			116053,
			-113981,
			-11168,
			-24504
		},
		{
			116061,
			-115611,
			-11168,
			24804
		},
		{
			115597,
			-116080,
			-11168,
			24804
		},
		{
			112942,
			-115480,
			-10960,
			52
		},
		{
			112940,
			-115146,
			-10960,
			52
		},
		{
			112945,
			-114453,
			-10960,
			52
		},
		{
			112945,
			-114123,
			-10960,
			52
		},
		{
			116497,
			-114117,
			-10960,
			32724
		},
		{
			116499,
			-114454,
			-10960,
			32724
		},
		{
			116501,
			-115145,
			-10960,
			32724
		},
		{
			116502,
			-115473,
			-10960,
			32724
		}
	};
	
	public static final int[][] _archeryKnightsSpawn =
	{
		{
			114713,
			-115109,
			-11202,
			16456
		},
		{
			114008,
			-115080,
			-11202,
			3568
		},
		{
			114422,
			-115508,
			-11202,
			12400
		},
		{
			115023,
			-115508,
			-11202,
			20016
		},
		{
			115459,
			-115079,
			-11202,
			27936
		}
	};
	
	private class FreyaWorld extends InstanceWorld
	{
		public L2Attackable _freyaThrone = null;
		public L2Npc _freyaSpelling = null;
		public L2Attackable _freyaStand = null;
		public L2Attackable _glakias = null;
		public L2Attackable _jinia = null;
		public L2Attackable _kegor = null;
		public boolean isMovieNow = false;
		public FastMap<Integer, L2Npc> _archery_knights = new FastMap<>();
		public FastMap<Integer, L2Npc> _simple_knights = new FastMap<>();
		public FastMap<Integer, L2Npc> _glaciers = new FastMap<>();
		
		public L2Attackable _freyaStand_hard = null;
		public L2Attackable _glakias_hard = null;
		public FastMap<Integer, L2Npc> _archery_knights_hard = new FastMap<>();
		
		public FreyaWorld()
		{
			InstanceManager.getInstance();
		}
	}
	
	private class spawnWave implements Runnable
	{
		private final int _waveId;
		private final FreyaWorld _world;
		
		public spawnWave(int waveId, int instanceId)
		{
			_waveId = waveId;
			_world = getWorld(instanceId);
		}
		
		@Override
		public void run()
		{
			switch (_waveId)
			{
				case 1:
					// Sirra
					spawnNpc(_sirra, 114766, -113141, -11200, 15956, _world.getInstanceId());
					handleWorldState(1, _world.getInstanceId());
					break;
				case 3:
					if (_world == null)
					{
						break;
					}
					if (Util.contains(archery_blocked_status, _world.getStatus()))
					{
						break;
					}
					if (((_world._archery_knights_hard.size() < 5) || (_world._archery_knights.size() < 5)) && (_world.getStatus() < 44))
					{
						L2Attackable mob = null;
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						if (_isHard)
						{
							mob = (L2Attackable) spawnNpc(archery_knight_hard, spawnXY[0], spawnXY[1], -11200, 20016, _world.getInstanceId());
						}
						else
						{
							mob = (L2Attackable) spawnNpc(archery_knight, spawnXY[0], spawnXY[1], -11200, 20016, _world.getInstanceId());
						}
						mob.setOnKillDelay(0);
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.setRunning();
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						if (_isHard)
						{
							_world._archery_knights_hard.put(mob.getObjectId(), mob);
						}
						else
						{
							_world._archery_knights.put(mob.getObjectId(), mob);
						}
						if ((_world.getStatus() == 1) || (_world.getStatus() == 11) || (_world.getStatus() == 24) || (_world.getStatus() == 30) || (_world.getStatus() == 40))
						{
							mob.setIsImmobilized(true);
						}
					}
					break;
				case 5:
					if ((_world != null) && (_world._glaciers.size() < 5) && (_world.getStatus() < 44) && !Util.contains(glacier_blocked_status, _world.getStatus()))
					{
						int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
						L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 20016, _world.getInstanceId());
						_world._glaciers.put(mob.getObjectId(), mob);
					}
					if ((_world != null) && (_world.getStatus() < 44))
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, _world.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
					}
					break;
				case 6:
					for (int[] iter : _archeryKnightsSpawn)
					{
						L2Attackable mob = null;
						if (_isHard)
						{
							mob = (L2Attackable) spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], _world.getInstanceId());
						}
						else
						{
							mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], _world.getInstanceId());
						}
						mob.setOnKillDelay(0);
						mob.setRunning();
						L2PcInstance victim = getRandomPlayer(_world);
						mob.setTarget(victim);
						mob.addDamageHate(victim, 0, 9999);
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
						if (_isHard)
						{
							_world._archery_knights_hard.put(mob.getObjectId(), mob);
						}
						else
						{
							_world._archery_knights.put(mob.getObjectId(), mob);
						}
					}
					handleWorldState(_world.getStatus() + 1, _world);
					break;
				case 7:
					handleWorldState(2, _world.getInstanceId());
					break;
				case 9:
					handleWorldState(19, _world.getInstanceId());
					break;
				case 10:
					handleWorldState(20, _world.getInstanceId());
					break;
				case 11:
					handleWorldState(25, _world.getInstanceId());
					break;
				case 12:
					handleWorldState(30, _world.getInstanceId());
					break;
				case 13:
					handleWorldState(31, _world.getInstanceId());
					break;
				case 14:
					handleWorldState(41, _world.getInstanceId());
					break;
				case 15:
					handleWorldState(43, _world.getInstanceId());
					break;
				case 16:
					setInstanceRestriction(_world);
					InstanceManager.getInstance().getInstance(_world.getInstanceId()).setDuration(300000);
					InstanceManager.getInstance().getInstance(_world.getInstanceId()).setEmptyDestroyTime(0);
					handleWorldState(45, _world.getInstanceId());
					break;
				case 19:
					stopAll(_world);
					break;
				case 20:
					_world.isMovieNow = false;
					startAll(_world);
					break;
			}
		}
	}
	
	private void broadcastMovie(int movieId, FreyaWorld world)
	{
		world.isMovieNow = true;
		
		stopAll(world);
		
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null)
			{
				player.showQuestMovie(movieId);
			}
		}
		
		int pause = 0;
		
		switch (movieId)
		{
			case 15:
				pause = 53500;
				break;
			case 16:
				pause = 21100;
				break;
			case 17:
				pause = 21500;
				break;
			case 18:
				pause = 27000;
				break;
			case 19:
				pause = 16000;
				break;
			case 23:
				pause = 7000;
				break;
			case 20:
				pause = 55500;
				break;
			default:
				pause = 0;
		}
		
		if (movieId != 15)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(20, world.getInstanceId()), pause);
		}
		if (movieId == 19)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 100);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 200);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 500);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 1000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 2000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 3000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 4000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 5000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 6000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 7000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 8000);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(19, world.getInstanceId()), 9000);
		}
	}
	
	private void broadcastString(int strId, int instanceId)
	{
		ExShowScreenMessage2 sm = new ExShowScreenMessage2(strId, 3000, ExShowScreenMessage2.ScreenMessageAlign.TOP_CENTER, true, false, -1, true);
		Broadcast.toPlayersInInstance(sm, instanceId);
	}
	
	private void broadcastTimer(FreyaWorld world)
	{
		for (int objId : world.getAllowed())
		{
			L2PcInstance plr = L2World.getInstance().getPlayer(objId);
			ExSendUIEvent time_packet = new ExSendUIEvent(plr, false, false, 60, 0, "Time for prepare to next stage. Buffs please and wait for next stage!");
			if (plr != null)
			{
				plr.sendPacket(time_packet);
			}
		}
	}
	
	public void handleWorldState(int statusId, int instanceId)
	{
		FreyaWorld world = getWorld(instanceId);
		if (world != null)
		{
			handleWorldState(statusId, world);
		}
		else
		{
			_log.warn("IceQueenCastle2: Not Found world at handleWorldState(int, int).");
		}
	}
	
	public void handleWorldState(int statusId, FreyaWorld world)
	{
		int instanceId = world.getInstanceId();
		
		switch (statusId)
		{
			case 0:
				break;
			case 1:
				broadcastMovie(15, world);
				InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(door).openMe();
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(7, world.getInstanceId()), 52500);
				break;
			case 2:
				world._freyaThrone = (L2Attackable) spawnNpc(freyaOnThrone, 114720, -117085, -11088, 15956, instanceId);
				world._freyaThrone.setIsNoRndWalk(true);
				world._freyaThrone.setisReturningToSpawnPoint(false);
				world._freyaThrone.setOnKillDelay(0);
				world._freyaThrone.setIsInvul(true);
				world._freyaThrone.setIsImmobilized(true);
				for (int objId : world.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if ((player != null) && player.isOnline())
					{
						player.getKnownList().addKnownObject(world._freyaThrone);
					}
				}
				
				for (int[] iter : frozeKnightsSpawn)
				{
					L2Attackable mob = null;
					if (_isHard)
					{
						mob = (L2Attackable) spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					else
					{
						mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					archerySpawn(mob);
					world._simple_knights.put(mob.getObjectId(), mob);
				}
				
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					if (_isHard)
					{
						mob = (L2Attackable) spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					else
					{
						mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					if (_isHard)
					{
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					else
					{
						world._archery_knights.put(mob.getObjectId(), mob);
					}
				}
				
				for (int objId : world.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					player.setIsImmobilized(false);
					player.setIsInvul(false);
				}
				
				world.isMovieNow = false;
				break;
			case 10:
				broadcastString(1801086, world.getInstanceId());
				InstanceManager.getInstance().getInstance(world.getInstanceId()).getDoor(door).closeMe();
				world._freyaThrone.setIsInvul(false);
				world._freyaThrone.setIsImmobilized(false);
				world._freyaThrone.getAI();
				world._freyaThrone.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));
				
				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				
				if (_isHard)
				{
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
				}
				else
				{
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
				}
				break;
			case 11:
				broadcastMovie(16, world);
				if (_isHard)
				{
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						mob.deleteMe();
					}
					world._archery_knights_hard.clear();
				}
				else
				{
					for (L2Npc mob : world._archery_knights.values())
					{
						mob.deleteMe();
					}
					world._archery_knights.clear();
				}
				world._freyaThrone.deleteMe();
				world._freyaThrone = null;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(9, world.getInstanceId()), 22000);
				break;
			case 12:
				break;
			case 19:
				world._freyaSpelling = spawnNpc(freyaSpelling, 114723, -117502, -10672, 15956, world.getInstanceId());
				world._freyaSpelling.setIsImmobilized(true);
				broadcastTimer(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(10, world.getInstanceId()), 60000);
				break;
			case 20:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					if (_isHard)
					{
						mob = (L2Attackable) spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					else
					{
						mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					archerySpawn(mob);
					mob.setDisplayEffect(1);
					if (_isHard)
					{
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					else
					{
						world._archery_knights.put(mob.getObjectId(), mob);
					}
				}
				break;
			case 21:
				broadcastString(1801087, instanceId);
				if (_isHard)
				{
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
				}
				else
				{
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
				}
				
				for (int i = 0; i < 5; i++)
				{
					int[] spawnXY = getRandomPoint(114385, 115042, -115106, -114466);
					L2Attackable mob = (L2Attackable) spawnNpc(glacier, spawnXY[0], spawnXY[1], -11200, 0, instanceId);
					world._glaciers.put(mob.getObjectId(), mob);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				break;
			case 22:
			case 23:
				break;
			case 24:
				broadcastMovie(23, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(11, world.getInstanceId()), 7000);
				break;
			case 25:
				if (_isHard)
				{
					world._glakias_hard = (L2Attackable) spawnNpc(Glakias_hard, 114707, -114799, -11199, 15956, instanceId);
					world._glakias_hard.setOnKillDelay(0);
				}
				else
				{
					world._glakias = (L2Attackable) spawnNpc(Glakias, 114707, -114799, -11199, 15956, instanceId);
					world._glakias.setOnKillDelay(0);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(5, world.getInstanceId()), 7000);
				break;
			case 29:
				broadcastTimer(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(12, world.getInstanceId()), 60000);
				break;
			case 30:
				for (int[] iter : _archeryKnightsSpawn)
				{
					L2Attackable mob = null;
					if (_isHard)
					{
						mob = (L2Attackable) spawnNpc(archery_knight_hard, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					else
					{
						mob = (L2Attackable) spawnNpc(archery_knight, iter[0], iter[1], iter[2], iter[3], instanceId);
					}
					mob.setOnKillDelay(0);
					if (_isHard)
					{
						world._archery_knights_hard.put(mob.getObjectId(), mob);
					}
					else
					{
						world._archery_knights.put(mob.getObjectId(), mob);
					}
				}
				world._freyaSpelling.deleteMe();
				world._freyaSpelling = null;
				broadcastMovie(17, world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(13, world.getInstanceId()), 21500);
				break;
			case 31:
				ExChangeNpcState as = new ExChangeNpcState(decoration, 2);
				Broadcast.toPlayersInInstance(as, world.getInstanceId());
				for (int emitter : emmiters)
				{
					OnEventTrigger et = new OnEventTrigger(emitter, false);
					Broadcast.toPlayersInInstance(et, world.getInstanceId());
				}
				
				broadcastString(1801088, instanceId);
				if (_isHard)
				{
					world._freyaStand_hard = (L2Attackable) spawnNpc(freyaStand_hard, 114720, -117085, -11088, 15956, world.getInstanceId());
					world._freyaStand_hard.setOnKillDelay(0);
					world._freyaStand_hard.getAI();
					world._freyaStand_hard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));
				}
				else
				{
					world._freyaStand = (L2Attackable) spawnNpc(freyaStand, 114720, -117085, -11088, 15956, world.getInstanceId());
					world._freyaStand.setOnKillDelay(0);
					world._freyaStand.getAI();
					world._freyaStand.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114722, -114798, -11205, 15956));
				}
				for (int objId : world.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if ((player != null) && player.isOnline())
					{
						if (_isHard)
						{
							player.getKnownList().addKnownObject(world._freyaStand_hard);
						}
						else
						{
							player.getKnownList().addKnownObject(world._freyaStand);
						}
					}
				}
				break;
			case 40:
				broadcastMovie(18, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(14, world.getInstanceId()), 27000);
				break;
			case 41:
				if (_isHard)
				{
					for (L2Npc mob : world._archery_knights_hard.values())
					{
						archeryAttack(mob, world);
					}
				}
				else
				{
					for (L2Npc mob : world._archery_knights.values())
					{
						archeryAttack(mob, world);
					}
				}
				world._jinia = (L2Attackable) spawnNpc(18850, 114727, -114700, -11200, -16260, instanceId);
				world._jinia.setAutoAttackable(false);
				world._jinia.setIsMortal(false);
				world._kegor = (L2Attackable) spawnNpc(18851, 114690, -114700, -11200, -16260, instanceId);
				world._kegor.setAutoAttackable(false);
				world._kegor.setIsMortal(false);
				handleWorldState(42, instanceId);
				break;
			case 42:
				broadcastString(1801089, instanceId);
				if (_isHard)
				{
					if ((world._freyaStand_hard != null) && !world._freyaStand_hard.isDead())
					{
						world._jinia.setTarget(world._freyaStand_hard);
						world._jinia.setRunning();
						world._jinia.addDamageHate(world._freyaStand_hard, 0, 9999);
						world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
						world._kegor.setTarget(world._freyaStand_hard);
						world._kegor.setRunning();
						world._kegor.addDamageHate(world._freyaStand_hard, 0, 9999);
						world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand_hard);
					}
					else
					{
						world._jinia.setIsImmobilized(true);
						world._kegor.setIsImmobilized(true);
					}
				}
				else
				{
					if ((world._freyaStand != null) && !world._freyaStand.isDead())
					{
						world._jinia.setTarget(world._freyaStand);
						world._jinia.setRunning();
						world._jinia.addDamageHate(world._freyaStand, 0, 9999);
						world._jinia.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand);
						world._kegor.setTarget(world._freyaStand);
						world._kegor.setRunning();
						world._kegor.addDamageHate(world._freyaStand, 0, 9999);
						world._kegor.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._freyaStand);
					}
					else
					{
						world._jinia.setIsImmobilized(true);
						world._kegor.setIsImmobilized(true);
					}
				}
				L2Skill skill1 = SkillData.getInstance().getInfo(6288, 1);
				L2Skill skill2 = SkillData.getInstance().getInfo(6289, 1);
				for (int objId : world.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objId);
					if (player != null)
					{
						skill1.getEffects(world._jinia, player);
						skill2.getEffects(world._kegor, player);
					}
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(15, instanceId), 6000);
				break;
			case 43:
				break;
			case 44:
				broadcastMovie(19, world);
				stopAll(world);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(16, instanceId), 20000);
			case 45:
				broadcastMovie(20, world);
				handleWorldState(46, instanceId);
			case 46:
				for (L2Npc mob : InstanceManager.getInstance().getInstance(instanceId).getNpcs())
				{
					if (_isHard)
					{
						if (mob.getId() != freyaStand_hard)
						{
							mob.deleteMe();
							InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
						}
					}
					else
					{
						if (mob.getId() != freyaStand)
						{
							mob.deleteMe();
							InstanceManager.getInstance().getInstance(instanceId).getNpcs().remove(mob);
						}
					}
				}
				
				if (!_isHard)
				{
					for (int objId : world.getAllowed())
					{
						L2PcInstance player = L2World.getInstance().getPlayer(objId);
						final QuestState qs = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
						if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(6))
						{
							qs.setMemoState(10);
							qs.setCond(7, true);
						}
					}
				}
				
				break;
			default:
				_log.warn("IceQueenCastle2: Not handled world status - " + statusId);
				break;
		}
		world.setStatus(statusId);
	}
	
	public L2PcInstance getRandomPlayer(FreyaWorld world)
	{
		boolean exists = false;
		while (!exists)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(world.getAllowed().get(Rnd.get(0, world.getAllowed().size() - 1)));
			if (player != null)
			{
				exists = true;
				return player;
			}
		}
		return null;
	}
	
	public FreyaWorld getWorld(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
		{
			world = (FreyaWorld) tmpworld;
		}
		
		if (world == null)
		{
			_log.warn("IceQueenCastle2: World not found in getWorld(int instanceId)");
		}
		return world;
	}
	
	private int getWorldStatus(L2PcInstance player)
	{
		return getWorld(player).getStatus();
	}
	
	private FreyaWorld getWorld(L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		FreyaWorld world = null;
		if (tmpworld instanceof FreyaWorld)
		{
			world = (FreyaWorld) tmpworld;
		}
		
		if (world == null)
		{
			_log.warn("IceQueenCastle2: World not found in getWorld(int instanceId)");
		}
		return world;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getId();
		if (npcId == archery_knight_hard)
		{
			if (npc.getDisplayEffect() == 1)
			{
				npc.setDisplayEffect(2);
			}
			
			if (getWorldStatus(attacker) == 2)
			{
				handleWorldState(10, attacker.getInstanceId());
			}
			else if (getWorldStatus(attacker) == 20)
			{
				handleWorldState(21, attacker.getInstanceId());
			}
		}
		else if (npcId == freyaStand_hard)
		{
			double cur_hp = npc.getCurrentHp();
			double max_hp = npc.getMaxHp();
			int percent = (int) Math.round((cur_hp / max_hp) * 100);
			if ((percent <= 20) && (getWorldStatus(attacker) < 40))
			{
				handleWorldState(40, attacker.getInstanceId());
			}
		}
		else if (npcId == archery_knight)
		{
			if (npc.getDisplayEffect() == 1)
			{
				npc.setDisplayEffect(2);
			}
			
			if (getWorldStatus(attacker) == 2)
			{
				handleWorldState(10, attacker.getInstanceId());
			}
			else if (getWorldStatus(attacker) == 20)
			{
				handleWorldState(21, attacker.getInstanceId());
			}
		}
		else if (npcId == freyaStand)
		{
			if (!npc.isCastingNow())
			{
				callSkillAI(npc);
			}
			
			double cur_hp = npc.getCurrentHp();
			double max_hp = npc.getMaxHp();
			int percent = (int) Math.round((cur_hp / max_hp) * 100);
			if ((percent <= 20) && (getWorldStatus(attacker) < 40))
			{
				handleWorldState(40, attacker.getInstanceId());
			}
		}
		else if (npcId == freyaOnThrone)
		{
			if (!npc.isCastingNow())
			{
				callSkillAI(npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getId();
		FreyaWorld world = getWorld(killer);
		
		if (npcId == glacier)
		{
			if (world != null)
			{
				world._glaciers.remove(npc.getObjectId());
			}
			npc.setDisplayEffect(3);
			L2Npc mob = spawnNpc(archery_breathe, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			mob.setTarget(killer);
			((L2Attackable) mob).addDamageHate(killer, 0, 99999);
			mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
		else if (npcId == freyaOnThrone)
		{
			handleWorldState(11, killer.getInstanceId());
		}
		else if ((npcId == archery_knight) && (world != null))
		{
			if (world._archery_knights.containsKey(npc.getObjectId()))
			{
				world._archery_knights.remove(npc.getObjectId());
				
				if ((world.getStatus() > 20) && (world.getStatus() < 24))
				{
					if (world._archery_knights.size() == 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 30000);
					}
				}
				else if (world.getStatus() < 44)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (getRandom(10, 40) * 1000) + 30000);
				}
			}
			else if (world._simple_knights.containsKey(npc.getObjectId()))
			{
				world._simple_knights.remove(npc.getObjectId());
				startQuestTimer("spawndeco_" + npc.getSpawn().getX() + "_" + npc.getSpawn().getY() + "_" + npc.getSpawn().getZ() + "_" + npc.getSpawn().getHeading() + "_" + npc.getInstanceId(), 30000, null, null);
			}
		}
		else if ((npcId == archery_knight_hard) && (world != null))
		{
			if (world._archery_knights_hard.containsKey(npc.getObjectId()))
			{
				world._archery_knights_hard.remove(npc.getObjectId());
				
				if ((world.getStatus() > 20) && (world.getStatus() < 24))
				{
					if (world._archery_knights_hard.size() == 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 8000);
					}
				}
				else if (world.getStatus() < 44)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
				}
			}
			else if (world._simple_knights.containsKey(npc.getObjectId()))
			{
				world._simple_knights.remove(npc.getObjectId());
				startQuestTimer("spawndeco_" + npc.getSpawn().getX() + "_" + npc.getSpawn().getY() + "_" + npc.getSpawn().getZ() + "_" + npc.getSpawn().getHeading() + "_" + npc.getInstanceId(), 30000, null, null);
			}
		}
		else if ((npcId == archery_knight) && (world != null))
		{
			if (world._archery_knights.containsKey(npc.getObjectId()))
			{
				world._archery_knights.remove(npc.getObjectId());
				
				if ((world.getStatus() > 20) && (world.getStatus() < 24))
				{
					if (world._archery_knights.size() == 0)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(6, killer.getInstanceId()), 8000);
					}
				}
				else if (world.getStatus() < 44)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(3, killer.getInstanceId()), (Rnd.get(10, 40) * 1000) + 20000);
				}
			}
			else if (world._simple_knights.containsKey(npc.getObjectId()))
			{
				world._simple_knights.remove(npc.getObjectId());
				startQuestTimer("spawndeco_" + npc.getSpawn().getX() + "_" + npc.getSpawn().getY() + "_" + npc.getSpawn().getZ() + "_" + npc.getSpawn().getHeading() + "_" + npc.getInstanceId(), 20000, null, null);
			}
		}
		else if (npcId == Glakias_hard)
		{
			handleWorldState(29, killer.getInstanceId());
		}
		else if (npcId == freyaStand_hard)
		{
			handleWorldState(44, killer.getInstanceId());
		}
		else if (npcId == Glakias)
		{
			handleWorldState(29, killer.getInstanceId());
		}
		else if (npcId == freyaStand)
		{
			handleWorldState(44, killer.getInstanceId());
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		FreyaWorld world = getWorld(npc.getInstanceId());
		if ((world != null) && (world.getStatus() >= 44))
		{
			npc.deleteMe();
		}
		
		if ((world != null) && world.isMovieNow && (npc instanceof L2Attackable))
		{
			npc.abortAttack();
			npc.abortCast();
			npc.setIsImmobilized(true);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		if (npc.getId() == glacier)
		{
			npc.setDisplayEffect(1);
			npc.setIsImmobilized(true);
			((L2Attackable) npc).setOnKillDelay(0);
			startQuestTimer("setDisplayEffect2", 1000, npc, null);
			startQuestTimer("cast", 20000, npc, null);
		}
		return super.onSpawn(npc);
	}
	
	private void enterInstance(L2PcInstance player, String template)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (world instanceof FreyaWorld)
			{
				player.stopAllEffectsExceptThoseThatLastThroughDeath();
				if (player.hasSummon())
				{
					player.getSummon().stopAllEffectsExceptThoseThatLastThroughDeath();
				}
				
				teleportPlayer(player, (FreyaWorld) world);
				return;
			}
			player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
			return;
		}
		// New instance
		if (!checkConditions(player))
		{
			return;
		}
		
		world = new FreyaWorld();
		world.setInstanceId(InstanceManager.getInstance().createDynamicInstance(template));
		world.setTemplateId(TEMPLATE_ID);
		world.setStatus(0);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Freya started " + template + " Instance: " + world.getInstanceId() + " created by player: " + player.getName());
		
		if (player.isGM())
		{
			world.addAllowed(player.getObjectId());
			teleportPlayer(player, (FreyaWorld) world);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.getInstanceId()), 100);
			return;
		}
		
		L2Party party = player.getParty();
		if ((party != null) && party.isInCommandChannel())
		{
			int count = 1;
			for (L2PcInstance plr : party.getCommandChannel().getMembers())
			{
				world.addAllowed(plr.getObjectId());
				_log.info("Freya Party Member " + count + ", Member name is: " + plr.getName());
				count++;
				teleportPlayer(plr, (FreyaWorld) world);
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnWave(1, world.getInstanceId()), 100);
			return;
		}
	}
	
	private boolean checkConditions(L2PcInstance player)
	{
		final L2Party party = player.getParty();
		final L2CommandChannel channel = party != null ? party.getCommandChannel() : null;
		
		if (player.isGM() && player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS))
		{
			return true;
		}
		
		if (party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		else if (channel == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}
		else if (player != channel.getLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if (_isHard)
		{
			if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_PLAYERS_TO_HARD)
			{
				player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2793).addInt(10));
				return false;
			}
			
			if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_PLAYERS_TO_HARD)
			{
				player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2102));
				return false;
			}
		}
		else
		{
			if (player.getParty().getCommandChannel().getMemberCount() < Config.MIN_PLAYERS_TO_EASY)
			{
				player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2793).addInt(10));
				return false;
			}
			
			if (player.getParty().getCommandChannel().getMemberCount() > Config.MAX_PLAYERS_TO_EASY)
			{
				player.getParty().getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(2102));
				return false;
			}
		}
		
		for (L2PcInstance partyMember : player.getParty().getCommandChannel().getMembers())
		{
			if (_isHard)
			{
				QuestState st = partyMember.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
				if ((st == null) || !st.isCompleted())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					player.getParty().getCommandChannel().broadcastPacket(sm);
					return false;
				}
				
				if (partyMember.getLevel() < Config.MIN_PLAYER_LEVEL_TO_HARD)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(2097);
					sm.addPcName(partyMember);
					player.getParty().getCommandChannel().broadcastPacket(sm);
					return false;
				}
			}
			else
			{
				if (partyMember.getLevel() < Config.MIN_PLAYER_LEVEL_TO_EASY)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(2097);
					sm.addPcName(partyMember);
					player.getParty().getCommandChannel().broadcastPacket(sm);
					return false;
				}
			}
			
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2096);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
			
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), TEMPLATE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(2100);
				sm.addPcName(partyMember);
				player.getParty().getCommandChannel().broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}
	
	private void teleportPlayer(L2PcInstance player, FreyaWorld world)
	{
		_log.info("Teleporting player to Freya: " + player.getName());
		
		final QuestState qs = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
		if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(5))
		{
			qs.setCond(6, true);
		}
		
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.getInstanceId());
		player.teleToLocation(113991, -112297, -11200);
		if (player.hasSummon())
		{
			player.getSummon().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getSummon().setInstanceId(world.getInstanceId());
			player.getSummon().teleToLocation(113991, -112297, -11200);
		}
		return;
	}
	
	public void setInstanceRestriction(FreyaWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		// if time is >= RESET_HOUR - roll to the next day
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (reenter.get(Calendar.DAY_OF_WEEK) <= Calendar.WEDNESDAY)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(TEMPLATE_ID));
		
		// set instance reenter time for all allowed players
		for (int objectId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, TEMPLATE_ID, reenter.getTimeInMillis());
			if ((player != null) && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("normalEnter"))
		{
			_isHard = false;
			enterInstance(player, "IceQueenCastle2.xml");
		}
		else if (event.equalsIgnoreCase("hardEnter"))
		{
			_isHard = true;
			enterInstance(player, "IceQueenCastle2.xml");
		}
		else if (event.startsWith("spawndeco"))
		{
			String[] params = event.split("_");
			FreyaWorld world = getWorld(Integer.parseInt(params[5]));
			if ((world != null) && (world.getStatus() < 44))
			{
				L2Attackable mob = null;
				if (_isHard)
				{
					mob = (L2Attackable) spawnNpc(archery_knight_hard, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
				}
				else
				{
					mob = (L2Attackable) spawnNpc(archery_knight, Integer.parseInt(params[1]), Integer.parseInt(params[2]), Integer.parseInt(params[3]), Integer.parseInt(params[4]), Integer.parseInt(params[5]));
				}
				mob.setIsImmobilized(true);
				mob.setDisplayEffect(1);
				world._simple_knights.put(mob.getObjectId(), mob);
			}
		}
		else if (event.equalsIgnoreCase("setDisplayEffect2"))
		{
			if (!npc.isDead())
			{
				npc.setDisplayEffect(2);
			}
		}
		else if (event.equalsIgnoreCase("show_string"))
		{
			if ((npc != null) && !npc.isDead())
			{
				broadcastString(1801111, npc.getInstanceId());
			}
		}
		else if (event.equalsIgnoreCase("summon_breathe"))
		{
			L2Npc mob = spawnNpc(archery_breathe, npc.getX() + getRandom(-90, 90), npc.getY() + getRandom(-90, 90), npc.getZ(), npc.getHeading(), npc.getInstanceId());
			mob.setRunning();
			if (npc.getTarget() != null)
			{
				mob.setTarget(npc.getTarget());
				((L2Attackable) mob).addDamageHate((L2Character) npc.getTarget(), 0, 99999);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc.getTarget());
			}
		}
		else if (event.equalsIgnoreCase("cast"))
		{
			if ((npc != null) && !npc.isDead())
			{
				L2Skill skill = SkillData.getInstance().getInfo(6437, getRandom(1, 3));
				for (L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(skill.getAffectRange()))
				{
					if (!hasBuff(6437, plr) && !plr.isDead() && !plr.isAlikeDead())
					{
						skill.getEffects(npc, plr);
					}
				}
				startQuestTimer("cast", 20000, npc, null);
			}
		}
		return null;
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((npc.getId() == archery_breathe) || (npc.getId() == archery_knight) || (npc.getId() == archery_knight_hard))
		{
			if (npc.isImmobilized())
			{
				npc.abortAttack();
				npc.abortCast();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	public int[] getRandomPoint(int min_x, int max_x, int min_y, int max_y)
	{
		int[] ret =
		{
			0,
			0
		};
		ret[0] = Rnd.get(min_x, max_x);
		ret[1] = Rnd.get(min_y, max_y);
		return ret;
	}
	
	@Override
	public L2Npc spawnNpc(int npcId, int x, int y, int z, int heading, int instId)
	{
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		Instance inst = InstanceManager.getInstance().getInstance(instId);
		try
		{
			L2Spawn npcSpawn = new L2Spawn(npcTemplate);
			npcSpawn.setX(x);
			npcSpawn.setY(y);
			npcSpawn.setZ(z);
			npcSpawn.setHeading(heading);
			npcSpawn.setAmount(1);
			npcSpawn.setInstanceId(instId);
			SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
			L2Npc npc = npcSpawn.spawnOne(false);
			inst.addNpc(npc);
			return npc;
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	private void archerySpawn(L2Npc mob)
	{
		((L2Attackable) mob).setOnKillDelay(0);
		mob.setDisplayEffect(1);
		mob.setIsImmobilized(true);
	}
	
	private void archeryAttack(L2Npc mob, FreyaWorld world)
	{
		mob.setDisplayEffect(2);
		mob.setIsImmobilized(false);
		mob.setRunning();
		L2PcInstance victim = getRandomPlayer(world);
		mob.setTarget(victim);
		((L2Attackable) mob).addDamageHate(victim, 0, 9999);
		mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim);
	}
	
	public void stopAll(FreyaWorld world)
	{
		if (world == null)
		{
			return;
		}
		
		if (_isHard)
		{
			if ((world._freyaStand_hard != null) && !world._freyaStand_hard.isDead())
			{
				if (world._freyaStand_hard.getTarget() != null)
				{
					world._freyaStand_hard.abortAttack();
					world._freyaStand_hard.abortCast();
					world._freyaStand_hard.setTarget(null);
					world._freyaStand_hard.clearAggroList();
					world._freyaStand_hard.setIsImmobilized(true);
					world._freyaStand_hard.teleToLocation(world._freyaStand_hard.getX() - 100, world._freyaStand_hard.getY() + 100, world._freyaStand_hard.getZ(), world._freyaStand_hard.getHeading(), false);
				}
			}
		}
		else
		{
			if ((world._freyaStand != null) && !world._freyaStand.isDead())
			{
				if (world._freyaStand.getTarget() != null)
				{
					world._freyaStand.abortAttack();
					world._freyaStand.abortCast();
					world._freyaStand.setTarget(null);
					world._freyaStand.clearAggroList();
					world._freyaStand.setIsImmobilized(true);
					world._freyaStand.teleToLocation(world._freyaStand.getX() - 100, world._freyaStand.getY() + 100, world._freyaStand.getZ(), world._freyaStand.getHeading(), false);
				}
			}
		}
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.getInstanceId()).getNpcs())
		{
			if ((mob != null) && !mob.isDead())
			{
				mob.abortAttack();
				mob.abortCast();
				if (mob instanceof L2Attackable)
				{
					((L2Attackable) mob).clearAggroList();
				}
				mob.setIsImmobilized(true);
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
		
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null)
			{
				player.abortAttack();
				player.abortCast();
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				player.setIsImmobilized(true);
				player.setIsInvul(true);
			}
		}
	}
	
	public void startAll(FreyaWorld world)
	{
		if (world == null)
		{
			return;
		}
		
		for (L2Npc mob : InstanceManager.getInstance().getInstance(world.getInstanceId()).getNpcs())
		{
			L2Object target = null;
			
			if (mob.getTarget() != null)
			{
				target = mob.getTarget();
			}
			else
			{
				target = getRandomPlayer(world);
			}
			
			if ((mob.getId() != glacier) && !world._simple_knights.containsKey(mob.getObjectId()) && (mob instanceof L2Attackable))
			{
				((L2Attackable) mob).addDamageHate((L2Character) target, 0, 9999);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				mob.setIsImmobilized(false);
			}
		}
		
		for (int objId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null)
			{
				player.setIsImmobilized(false);
				if (player.getFirstEffect(L2EffectType.INVINCIBLE) == null)
				{
					player.setIsInvul(false);
				}
			}
		}
	}
	
	private void callSkillAI(L2Npc mob)
	{
		int[][] freya_skills =
		{
			{
				6274,
				1,
				4000,
				10
			},
			{
				6276,
				1,
				-1,
				100
			},
			{
				6277,
				1,
				-1,
				100
			},
			{
				6278,
				1,
				-1,
				100
			},
			{
				6279,
				1,
				2000,
				100
			},
			{
				6282,
				1,
				-1,
				100
			}
		};
		
		int iter = getRandom(0, 2);
		
		if ((freya_skills[iter][3] < 100) && (getRandom(100) > freya_skills[iter][3]))
		{
			iter = 3;
		}
		
		mob.doCast(SkillData.getInstance().getInfo(freya_skills[iter][0], freya_skills[iter][1]));
		if (freya_skills[iter][2] > 0)
		{
			startQuestTimer("show_string", freya_skills[iter][2], mob, null);
		}
		
		if (freya_skills[iter][0] == 6277)
		{
			startQuestTimer("summon_breathe", 10000, mob, null);
		}
	}
	
	private boolean hasBuff(int id, L2PcInstance player)
	{
		for (L2Effect e : player.getAllEffects())
		{
			if (e.getSkill().getId() == id)
			{
				return true;
			}
		}
		return false;
	}
	
	public IceQueenCastle2()
	{
		super(IceQueenCastle2.class.getSimpleName(), "instances");
		addTalkId(Jinia);
		
		// Easy and hard
		addAttackId(freyaStand);
		addSpawnId(glacier, 18854);
		addKillId(freyaOnThrone, freyaSpelling, glacier);
		
		// Easy
		addAttackId(archery_knight, freyaStand);
		addKillId(freyaStand, archery_knight, Glakias);
		addSpawnId(archery_knight);
		addAggroRangeEnterId(archery_knight);
		
		// Hard
		addAttackId(archery_knight_hard, freyaStand_hard);
		addKillId(freyaStand_hard, Glakias_hard, archery_knight_hard);
		addSpawnId(archery_knight_hard);
		addAggroRangeEnterId(archery_knight_hard);
	}
	
	public static void main(String[] args)
	{
		new IceQueenCastle2();
	}
}