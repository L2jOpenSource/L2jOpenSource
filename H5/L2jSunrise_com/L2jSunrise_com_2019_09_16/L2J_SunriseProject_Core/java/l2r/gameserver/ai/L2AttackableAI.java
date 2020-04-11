/*
 * Copyright (C) 2004-2015 L2J Server
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.sql.TerritoryTable;
import l2r.gameserver.enums.AISkillScope;
import l2r.gameserver.enums.AIType;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
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
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.model.events.impl.character.npc.attackable.OnAttackableFactionCall;
import l2r.gameserver.model.events.impl.character.npc.attackable.OnAttackableHate;
import l2r.gameserver.model.events.returns.TerminateReturn;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * This class manages AI of L2Attackable.
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	/**
	 * Fear task.
	 * @author Zoey76
	 */
	public static class FearTask implements Runnable
	{
		private final L2AttackableAI _ai;
		private final L2Character _effector;
		private boolean _start;
		
		public FearTask(L2AttackableAI ai, L2Character effector, boolean start)
		{
			_ai = ai;
			_effector = effector;
			_start = start;
		}
		
		@Override
		public void run()
		{
			final int fearTimeLeft = _ai.getFearTime() - FEAR_TICKS;
			_ai.setFearTime(fearTimeLeft);
			_ai.onEvtAfraid(_effector, _start);
			_start = false;
		}
	}
	
	protected static final int FEAR_TICKS = 5;
	private static final int RANDOM_WALK_RATE = 30; // confirmed
	private static final int MAX_DRIFT_RANGE = Config.MAX_DRIFT_RANGE;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2min
	/** The L2Attackable AI task executed every 1s (call onEvtThink method). */
	private Future<?> _aiTask;
	/** The delay after which the attacked is stopped. */
	protected int _attackTimeout;
	/** The L2Attackable aggro counter. */
	private int _globalAggro;
	/** The flag used to indicate that a thinking action is in progress, to prevent recursive thinking. */
	private boolean _thinking;
	
	protected int _chaosTime = 0;
	private int _lastBuffTick;
	// Fear parameters
	private int _fearTime;
	private Future<?> _fearTask = null;
	
	/**
	 * Constructor of L2AttackableAI.
	 * @param creature the creature
	 */
	public L2AttackableAI(L2Attackable creature)
	{
		super(creature);
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
			if (target.isPlayer() && target.isGM())
			{
				return false;
			}
			if (target.isSummon() && ((L2Summon) target).getOwner().isGM())
			{
				return false;
			}
		}
		
		// Check if the target isn't a Folk or a Door
		if (target.isDoor())
		{
			return false;
		}
		
		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || ((target.isPlayable()) && !me.isInsideRadius(target, me.getAggroRange(), true, false)))
		{
			return false;
		}
		
		// Check if the target is a L2Playable
		if (target.isPlayable())
		{
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!(me.isRaid()) && !(me.canSeeThroughSilentMove()) && ((L2Playable) target).isSilentMovingAffected())
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
			if (player.isRecentFakeDeath() || (player.getFirstEffect(L2EffectType.HIDE) != null))
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
				if ((me.isChaos() > 0) && me.isInsideRadius(target, me.isChaos(), false, false))
				{
					if (((L2Attackable) target).isInMyClan(me))
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
						final int range = MAX_DRIFT_RANGE;
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
				stopAITask();
				
				// Cancel the AI
				_actor.detachAI();
				
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
		if (Rnd.get(1000) < (30 * 10))
		{
			if ((_lastBuffTick + 30) < GameTimeController.getInstance().getGameTicks())
			{
				for (L2Skill buff : getActiveChar().getTemplate().getBuffSkills())
				{
					if (checkSkillCastConditions(getActiveChar(), target, buff))
					{
						if (!_actor.isAffectedBySkill(buff.getId()))
						{
							_actor.setTarget(_actor);
							_actor.doCast(buff);
							_actor.setTarget(target);
							_log.debug(this + " used buff skill " + buff + " on {}", _actor);
							break;
						}
					}
				}
				_lastBuffTick = GameTimeController.getInstance().getGameTicks();
			}
		}
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}
	
	@Override
	protected void onEvtAfraid(L2Character effector, boolean start)
	{
		if ((_fearTime > 0) && (_fearTask == null))
		{
			_fearTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FearTask(this, effector, start), 0, FEAR_TICKS, TimeUnit.SECONDS);
			_actor.startAbnormalEffect(true, AbnormalEffect.SKULL_FEAR);
		}
		else
		{
			super.onEvtAfraid(effector, start);
			
			if ((_actor.isDead() || (_fearTime <= 0)) && (_fearTask != null))
			{
				_fearTask.cancel(true);
				_fearTask = null;
				_actor.stopAbnormalEffect(true, AbnormalEffect.SKULL_FEAR);
				setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
	}
	
	protected void thinkCast()
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
		_actor.doCast(_skill);
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
	protected void thinkActive()
	{
		L2Attackable npc = getActiveChar();
		
		// vGodFather: Delay used in order to find target again after removeTarget effect applied
		if (npc.getFindTargetDelay() > System.currentTimeMillis())
		{
			clientStartAutoAttack();
			return;
		}
		
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
				
				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					if (target.isPlayable())
					{
						final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(new OnAttackableHate(getActiveChar(), target.getActingPlayer(), target.isSummon()), getActiveChar(), TerminateReturn.class);
						if ((term != null) && term.terminate())
						{
							continue;
						}
					}
					
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					long hating = npc.getHating(target);
					
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
				long aggro = npc.getHating(hated);
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
			if ((npc.getSpawn() != null) && (Util.calculateDistance(npc.getX(), npc.getY(), npc.getZ(), npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), true) > 50))
			{
				// Order to the L2GuardInstance to return to its home location because there's no target to attack
				tryToGoHome(npc);
			}
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
				for (L2Skill sk : npc.getTemplate().getBuffSkills())
				{
					if (cast(sk))
					{
						return;
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if ((npc.getSpawn() != null) && (Rnd.nextInt(npc.RANDOM_WALK_RATE > 0 ? npc.RANDOM_WALK_RATE : RANDOM_WALK_RATE) == 0) && !npc.isNoRndWalk())
		{
			int x1 = 0;
			int y1 = 0;
			int z1 = 0;
			final int range = MAX_DRIFT_RANGE;
			
			for (L2Skill sk : npc.getTemplate().getBuffSkills())
			{
				if (cast(sk))
				{
					return;
				}
			}
			
			boolean useTeleport = false;
			// If NPC with random coord in territory
			if ((npc.getSpawn().getX() == 0) && (npc.getSpawn().getY() == 0) && (npc.getSpawn().getSpawnTerritory() == null))
			{
				// Calculate a destination point in the spawn area
				final Location location = TerritoryTable.getInstance().getRandomPoint(npc.getSpawn().getLocationId());
				if (location != null)
				{
					x1 = location.getX();
					y1 = location.getY();
					z1 = location.getZ();
				}
				
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
				if (!npc.isReturningToSpawnPoint() && (TerritoryTable.getInstance().getProcMax(npc.getSpawn().getLocationId()) > 0))
				{
					return;
				}
			}
			else
			{
				x1 = npc.getSpawn().getX(npc);
				y1 = npc.getSpawn().getY(npc);
				z1 = npc.getSpawn().getZ(npc);
				
				if (!npc.isInsideRadius(x1, y1, z1, range + 150, true, false))
				{
					useTeleport = true;
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
			
			npc.RANDOM_WALK_RATE = -1;
			if (useTeleport && !npc.isRunner())
			{
				tryToGoHome(npc);
			}
			else
			{
				// Move the actor to Location (x,y,z)
				final Location moveLoc = GeoData.getInstance().moveCheck(npc.getX(), npc.getY(), npc.getZ(), x1, y1, z1, npc.getInstanceId());
				moveTo(moveLoc.getX(), moveLoc.getY(), moveLoc.getZ());
			}
		}
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink). <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li>
	 * </ul>
	 */
	protected void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();
		
		// vGodFather: this will fix broken attack animations from mobs
		if (npc.isCastingNow() || npc.isAttackingNow())
		{
			return;
		}
		
		if (npc.isCoreAIDisabled())
		{
			return;
		}
		
		final L2Character mostHate = npc.getMostHated();
		if (mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		setAttackTarget(mostHate);
		npc.setTarget(mostHate);
		
		// Immobilize condition
		if (npc.isMovementDisabled())
		{
			movementDisable();
			return;
		}
		
		// Check if target is dead or if timeout is expired to stop this attack
		final L2Character originalAttackTarget = getAttackTarget();
		if ((originalAttackTarget == null) || originalAttackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getInstance().getGameTicks()))
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			// Retail you must give time to mobs before start go back
			npc.RANDOM_WALK_RATE = 15;
			setIntention(AI_INTENTION_ACTIVE);
			
			// tryToGoHome(npc);
			
			npc.setWalking();
			return;
		}
		
		// Initialize data
		final int collision = npc.getTemplate().getCollisionRadius();
		final int combinedCollision = collision + mostHate.getTemplate().getCollisionRadius();
		
		if (!npc.getTemplate().getSuicideSkills().isEmpty() && ((int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 30))
		{
			final L2Skill skill = npc.getTemplate().getSuicideSkills().get(Rnd.get(npc.getTemplate().getSuicideSkills().size()));
			if (Util.checkIfInRange(skill.getAffectRange(), getActiveChar(), mostHate, false) && npc.hasSkillChance())
			{
				if (cast(skill))
				{
					_log.debug("{} used suicide skill {}", this, skill);
					return;
				}
			}
		}
		
		if (!npc.isMovementDisabled())
		{
			// apply move around
			if (Rnd.nextInt(100) <= 3)
			{
				for (L2Object nearby : npc.getKnownList().getKnownObjects().values())
				{
					if ((nearby instanceof L2Attackable) && npc.isInsideRadius(nearby, collision, false, false) && (nearby != mostHate))
					{
						int newX = (Rnd.nextBoolean() ? mostHate.getX() + combinedCollision + Rnd.get(40) : (mostHate.getX() - combinedCollision) + Rnd.get(40));
						int newY = (Rnd.nextBoolean() ? mostHate.getY() + combinedCollision + Rnd.get(40) : (mostHate.getY() - combinedCollision) + Rnd.get(40));
						
						if (!npc.isInsideRadius(newX, newY, 0, collision, false, false))
						{
							int newZ = npc.getZ() + 30;
							Location loc = GeoData.getInstance().moveCheck(npc, new Location(newX, newY, newZ));
							moveTo(loc);
							return;
						}
					}
				}
			}
			
			// apply dodge
			if (npc.getCanDodge() > 0)
			{
				if (Rnd.get(100) <= npc.getCanDodge())
				{
					double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
					if (Math.sqrt(distance2) <= (60 + combinedCollision))
					{
						int newX = (Rnd.nextBoolean() ? mostHate.getX() + combinedCollision + Rnd.get(150) : (mostHate.getX() - combinedCollision) + Rnd.get(150));
						int newY = (Rnd.nextBoolean() ? mostHate.getY() + combinedCollision + Rnd.get(150) : (mostHate.getY() - combinedCollision) + Rnd.get(150));
						int posZ = npc.getZ() + 30;
						
						Location loc = GeoData.getInstance().moveCheck(npc, new Location(newX, newY, posZ));
						moveTo(loc);
						return;
					}
				}
			}
		}
		
		// BOSS/Raid Minion Target Reconsider
		if (npc.isRaid() || npc.isRaidMinion())
		{
			_chaosTime++;
			if (npc instanceof L2RaidBossInstance)
			{
				if (!((L2MonsterInstance) npc).hasMinions())
				{
					if (_chaosTime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 100) / npc.getMaxHp())))
						{
							aggroReconsider();
							_chaosTime = 0;
							return;
						}
					}
				}
				else
				{
					if (_chaosTime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
						{
							aggroReconsider();
							_chaosTime = 0;
							return;
						}
					}
				}
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				if (_chaosTime > Config.GRAND_CHAOS_TIME)
				{
					double chaosRate = 100 - ((npc.getCurrentHp() * 300) / npc.getMaxHp());
					if (((chaosRate <= 10) && (Rnd.get(100) <= 10)) || ((chaosRate > 10) && (Rnd.get(100) <= chaosRate)))
					{
						aggroReconsider();
						_chaosTime = 0;
						return;
					}
				}
			}
			else
			{
				if (_chaosTime > Config.MINION_CHAOS_TIME)
				{
					if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
					{
						aggroReconsider();
						_chaosTime = 0;
						return;
					}
				}
			}
		}
		
		final List<L2Skill> generalSkills = npc.getTemplate().getGeneralskills();
		if (!generalSkills.isEmpty())
		{
			// Heal Condition
			final List<L2Skill> aiHealSkills = npc.getTemplate().getHealSkills();
			if (!aiHealSkills.isEmpty())
			{
				double percentage = (npc.getCurrentHp() / npc.getMaxHp()) * 100;
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
					{
						for (L2Skill healSkill : aiHealSkills)
						{
							if (healSkill.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							
							if (!checkSkillCastConditions(npc, healSkill))
							{
								continue;
							}
							if (!Util.checkIfInRange((healSkill.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(healSkill) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, healSkill.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								final L2Object target = npc.getTarget();
								npc.setTarget(leader);
								npc.doCast(healSkill);
								npc.setTarget(target);
								_log.debug(this + " used heal skill " + healSkill + " on leader {}", leader);
								return;
							}
						}
					}
				}
				
				if (Rnd.get(100) < ((100 - percentage) / 3))
				{
					for (L2Skill sk : aiHealSkills)
					{
						if (!checkSkillCastConditions(npc, sk))
						{
							continue;
						}
						
						clientStopMoving(null);
						final L2Object target = npc.getTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						_log.debug("{} used heal skill {} on itself", this, sk);
						return;
					}
				}
				
				for (L2Skill sk : aiHealSkills)
				{
					if (!checkSkillCastConditions(npc, sk))
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
							
							final L2Attackable targets = (L2Attackable) obj;
							if (!((L2Attackable) obj).isInMyClan(npc))
							{
								continue;
							}
							
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									final L2Object target = npc.getTarget();
									npc.setTarget(obj);
									npc.doCast(sk);
									npc.setTarget(target);
									_log.debug(this + " used heal skill " + sk + " on {}", obj);
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
			
			// Res Skill Condition
			final List<L2Skill> aiResSkills = npc.getTemplate().getResSkills();
			if (!aiResSkills.isEmpty())
			{
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && leader.isDead())
					{
						for (L2Skill sk : aiResSkills)
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							
							if (!checkSkillCastConditions(npc, sk))
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
								final L2Object target = npc.getTarget();
								npc.setTarget(leader);
								npc.doCast(sk);
								npc.setTarget(target);
								_log.debug(this + " used resurrection skill " + sk + " on leader {}", leader);
								return;
							}
						}
					}
				}
				
				for (L2Skill sk : aiResSkills)
				{
					if (!checkSkillCastConditions(npc, sk))
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
							if (!npc.isInMyClan(targets))
							{
								continue;
							}
							if (Rnd.get(100) < 10)
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									final L2Object target = npc.getTarget();
									npc.setTarget(obj);
									npc.doCast(sk);
									npc.setTarget(target);
									_log.debug(this + " used heal skill " + sk + " on clan member {}", obj);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						final L2Object target = npc.getTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						_log.debug("{} used heal skill {} on party", this, sk);
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
		
		// Long/Short Range skill usage.
		if (!npc.getShortRangeSkills().isEmpty() && npc.hasSkillChance() && (dist2 <= 150))
		{
			final L2Skill shortRangeSkill = npc.getShortRangeSkills().get(Rnd.get(npc.getShortRangeSkills().size()));
			if (checkSkillCastConditions(npc, npc.getTarget(), shortRangeSkill))
			{
				clientStopMoving(null);
				npc.doCast(shortRangeSkill);
				_log.debug(this + " used short range skill " + shortRangeSkill + " on {}", npc.getTarget());
				return;
			}
		}
		
		if (!npc.getLongRangeSkills().isEmpty() && npc.hasSkillChance() && (dist2 > 150))
		{
			final L2Skill longRangeSkill = npc.getLongRangeSkills().get(Rnd.get(npc.getLongRangeSkills().size()));
			if (checkSkillCastConditions(npc, npc.getTarget(), longRangeSkill))
			{
				clientStopMoving(null);
				npc.doCast(longRangeSkill);
				_log.debug(this + " used long range skill " + longRangeSkill + " on {}", npc.getTarget());
				return;
			}
		}
		
		// Starts melee attack
		if ((dist2 > range) || !GeoData.getInstance().canSeeTarget(npc, mostHate))
		{
			if (npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else
			{
				final L2Character target = getAttackTarget();
				if (target != null)
				{
					if (target.isMoving())
					{
						range -= 100;
					}
					// moveToPawn(target, Math.max(range, 5));
					moveTo(target.getLocation());
				}
			}
			return;
		}
		
		// Attacks target
		_actor.doAttack(getAttackTarget());
	}
	
	protected boolean cast(L2Skill sk)
	{
		if (sk == null)
		{
			return false;
		}
		
		final L2Attackable caster = getActiveChar();
		if (!checkSkillCastConditions(caster, sk))
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
		
		if (sk.hasEffectType(L2EffectType.RESURRECTION))
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
							caster.setTarget(attackTarget);
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
					if (!caster.isInMyClan(targets))
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
							caster.setTarget(attackTarget);
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
					if (targets.isInMyClan(caster))
					{
						if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
						{
							clientStopMoving(null);
							caster.setTarget(caster);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
				}
			}
		}
		
		if (sk.hasEffectType(L2EffectType.MAGICAL_ATTACK_MP, L2EffectType.PHYSICAL_ATTACK, L2EffectType.PHYSICAL_ATTACK_HP_LINK, L2EffectType.DEATH_LINK))
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
					caster.setTarget(target);
					caster.doCast(sk);
					caster.setTarget(attackTarget);
					return true;
				}
			}
			else
			{
				clientStopMoving(null);
				caster.doCast(sk);
				return true;
			}
		}
		
		switch (sk.getSkillType())
		{
			case BUFF:
			{
				if (!caster.isAffectedBySkill(sk.getId()))
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					_actor.setTarget(attackTarget);
					return true;
				}
				
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
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
						return true;
					}
				}
				if (canParty(sk))
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(attackTarget);
					return true;
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
					if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB) || (sk.getTargetType() == L2TargetType.AURA_UNDEAD_ENEMY))
					{
						clientStopMoving(null);
						caster.doCast(sk);
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
						caster.doCast(sk);
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
						caster.doCast(sk);
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
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
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
				if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.DISPEL, L2EffectType.DISPEL_BY_SLOT))
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
							caster.doCast(sk);
							return true;
						}
						L2Character target = effectTargetReconsider(sk, false);
						if (target != null)
						{
							clientStopMoving(null);
							caster.setTarget(target);
							caster.doCast(sk);
							caster.setTarget(attackTarget);
							return true;
						}
					}
					else if (canAOE(sk))
					{
						if (((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA)) && GeoData.getInstance().canSeeTarget(caster, attackTarget))
						
						{
							clientStopMoving(null);
							caster.doCast(sk);
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
								caster.setTarget(attackTarget);
								return true;
							}
						}
					}
					if (Rnd.get(100) < ((100 - percentage) / 3))
					{
						clientStopMoving(null);
						caster.setTarget(caster);
						caster.doCast(sk);
						caster.setTarget(attackTarget);
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
							if (!caster.isInMyClan(targets))
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
									caster.setTarget(attackTarget);
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
							if (caster.isInMyClan(targets))
							{
								if ((obj.getCurrentHp() < obj.getMaxHp()) && (Rnd.get(100) <= 20))
								{
									clientStopMoving(null);
									caster.setTarget(caster);
									caster.doCast(sk);
									caster.setTarget(attackTarget);
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
	
	protected void movementDisable()
	{
		final L2Attackable npc = getActiveChar();
		final L2Character target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		
		if (npc.getTarget() == null)
		{
			npc.setTarget(target);
		}
		
		final double dist = npc.calculateDistance(target, false, false);
		final int range = npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
		// TODO(Zoey76): Review this "magic changes".
		final int random = Rnd.get(100);
		if (!target.isImmobilized() && (random < 15))
		{
			if (tryCast(npc, target, AISkillScope.IMMOBILIZE, dist))
			{
				return;
			}
		}
		
		if (random < 20)
		{
			if (tryCast(npc, target, AISkillScope.COT, dist))
			{
				return;
			}
		}
		
		if (random < 30)
		{
			if (tryCast(npc, target, AISkillScope.DEBUFF, dist))
			{
				return;
			}
		}
		
		if (random < 40)
		{
			if (tryCast(npc, target, AISkillScope.NEGATIVE, dist))
			{
				return;
			}
		}
		
		if (npc.isMovementDisabled() || (npc.getAiType() == AIType.MAGE) || (npc.getAiType() == AIType.HEALER))
		{
			if (tryCast(npc, target, AISkillScope.ATTACK, dist))
			{
				return;
			}
		}
		
		if (tryCast(npc, target, AISkillScope.UNIVERSAL, dist))
		{
			return;
		}
		// If cannot cast, try to attack.
		if ((dist <= range) && GeoData.getInstance().canSeeTarget(npc, target))
		{
			_actor.doAttack(target);
			return;
		}
		
		// If cannot cast nor attack, find a new target.
		targetReconsider();
	}
	
	private boolean tryCast(L2Attackable npc, L2Character target, AISkillScope aiSkillScope, double dist)
	{
		for (L2Skill sk : npc.getTemplate().getAISkills(aiSkillScope))
		{
			if (!checkSkillCastConditions(npc, sk) || (((sk.getCastRange() + target.getTemplate().getCollisionRadius()) <= dist) && !canAura(sk)))
			{
				continue;
			}
			
			if (!GeoData.getInstance().canSeeTarget(npc, target))
			{
				continue;
			}
			
			clientStopMoving(null);
			npc.doCast(sk);
			return true;
		}
		return false;
	}
	
	/**
	 * @param caster the caster
	 * @param skill the skill to check.
	 * @return {@code true} if the skill is available for casting {@code false} otherwise.
	 */
	protected static boolean checkSkillCastConditions(L2Attackable caster, L2Skill skill)
	{
		return checkSkillCastConditions(caster, null, skill);
	}
	
	protected static boolean checkSkillCastConditions(L2Attackable caster, L2Object target, L2Skill skill)
	{
		if (caster.isCastingNow() && !skill.isSimultaneousCast())
		{
			return false;
		}
		
		// Not enough MP.
		if (skill.getMpConsume() >= caster.getCurrentMp())
		{
			return false;
		}
		// Character is in "skill disabled" mode.
		if (caster.isSkillDisabled(skill))
		{
			return false;
		}
		// If is a static skill and magic skill and character is muted or is a physical skill muted and character is physically muted.
		if (!skill.isStatic() && ((skill.isMagic() && caster.isMuted()) || caster.isPhysicalMuted()))
		{
			return false;
		}
		
		// vGodFather: finally check for skill range and geodata
		if ((target != null) && (!Util.checkIfInRange(skill.getCastRange(), caster, target, true) || !GeoData.getInstance().canSeeTarget(caster, target)))
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
		if (!sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.DISPEL, L2EffectType.DISPEL_BY_SLOT))
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
					if (targets.isInMyClan(actor))
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
					if (actor.isChaos() != 0)
					{
						if (((L2Attackable) obj).isInMyClan(actor))
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
	
	protected void targetReconsider()
	{
		double dist = 0;
		double dist2 = 0;
		int range = 0;
		L2Attackable actor = getActiveChar();
		L2Character MostHate = actor.getMostHated();
		List<L2Character> hateList = actor.getHateList();
		if (hateList != null)
		{
			for (L2Character obj : hateList)
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
					actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
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
				if (obj.isPlayer())
				{
					actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj.isAttackable())
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, actor.getHating(MostHate));
						actor.setTarget(obj);
					}
					if (actor.isChaos() != 0)
					{
						if (((L2Attackable) obj).isInMyClan(actor))
						{
							continue;
						}
						
						actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
	}
	
	protected void aggroReconsider()
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
				
				actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
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
				if (obj.isPlayer())
				{
					actor.addDamageHate(obj, 0, ((MostHate != null) && !MostHate.isDead()) ? actor.getHating(MostHate) : 2000);
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
				else if (obj.isAttackable())
				{
					if ((actor.getEnemyClan() != null) && actor.getEnemyClan().equals(((L2Attackable) obj).getClan()))
					{
						actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
					if (actor.isChaos() != 0)
					{
						if (((L2Attackable) obj).isInMyClan(actor))
						{
							continue;
						}
						
						actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
						actor.setTarget(obj);
						setAttackTarget(obj);
					}
				}
				else if (obj instanceof L2Summon)
				{
					actor.addDamageHate(obj, 0, MostHate != null ? actor.getHating(MostHate) : 2000);
					actor.setTarget(obj);
					setAttackTarget(obj);
				}
			}
		}
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
					// vGodFather addon
				case AI_INTENTION_MOVE_TO:
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
			_log.warn(getClass().getSimpleName() + ": " + this + " -  onEvtThink() failed: " + e.getMessage(), e);
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
		
		if (me.isDead())
		{
			return;
		}
		
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
		
		// Faction call
		factionCall(me, attacker);
		
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
	protected void onEvtAggression(L2Character target, long aggro)
	{
		final L2Attackable me = getActiveChar();
		if (me.isDead())
		{
			return;
		}
		
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
			
			// Faction call
			factionCall(me, target);
		}
	}
	
	// vGodFather factionCall method
	private void factionCall(L2Attackable me, L2Character target)
	{
		if (target == null)
		{
			return;
		}
		
		// Fanction call
		String faction_id = getActiveChar().getFactionId();
		if ((faction_id != null) && !faction_id.isEmpty())
		{
			final int factionRange = me.getClanRange() + me.getTemplate().getCollisionRadius();
			// Go through all L2Object that belong to its faction
			for (L2Object obj : me.getKnownList().getKnownCharactersInRadius(factionRange))
			{
				if (!(obj instanceof L2Npc) || (obj instanceof L2GuardInstance) || (obj instanceof L2FriendlyMobInstance))
				{
					continue;
				}
				
				L2Npc called = (L2Npc) obj;
				
				// Handle SevenSigns mob Factions
				final String npcfaction = called.getFactionId();
				if ((npcfaction == null) || npcfaction.isEmpty() || !called.hasAI() || called.isDead())
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
				final CtrlIntention calledIntention = called.getAI().getIntention();
				if ((Math.abs(target.getZ() - called.getZ()) < 600) && me.getAttackByList().contains(target) && ((calledIntention == CtrlIntention.AI_INTENTION_IDLE) || (calledIntention == CtrlIntention.AI_INTENTION_ACTIVE)) && (called.getInstanceId() == me.getInstanceId()) && GeoData.getInstance().canSeeTarget(me, called))
				{
					if (target.isPlayable())
					{
						if (target.isInParty() && target.getParty().isInDimensionalRift())
						{
							byte riftType = target.getParty().getDimensionalRift().getType();
							byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();
							
							if ((me instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
							{
								continue;
							}
						}
						
						// By default, when a faction member calls for help, attack the caller's attacker.
						// Notify the AI with EVT_AGGRESSION
						called.setIsRunning(true);
						if (called.isAttackable())
						{
							((L2Attackable) called).addDamageHate(getAttackTarget(), 0, me.getHating(getAttackTarget()));
							called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
						else
						{
							called.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
						}
						EventDispatcher.getInstance().notifyEventAsync(new OnAttackableFactionCall(called, getActiveChar(), target.getActingPlayer(), target.isSummon()), called);
					}
					else if ((called instanceof L2Attackable) && (getAttackTarget() != null) && (calledIntention != CtrlIntention.AI_INTENTION_ATTACK))
					{
						((L2Attackable) called).addDamageHate(getAttackTarget(), 0, me.getHating(getAttackTarget()));
						called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getAttackTarget());
					}
				}
			}
		}
	}
	
	// vGodFather this will fix mobs return to home function
	private void tryToGoHome(L2Attackable npc)
	{
		if ((npc == null) || npc.isDead() || npc.isDecayed())
		{
			return;
		}
		
		try
		{
			if (npc.getSpawn() != null)
			{
				npc.setisReturningToSpawnPoint(true);
				if (GeoData.getInstance().canMove(npc.getX(), npc.getY(), npc.getZ(), npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getInstanceId()))
				{
					moveTo(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ());
				}
				else
				{
					npc.returnHome(true);
				}
			}
		}
		catch (Exception e)
		{
			// This can only happen if actor is outside of world area
			// nothing to log
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
	
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
	
	public int getFearTime()
	{
		return _fearTime;
	}
	
	public void setFearTime(int fearTime)
	{
		_fearTime = fearTime;
	}
}
