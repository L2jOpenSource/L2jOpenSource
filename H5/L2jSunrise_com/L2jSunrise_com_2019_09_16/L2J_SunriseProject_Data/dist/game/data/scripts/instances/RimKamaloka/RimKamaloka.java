package instances.RimKamaloka;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class RimKamaloka extends AbstractNpcAI
{
	public RimKamaloka()
	{
		super(RimKamaloka.class.getSimpleName(), "instances");
		
		addStartNpc(START_NPC);
		// addFirstTalkId(START_NPC);
		addFirstTalkId(REWARDER);
		addTalkId(START_NPC);
		addTalkId(REWARDER);
		
		for (int[] list : KANABIONS)
		{
			addFactionCallId(list[0]);
			addAttackId(list);
			addKillId(list);
		}
	}
	
	/*
	 * Reset time for all kamaloka Default: 6:30AM on server time
	 */
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	
	private static final int LOCK_TIME = 10;
	
	/*
	 * Duration of the instance, minutes
	 */
	private static final int DURATION = 20;
	
	/*
	 * Time after which instance without players will be destroyed Default: 5 minutes
	 */
	private static final int EMPTY_DESTROY_TIME = 5;
	
	/*
	 * Time to destroy instance (and eject player away) Default: 10 minutes
	 */
	private static final int EXIT_TIME = 10;
	
	/*
	 * Maximum level difference between players level and kamaloka level Default: 5
	 */
	private static final int MAX_LEVEL_DIFFERENCE = 5;
	
	private static final int RESPAWN_DELAY = 30;
	
	private static final int DESPAWN_DELAY = 10000;
	
	/*
	 * Hardcoded instance ids for kamaloka
	 */
	private static final int[] INSTANCE_IDS =
	{
		46,
		47,
		48,
		49,
		50,
		51,
		52,
		53,
		54,
		55,
		56
	};
	
	/*
	 * Level of the kamaloka
	 */
	private static final int[] LEVEL =
	{
		25,
		30,
		35,
		40,
		45,
		50,
		55,
		60,
		65,
		70,
		75
	};
	
	/*
	 * Teleport points into instances x, y, z
	 */
	private static final Location[] TELEPORTS =
	{
		new Location(10025, -219868, -8021),
		new Location(15617, -219883, -8021),
		new Location(22742, -220079, -7802),
		new Location(8559, -212987, -7802),
		new Location(15867, -212994, -7802),
		new Location(23038, -213052, -8007),
		new Location(9139, -205132, -8007),
		new Location(15943, -205740, -8008),
		new Location(22343, -206237, -7991),
		new Location(41496, -219694, -8759),
		new Location(48137, -219716, -8759)
	};
	
	private static final int[][] KANABIONS =
	{
		{
			22452,
			22453,
			22454
		},
		{
			22455,
			22456,
			22457
		},
		{
			22458,
			22459,
			22460
		},
		{
			22461,
			22462,
			22463
		},
		{
			22464,
			22465,
			22466
		},
		{
			22467,
			22468,
			22469
		},
		{
			22470,
			22471,
			22472
		},
		{
			22473,
			22474,
			22475
		},
		{
			22476,
			22477,
			22478
		},
		{
			22479,
			22480,
			22481
		},
		{
			22482,
			22483,
			22484
		}
	};
	
	private static final int[][][] SPAWNLIST =
	{
		{
			{
				8971,
				-219546,
				-8021
		},
			{
				9318,
				-219644,
				-8021
		},
			{
				9266,
				-220208,
				-8021
		},
			{
				9497,
				-220054,
				-8024
		}
		},
		{
			{
				16107,
				-219574,
				-8021
		},
			{
				16769,
				-219885,
				-8021
		},
			{
				16363,
				-220219,
				-8021
		},
			{
				16610,
				-219523,
				-8021
		}
		},
		{
			{
				23019,
				-219730,
				-7803
		},
			{
				23351,
				-220455,
				-7803
		},
			{
				23900,
				-219864,
				-7803
		},
			{
				23851,
				-220294,
				-7803
		}
		},
		{
			{
				9514,
				-212478,
				-7803
		},
			{
				9236,
				-213348,
				-7803
		},
			{
				8868,
				-212683,
				-7803
		},
			{
				9719,
				-213042,
				-7803
		}
		},
		{
			{
				16925,
				-212811,
				-7803
		},
			{
				16885,
				-213199,
				-7802
		},
			{
				16487,
				-213339,
				-7803
		},
			{
				16337,
				-212529,
				-7803
		}
		},
		{
			{
				23958,
				-213282,
				-8009
		},
			{
				23292,
				-212782,
				-8012
		},
			{
				23844,
				-212781,
				-8009
		},
			{
				23533,
				-213301,
				-8009
		}
		},
		{
			{
				8828,
				-205518,
				-8009
		},
			{
				8895,
				-205989,
				-8009
		},
			{
				9398,
				-205967,
				-8009
		},
			{
				9393,
				-205409,
				-8009
		}
		},
		{
			{
				16185,
				-205472,
				-8009
		},
			{
				16808,
				-205929,
				-8009
		},
			{
				16324,
				-206042,
				-8009
		},
			{
				16782,
				-205454,
				-8009
		}
		},
		{
			{
				23476,
				-206310,
				-7991
		},
			{
				23230,
				-205861,
				-7991
		},
			{
				22644,
				-205888,
				-7994
		},
			{
				23078,
				-206714,
				-7991
		}
		},
		{
			{
				42981,
				-219308,
				-8759
		},
			{
				42320,
				-220160,
				-8759
		},
			{
				42434,
				-219181,
				-8759
		},
			{
				42101,
				-219550,
				-8759
		},
			{
				41859,
				-220236,
				-8759
		},
			{
				42881,
				-219942,
				-8759
		}
		},
		{
			{
				48770,
				-219304,
				-8759
		},
			{
				49036,
				-220190,
				-8759
		},
			{
				49363,
				-219814,
				-8759
		},
			{
				49393,
				-219102,
				-8759
		},
			{
				49618,
				-220490,
				-8759
		},
			{
				48526,
				-220493,
				-8759
		}
		}
	};
	
	public static final int[][] REWARDERS =
	{
		{
			9261,
			-219862,
			-8021
		},
		{
			16301,
			-219806,
			-8021
		},
		{
			23478,
			-220079,
			-7799
		},
		{
			9290,
			-212993,
			-7799
		},
		{
			16598,
			-212997,
			-7802
		},
		{
			23650,
			-213051,
			-8007
		},
		{
			9136,
			-205733,
			-8007
		},
		{
			16508,
			-205737,
			-8007
		},
		{
			23229,
			-206316,
			-7991
		},
		{
			42638,
			-219781,
			-8759
		},
		{
			49014,
			-219737,
			-8759
		}
	};
	
	private static final int START_NPC = 32484;
	
	private static final int REWARDER = 32485;
	
	private static final int[][][] REWARDS =
	{
		{ // 20-30
			null, // Grade F
			{
				13002,
				2,
				10839,
				1
		}, // Grade D
			{
				13002,
				2,
				10838,
				1
		}, // Grade C
			{
				13002,
				2,
				10837,
				1
		}, // Grade B
			{
				13002,
				2,
				10836,
				1
		}, // Grade A
			{
				13002,
				2,
				12824,
				1
		}
		// Grade S
		},
		{ // 25-35
			null,
			{
				13002,
				3,
				10838,
				1
		},
			{
				13002,
				3,
				10837,
				1
		},
			{
				13002,
				3,
				10836,
				1
		},
			{
				13002,
				3,
				10840,
				1
		},
			{
				13002,
				3,
				12825,
				1
		}
		},
		{ // 30-40
			null,
			{
				13002,
				3,
				10841,
				1
		},
			{
				13002,
				3,
				10842,
				1
		},
			{
				13002,
				3,
				10843,
				1
		},
			{
				13002,
				3,
				10844,
				1
		},
			{
				13002,
				3,
				12826,
				1
		}
		},
		{ // 35-45
			null,
			{
				13002,
				5,
				10842,
				1
		},
			{
				13002,
				5,
				10843,
				1
		},
			{
				13002,
				5,
				10844,
				1
		},
			{
				13002,
				5,
				10845,
				1
		},
			{
				13002,
				5,
				12827,
				1
		}
		},
		{ // 40-50
			null,
			{
				13002,
				7,
				10846,
				1
		},
			{
				13002,
				7,
				10847,
				1
		},
			{
				13002,
				7,
				10848,
				1
		},
			{
				13002,
				7,
				10849,
				1
		},
			{
				13002,
				7,
				12828,
				1
		}
		},
		{ // 45-55
			null,
			{
				13002,
				8,
				10847,
				1
		},
			{
				13002,
				8,
				10848,
				1
		},
			{
				13002,
				8,
				10849,
				1
		},
			{
				13002,
				8,
				10850,
				1
		},
			{
				13002,
				8,
				12829,
				1
		}
		},
		{ // 50-60
			null,
			{
				13002,
				10,
				10851,
				1
		},
			{
				13002,
				10,
				10852,
				1
		},
			{
				13002,
				10,
				10853,
				1
		},
			{
				13002,
				10,
				10854,
				1
		},
			{
				13002,
				10,
				12830,
				1
		}
		},
		{ // 55-65
			null,
			{
				13002,
				12,
				10852,
				1
		},
			{
				13002,
				12,
				10853,
				1
		},
			{
				13002,
				12,
				10854,
				1
		},
			{
				13002,
				12,
				10855,
				1
		},
			{
				13002,
				12,
				12831,
				1
		}
		},
		{ // 60-70
			null,
			{
				13002,
				13,
				10856,
				1
		},
			{
				13002,
				13,
				10857,
				1
		},
			{
				13002,
				13,
				10858,
				1
		},
			{
				13002,
				13,
				10859,
				1
		},
			{
				13002,
				13,
				12832,
				1
		}
		},
		{ // 65-75
			null,
			{
				13002,
				15,
				10857,
				1
		},
			{
				13002,
				15,
				10858,
				1
		},
			{
				13002,
				15,
				10859,
				1
		},
			{
				13002,
				15,
				10860,
				1
		},
			{
				13002,
				15,
				12833,
				1
		}
		},
		{ // 70-80
			null,
			{
				13002,
				17,
				10861,
				1
		},
			{
				13002,
				17,
				12834,
				1
		},
			{
				13002,
				17,
				10862,
				1
		},
			{
				13002,
				17,
				10863,
				1
		},
			{
				13002,
				17,
				10864,
				1
		}
		}
	};
	
	private class RimKamaWorld extends InstanceWorld
	{
		public int index;
		public int KANABION;
		public int DOPPLER;
		public int VOIDER;
		
		public int kanabionsCount = 0;
		public int dopplersCount = 0;
		public int voidersCount = 0;
		public int grade = 0;
		public boolean isFinished = false;
		public boolean isRewarded = false;
		
		@SuppressWarnings("unused")
		public ScheduledFuture<?> lockTask = null;
		public ScheduledFuture<?> finishTask = null;
		
		public Set<L2MonsterInstance> spawnedMobs = ConcurrentHashMap.newKeySet();
		public Map<Integer, Long> lastAttack = new ConcurrentHashMap<>();
		public ScheduledFuture<?> despawnTask = null;
		
		public RimKamaWorld()
		{
			// InstanceManager.getInstance().super();
		}
	}
	
	/**
	 * Check if party with player as leader allowed to enter
	 * @param player party leader
	 * @param index (0-17) index of the kamaloka in arrays
	 * @return true if party allowed to enter
	 */
	private static final boolean checkConditions(L2PcInstance player, int index)
	{
		final L2Party party = player.getParty();
		// player must not be in party
		if (party != null)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		
		// get level of the instance
		final int level = LEVEL[index];
		// and client name
		final String instanceName = InstanceManager.getInstance().getInstanceIdName(INSTANCE_IDS[index]);
		
		// player level must be in range
		if (Math.abs(player.getLevel() - level) > MAX_LEVEL_DIFFERENCE)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		// get instances reenter times for player
		final Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player.getObjectId());
		if (instanceTimes != null)
		{
			for (int id : instanceTimes.keySet())
			{
				// find instance with same name (kamaloka or labyrinth)
				if (!instanceName.equals(InstanceManager.getInstance().getInstanceIdName(id)))
				{
					continue;
				}
				// if found instance still can't be reentered - exit
				if (System.currentTimeMillis() < instanceTimes.get(id))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_RE_ENTER_YET);
					sm.addPcName(player);
					player.sendPacket(sm);
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Handling enter of the players into kamaloka
	 * @param player party leader
	 * @param index (0-17) kamaloka index in arrays
	 */
	protected synchronized final void enterInstance(L2PcInstance player, int index)
	{
		int templateId;
		try
		{
			templateId = INSTANCE_IDS[index];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return;
		}
		
		// check for existing instances for this player
		InstanceWorld tmpWorld = InstanceManager.getInstance().getPlayerWorld(player);
		// player already in the instance
		if (tmpWorld != null)
		{
			// but not in kamaloka
			if (!(tmpWorld instanceof RimKamaWorld) || (tmpWorld.getTemplateId() != templateId))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			RimKamaWorld world = (RimKamaWorld) tmpWorld;
			// check for level difference again on reenter
			if (Math.abs(player.getLevel() - LEVEL[world.index]) > MAX_LEVEL_DIFFERENCE)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
				sm.addPcName(player);
				player.sendPacket(sm);
				return;
			}
			// check what instance still exist
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(player, TELEPORTS[index], world.getInstanceId());
			}
		}
		// Creating new kamaloka instance
		else
		{
			if (!checkConditions(player, index))
			{
				return;
			}
			
			// Creating dynamic instance without template
			final int instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
			// set name for the kamaloka
			inst.setName(InstanceManager.getInstance().getInstanceIdName(templateId));
			// set return location
			inst.setExitLoc(new Location(player));
			// disable summon friend into instance
			inst.setAllowSummon(false);
			
			// Creating new instanceWorld, using our instanceId and templateId
			RimKamaWorld world = new RimKamaWorld();
			world.setInstanceId(instanceId);
			world.setTemplateId(templateId);
			// set index for easy access to the arrays
			world.index = index;
			InstanceManager.getInstance().addWorld(world);
			
			// spawn npcs
			spawnKama(world);
			world.finishTask = ThreadPoolManager.getInstance().scheduleGeneral(new FinishTask(world), DURATION * 60000);
			world.lockTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockTask(world), LOCK_TIME * 60000);
			world.despawnTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DespawnTask(world), 1000, 1000);
			
			world.addAllowed(player.getObjectId());
			
			teleportPlayer(player, TELEPORTS[index], instanceId);
		}
	}
	
	/**
	 * Spawn all NPCs in kamaloka
	 * @param world instanceWorld
	 */
	private static final void spawnKama(RimKamaWorld world)
	{
		int[][] spawnlist;
		final int index = world.index;
		world.KANABION = KANABIONS[index][0];
		world.DOPPLER = KANABIONS[index][1];
		world.VOIDER = KANABIONS[index][2];
		
		try
		{
			final L2NpcTemplate mob1 = NpcTable.getInstance().getTemplate(world.KANABION);
			spawnlist = SPAWNLIST[index];
			
			L2Spawn spawn;
			for (final int[] loc : spawnlist)
			{
				spawn = new L2Spawn(mob1);
				spawn.setInstanceId(world.getInstanceId());
				spawn.setX(loc[0]);
				spawn.setY(loc[1]);
				spawn.setZ(loc[2]);
				spawn.setHeading(-1);
				spawn.setRespawnDelay(RESPAWN_DELAY);
				spawn.setAmount(1);
				spawn.startRespawn();
				spawn.doSpawn();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static final void spawnNextMob(RimKamaWorld world, L2Npc oldNpc, int npcId, L2PcInstance player)
	{
		if (world.isFinished)
		{
			return;
		}
		
		L2MonsterInstance monster = null;
		if (!world.spawnedMobs.isEmpty())
		{
			for (L2MonsterInstance mob : world.spawnedMobs)
			{
				if ((mob == null) || !mob.isDecayed() || (mob.getId() != npcId))
				{
					continue;
				}
				mob.setDecayed(false);
				mob.setIsDead(false);
				mob.overhitEnabled(false);
				mob.refreshID();
				monster = mob;
				break;
			}
		}
		
		if (monster == null)
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			monster = new L2MonsterInstance(template);
			world.spawnedMobs.add(monster);
		}
		
		synchronized (world.lastAttack)
		{
			world.lastAttack.put(monster.getObjectId(), System.currentTimeMillis());
		}
		
		monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(oldNpc.getHeading());
		monster.setInstanceId(oldNpc.getInstanceId());
		monster.spawnMe(oldNpc.getX(), oldNpc.getY(), oldNpc.getZ() + 20);
		monster.setRunning();
		monster.addDamageHate(player, 0, 9999);
		monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}
	
	private synchronized final void rewardPlayer(RimKamaWorld world, L2Npc npc)
	{
		if (!world.isFinished || world.isRewarded)
		{
			return;
		}
		world.isRewarded = true;
		
		final int[][] allRewards = REWARDS[world.index];
		world.grade = Math.min(world.grade, allRewards.length);
		final int[] reward = allRewards[world.grade];
		if (reward == null)
		{
			return;
		}
		for (int objectId : world.getAllowed())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			if ((player != null) && player.isOnline())
			{
				player.sendMessage("Grade:" + world.grade);
				for (int i = 0; i < reward.length; i += 2)
				{
					player.addItem("Reward", reward[i], reward[i + 1], npc, true);
				}
			}
		}
	}
	
	class LockTask implements Runnable
	{
		private final RimKamaWorld _world;
		
		LockTask(RimKamaWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (_world != null)
			{
				Calendar reenter = Calendar.getInstance();
				reenter.set(Calendar.MINUTE, RESET_MIN);
				// if time is >= RESET_HOUR - roll to the next day
				if (reenter.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
				{
					reenter.roll(Calendar.DATE, true);
				}
				reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_FROM_HERE_S1_S_ENTRY_HAS_BEEN_RESTRICTED);
				sm.addString(InstanceManager.getInstance().getInstanceIdName(_world.getTemplateId()));
				
				// set instance reenter time for all allowed players
				boolean found = false;
				for (int objectId : _world.getAllowed())
				{
					L2PcInstance player = L2World.getInstance().getPlayer(objectId);
					if ((player != null) && player.isOnline())
					{
						found = true;
						InstanceManager.getInstance().setInstanceTime(objectId, _world.getTemplateId(), reenter.getTimeInMillis());
						player.sendPacket(sm);
					}
				}
				if (!found)
				{
					_world.isFinished = true;
					_world.spawnedMobs.clear();
					_world.lastAttack.clear();
					if (_world.finishTask != null)
					{
						_world.finishTask.cancel(false);
						_world.finishTask = null;
					}
					if (_world.despawnTask != null)
					{
						_world.despawnTask.cancel(false);
						_world.despawnTask = null;
					}
					
					InstanceManager.getInstance().destroyInstance(_world.getInstanceId());
				}
			}
		}
	}
	
	class FinishTask implements Runnable
	{
		private final RimKamaWorld _world;
		
		FinishTask(RimKamaWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (_world != null)
			{
				_world.isFinished = true;
				if (_world.despawnTask != null)
				{
					_world.despawnTask.cancel(false);
					_world.despawnTask = null;
				}
				_world.spawnedMobs.clear();
				_world.lastAttack.clear();
				// destroy instance after EXIT_TIME
				final Instance inst = InstanceManager.getInstance().getInstance(_world.getInstanceId());
				if (inst != null)
				{
					inst.removeNpcs();
					inst.setDuration(EXIT_TIME * 60000);
					if (inst.getPlayers().isEmpty())
					{
						inst.setDuration(EMPTY_DESTROY_TIME * 60000);
					}
					else
					{
						inst.setDuration(EXIT_TIME * 60000);
						inst.setEmptyDestroyTime(EMPTY_DESTROY_TIME * 60000);
					}
				}
				
				// calculate reward
				if (_world.kanabionsCount < 10)
				{
					_world.grade = 0;
				}
				else
				{
					_world.grade = Math.min(((_world.dopplersCount + (2 * _world.voidersCount)) / _world.kanabionsCount) + 1, 5);
				}
				
				final int index = _world.index;
				// spawn rewarder npc
				addSpawn(REWARDER, REWARDERS[index][0], REWARDERS[index][1], REWARDERS[index][2], 0, false, 0, false, _world.getInstanceId());
			}
		}
	}
	
	class DespawnTask implements Runnable
	{
		private final RimKamaWorld _world;
		
		DespawnTask(RimKamaWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if ((_world != null) && !_world.isFinished && !_world.lastAttack.isEmpty() && !_world.spawnedMobs.isEmpty())
			{
				final long time = System.currentTimeMillis();
				for (L2MonsterInstance mob : _world.spawnedMobs)
				{
					if ((mob == null) || mob.isDead() || !mob.isVisible())
					{
						continue;
					}
					if (_world.lastAttack.containsKey(mob.getObjectId()) && ((time - _world.lastAttack.get(mob.getObjectId())) > DESPAWN_DELAY))
					{
						mob.deleteMe();
						synchronized (_world.lastAttack)
						{
							_world.lastAttack.remove(mob.getObjectId());
						}
					}
				}
			}
		}
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc == null) || (player == null))
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("Exit"))
		{
			try
			{
				final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if ((world instanceof RimKamaWorld) && world.isAllowed(player.getObjectId()))
				{
					Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
					teleportPlayer(player, inst.getExitLoc(), 0);
				}
			}
			catch (Exception e)
			{
				_log.warn("RimKamaloka: problem with exit: ", e);
			}
			return null;
		}
		else if (event.equalsIgnoreCase("Reward"))
		{
			try
			{
				final InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if ((world instanceof RimKamaWorld) && world.isAllowed(player.getObjectId()))
				{
					rewardPlayer((RimKamaWorld) world, npc);
				}
			}
			catch (Exception e)
			{
				_log.warn("RimKamaloka: problem with reward: ", e);
			}
			return "Rewarded.htm";
		}
		
		try
		{
			enterInstance(player, Integer.parseInt(event));
		}
		catch (Exception e)
		{
		}
		return null;
	}
	
	@Override
	public final String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if ((npc == null) || (caller == null))
		{
			return null;
		}
		
		if (npc.getId() == caller.getId())
		{
			return null;
		}
		
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			return null;
		}
		
		final int npcId = npc.getId();
		if (npcId == START_NPC)
		{
			return npc.getCastle().getName() + ".htm";
		}
		else if (npcId == REWARDER)
		{
			final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpWorld instanceof RimKamaWorld)
			{
				final RimKamaWorld world = (RimKamaWorld) tmpWorld;
				if (!world.isFinished)
				{
					return "";
				}
				
				switch (world.grade)
				{
					case 0:
						return "GradeF.htm";
					case 1:
						return "GradeD.htm";
					case 2:
						return "GradeC.htm";
					case 3:
						return "GradeB.htm";
					case 4:
						return "GradeA.htm";
					case 5:
						return "GradeS.htm";
				}
			}
		}
		
		return null;
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return String.valueOf(npc.getId()) + ".htm";
	}
	
	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if ((npc == null) || (attacker == null))
		{
			return null;
		}
		
		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof RimKamaWorld)
		{
			final RimKamaWorld world = (RimKamaWorld) tmpWorld;
			synchronized (world.lastAttack)
			{
				world.lastAttack.put(npc.getObjectId(), System.currentTimeMillis());
			}
			
			final int maxHp = npc.getMaxHp();
			if (npc.getCurrentHp() == maxHp)
			{
				if (((damage * 100) / maxHp) > 40)
				{
					final int npcId = npc.getId();
					final int chance = Rnd.get(100);
					int nextId = 0;
					
					if (npcId == world.KANABION)
					{
						if (chance < 5)
						{
							nextId = world.DOPPLER;
						}
					}
					else if (npcId == world.DOPPLER)
					{
						if (chance < 5)
						{
							nextId = world.DOPPLER;
						}
						else if (chance < 10)
						{
							nextId = world.VOIDER;
						}
					}
					else if (npcId == world.VOIDER)
					{
						if (chance < 5)
						{
							nextId = world.VOIDER;
						}
					}
					
					if (nextId > 0)
					{
						spawnNextMob(world, npc, nextId, attacker);
					}
				}
			}
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((npc == null) || (player == null))
		{
			return null;
		}
		
		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof RimKamaWorld)
		{
			final RimKamaWorld world = (RimKamaWorld) tmpWorld;
			synchronized (world.lastAttack)
			{
				world.lastAttack.remove(npc.getObjectId());
			}
			
			final int npcId = npc.getId();
			final int chance = Rnd.get(100);
			int nextId = 0;
			
			if (npcId == world.KANABION)
			{
				world.kanabionsCount++;
				if (((L2Attackable) npc).isOverhit())
				{
					if (chance < 30)
					{
						nextId = world.DOPPLER;
					}
					else if (chance < 40)
					{
						nextId = world.VOIDER;
					}
				}
				else if (chance < 15)
				{
					nextId = world.DOPPLER;
				}
			}
			else if (npcId == world.DOPPLER)
			{
				world.dopplersCount++;
				if (((L2Attackable) npc).isOverhit())
				{
					if (chance < 30)
					{
						nextId = world.DOPPLER;
					}
					else if (chance < 60)
					{
						nextId = world.VOIDER;
					}
				}
				else
				{
					if (chance < 10)
					{
						nextId = world.DOPPLER;
					}
					else if (chance < 20)
					{
						nextId = world.VOIDER;
					}
				}
			}
			else if (npcId == world.VOIDER)
			{
				world.voidersCount++;
				if (((L2Attackable) npc).isOverhit())
				{
					if (chance < 50)
					{
						nextId = world.VOIDER;
					}
				}
				else if (chance < 20)
				{
					nextId = world.VOIDER;
				}
			}
			
			if (nextId > 0)
			{
				spawnNextMob(world, npc, nextId, player);
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
}
