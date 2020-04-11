/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.ai;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_IDLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.datatables.sql.TerritoryTable;
import l2r.gameserver.enums.AIType;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2EventMapGuardInstance;
import l2r.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import l2r.gameserver.model.actor.instance.L2FriendlyMobInstance;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2r.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * This class manages AI of L2Attackable.
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2min
	/**
	 * The L2Attackable AI task executed every 1s (call onEvtThink method).
	 */
	private Future<?> _aiTask;
	/**
	 * The delay after which the attacked is stopped.
	 */
	private int _attackTimeout;
	/**
	 * The L2Attackable aggro counter.
	 */
	private int _globalAggro;
	/**
	 * The flag used to indicate that a thinking action is in progress, to prevent recursive thinking.
	 */
	private boolean _thinking;
	
	private int timepass = 0;
	private int chaostime = 0;
	private final L2NpcTemplate _skillrender;
	private List<L2Skill> shortRangeSkills = new ArrayList<>();
	private List<L2Skill> longRangeSkills = new ArrayList<>();
	int lastBuffTick;
	
	/**
	 * Constructor of L2AttackableAI.
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_skillrender = NpcTable.getInstance().getTemplate(getActiveChar().getTemplate().getId());
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * <B><U> Actor is a L2GuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li>
	 * </ul>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li>
	 * </ul>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * </ul>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li>
	 * </ul>
	 * @param target The targeted L2Object
	 * @return True if the target is autoattackable (depends on the actor type).
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || (getActiveChar() == null))
		{
			return false;
		}
		final L2Attackable me = getActiveChar();
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			// However EffectInvincible requires to check GMs specially
			if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isGM())
			{
				return false;
			}
			if ((target instanceof L2Summon) && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}
		
		// Check if the target isn't a Folk or a Door
		if (target instanceof L2DoorInstance)
		{
			return false;
		}
		
		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || ((target instanceof L2Playable) && !me.isInsideRadius(target, me.getAggroRange(), true, false)))
		{
			return false;
		}
		
		// Check if the target is a L2Playable
		if (target.isPlayable())
		{
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!(me.isRaid()) && !(me.canSeeThroughSilentMove()) && ((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}
		
		// Gets the player if there is any.
		final L2PcInstance player = target.getActingPlayer();
		if (player != null)
		{
			// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
			if (player.isGM() && !player.getAccessLevel().canTakeAggro())
			{
				return false;
			}
			
			// check if the target is within the grace period for JUST getting up from fake death
			if (player.isRecentFakeDeath())
			{
				return false;
			}
			
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				byte riftType = player.getParty().getDimensionalRift().getType();
				byte riftRoom = player.getParty().getDimensionalRift().getCurrentRoom();
				
				if ((me instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
				{
					return false;
				}
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (me instanceof L2GuardInstance)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if ((player != null) && ((player.getKarma() > 0) || (getActiveChar() instanceof L2EventMapGuardInstance)))
			{
				return GeoData.getInstance().canSeeTarget(me, player); // Los Check
			}
			// Check if the L2MonsterInstance target is aggressive
			if ((target instanceof L2MonsterInstance) && Config.GUARD_ATTACK_AGGRO_MOB)
			{
				return (((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
			}
			
			return false;
		}
		else if (me instanceof L2FriendlyMobInstance)
		{
			// Check if the target isn't another L2Npc
			if (target instanceof L2Npc)
			{
				return false;
			}
			
			// Check if the L2PcInstance target has karma (=PK)
			if ((target instanceof L2PcInstance) && (((L2PcInstance) target).getKarma() > 0))
			{
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			}
			return false;
		}
		else
		{
			if (target instanceof L2Attackable)
			{
				if ((me.getEnemyClan() == null) || (((L2Attackable) target).getClan() == null))
				{
					return false;
				}
				
				if (!target.isAutoAttackable(me))
				{
					return false;
				}
				
				if (me.getEnemyClan().equals(((L2Attackable) target).getClan()))
				{
					if (me.isInsideRadius(target, me.getEnemyRange(), false, false))
					{
						return GeoData.getInstance().canSeeTarget(me, target);
					}
					return false;
				}
				if ((me.getIsChaos() > 0) && me.isInsideRadius(target, me.getIsChaos(), false, false))
				{
					if ((me.getFactionId() != null) && me.getFactionId().equals(((L2Attackable) target).getFactionId()))
					{
						return false;
					}
					// Los Check
					return GeoData.getInstance().canSeeTarget(me, target);
				}
			}
			
			if ((target instanceof L2Attackable) || (target instanceof L2Npc))
			{
				return false;
			}
			
			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneIdType.PEACE))
			{
				return false;
			}
			
			if (me.isChampion() && Config.L2JMOD_CHAMPION_PASSIVE)
			{
				return false;
			}
			
			// Check if the actor is Aggressive
			return (me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
		}
	}
	
	public void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}
	
	/**
	 * Set the Intention of this L2CharacterAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if ((intention == AI_INTENTION_IDLE) || (intention == AI_INTENTION_ACTIVE))
		{
			// Check if actor is not dead
			L2Attackable npc = getActiveChar();
			if (!npc.isAlikeDead())
			{
				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), range + range, true, false))
						{
							intention = AI_INTENTION_ACTIVE;
						}
					}
				}
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				if (_aiTask != null)
				{
					_aiTask.cancel(true);
					_aiTask = null;
				}
				
				// Cancel the AI
				_accessor.detachAI();
				
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		// self and buffs
		if ((lastBuffTick + 30) < GameTimeController.getInstance().getGameTicks())
		{
			for (L2Skill sk : _skillrender.getBuffSkills())
			{
				if (cast(sk))
				{
					break;
				}
			}
			lastBuffTick = GameTimeController.getInstance().getGameTicks();
		}
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}
	
	private void thinkCast()
	{
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		clientStopMoving(null);
		setIntention(AI_INTENTION_ACTIVE);
		_accessor.doCast(_skill);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink). <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li>
	 * </ul>
	 */
	private void thinkActive()
	{
		L2Attackable npc = getActiveChar();
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			
			for (L2Object obj : objs)
			{
				if (!(obj instanceof L2Character) || (obj instanceof L2StaticObjectInstance))
				{
					continue;
				}
				L2Character target = (L2Character) obj;
				
				/*
				 * Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
				 */
				if ((npc instanceof L2FestivalMonsterInstance) && (obj instanceof L2PcInstance))
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;
					
					if (!(targetPlayer.isFestivalParticipant()))
					{
						continue;
					}
				}
				
				if (obj.isPlayable())
				{
					final List<Quest> quests = getActiveChar().getTemplate().getEventQuests(QuestEventType.ON_NPC_HATE);
					if (quests != null)
					{
						boolean breaking = false;
						for (Quest q : quests)
						{
							if (!q.onNpcHate(getActiveChar(), (L2Playable) obj))
							{
								breaking = true;
							}
						}
						if (breaking)
						{
							continue;
						}
					}
				}
				
				// TODO: The AI Script ought to handle aggro behaviors in onSee. Once implemented, aggro behaviors ought
				// to be removed from here. (Fulminus)
				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					int hating = npc.getHating(target);
					
					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 0);
					}
				}
			}
			
			// Chose a target from its aggroList
			L2Character hated;
			if (npc.isConfused())
			{
				hated = getAttackTarget(); // effect handles selection
			}
			else
			{
				hated = npc.getMostHated();
			}
			
			// Order to the L2Attackable to attack the target
			if ((hated != null) && !npc.isCoreAIDisabled())
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!npc.isRunning())
					{
						npc.setRunning();
					}
					
					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				
				return;
			}
		}
		
		// Chance to forget attackers after some time
		if ((npc.getCurrentHp() == npc.getMaxHp()) && (npc.getCurrentMp() == npc.getMaxMp()) && !npc.getAttackByList().isEmpty() && (Rnd.nextInt(500) == 0))
		{
			npc.clearAggroList();
			npc.getAttackByList().clear();
			if (npc instanceof L2MonsterInstance)
			{
				if (((L2MonsterInstance) npc).hasMinions())
				{
					((L2MonsterInstance) npc).getMinionList().deleteReusedMinions();
				}
			}
		}
		
		// Check if the mob should not return to spawn point
		if (!npc.canReturnToSpawnPoint())
		{
			return;
		}
		
		// Check if the actor is a L2GuardInstance
		if ((npc instanceof L2GuardInstance) && !npc.isWalker() && !npc.isRunner())
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			npc.returnHome();
		}
		
		// If this is a festival monster, then it remains in the same location.
		if (npc instanceof L2FestivalMonsterInstance)
		{
			return;
		}
		
		// Minions following leader
		final L2Character leader = npc.getLeader();
		if ((leader != null) && !leader.isAlikeDead())
		{
			final int offset;
			final int minRadius = 30;
			
			if (npc.isRaidMinion())
			{
				offset = 500; // for Raids - need correction
			}
			else
			{
				offset = 200; // for normal minions - need correction :)
			}
			
			if (leader.isRunning())
			{
				npc.setRunning();
			}
			else
			{
				npc.setWalking();
			}
			
			if (npc.getPlanDistanceSq(leader) > (offset * offset))
			{
				int x1, y1, z1;
				x1 = Rnd.get(minRadius * 2, offset * 2); // x
				y1 = Rnd.get(x1, offset * 2); // distance
				y1 = (int) Math.sqrt((y1 * y1) - (x1 * x1)); // y
				if (x1 > (offset + minRadius))
				{
					x1 = (leader.getX() + x1) - offset;
				}
				else
				{
					x1 = (leader.getX() - x1) + minRadius;
				}
				if (y1 > (offset + minRadius))
				{
					y1 = (leader.getY() + y1) - offset;
				}
				else
				{
					y1 = (leader.getY() - y1) + minRadius;
				}
				
				z1 = leader.getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
				return;
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				for (L2Skill sk : _skillrender.getBuffSkills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if ((npc.getSpawn() != null) && (Rnd.nextInt(RANDOM_WALK_RATE) == 0) && !npc.isNoRndWalk())
		{
			int x1, y1, z1;
			final int range = Config.MAX_DRIFT_RANGE;
			
			for (L2Skill sk : _skillrender.getBuffSkills())
			{
				if (cast(sk))
				{
					return;
				}
			}
			
			// If NPC with random coord in territory
			if ((npc.getSpawn().getX() == 0) && (npc.getSpawn().getY() == 0) && (npc.getSpawn().getSpawnTerritory() == null))
			{
				// Calculate a destination point in the spawn area
				int p[] = TerritoryTable.getInstance().getRandomPoint(npc.getSpawn().getLocationId());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];
				
				// Calculate the distance between the current position of the L2Character and the target (x,y)
				double distance2 = npc.getPlanDistanceSq(x1, y1);
				
				if (distance2 > ((range + range) * (range + range)))
				{
					npc.setisReturningToSpawnPoint(true);
					float delay = (float) Math.sqrt(distance2) / range;
					x1 = npc.getX() + (int) ((x1 - npc.getX()) / delay);
					y1 = npc.getY() + (int) ((y1 - npc.getY()) / delay);
				}
				
				// If NPC with random fixed coord, don't move (unless needs to return to spawnpoint)
				if ((TerritoryTable.getInstance().getProcMax(npc.getSpawn().getLocationId()) > 0) && !npc.isReturningToSpawnPoint())
				{
					return;
				}
			}
			else
			{
				x1 = npc.getSpawn().getX(npc);
				y1 = npc.getSpawn().getY(npc);
				z1 = npc.getSpawn().getZ(npc);
				
				if (!npc.isInsideRadius(x1, y1, 0, range, false, false))
				{
					npc.setisReturningToSpawnPoint(true);
				}
				else
				{
					int deltaX = Rnd.nextInt(range * 2); // x
					int deltaY = Rnd.get(deltaX, range * 2); // distance
					deltaY = (int) Math.sqrt((deltaY * deltaY) - (deltaX * deltaX)); // y
					x1 = (deltaX + x1) - range;
					y1 = (deltaY + y1) - range;
					z1 = npc.getZ();
				}
			}
			
			// _log.debug("Current pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink). <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li>
	 * </ul>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();
		if (npc.isCastingNow())
		{
			return;
		}
		
		L2Character originalAttackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if ((originalAttackTarget == null) || originalAttackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getInstance().getGameTicks()))
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			
			npc.setWalking();
			return;
		}
		
		final int collision = npc.getTemplate().getCollisionRadius();
		
		// Handle all L2Object of its Faction inside the Faction Range
		
		String faction_id = getActiveChar().getFactionId();
		if ((faction_id != null) && !faction_id.isEmpty())
		{
			int factionRange = npc.getClanRange() + collision;
			// Go through all L2Object that belong to its faction
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			
			try
			{
				for (L2Object obj : objs)
				{
					if (obj instanceof L2Npc)
					{
						L2Npc called = (L2Npc) obj;
						
						// Handle SevenSigns mob Factions
						final String npcfaction = called.getFactionId();
						if ((npcfaction == null) || npcfaction.isEmpty())
						{
							continue;
						}
						
						boolean sevenSignFaction = false;
						
						// TODO: Unhardcode this by AI scripts (DrHouse)
						// Catacomb mobs should assist lilim and nephilim other than dungeon
						if ("c_dungeon_clan".equals(faction_id) && ("c_dungeon_lilim".equals(npcfaction) || "c_dungeon_nephi".equals(npcfaction)))
						{
							sevenSignFaction = true;
						}
						// Lilim mobs should assist other Lilim and catacomb mobs
						else if ("c_dungeon_lilim".equals(faction_id) && "c_dungeon_clan".equals(npcfaction))
						{
							sevenSignFaction = true;
						}
						// Nephilim mobs should assist other Nephilim and catacomb mobs
						else if ("c_dungeon_nephi".equals(faction_id) && "c_dungeon_clan".equals(npcfaction))
						{
							sevenSignFaction = true;
						}
						
						if (!faction_id.equals(npcfaction) && !sevenSignFaction)
						{
							continue;
						}
						
						// Check if the L2Object is inside the Faction Range of the actor
						if (npc.isInsideRadius(called, factionRange, true, false) && called.hasAI())
						{
							if ((Math.abs(originalAttackTarget.getZ() - called.getZ()) < 600) && npc.getAttackByList().contains(originalAttackTarget) && ((called.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (called.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && (called.getInstanceId() == npc.getInstanceId()))
							{
								if (originalAttackTarget.isPlayable())
								{
									if (originalAttackTarget.isInParty() && originalAttackTarget.getParty().isInDimensionalRift())
									{
										byte riftType = originalAttackTarget.getParty().getDimensionalRift().getType();
										byte riftRoom = originalAttackTarget.getParty().getDimensionalRift().getCurrentRoom();
										
										if ((npc instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
										{
											continue;
										}
									}
									
									// By default, when a faction member calls for help, attack the caller's attacker.
									// Notify the AI with EVT_AGGRESSION
									npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
									
									List<Quest> quests = called.getTemplate().getEventQuests(QuestEventType.ON_FACTION_CALL);
									if ((quests != null) && !quests.isEmpty())
									{
										L2PcInstance player = originalAttackTarget.getActingPlayer();
										boolean isSummon = originalAttackTarget.isSummon();
										for (Quest quest : quests)
										{
											quest.notifyFactionCall(called, getActiveChar(), player, isSummon);
										}
									}
								}
								else if ((called instanceof L2Attackable) && (getAttackTarget() != null) && (called.getAI()._intention != CtrlIntention.AI_INTENTION_ATTACK))
								{
									((L2Attackable) called).addDamageHate(getAttackTarget(), 0, npc.getHating(getAttackTarget()));
									called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
								}
							}
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				_log.warn(getClass().getSimpleName() + ": thinkAttack() faction call failed: " + e.getMessage());
			}
		}
		
		if (npc.isCoreAIDisabled())
		{
			return;
		}
		
		// Initialize data
		L2Character mostHate = npc.getMostHated();
		if (mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		setAttackTarget(mostHate);
		npc.setTarget(mostHate);
		
		final int combinedCollision = collision + mostHate.getTemplate().getCollisionRadius();
		
		if (!_skillrender.getSuicideSkills().isEmpty() && ((int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 30))
		{
			final L2Skill skill = _skillrender.getSuicideSkills().get(Rnd.nextInt(_skillrender.getSuicideSkills().size()));
			if (Util.checkIfInRange(skill.getAffectRange(), getActiveChar(), mostHate, false) && (Rnd.get(100) < Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance())))
			{
				if (cast(skill))
				{
					return;
				}
				
				for (L2Skill sk : _skillrender.getSuicideSkills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
		}
		
		// ------------------------------------------------------
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		// Note from Gnacik:
		// On l2js because of that sometimes mobs don't attack player only running
		// around player without any sense, so decrease chance for now
		if (!npc.isMovementDisabled() && (Rnd.nextInt(100) <= 3))
		{
			for (L2Object nearby : npc.getKnownList().getKnownObjects().values())
			{
				if ((nearby instanceof L2Attackable) && npc.isInsideRadius(nearby, collision, false, false) && (nearby != mostHate))
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newX = mostHate.getX() + newX;
					}
					else
					{
						newX = mostHate.getX() - newX;
					}
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
					{
						newY = mostHate.getY() + newY;
					}
					else
					{
						newY = mostHate.getY() - newY;
					}
					
					if (!npc.isInsideRadius(newX, newY, 0, collision, false, false))
					{
						int newZ = npc.getZ() + 30;
						if ((Config.GEODATA == 0) || GeoData.getInstance().canMove(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ, npc.getInstanceId()))
						{
							moveTo(newX, newY, newZ);
						}
					}
					return;
				}
			}
		}
		// Dodge if its needed
		if (!npc.isMovementDisabled() && (npc.getCanDodge() > 0))
		{
			if (Rnd.get(100) <= npc.getCanDodge())
			{
				// Micht: kepping this one otherwise we should do 2 sqrt
				double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
				if (Math.sqrt(distance2) <= (60 + combinedCollision))
				{
					int posX = npc.getX();
					int posY = npc.getY();
					int posZ = npc.getZ() + 30;
					
					if (originalAttackTarget.getX() < posX)
					{
						posX = posX + 300;
					}
					else
					{
						posX = posX - 300;
					}
					
					if (originalAttackTarget.getY() < posY)
					{
						posY = posY + 300;
					}
					else
					{
						posY = posY - 300;
					}
					
					if ((Config.GEODATA == 0) || GeoData.getInstance().canMove(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ, npc.getInstanceId()))
					{
						setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ, 0));
					}
					return;
				}
			}
		}
		
		// ------------------------------------------------------------------------------
		// BOSS/Raid Minion Target Reconsider
		if (npc.isRaid() || npc.isRaidMinion())
		{
			chaostime++;
			if (npc instanceof L2RaidBossInstance)
			{
				if (!((L2MonsterInstance) npc).hasMinions())
				{
					if (chaostime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 100) / npc.getMaxHp())))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
				else
				{
					if (chaostime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
						{
							aggroReconsider();
							chaostime = 0;
							return;
						}
					}
				}
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				if (chaostime > Config.GRAND_CHAOS_TIME)
				{
					double chaosRate = 100 - ((npc.getCurrentHp() * 300) / npc.getMaxHp());
					if (((chaosRate <= 10) && (Rnd.get(100) <= 10)) || ((chaosRate > 10) && (Rnd.get(100) <= chaosRate)))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
			else
			{
				if (chaostime > Config.MINION_CHAOS_TIME)
				{
					if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
					{
						aggroReconsider();
						chaostime = 0;
						return;
					}
				}
			}
		}
		
		if (!_skillrender.getGeneralskills().isEmpty())
		{
			// -------------------------------------------------------------------------------
			// Heal Condition
			if (!_skillrender.getHealSkills().isEmpty())
			{
				double percentage = (npc.getCurrentHp() / npc.getMaxHp()) * 100;
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
					{
						for (L2Skill sk : _skillrender.getHealSkills())
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							if (!checkSkillCastConditions(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								clientStopMoving(null);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				if (Rnd.get(100) < ((100 - percentage) / 3))
				{
					for (L2Skill sk : _skillrender.getHealSkills())
					{
						if (!checkSkillCastConditions(sk))
						{
							continue;
						}
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				for (L2Skill sk : _skillrender.getHealSkills())
				{
					if (!checkSkillCastConditions(sk))
					{
						continue;
					}
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((npc.getFactionId() != null) && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			// -------------------------------------------------------------------------------
			// Res Skill Condition
			if (!_skillrender.getResSkills().isEmpty())
			{
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && leader.isDead())
					{
						for (L2Skill sk : _skillrender.getResSkills())
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							if (!checkSkillCastConditions(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange((sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				for (L2Skill sk : _skillrender.getResSkills())
				{
					if (!checkSkillCastConditions(sk))
					{
						continue;
					}
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || !obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((npc.getFactionId() != null) && !npc.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							if (Rnd.get(100) < 10)
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						L2Object target = getAttackTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						return;
					}
				}
			}
		}
		
		double dist = Math.sqrt(npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY()));
		int dist2 = (int) dist - collision;
		int range = npc.getPhysicalAttackRange() + combinedCollision;
		if (mostHate.isMoving())
		{
			range = range + 50;
			if (npc.isMoving())
			{
				range = range + 50;
			}
		}
		
		// -------------------------------------------------------------------------------
		// Immobilize Condition
		if ((npc.isMovementDisabled() && ((dist > range) || mostHate.isMoving())) || ((dist > range) && mostHate.isMoving()))
		{
			movementDisable();
			return;
		}
		
		setTimepass(0);
		// --------------------------------------------------------------------------------
		// Skill Use
		if (!_skillrender.getGeneralskills().isEmpty())
		{
			if (Rnd.get(100) < Rnd.get(npc.getMinSkillChance(), npc.getMaxSkillChance()))
			{
				L2Skill skills = _skillrender.getGeneralskills().get(Rnd.nextInt(_skillrender.getGeneralskills().size()));
				if (cast(skills))
				{
					return;
				}
				for (L2Skill sk : _skillrender.getGeneralskills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
			
			// --------------------------------------------------------------------------------
			// Long/Short Range skill usage.
			if (npc.hasLSkill() || npc.hasSSkill())
			{
				final List<L2Skill> shortRangeSkills = shortRangeSkillRender();
				if (!shortRangeSkills.isEmpty() && npc.hasSSkill() && (dist2 <= 150) && (Rnd.get(100) <= npc.getSSkillChance()))
				{
					final L2Skill shortRangeSkill = shortRangeSkills.get(Rnd.get(shortRangeSkills.size()));
					if ((shortRangeSkill != null) && cast(shortRangeSkill))
					{
						return;
					}
					for (L2Skill sk : shortRangeSkills)
					{
						if ((sk != null) && cast(sk))
						{
							return;
						}
					}
				}
				
				final List<L2Skill> longRangeSkills = longRangeSkillRender();
				if (!longRangeSkills.isEmpty() && npc.hasLSkill() && (dist2 > 150) && (Rnd.get(100) <= npc.getLSkillChance()))
				{
					final L2Skill longRangeSkill = longRangeSkills.get(Rnd.get(longRangeSkills.size()));
					if ((longRangeSkill != null) && cast(longRangeSkill))
					{
						return;
					}
					for (L2Skill sk : longRangeSkills)
					{
						if ((sk != null) && cast(sk))
						{
							return;
						}
					}
				}
			}
		}
		
		// --------------------------------------------------------------------------------
		// Starts Melee or Primary Skill
		if ((dist2 > range) || !GeoData.getInstance().canSeeTarget(npc, mostHate))
		{
			if (npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else if (getAttackTarget() != null)
			{
				if (getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
			}
			return;
		}
		
		melee(npc.getPrimarySkillId());
	}
	
	private void melee(int type)
	{
		if (type != 0)
		{
			switch (type)
			{
				case -1:
				{
					if (_skillrender.getGeneralskills() != null)
					{
						for (L2Skill sk : _skillrender.getGeneralskills())
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
					break;
				}
				case 1:
				{
					for (L2Skill sk : _skillrender.getAtkSkills())
					{
						if (cast(sk))
						{
							return;
						}
					}
					break;
				}
				default:
				{
					for (L2Skill sk : _skillrender.getGeneralskills())
					{
						if (sk.getId() == getActiveChar().getPrimarySkillId())
						{
							if (cast(sk))
							{
								return;
							}
						}
					}
				}
					break;
			}
		}
		
		_accessor.doAttack(getAttackTarget());
	}
	
	private boolean cast(L2Skill sk)
	{
		if (sk == null)
		{
			return false;
		}
		
		final L2Attackable caster = getActiveChar();
		
		if (caster.isCastingNow() && !sk.isSimultaneousCast())
		{
			return false;
		}
		
		if (!checkSkillCastConditions(sk))
		{
			return false;
		}
		if (getAttackTarget() == null)
		{
			if (caster.getMostHated() != null)
			{
				setAttackTarget(caster.getMostHated());
			}
		}
		L2Character attackTarget = getAttackTarget();
		if (attackTarget == null)
		{
			return false;
		}
		double dist = Math.sqrt(caster.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		double dist2 = dist - attackTarget.getTemplate().getCollisionRadius();
		double range = caster.getPhysicalAttackRange() + caster.getTemplate().getCollisionRadius() + attackTarget.getTemplate().getCollisionRadius();
		double srange = sk.getCastRange() + caster.getTemplate().getCollisionRadius();
		if (attackTarget.isMoving())
		{
			dist2 = dist2 - 30;
		}
		
		switch (sk.getSkillType())
		{
			case BUFF:
			{
				if (caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					// L2Object target = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					// _actor.setTarget(target);
					return true;
				}
				// ----------------------------------------
				// If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == L2TargetType.SELF)
				{
					return false;
				}
				if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, true);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				if (canParty(sk))
				{
					clientStopMoving(null);
					L2Object targets = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			}
			case RESURRECT:
			{
				if (!isParty(sk))
				{
					if (caster.isMinion() && (sk.getTargetType() != L2TargetType.SELF))
					{
						L2Character leader = caster.getLeader();
						if (leader != null)
						{
							if (leader.isDead())
							{
								if (!Util.checkIfInRange((sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius()), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
								{
									moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius());
								}
							}
							if (GeoData.getInstance().canSeeTarget(caster, leader))
							{
								clientStopMoving(null);
								caster.setTarget(leader);
								caster.doCast(sk);
								return true;
							}
						}
					}
					
					for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable) || !obj.isDead())
						{
							continue;
						}
						
						L2Attackable targets = ((L2Attackable) obj);
						if ((caster.getFactionId() != null) && !caster.getFactionId().equals(targets.getFactionId()))
						{
							continue;
						}
						if (Rnd.get(100) < 10)
						{
							if (GeoData.getInstance().canSeeTarget(caster, targets))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				else if (isParty(sk))
				{
					for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getAffectRange() + caster.getTemplate().getCollisionRadius()))
					{
						if (!(obj instanceof L2Attackable))
						{
							continue;
						}
						L2Npc targets = ((L2Npc) obj);
						if ((caster.getFactionId() != null) && caster.getFactionId().equals(targets.getFactionId()))
						{
							if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
							{
								clientStopMoving(null);
								caster.setTarget(caster);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				break;
			}
			case DEBUFF:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
			{
				if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && (dist2 <= srange))
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case SLEEP:
			{
				if (sk.getTargetType() == L2TargetType.ONE)
				{
					if (!attackTarget.isDead() && (dist2 <= srange))
					{
						if ((dist2 > range) || attackTarget.isMoving())
						{
							if (attackTarget.getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								// _actor.setTarget(attackTarget);
								caster.doCast(sk);
								return true;
							}
						}
					}
					
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA))
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case ROOT:
			case STUN:
			case PARALYZE:
			case MUTE:
			case FEAR:
			{
				if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && (dist2 <= srange))
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA))
					{
						clientStopMoving(null);
						// L2Object target = attackTarget;
						// _actor.setTarget(_actor);
						caster.doCast(sk);
						// _actor.setTarget(target);
						return true;
					}
					if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == L2TargetType.ONE)
				{
					L2Character target = effectTargetReconsider(sk, false);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case MANADAM:
			{
				if (!canAura(sk))
				{
					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					L2Character target = skillTargetReconsider(sk);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
				break;
			}
			default:
			{
				if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
				{
					// decrease cancel probability
					if (Rnd.get(50) != 0)
					{
						return true;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						if ((attackTarget.getFirstEffect(L2EffectType.BUFF) != null) && GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
						{
							clientStopMoving(null);
							// L2Object target = attackTarget;
							// _actor.setTarget(_actor);
							caster.doCast(sk);
							// _actor.setTarget(target);
							return true;
						}
						L2Character target = effectTargetReconsider(sk, false);
						if (target != null)
						{
							clientStopMoving(null);
							L2Object targets = attackTarget;
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(targets);
							return true;
						}
					}
					else if (canAOE(sk))
					{
						if (((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget))
						
						{
							clientStopMoving(null);
							// L2Object target = attackTarget;
							// _actor.setTarget(_actor);
							caster.doCast(sk);
							// _actor.setTarget(target);
							return true;
						}
						else if (((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
						{
							clientStopMoving(null);
							caster.doCast(sk);
							return true;
						}
					}
				}
				if (sk.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
				{
					double percentage = (caster.getCurrentHp() / caster.getMaxHp()) * 100;
					if (caster.isMinion() && (sk.getTargetType() != L2TargetType.SELF))
					{
						L2Character leader = caster.getLeader();
						if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
						{
							if (!Util.checkIfInRange((sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius()), caster, leader, false) && !isParty(sk) && !caster.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + caster.getTemplate().getCollisionRadius() + leader.getTemplate().getCollisionRadius());
							}
							if (GeoData.getInstance().canSeeTarget(caster, leader))
							{
								clientStopMoving(null);
								caster.setTarget(leader);
								caster.doCast(sk);
								return true;
							}
						}
					}
					if (Rnd.get(100) < ((100 - percentage) / 3))
					{
						clientStopMoving(null);
						caster.setTarget(caster);
						caster.doCast(sk);
						return true;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + caster.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}
							
							L2Attackable targets = ((L2Attackable) obj);
							if ((caster.getFactionId() != null) && !caster.getFactionId().equals(targets.getFactionId()))
							{
								continue;
							}
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoData.getInstance().canSeeTarget(caster, targets))
								{
									clientStopMoving(null);
									caster.setTarget(obj);
									caster.doCast(sk);
									return true;
								}
							}
						}
					}
					if (isParty(sk))
					{
						for (L2Character obj : caster.getKnownList().getKnownCharactersInRadius(sk.getAffectRange() + caster.getTemplate().getCollisionRadius()))
						{
							if (!(obj instanceof L2Attackable))
							{
								continue;
							}
							L2Npc targets = ((L2Npc) obj);
							if ((caster.getFactionId() != null) && targets.getFactionId().equals(caster.getFactionId()))
							{
								if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
								{
									clientStopMoving(null);
									caster.setTarget(caster);
									caster.doCast(sk);
									return true;
								}
							}
						}
					}
				}
				if (!canAura(sk))
				{
					
					if (GeoData.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && (dist2 <= srange))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					L2Character target = skillTargetReconsider(sk);
					if (target != null)
					{
						clientStopMoving(null);
						L2Object targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					// L2Object targets = attackTarget;
					// _actor.setTarget(_actor);
					caster.doCast(sk);
					// _actor.setTarget(targets);
					return true;
				}
			}
				break;
		}
		
		return false;
	}
	
	/**
	 * This AI task will start when ACTOR cannot move and attack range larger than distance
	 */
	private void movementDisable()
	{
		final L2Attackable npc = getActiveChar();
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		try
		{
			if (npc.getTarget() == null)
			{
				npc.setTarget(getAttackTarget());
			}
			dist = Math.sqrt(npc.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY()));
			dist2 = dist - npc.getTemplate().getCollisionRadius();
			range = npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			if (getAttackTarget().isMoving())
			{
				dist = dist - 30;
				if (npc.isMoving())
				{
					dist = dist - 50;
				}
			}
			
			// Check if activeChar has any skill
			if (!_skillrender.getGeneralskills().isEmpty())
			{
				// -------------------------------------------------------------
				// Try to stop the target or disable the target as priority
				int random = Rnd.get(100);
				if (!_skillrender.getImmobiliseSkills().isEmpty() && !getAttackTarget().isImmobilized() && (random < 2))
				{
					for (L2Skill sk : _skillrender.getImmobiliseSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Same as Above, but with Mute/FEAR etc....
				if (!_skillrender.getCostOverTimeSkills().isEmpty() && (random < 5))
				{
					for (L2Skill sk : _skillrender.getCostOverTimeSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				if (!_skillrender.getDebuffSkills().isEmpty() && (random < 8))
				{
					for (L2Skill sk : _skillrender.getDebuffSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Some side effect skill like CANCEL or NEGATE
				if (!_skillrender.getNegativeSkills().isEmpty() && (random < 9))
				{
					for (L2Skill sk : _skillrender.getNegativeSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							clientStopMoving(null);
							// L2Object target = getAttackTarget();
							// _actor.setTarget(_actor);
							npc.doCast(sk);
							// _actor.setTarget(target);
							return;
						}
					}
				}
				// -------------------------------------------------------------
				// Start ATK SKILL when nothing can be done
				if (!_skillrender.getAtkSkills().isEmpty() && (npc.isMovementDisabled() || (npc.getAiType() == AIType.MAGE) || (npc.getAiType() == AIType.HEALER)))
				{
					for (L2Skill sk : _skillrender.getAtkSkills())
					{
						if (!checkSkillCastConditions(sk) || (((sk.getCastRange() + npc.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius()) <= dist2) && !canAura(sk)))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
						{
							continue;
						}
						clientStopMoving(null);
						// L2Object target = getAttackTarget();
						// _actor.setTarget(_actor);
						npc.doCast(sk);
						// _actor.setTarget(target);
						return;
					}
				}
				// -------------------------------------------------------------
				// if there is no ATK skill to use, then try Universal skill
				// @formatter:off
				/*
				for(L2Skill sk:_skillrender.getUniversalSkills())
				{
					if(sk.getMpConsume()>=_actor.getCurrentMp()
							|| _actor.isSkillDisabled(sk.getId())
							||(sk.getCastRange()+ _actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius() <= dist2 && !canAura(sk))
							||(sk.isMagic()&&_actor.isMuted())
							||(!sk.isMagic()&&_actor.isPhysicalMuted()))
					{
						continue;
					}
					if(!GeoData.getInstance().canSeeTarget(_actor,getAttackTarget()))
						continue;
					clientStopMoving(null);
					L2Object target = getAttackTarget();
					//_actor.setTarget(_actor);
					_actor.doCast(sk);
					//_actor.setTarget(target);
					return;
				}
				*/
				// @formatter:on
			}
			// timepass = timepass + 1;
			if (npc.isMovementDisabled())
			{
				// timepass = 0;
				targetReconsider();
				
				return;
			}
			// else if(timepass>=5)
			// {
			// timepass = 0;
			// AggroReconsider();
			// return;
			// }
			
			if ((dist > range) || !GeoData.getInstance().canSeeTarget(npc, getAttackTarget()))
			{
				if (getAttackTarget().isMoving())
				{
					range -= 100;
				}
				if (range < 5)
				{
					range = 5;
				}
				moveToPawn(getAttackTarget(), range);
				return;
				
			}
			
			melee(npc.getPrimarySkillId());
		}
		catch (NullPointerException e)
		{
			setIntention(AI_INTENTION_ACTIVE);
			_log.warn(getClass().getSimpleName() + ": " + this + " - failed executing movementDisable(): " + e.getMessage());
			return;
		}
	}
	
	/**
	 * @param skill the skill to check.
	 * @return {@code true} if the skill is available for casting {@code false} otherwise.
	 */
	private boolean checkSkillCastConditions(L2Skill skill)
	{
		// Not enough MP.
		if (skill.getMpConsume() >= getActiveChar().getCurrentMp())
		{
			return false;
		}
		// Character is in "skill disabled" mode.
		if (getActiveChar().isSkillDisabled(skill))
		{
			return false;
		}
		// If is a static skill and magic skill and character is muted or is a physical skill muted and character is physically muted.
		if (!skill.isStatic() && ((skill.isMagic() && getActiveChar().isMuted()) || getActiveChar().isPhysicalMuted()))
		{
			return false;
		}
		return true;
	}
	
	private L2Character effectTargetReconsider(L2Skill sk, boolean positive)
	{
		if (sk == null)
		{
			return null;
		}
		L2Attackable actor = getActiveChar();
		if (!sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
		{
			if (!positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				
				for (L2Character obj : actor.getAttackByList())
				{
					if ((obj == null) || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj) || (obj == getAttackTarget()))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius();
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
				
				// ----------------------------------------------------------------------
				// If there is nearby Target with aggro, start going on random target that is attackable
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist;
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (obj instanceof L2Attackable)
					{
						if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
						{
							if (dist2 <= range)
							{
								if (getAttackTarget().getFirstEffect(sk) == null)
								{
									return obj;
								}
							}
						}
					}
					if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(sk) == null)
							{
								return obj;
							}
						}
					}
				}
			}
			else if (positive)
			{
				double dist = 0;
				double dist2 = 0;
				int range = 0;
				for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
				{
					if (!(obj instanceof L2Attackable) || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj))
					{
						continue;
					}
					
					L2Attackable targets = ((L2Attackable) obj);
					if ((actor.getFactionId() != null) && !actor.getFactionId().equals(targets.getFactionId()))
					{
						continue;
					}
					
					try
					{
						actor.setTarget(getAttackTarget());
						dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
						dist2 = dist - actor.getTemplate().getCollisionRadius();
						range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
						if (obj.isMoving())
						{
							dist2 = dist2 - 70;
						}
					}
					catch (NullPointerException e)
					{
						continue;
					}
					if (dist2 <= range)
					{
						if (obj.getFirstEffect(sk) == null)
						{
							return obj;
						}
					}
				}
			}
		}
		else
		{
			double dist = 0;
			double dist2 = 0;
			int range = 0;
			range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			for (L2Character obj : actor.getKnownList().getKnownCharactersInRadius(range))
			{
				if ((obj == null) || obj.isDead() || !GeoData.getInstance().canSeeTarget(actor, obj))
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (dist2 <= range)
						{
							if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
							{
								return obj;
							}
						}
					}
				}
				if ((obj instanceof L2PcInstance) || (obj instanceof L2Summon))
				{
					
					if (dist2 <= range)
					{
						if (getAttackTarget().getFirstEffect(L2EffectType.BUFF) != null)
						{
							return obj;
						}
					}
				}
			}
		}
		return null;
	}
	
	private L2Character skillTargetReconsider(L2Skill sk)
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		if (actor.getHateList() != null)
		{
			for (L2Character obj : actor.getHateList())
			{
				if ((obj == null) || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead())
				{
					continue;
				}
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
					// if(obj.isMoving())
					// dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (dist2 <= range)
				{
					return obj;
				}
			}
		}
		
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				try
				{
					actor.setTarget(getAttackTarget());
					dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY()));
					dist2 = dist;
					range = sk.getCastRange() + actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
					// if(obj.isMoving())
					// dist2 = dist2 - 40;
				}
				catch (NullPointerException e)
				{
					continue;
				}
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				if ((obj == null) || !GeoData.getInstance().canSeeTarget(actor, obj) || (dist2 > range))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					return obj;
					
				}
				if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						return obj;
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						return obj;
					}
				}
				if (obj instanceof L2Summon)
				{
					return obj;
				}
			}
		}
		return null;
	}
	
	private void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		if (actor.getHateList() != null)
		{
			for (L2Character obj : actor.getHateList())
			{
				if ((obj == null) || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor))
				{
					continue;
				}
				try
				{
					dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY()));
					dist2 = dist - actor.getTemplate().getCollisionRadius();
					range = actor.getPhysicalAttackRange() + actor.getTemplate().getCollisionRadius() + obj.getTemplate().getCollisionRadius();
					if (obj.isMoving())
					{
						dist2 = dist2 - 70;
					}
				}
				catch (NullPointerException e)
				{
					continue;
				}
				
				if (dist2 <= range)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					return;
				}
			}
		}
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				
				if ((obj == null) || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor) || (obj == getAttackTarget()))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
					
				}
				else if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	private void aggroReconsider()
	{
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		if (actor.getHateList() != null)
		{
			
			int rand = Rnd.get(actor.getHateList().size());
			int count = 0;
			for (L2Character obj : actor.getHateList())
			{
				if (count < rand)
				{
					count++;
					continue;
				}
				
				if ((obj == null) || !GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj == getAttackTarget()) || (obj == actor))
				{
					continue;
				}
				
				try
				{
					actor.setTarget(getAttackTarget());
				}
				catch (NullPointerException e)
				{
					continue;
				}
				if (MostHate != null)
				{
					actor.addDamageHate(obj, 0, actor.getHating(MostHate));
				}
				else
				{
					actor.addDamageHate(obj, 0, 2000);
				}
				actor.setTarget(obj);
				setAttackTarget(obj);
				return;
			}
		}
		
		if (!(actor instanceof L2GuardInstance))
		{
			Collection<L2Object> objs = actor.getKnownList().getKnownObjects().values();
			for (L2Object target : objs)
			{
				L2Character obj = null;
				if (target instanceof L2Character)
				{
					obj = (L2Character) target;
				}
				else
				{
					continue;
				}
				
				if (!GeoData.getInstance().canSeeTarget(actor, obj) || obj.isDead() || (obj != MostHate) || (obj == actor))
				{
					continue;
				}
				if (obj instanceof L2PcInstance)
				{
					if ((MostHate != null) && !MostHate.isDead())
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj instanceof L2Attackable)
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
					}
					if (actor.getIsChaos() != 0)
					{
						if ((((L2Attackable) obj).getFactionId() != null) && ((L2Attackable) obj).getFactionId().equals(actor.getFactionId()))
						{
							continue;
						}
						
						if (MostHate != null)
						{
							actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						}
						else
						{
							actor.addDamageHate(obj, 0, 2000);
						}
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					if (MostHate != null)
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
					}
					else
					{
						actor.addDamageHate(obj, 0, 2000);
					}
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	private List<L2Skill> longRangeSkillRender()
	{
		longRangeSkills = _skillrender.getLongRangeSkills();
		if (longRangeSkills.isEmpty())
		{
			longRangeSkills = getActiveChar().getLongRangeSkill();
		}
		return longRangeSkills;
	}
	
	private List<L2Skill> shortRangeSkillRender()
	{
		shortRangeSkills = _skillrender.getShortRangeSkills();
		if (shortRangeSkills.isEmpty())
		{
			shortRangeSkills = getActiveChar().getShortRangeSkill();
		}
		return shortRangeSkills;
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (_thinking || getActiveChar().isAllSkillsDisabled())
		{
			return;
		}
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			switch (getIntention())
			{
				case AI_INTENTION_ACTIVE:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": " + this + " -  onEvtThink() failed: " + e.getMessage());
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li>
	 * </ul>
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		L2Attackable me = getActiveChar();
		
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);
		
		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!me.isRunning())
		{
			me.setRunning();
		}
		
		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (me.getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		
		if (me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;
			
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
			
			master = master.getLeader();
			if ((master != null) && master.hasMinions())
			{
				master.getMinionList().onAssist(me, attacker);
			}
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li>
	 * </ul>
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = getActiveChar();
		
		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);
			
			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!me.isRunning())
				{
					me.setRunning();
				}
				
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			
			if (me instanceof L2MonsterInstance)
			{
				L2MonsterInstance master = (L2MonsterInstance) me;
				
				if (master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
				
				master = master.getLeader();
				if ((master != null) && master.hasMinions())
				{
					master.getMinionList().onAssist(me, target);
				}
			}
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}
	
	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	/**
	 * @param TP The timepass to set.
	 */
	public void setTimepass(int TP)
	{
		timepass = TP;
	}
	
	/**
	 * @return Returns the timepass.
	 */
	public int getTimepass()
	{
		return timepass;
	}
	
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
}
