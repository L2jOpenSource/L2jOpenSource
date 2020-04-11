package com.l2jfrozen.gameserver.ai;

import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jfrozen.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;
import com.l2jfrozen.gameserver.model.extender.BaseExtender.EventType;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStart;
import com.l2jfrozen.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jfrozen.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jfrozen.gameserver.network.serverpackets.Die;
import com.l2jfrozen.gameserver.network.serverpackets.MoveToLocationInVehicle;
import com.l2jfrozen.gameserver.network.serverpackets.MoveToPawn;
import com.l2jfrozen.gameserver.network.serverpackets.StopMove;
import com.l2jfrozen.gameserver.network.serverpackets.StopRotation;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * Mother class of all objects AI in the world.<BR>
 * <BR>
 * AbastractAI :<BR>
 * <BR>
 * <li>L2CharacterAI</li><BR>
 * <BR>
 */
abstract class AbstractAI implements Ctrl
{
	
	protected static final Logger LOGGER = Logger.getLogger(AbstractAI.class);
	
	class FollowTask implements Runnable
	{
		protected int range = 60;
		protected boolean newtask = true;
		
		public FollowTask()
		{
			// null
		}
		
		public FollowTask(final int range)
		{
			this.range = range;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (followTask == null)
				{
					return;
				}
				
				final L2Character follow = getFollowTarget();
				
				if (follow == null)
				{
					stopFollow();
					return;
				}
				if (!actor.isInsideRadius(follow, range, true, false))
				{
					moveToPawn(follow, range);
				}
				else if (newtask)
				{
					newtask = false;
					actor.broadcastPacket(new MoveToPawn(actor, follow, range));
				}
			}
			catch (final Throwable t)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					t.printStackTrace();
				}
				
