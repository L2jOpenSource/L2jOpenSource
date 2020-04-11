/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package instances.RimKamaloka;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

import instances.AbstractInstance;

/**
 * Instance Rim Pailaka.
 * @author MaGa
 * Instance level 80-85 is doesn't work because is not supported by the client.
 */
public class RimKamaloka extends AbstractInstance
{
	public RimKamaloka()
	{
		super(RimKamaloka.class.getSimpleName());
		
		addStartNpc(START_NPC);
		addFirstTalkId(REWARDER);
		addTalkId(START_NPC);
		addTalkId(REWARDER);
		
		for (int[] list : KANABIONS)
		{
			addFactionCallId(list[0]);
			for (int mob : list)
			{
				addAttackId(mob);
				addKillId(mob);
			}
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
	 * Time to destroy instance (and eject player away) Default: 5 minutes
	 */
	private static final int EXIT_TIME = 5;
	
	/*
	 * Maximum level difference between players level and kamaloka level Default: 5
	 */
	private static final int MAX_LEVEL_DIFFERENCE = 5;
	
	private static final int RESPAWN_DELAY = 20;
	
	private static final int DESPAWN_DELAY = 10000;
	
	/*
	 * Hardcoded instance ids for kamaloka
	 */
	private static final int[] TEMPLATE_IDS =
	{
		46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56
	};
	
	/*
	 * Level of the kamaloka
	 */
	private static final int[] LEVEL =
	{
		25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75 //80
	};
	
	/*
	 * Teleport points into instances x, y, z
	 */
	private static final Location[] TELEPORTS =
	{
		new Location(9264, -220641, -8032), // 25 (Town of Gludio, Town of Dion)
		new Location(16436, -220654, -8032), // 30 (Town of Gludio, Town of Dion)
		new Location(23486, -220839, -7808), // 35 (Town of Dion, Heine)
		new Location(9307, -213681, -7808), // 40 (Heine, Town of Oren)
		new Location(16603, -213720, -7808), // 45 (Heine, Town of Oren)
		new Location(23651, -213584, -8016), // 50 (Town of Oren, Town of Schuttgart)
		new Location(9134, -206264, -8016), // 55 (Town of Oren, Town of Schuttgart)
		new Location(16505, -206244, -8016), // 60 (Rune Township, Town of Schuttgart)
		new Location(23246,-206950,-8000), // 65 (Rune Township, Town of Schuttgart)
		new Location(42423, -220443, -8768), // 70 (Rune Township)
		new Location(49050, -220440, -8768), // 75 (Rune Township)
		//new Location(49050, -220440, -8768) // 80 (Rune Township)
	};
	
	private static final int[][] KANABIONS =
	{
		{22452, 22453, 22454}, //25
		{22455, 22456, 22457}, //30
		{22458, 22459, 22460}, //35
		{22461, 22462, 22463}, //40
		{22464, 22465, 22466}, //45
		{22467, 22468, 22469}, //50
		{22470, 22471, 22472}, //55
		{22473, 22474, 22475}, //60
		{22476, 22477, 22478}, //65
		{22479, 22480, 22481}, //70
		{22482, 22483, 22484}, //75
		//{2081, 2082, 2083} //80
	};
	
	private static final int[][][] SPAWNLIST =
	{
		{	//25
			{8971, -219546, -8021},
			{9318, -219644, -8021},
			{9266, -220208, -8021},
			{9497, -220054, -8024}
		},
		{	//30
			{16107,-219574,-8021},
			{16769,-219885,-8021},
			{16363,-220219,-8021},
			{16610,-219523,-8021}
		},
		{	//35
			{22753, -219354, -7968},
			{22735, -220861, -7968},
			{24243, -220855, -7968},
			{24246, -219342, -7968}
		},
		{	//40
			{8568, -212246, -7848},
			{8550, -213753, -7848},
			{10058, -213747, -7848},
			{10061, -212235, -7848}
		},
		{	//45
			{15859, -212234, -7844},
			{15841, -213741, -7844},
			{17349, -213734, -7844},
			{17351, -212222, -7844}
		},
		{	//50
			{23035, -212409, -8049},
			{23024, -213687, -8049},
			{24278, -213692, -8049},
			{24289, -212412, -8049}
		},
		{	//55
			{8512, -205084, -8055},
			{8501, -206363, -8055},
			{9755, -206368, -8055},
			{9766, -205087, -8055}
		},
		{	//60
			{15895, -205097, -8057},
			{15883, -206376, -8057},
			{17137, -206380, -8057},
			{17149, -205100, -8057}
		},
		{	//65
			{22374, -205347, -8049},
			{22377, -207249, -8049},
			{24115, -207242, -8049},
			{24139, -205352, -8049}
		},
		{	//70
			{41603, -220622, -8792},
			{43230, -220615, -8792},
			{43241, -218843, -8792},
			{41603, -218836, -8792},
			{41859,-220236,-8759},
			{42881,-219942,-8759}
		},
		{	//75
			{48201, -220688, -8812},
			{49828, -220682, -8812},
			{49832, -218903, -8812},
			{48201, -218903, -8812},
			{49618,-220490,-8759},
			{48526,-220493,-8759}
		},
		/*
		{	//80
			{48201, -220688, -8812},
			{49828, -220682, -8812},
			{49832, -218903, -8812},
			{48201, -218903, -8812},
			{49618,-220490,-8759},
			{48526,-220493,-8759}
		}
		*/
	};
	
	public static final int[][] REWARDERS =
	{
		{9252, -219908, -8032},
		{16419, -219934, -8032},
		{23479, -220080, -7808},
		{9296, -212995, -7808},
		{16594, -212997, -7808},
		{23655, -213043, -8016},
		{9137, -205732, -8016},
		{16510, -205735, -8016},
		{23244, -206317, -8000},
		{42429, -219785, -8768},
		{49052, -219762, -8768},
		//{49052, -219762, -8768}
	};
	
	private static final int START_NPC = 32484;
	
	private static final int REWARDER = 32485;
	
	private static final int[][][] REWARDS =
	{
		{ // 20-30
			null,
			{ // Grade D
				13002,6,
				12824,1
			},
			{	// Grade C
				13002,12,
				10836,1
			},
			{	// Grade B
				13002,19,
				10837,1
			},
			{	// Grade A
				13002,26,
				10838,1
			},
			{	// Grade S
				13002,32,
				10844,1
			}
		},
		{ // 25-35
			null,
			{ // Grade D
				13002,6,
				12825,1
			},
			{	// Grade C
				13002,12,
				10837,1
			},
			{	// Grade B
				13002,19,
				10838,1
			},
			{	// Grade A
				13002,26,
				10841,1
			},
			{	// Grade S
				13002,32,
				12827,1
			}
		},
		{ // 30-40
			null,
			{ // Grade D
				13002,6,
				10840,1
			},
			{	// Grade C
				13002,12,
				10841,1
			},
			{	// Grade B
				13002,19,
				10842,1
			},
			{	// Grade A
				13002,26,
				10843,1
			},
			{	// Grade S
				13002,32,
				10844,1
			}
		},
		{ // 35-45
			null,
			{ // Grade D
				13002,6,
				12826,1
			},
			{	// Grade C
				13002,12,
				10842,1
			},
			{	// Grade B
				13002,19,
				10843,1
			},
			{	// Grade A
				13002,26,
				10846,1
			},
			{	// Grade S
				13002,32,
				10829,1
			}
		},
		{ // 40-50
			null,
			{ // Grade D
				13002,6,
				10845,1
			},
			{	// Grade C
				13002,12,
				10846,1
			},
			{	// Grade B
				13002,19,
				10847,1
			},
			{	// Grade A
				13002,26,
				10848,1
			},
			{	// Grade S
				13002,32,
				10849,1
			}
		},
		{ // 45-55
			null,
			{ // Grade D
				13002,6,
				12828,1
			},
			{	// Grade C
				13002,12,
				10847,1
			},
			{	// Grade B
				13002,19,
				10848,1
			},
			{	// Grade A
				13002,26,
				10851,1
			},
			{	// Grade S
				13002,32,
				10831,1
			}
		},
		{ // 50-60
			null,
			{ // Grade D
				13002,6,
				10850,1
			},
			{	// Grade C
				13002,12,
				10851,1
			},
			{	// Grade B
				13002,19,
				10852,1
			},
			{	// Grade A
				13002,26,
				10853,1
			},
			{	// Grade S
				13002,32,
				10854,1
			}
		},
		{ // 55-65
			null,
			{ // Grade D
				13002,6,
				12830,1
			},
			{	// Grade C
				13002,12,
				10852,1
			},
			{	// Grade B
				13002,19,
				10853,1
			},
			{	// Grade A
				13002,26,
				10856,1
			},
			{	// Grade S
				13002,32,
				10833,1
			}
		},
		{ // 60-70
			null,
			{ // Grade D
				13002,6,
				12855,1
			},
			{	// Grade C
				13002,12,
				10856,1
			},
			{	// Grade B
				13002,19,
				10857,1
			},
			{	// Grade A
				13002,26,
				10858,1
			},
			{	// Grade S
				13002,32,
				10859,1
			}
		},
		{ // 65-75
			null,
			{ // Grade D
				13002,6,
				12832,1
			},
			{	// Grade C
				13002,12,
				10857,1
			},
			{	// Grade B
				13002,19,
				10858,1
			},
			{	// Grade A
				13002,26,
				10861,1
			},
			{	// Grade S
				13002,32,
				12834,1
			}
		},
		{ // 70-80
			null,
			{ // Grade D
				13002,6,
				10860,1
			},
			{	// Grade C
				13002,12,
				10861,1
			},
			{	// Grade B
				13002,19,
				10862,1
			},
			{	// Grade A
				13002,26,
				10864,1
			},
			{	// Grade S
				13002,32,
				10865,1
			}
		}
		/*
		{ // 80-85
			null,
			{ // Grade D
				13002,6,
				10864,1
			},
			{	// Grade C
				13002,12,
				10865,1
			},
			{	// Grade B
				13002,19,
				10865,1
			},
			{	// Grade A
				13002,26,
				10865,1
			},
			{	// Grade S
				13002,32,
				10865,1
			}
		}
		*/
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
		
		public List<L2MonsterInstance> spawnedMobs = new ArrayList<>();
		public Map<Integer, Long> lastAttack = new HashMap<>();
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
	protected boolean checkConditions(L2PcInstance player, int index)
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
		final String instanceName = InstanceManager.getInstance().getInstanceIdName(TEMPLATE_IDS[index]);
		
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
			templateId = TEMPLATE_IDS[index];
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
			final L2NpcTemplate mob1 = NpcData.getInstance().getTemplate(world.KANABION);
			
			spawnlist = SPAWNLIST[index];
			final int length = spawnlist.length;
			
			L2Spawn spawn;
			for (int i = 0; i < length; i++)
			{
				final int[] loc = spawnlist[i];
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
			_log.warning("RimKamaloka: error during spawn: ");
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
			final L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
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
				_log.warning("RimKamaloka: problem with exit: ");
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
				_log.warning("RimKamaloka: problem with reward: ");
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
		if (npcId == START_NPC && !player.hasPremiumStatus())
		{
			return "noPC.htm";
		}
		
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

	@Override
	protected void onEnterInstance(L2PcInstance player, InstanceWorld world,boolean firstEntrance)
	{
		
	}
}