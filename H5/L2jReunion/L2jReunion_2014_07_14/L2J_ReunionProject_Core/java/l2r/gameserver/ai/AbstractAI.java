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

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.AutoAttackStart;
import l2r.gameserver.network.serverpackets.AutoAttackStop;
import l2r.gameserver.network.serverpackets.Die;
import l2r.gameserver.network.serverpackets.MoveToLocation;
import l2r.gameserver.network.serverpackets.MoveToPawn;
import l2r.gameserver.network.serverpackets.StopMove;
import l2r.gameserver.network.serverpackets.StopRotation;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mother class of all objects AI in the world.<br>
 * AbastractAI :<br>
 * <li>L2CharacterAI</li>
 */
public abstract class AbstractAI implements Ctrl
{
	protected final Logger _log = LoggerFactory.getLogger(getClass().getName());
	
	private NextAction _nextAction;
	
	/**
	 * @return the _nextAction
	 */
	public NextAction getNextAction()
	{
		return _nextAction;
	}
	
	/**
	 * @param nextAction the next action to set.
	 */
	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}
	
	private class FollowTask implements Runnable
	{
		protected int _range = 70;
		
		public FollowTask()
		{
		}
		
		public FollowTask(int range)
		{
			_range = range;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_followTask == null)
				{
					return;
				}
				
				L2Character followTarget = _followTarget; // copy to prevent NPE
				if (followTarget == null)
				{
					if (_actor instanceof L2Summon)
					{
						((L2Summon) _actor).setFollowStatus(false);
					}
					setIntention(AI_INTENTION_IDLE);
					return;
				}
				
				if (!_actor.isInsideRadius(followTarget, _range, true, false))
				{
					if (!_actor.isInsideRadius(followTarget, 3000, true, false))
					{
						// if the target is too far (maybe also teleported)
						if (_actor instanceof L2Summon)
						{
							((L2Summon) _actor).setFollowStatus(false);
						}
						
						setIntention(AI_INTENTION_IDLE);
						return;
					}
					
					moveToPawn(followTarget, _range);
				}
			}
			catch (Exception e)
			{
				_log.warn(getClass().getSimpleName() + ": Error: " + e.getMessage());
			}
		}
	}
	
	/** The character that this AI manages */
	protected final L2Character _actor;
	
	/** An accessor for private methods of the actor */
	protected final L2Character.AIAccessor _accessor;
	
	/** Current long-term intention */
	protected CtrlIntention _intention = AI_INTENTION_IDLE;
	/** Current long-term intention parameter */
	protected Object _intentionArg0 = null;
	/** Current long-term intention parameter */
	protected Object _intentionArg1 = null;
	
	/** Flags about client's state, in order to know which messages to send */
	protected volatile boolean _clientMoving;
	/** Flags about client's state, in order to know which messages to send */
	protected volatile boolean _clientAutoAttacking;
	/** Flags about client's state, in order to know which messages to send */
	protected int _clientMovingToPawnOffset;
	
	/** Different targets this AI maintains */
	private L2Object _target;
	private L2Character _castTarget;
	protected L2Character _attackTarget;
	protected L2Character _followTarget;
	
	/** The skill we are currently casting by INTENTION_CAST */
	L2Skill _skill;
	
	/** Different internal state flags */
	private int _moveToPawnTimeout;
	
	protected Future<?> _followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	/**
	 * Constructor of AbstractAI.
	 * @param accessor The AI accessor of the L2Character
	 */
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;
		
		// Get the L2Character managed by this Accessor AI
		_actor = accessor.getActor();
	}
	
	/**
	 * @return the L2Character managed by this Accessor AI.
	 */
	@Override
	public L2Character getActor()
	{
		return _actor;
	}
	
	/**
	 * @return the current Intention.
	 */
	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	protected void setCastTarget(L2Character target)
	{
		_castTarget = target;
	}
	
	/**
	 * @return the current cast target.
	 */
	public L2Character getCastTarget()
	{
		return _castTarget;
	}
	
	protected void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}
	
	/**
	 * @return current attack target.
	 */
	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}
	
	/**
	 * Set the Intention of this AbstractAI.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><B><U><br>
	 * Overridden in </U> : </B><BR>
	 * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary.
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}
	
	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT>
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		// Stop the follow mode if necessary
		if ((intention != AI_INTENTION_FOLLOW) && (intention != AI_INTENTION_ATTACK))
		{
			stopFollow();
		}
		
		// Launch the onIntention method of the L2CharacterAI corresponding to the new Intention
		switch (intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((Location) arg0);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
		
		// If do move or follow intention drop next action.
		if ((_nextAction != null) && _nextAction.getIntentions().contains(intention))
		{
			_nextAction = null;
		}
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT>
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event. <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event. <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned period)</B></FONT>
	 * @param evt The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if ((!_actor.isVisible() && !_actor.isTeleporting()) || !_actor.hasAI())
		{
			return;
		}
		
		switch (evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_PARALYZED:
				onEvtParalyzed((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_EVADED:
				onEvtEvaded((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtReadyToAct();
				}
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				// happens e.g. from stopmove but we don't process it if we're casting
				if (!_actor.isCastingNow() && !_actor.isCastingSimultaneouslyNow())
				{
					onEvtArrived();
				}
				break;
			case EVT_ARRIVED_REVALIDATE:
				// this is disregarded if the char is not moving any more
				if (_actor.isMoving())
				{
					onEvtArrivedRevalidate();
				}
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((Location) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
		
		// Do next action.
		if ((_nextAction != null) && _nextAction.getEvents().contains(evt))
		{
			_nextAction.doAction();
		}
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionRest();
	
	protected abstract void onIntentionAttack(L2Character target);
	
	protected abstract void onIntentionCast(L2Skill skill, L2Object target);
	
	protected abstract void onIntentionMoveTo(Location destination);
	
	protected abstract void onIntentionFollow(L2Character target);
	
	protected abstract void onIntentionPickUp(L2Object item);
	
	protected abstract void onIntentionInteract(L2Object object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(L2Character attacker);
	
	protected abstract void onEvtAggression(L2Character target, int aggro);
	
	protected abstract void onEvtStunned(L2Character attacker);
	
	protected abstract void onEvtParalyzed(L2Character attacker);
	
	protected abstract void onEvtSleeping(L2Character attacker);
	
	protected abstract void onEvtRooted(L2Character attacker);
	
	protected abstract void onEvtConfused(L2Character attacker);
	
	protected abstract void onEvtMuted(L2Character attacker);
	
	protected abstract void onEvtEvaded(L2Character attacker);
	
	protected abstract void onEvtReadyToAct();
	
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	
	protected abstract void onEvtArrived();
	
	protected abstract void onEvtArrivedRevalidate();
	
	protected abstract void onEvtArrivedBlocked(Location blocked_at_pos);
	
	protected abstract void onEvtForgetObject(L2Object object);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFakeDeath();
	
	protected abstract void onEvtFinishCasting();
	
	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor. <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 */
	protected void clientActionFailed()
	{
		if (_actor instanceof L2PcInstance)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 * @param pawn
	 * @param offset
	 */
	protected void moveToPawn(L2Object pawn, int offset)
	{
		// Check if actor can move
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
			{
				offset = 10;
			}
			
			// prevent possible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if (_clientMoving && (_target == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (GameTimeController.getInstance().getGameTicks() < _moveToPawnTimeout)
					{
						return;
					}
					sendPacket = false;
				}
				else if (_actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if (GameTimeController.getInstance().getGameTicks() < (_moveToPawnTimeout + 10))
					{
						return;
					}
				}
			}
			
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = GameTimeController.getInstance().getGameTicks();
			_moveToPawnTimeout += 1000 / GameTimeController.MILLIS_IN_TICK;
			
			if ((pawn == null) || (_accessor == null))
			{
				return;
			}
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!_actor.isMoving())
			{
				clientActionFailed();
				return;
			}
			
			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			if (pawn instanceof L2Character)
			{
				if (_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
				{
					_actor.broadcastPacket(new MoveToPawn(_actor, (L2Character) pawn, offset));
				}
			}
			else
			{
				_actor.broadcastPacket(new MoveToLocation(_actor));
			}
		}
		else
		{
			clientActionFailed();
		}
	}
	
	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void moveTo(int x, int y, int z)
	{
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_accessor.moveTo(x, y, z);
			
			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new MoveToLocation(_actor));
			
		}
		else
		{
			clientActionFailed();
		}
	}
	
	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 * @param pos
	 */
	protected void clientStopMoving(Location pos)
	{
		// Stop movement of the L2Character
		if (_actor.isMoving())
		{
			_accessor.stopMove(pos);
		}
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || (pos != null))
		{
			_clientMoving = false;
			
			// Send a Server->Client packet StopMove to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new StopMove(_actor));
			
			if (pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all L2PcInstance in its _knownPlayers
				_actor.broadcastPacket(new StopRotation(_actor.getObjectId(), pos.getHeading(), 0));
			}
		}
	}
	
	/**
	 * Client has already arrived to target, no need to force StopMove packet.
	 */
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}
	
	public boolean isAutoAttacking()
	{
		return _clientAutoAttacking;
	}
	
	public void setAutoAttacking(boolean isAutoAttacking)
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().setAutoAttacking(isAutoAttacking);
			}
			return;
		}
		_clientAutoAttacking = isAutoAttacking;
	}
	
	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 */
	public void clientStartAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if (!isAutoAttacking())
		{
			if (_actor.isPlayer() && _actor.hasSummon())
			{
				_actor.getSummon().broadcastPacket(new AutoAttackStart(_actor.getSummon().getObjectId()));
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its _knownPlayers
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
	}
	
	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 */
	public void clientStopAutoAttack()
	{
		if (_actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) _actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		if (_actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor) && isAutoAttacking())
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(_actor);
			}
		}
		else if (isAutoAttacking())
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			setAutoAttacking(false);
		}
	}
	
	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 */
	protected void clientNotifyDead()
	{
		// Send a Server->Client packet Die to the actor and all L2PcInstance in its _knownPlayers
		Die msg = new Die(_actor);
		_actor.broadcastPacket(msg);
		
		// Init AI
		_intention = AI_INTENTION_IDLE;
		_target = null;
		_castTarget = null;
		_attackTarget = null;
		
		// Cancel the follow task if necessary
		stopFollow();
	}
	
	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance player.<br>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
	 * @param player The L2PcIstance to notify with state of this L2Character
	 */
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (getActor().isVisibleFor(player))
		{
			if (_clientMoving)
			{
				if ((_clientMovingToPawnOffset != 0) && (_followTarget != null))
				{
					// Send a Server->Client packet MoveToPawn to the actor and all L2PcInstance in its _knownPlayers
					player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
				}
				else
				{
					// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
					player.sendPacket(new MoveToLocation(_actor));
				}
			}
		}
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 1s.
	 * @param target The L2Character to follow
	 */
	public synchronized void startFollow(L2Character target)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		// Create and Launch an AI Follow Task to execute every 1s
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.
	 * @param target The L2Character to follow
	 * @param range
	 */
	public synchronized void startFollow(L2Character target, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	/**
	 * Stop an AI Follow Task.
	 */
	public synchronized void stopFollow()
	{
		if (_followTask != null)
		{
			// Stop the Follow Task
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTarget = null;
	}
	
	protected L2Character getFollowTarget()
	{
		return _followTarget;
	}
	
	protected L2Object getTarget()
	{
		return _target;
	}
	
	protected void setTarget(L2Object target)
	{
		_target = target;
	}
	
	/**
	 * Stop all Ai tasks and futures.
	 */
	public void stopAITask()
	{
		stopFollow();
	}
	
	@Override
	public String toString()
	{
		return "Actor: " + _actor;
	}
}