				LOGGER.warn("", t);
			}
		}
	}
	
	/** The character that this AI manages */
	protected final L2Character actor;
	
	/** An accessor for private methods of the actor */
	protected final L2Character.AIAccessor accessor;
	
	/** Current long-term intention */
	private CtrlIntention intention = AI_INTENTION_IDLE;
	/** Current long-term intention parameter */
	private Object intentionArg0 = null;
	/** Current long-term intention parameter */
	private Object intentionArg1 = null;
	
	/** Flags about client's state, in order to know which messages to send */
	protected boolean clientMoving;
	/** Flags about client's state, in order to know which messages to send */
	protected boolean clientAutoAttacking;
	/** Flags about client's state, in order to know which messages to send */
	protected int clientMovingToPawnOffset;
	
	/** Different targets this AI maintains */
	private L2Object target;
	private L2Character castTarget;
	private L2Character attackTarget;
	private L2Character followTarget;
	
	/** Diferent internal state flags */
	private int moveToPawnTimeout;
	
	protected Future<?> followTask = null;
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	/**
	 * Constructor of AbstractAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the L2Character
	 */
	protected AbstractAI(final L2Character.AIAccessor accessor)
	{
		this.accessor = accessor;
		
		// Get the L2Character managed by this Accessor AI
		actor = accessor.getActor();
	}
	
	/**
	 * Return the L2Character managed by this Accessor AI.<BR>
	 * <BR>
	 */
	@Override
	public L2Character getActor()
	{
		return actor;
	}
	
	/**
	 * Set the Intention of this AbstractAI.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR>
	 * <BR>
	 * <B><U> Overriden in </U> : </B><BR>
	 * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR>
	 * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention
	 * @param arg1      The second parameter of the Intention
	 */
	public synchronized void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1)
	{
		/*
		 * if (Config.DEBUG) LOGGER.warn("AbstractAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */
		
		this.intention = intention;
		intentionArg0 = arg0;
		intentionArg1 = arg1;
	}
	
	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 */
	@Override
	public final void setIntention(final CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(final CtrlIntention intention, final Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	/**
	 * Launch the L2CharacterAI onIntention method corresponding to the new Intention.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0      The first parameter of the Intention (optional target)
	 * @param arg1      The second parameter of the Intention (optional target)
	 */
	@Override
	public final void setIntention(final CtrlIntention intention, final Object arg0, final Object arg1)
	{
		if (!actor.isVisible() || !actor.hasAI())
		{
			return;
		}
		
		/*
		 * if (Config.DEBUG) LOGGER.warn("AbstractAI: setIntention -> " + intention + " " + arg0 + " " + arg1);
		 */
		
		// Stop the follow mode if necessary
		if (intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK)
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
				onIntentionMoveTo((L2CharPosition) arg0);
				break;
			case AI_INTENTION_MOVE_TO_IN_A_BOAT:
				onIntentionMoveToInABoat((L2CharPosition) arg0, (L2CharPosition) arg1);
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
		actor.fireEvent(EventType.SETINTENTION.name, new Object[]
		{
			intention
		});
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt The event whose the AI must be notified
	 */
	@Override
	public final void notifyEvent(final CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(final CtrlEvent evt, final Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	/**
	 * Launch the L2CharacterAI onEvt method corresponding to the Event.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR>
	 * <BR>
	 * @param evt  The event whose the AI must be notified
	 * @param arg0 The first parameter of the Event (optional target)
	 * @param arg1 The second parameter of the Event (optional target)
	 */
	@Override
	public final void notifyEvent(final CtrlEvent evt, final Object arg0, final Object arg1)
	{
		if (!actor.isVisible() || !actor.hasAI() || (actor instanceof L2PcInstance && !((L2PcInstance) actor).isOnline()) || (actor instanceof L2PcInstance && ((L2PcInstance) actor).isInOfflineMode()))
		{
			return;
		}
		
		/*
		 * if (Config.DEBUG) LOGGER.warn("AbstractAI: notifyEvent -> " + evt + " " + arg0 + " " + arg1);
		 */
		
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
			case EVT_READY_TO_ACT:
				onEvtReadyToAct();
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				onEvtArrived();
				break;
			case EVT_ARRIVED_REVALIDATE:
				onEvtArrivedRevalidate();
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((L2CharPosition) arg0);
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
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionRest();
	
	protected abstract void onIntentionAttack(L2Character target);
	
	protected abstract void onIntentionCast(L2Skill skill, L2Object target);
	
	protected abstract void onIntentionMoveTo(L2CharPosition destination);
	
	protected abstract void onIntentionMoveToInABoat(L2CharPosition destination, L2CharPosition origin);
	
	protected abstract void onIntentionFollow(L2Character target);
	
	protected abstract void onIntentionPickUp(L2Object item);
	
	protected abstract void onIntentionInteract(L2Object object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(L2Character attacker);
	
	protected abstract void onEvtAggression(L2Character target, int aggro);
	
	protected abstract void onEvtStunned(L2Character attacker);
	
	protected abstract void onEvtSleeping(L2Character attacker);
	
	protected abstract void onEvtRooted(L2Character attacker);
	
	protected abstract void onEvtConfused(L2Character attacker);
	
	protected abstract void onEvtMuted(L2Character attacker);
	
	protected abstract void onEvtReadyToAct();
	
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	
	protected abstract void onEvtArrived();
	
	protected abstract void onEvtArrivedRevalidate();
	
	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);
	
	protected abstract void onEvtForgetObject(L2Object object);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFakeDeath();
	
	protected abstract void onEvtFinishCasting();
	
	/**
	 * Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientActionFailed()
	{
		if (actor instanceof L2PcInstance)
		{
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param pawn
	 * @param offset
	 */
	public void moveToPawn(final L2Object pawn, int offset)
	{
		// Chek if actor can move
		if (!actor.isMovementDisabled())
		{
			if (offset < 10)
			{
				offset = 10;
			}
			
			// prevent possible extra calls to this function (there is none?),
			// also don't send movetopawn packets too often
			boolean sendPacket = true;
			if (clientMoving && getTarget() == pawn)
			{
				if (clientMovingToPawnOffset == offset)
				{
					if (GameTimeController.getGameTicks() < moveToPawnTimeout)
					{
						return;
					}
					
					sendPacket = false;
				}
				else if (actor.isOnGeodataPath())
				{
					// minimum time to calculate new route is 2 seconds
					if (GameTimeController.getGameTicks() < moveToPawnTimeout + 10)
					{
						return;
					}
				}
			}
			
			// Set AI movement data
			clientMoving = true;
			clientMovingToPawnOffset = offset;
			
			setTarget(pawn);
			
			moveToPawnTimeout = GameTimeController.getGameTicks();
			moveToPawnTimeout += /* 1000 */200 / GameTimeController.MILLIS_IN_TICK;
			
			if (pawn == null || accessor == null)
			{
				return;
			}
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!actor.isMoving())
			{
				actor.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Send a Server->Client packet MoveToPawn/CharMoveToLocation to the actor and all L2PcInstance in its knownPlayers
			if (pawn instanceof L2Character)
			{
				if (actor.isOnGeodataPath())
				{
					actor.broadcastPacket(new CharMoveToLocation(actor));
					clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
				{
					actor.broadcastPacket(new MoveToPawn(actor, (L2Character) pawn, offset));
				}
			}
			else
			{
				actor.broadcastPacket(new CharMoveToLocation(actor));
			}
		}
		else
		{
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param x
	 * @param y
	 * @param z
	 */
	public void moveTo(final int x, final int y, final int z)
	{
		// Chek if actor can move
		if (!actor.isMovementDisabled())
		{
			// Set AI movement data
			clientMoving = true;
			clientMovingToPawnOffset = 0;
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			accessor.moveTo(x, y, z);
			
			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its knownPlayers
			CharMoveToLocation msg = new CharMoveToLocation(actor);
			actor.broadcastPacket(msg);
			msg = null;
			
		}
		else
		{
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	protected void moveToInABoat(final L2CharPosition destination, final L2CharPosition origin)
	{
		// Chek if actor can move
		if (!actor.isMovementDisabled())
		{
			// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its knownPlayers
			// CharMoveToLocation msg = new CharMoveToLocation(actor);
			if (((L2PcInstance) actor).getBoat() != null)
			{
				MoveToLocationInVehicle msg = new MoveToLocationInVehicle(actor, destination, origin);
				actor.broadcastPacket(msg);
				msg = null;
			}
			
		}
		else
		{
			actor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param pos
	 */
	protected void clientStopMoving(final L2CharPosition pos)
	{
		/*
		 * if (true && actor instanceof L2PcInstance){ LOGGER.warn("clientStopMoving();"); Thread.dumpStack(); }
		 */
		
		// Stop movement of the L2Character
		if (actor.isMoving())
		{
			accessor.stopMove(pos);
		}
		
		clientMovingToPawnOffset = 0;
		
		if (clientMoving || pos != null)
		{
			clientMoving = false;
			
			// Send a Server->Client packet StopMove to the actor and all L2PcInstance in its knownPlayers
			StopMove msg = new StopMove(actor);
			actor.broadcastPacket(msg);
			msg = null;
			
			if (pos != null)
			{
				// Send a Server->Client packet StopRotation to the actor and all L2PcInstance in its knownPlayers
				StopRotation sr = new StopRotation(actor, pos.heading, 0);
				actor.sendPacket(sr);
				actor.broadcastPacket(sr);
				sr = null;
			}
		}
	}
	
	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if (clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			clientMovingToPawnOffset = 0;
			StopMove msg = new StopMove(actor);
			actor.broadcastPacket(msg);
			msg = null;
		}
		clientMoving = false;
	}
	
	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	/*
	 * public void clientStartAutoAttack() { if(!isAutoAttacking()) { // Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its knownPlayers actor.broadcastPacket(new AutoAttackStart(actor.getObjectId())); setAutoAttacking(true); }
	 * AttackStanceTaskManager.getInstance().addAttackStanceTask(actor); }
	 */
	
	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	/*
	 * public void clientStopAutoAttack() { if(actor instanceof L2PcInstance) { if(!AttackStanceTaskManager.getInstance().getAttackStanceTask(actor) && isAutoAttacking()) { AttackStanceTaskManager.getInstance().addAttackStanceTask(actor); } } else if(isAutoAttacking()) { actor.broadcastPacket(new
	 * AutoAttackStop(actor.getObjectId())); } setAutoAttacking(false); }
	 */
	/**
	 * Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	public void clientStartAutoAttack()
	{
		if (((actor instanceof L2NpcInstance && !(actor instanceof L2Attackable)) && !(actor instanceof L2PlayableInstance)))
		{
			return;
		}
		
		if (actor instanceof L2Summon)
		{
			final L2Summon summon = (L2Summon) actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStartAutoAttack();
			}
			return;
		}
		if (!isAutoAttacking())
		{
			if (actor instanceof L2PcInstance && ((L2PcInstance) actor).getPet() != null)
			{
				((L2PcInstance) actor).getPet().broadcastPacket(new AutoAttackStart(((L2PcInstance) actor).getPet().getObjectId()));
			}
			// Send a Server->Client packet AutoAttackStart to the actor and all L2PcInstance in its knownPlayers
			actor.broadcastPacket(new AutoAttackStart(actor.getObjectId()));
			setAutoAttacking(true);
		}
		AttackStanceTaskManager.getInstance().addAttackStanceTask(actor);
	}
	
	/**
	 * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	public void clientStopAutoAttack()
	{
		if (actor instanceof L2Summon)
		{
			final L2Summon summon = (L2Summon) actor;
			if (summon.getOwner() != null)
			{
				summon.getOwner().getAI().clientStopAutoAttack();
			}
			return;
		}
		
		final boolean isAutoAttacking = isAutoAttacking();
		
		if (actor instanceof L2PcInstance)
		{
			if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(actor) && isAutoAttacking)
			{
				AttackStanceTaskManager.getInstance().addAttackStanceTask(actor);
			}
		}
		else if (isAutoAttacking)
		{
			actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
			setAutoAttacking(false);
		}
	}
	
	/**
	 * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 */
	protected void clientNotifyDead()
	{
		// Send a Server->Client packet Die to the actor and all L2PcInstance in its knownPlayers
		Die msg = new Die(actor);
		actor.broadcastPacket(msg);
		msg = null;
		
		// Init AI
		setIntention(AI_INTENTION_IDLE);
		setTarget(null);
		setAttackTarget(null);
		setCastTarget(null);
		
		// Cancel the follow task if necessary
		stopFollow();
	}
	
	/**
	 * Update the state of this actor client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance player.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR>
	 * <BR>
	 * @param player The L2PcIstance to notify with state of this L2Character
	 */
	public void describeStateToPlayer(final L2PcInstance player)
	{
		if (clientMoving)
		{
			final L2Character follow = getFollowTarget();
			
			if (clientMovingToPawnOffset != 0 && follow != null)
			{
				// Send a Server->Client packet MoveToPawn to the actor and all L2PcInstance in its knownPlayers
				MoveToPawn msg = new MoveToPawn(actor, follow, clientMovingToPawnOffset);
				player.sendPacket(msg);
				msg = null;
			}
			else
			{
				// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its knownPlayers
				CharMoveToLocation msg = new CharMoveToLocation(actor);
				player.sendPacket(msg);
				msg = null;
			}
		}
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 1s.<BR>
	 * <BR>
	 * @param target The L2Character to follow
	 */
	public synchronized void startFollow(final L2Character target)
	{
		if (followTask != null)
		{
			followTask.cancel(false);
			followTask = null;
		}
		
		// Create and Launch an AI Follow Task to execute every 1s
		followTarget = target;
		followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}
	
	/**
	 * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.<BR>
	 * <BR>
	 * @param target The L2Character to follow
	 * @param range
	 */
	public synchronized void startFollow(final L2Character target, final int range)
	{
		if (followTask != null)
		{
			followTask.cancel(false);
			followTask = null;
		}
		
		followTarget = target;
		followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	/**
	 * Stop an AI Follow Task.<BR>
	 * <BR>
	 */
	public synchronized void stopFollow()
	{
		if (followTask != null)
		{
			// Stop the Follow Task
			followTask.cancel(false);
			followTask = null;
		}
		followTarget = null;
	}
	
	protected synchronized L2Character getFollowTarget()
	{
		return followTarget;
	}
	
	protected synchronized L2Object getTarget()
	{
		return target;
	}
	
	protected synchronized void setTarget(final L2Object target)
	{
		this.target = target;
	}
	
	protected synchronized void setCastTarget(final L2Character target)
	{
		castTarget = target;
	}
	
	/**
	 * @return the current cast target.
	 */
	public synchronized L2Character getCastTarget()
	{
		return castTarget;
	}
	
	protected synchronized void setAttackTarget(final L2Character target)
	{
		attackTarget = target;
	}
	
	/**
	 * Return current attack target.<BR>
	 * <BR>
	 */
	@Override
	public synchronized L2Character getAttackTarget()
	{
		return attackTarget;
	}
	
	public synchronized boolean isAutoAttacking()
	{
		return clientAutoAttacking;
	}
	
	public synchronized void setAutoAttacking(final boolean isAutoAttacking)
	{
		clientAutoAttacking = isAutoAttacking;
	}
	
	/**
	 * @return the intentionArg0
	 */
	public synchronized Object get_intentionArg0()
	{
		return intentionArg0;
	}
	
	/**
	 * @param intentionArg0 the intentionArg0 to set
	 */
	public synchronized void set_intentionArg0(final Object intentionArg0)
	{
		this.intentionArg0 = intentionArg0;
	}
	
	/**
	 * @return the intentionArg1
	 */
	public synchronized Object get_intentionArg1()
	{
		return intentionArg1;
	}
	
	/**
	 * @param intentionArg1 the intentionArg1 to set
	 */
	public synchronized void set_intentionArg1(final Object intentionArg1)
	{
		this.intentionArg1 = intentionArg1;
	}
	
	/**
	 * Return the current Intention.<BR>
	 * <BR>
	 */
	@Override
	public synchronized CtrlIntention getIntention()
	{
		return intention;
	}
	
}
