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
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_CAST;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_FOLLOW;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_IDLE;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_INTERACT;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_MOVE_TO;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_PICK_UP;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_REST;

import java.util.List;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.ItemLocation;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.instancemanager.WalkingManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.AutoAttackStop;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import l2r.util.Rnd;

/**
 * This class manages AI of L2Character.<br>
 * L2CharacterAI :
 * <ul>
 * <li>L2AttackableAI</li>
 * <li>L2DoorAI</li>
 * <li>L2PlayerAI</li>
 * <li>L2SummonAI</li>
 * </ul>
 */
public class L2CharacterAI extends AbstractAI
{
	public static class IntentionCommand
	{
		protected final CtrlIntention _crtlIntention;
		protected final Object _arg0, _arg1;
		
		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}
		
		public CtrlIntention getCtrlIntention()
		{
			return _crtlIntention;
		}
	}
	
	/**
	 * Cast Task
	 * @author Zoey76
	 */
	public static class CastTask implements Runnable
	{
		private final L2Character _activeChar;
		private final L2Object _target;
		private final L2Skill _skill;
		
		public CastTask(L2Character actor, L2Skill skill, L2Object target)
		{
			_activeChar = actor;
			_target = target;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isAttackingNow())
			{
				_activeChar.abortAttack();
			}
			_activeChar.getAI().changeIntentionToCast(_skill, _target);
		}
	}
	
	/**
	 * Constructor of L2CharacterAI.
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}
	
	public IntentionCommand getNextIntention()
	{
		return null;
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if ((attacker instanceof L2Attackable) && !((L2Attackable) attacker).isCoreAIDisabled())
		{
			clientStartAutoAttack();
		}
	}
	
	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);
		
		// Init cast and attack target
		setCastTarget(null);
		setAttackTarget(null);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
	}
	
	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.<br>
	 * <B><U> Actions</U> : <I>if the Intention is not already Active</I></B>
	 * <ul>
	 * <li>Set the AI Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();
			
			// Also enable random animations for this L2Character if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (_actor instanceof L2Attackable)
			{
				((L2Npc) _actor).startRandomAnimationTimer();
			}
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	/**
	 * Manage the Rest Intention.<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionRest()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_ATTACK</li>
	 * <li>Set or change the AI attack target</li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast)</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 * <B><U> Overridden in</U> :</B>
	 * <ul>
	 * <li>L2AttackableAI : Calculate attack timeout</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Check if the Intention is already AI_INTENTION_ATTACK
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			// Check if the AI already targets the L2Character
			if (getAttackTarget() != target)
			{
				// Set the AI attack target (change target)
				setAttackTarget(target);
				
				stopFollow();
				
				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);
				
			}
			else
			{
				clientActionFailed(); // else client freezes until cancel target
			}
		}
		else
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);
			
			// Set the AI attack target
			setAttackTarget(target);
			
			stopFollow();
			
			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}
	
	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Set the AI cast target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor</li>
	 * <li>Set the AI skill used by INTENTION_CAST</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_CAST</li>
	 * <li>Launch the Think Event</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if ((getIntention() == AI_INTENTION_REST) && skill.isMagic())
		{
			clientActionFailed();
			_actor.setIsCastingNow(false);
			return;
		}
		
		if (_actor.getBowAttackEndTime() > GameTimeController.getInstance().getGameTicks())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new CastTask(_actor, skill, target), (_actor.getBowAttackEndTime() - GameTimeController.getInstance().getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
		}
		else
		{
			changeIntentionToCast(skill, target);
		}
	}
	
	protected void changeIntentionToCast(L2Skill skill, L2Object target)
	{
		// Set the AI cast target
		setCastTarget((L2Character) target);
		// Set the AI skill used by INTENTION_CAST
		_skill = skill;
		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);
		
		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO</li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, loc, null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_FOLLOW</li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isMovementDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);
		
		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}
	
	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Set the AI pick up target</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_PICK_UP</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if ((object instanceof L2ItemInstance) && (((L2ItemInstance) object).getItemLocation() != ItemLocation.VOID))
		{
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		
		// Set the AI pick up target
		setTarget(object);
		if ((object.getX() == 0) && (object.getY() == 0)) // TODO: Find the drop&spawn bug
		{
			_log.warn("Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}
	
	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).<br>
	 * <B><U> Actions</U> : </B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the AI interact target</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_INTERACT</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);
			
			// Set the AI interact target
			setTarget(object);
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character</li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)</li>
	 * </ul>
	 */
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		// Stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtParalyzed(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		// Stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Sleeping.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character</li>
	 * </ul>
	 */
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		// stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	/**
	 * Launch actions corresponding to the Event Rooted.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li>
	 * </ul>
	 */
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		// _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		// if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		// AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
		
	}
	
	/**
	 * Launch actions corresponding to the Event Confused.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li>
	 * </ul>
	 */
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Muted.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character</li>
	 * </ul>
	 */
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character
		onEvtAttacked(attacker);
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	protected void onEvtEvaded(L2Character attacker)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event Arrived.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtArrived()
	{
		_accessor.getActor().revalidateZone(true);
		
		if (_accessor.getActor().moveToNextRoutePoint())
		{
			return;
		}
		
		if (_accessor.getActor() instanceof L2Attackable)
		{
			((L2Attackable) _accessor.getActor()).setisReturningToSpawnPoint(false);
		}
		clientStoppedMoving();
		
		if (_actor instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) _actor;
			WalkingManager.getInstance().onArrived(npc); // Walking Manager support
			
			// Notify quest
			if (npc.getTemplate().getEventQuests(QuestEventType.ON_MOVE_FINISHED) != null)
			{
				for (Quest quest : npc.getTemplate().getEventQuests(QuestEventType.ON_MOVE_FINISHED))
				{
					quest.notifyMoveFinished(npc);
				}
			}
		}
		
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO)
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ArrivedRevalidate.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if ((getIntention() == AI_INTENTION_MOVE_TO) || (getIntention() == AI_INTENTION_CAST))
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blocked_at_pos);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ForgetObject.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the object was targeted and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to attack, stop the auto-attack, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to cast, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to follow, stop the movement, cancel AI Follow Task and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the targeted object was the actor , cancel AI target, stop AI Follow Task, stop the movement and set the Intention to AI_INTENTION_IDLE</li>
	 * </ul>
	 */
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		// If the object was targeted and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE
		if (getTarget() == object)
		{
			setTarget(null);
			
			if (getIntention() == AI_INTENTION_INTERACT)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
		}
		
		// Check if the object was targeted to attack
		if (getAttackTarget() == object)
		{
			// Cancel attack target
			setAttackTarget(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the object was targeted to cast
		if (getCastTarget() == object)
		{
			// Cancel cast target
			setCastTarget(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the object was targeted to follow
		if (getFollowTarget() == object)
		{
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop an AI Follow Task
			stopFollow();
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the targeted object was the actor
		if (_actor == object)
		{
			// Cancel AI target
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);
			
			// Stop an AI Follow Task
			stopFollow();
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_IDLE
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Cancel.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();
		
		// Stop an AI Follow Task
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event Dead.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Tasks
		stopAITask();
		
		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();
		
		if (!(_actor instanceof L2Playable))
		{
			_actor.setWalking();
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Fake Death.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * </ul>
	 */
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Init AI
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}
	
	/**
	 * Do nothing.
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}
	
	protected boolean maybeMoveToPosition(Location worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.warn("maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}
		
		if (offset < 0)
		{
			return false; // skill radius -1
		}
		
		if (!_actor.isInsideRadius(worldPosition, offset + _actor.getTemplate().getCollisionRadius(), false, false))
		{
			if (_actor.isMovementDisabled())
			{
				return true;
			}
			
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}
			
			stopFollow();
			
			int x = _actor.getX();
			int y = _actor.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt((dx * dx) + (dy * dy));
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}
		
		if (getFollowTarget() != null)
		{
			stopFollow();
		}
		
		return false;
	}
	
	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get the distance between the current position of the L2Character and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * </ul>
	 * <B><U> Example of use </U> :</B>
	 * <ul>
	 * <li>L2PLayerAI, L2SummonAI</li>
	 * </ul>
	 * @param target The targeted L2Object
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		// Get the distance between the current position of the L2Character and the target (x,y)
		if (target == null)
		{
			_log.warn("maybeMoveToPawn: target == NULL!");
			return false;
		}
		if (offset < 0)
		{
			return false; // skill radius -1
		}
		
		offset += _actor.getTemplate().getCollisionRadius();
		if (target instanceof L2Character)
		{
			offset += ((L2Character) target).getTemplate().getCollisionRadius();
		}
		
		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			// Caller should be L2Playable and thinkAttack/thinkCast/thinkInteract/thinkPickUp
			if (getFollowTarget() != null)
			{
				
				// allow larger hit range when the target is moving (check is run only once per second)
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
				{
					return true;
				}
				stopFollow();
				return false;
			}
			
			if (_actor.isMovementDisabled())
			{
				// If player is trying attack target but he cannot move to attack target
				// change his intention to idle
				if (_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
				
				return true;
			}
			
			// while flying there is no move to cast
			if ((_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST) && (_actor instanceof L2PcInstance) && ((L2PcInstance) _actor).isTransformed())
			{
				if (!((L2PcInstance) _actor).getTransformation().isCombat())
				{
					_actor.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					_actor.sendPacket(ActionFailed.STATIC_PACKET);
					return true;
				}
			}
			
			// If not running, set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
			{
				_actor.setRunning();
			}
			
			stopFollow();
			if ((target instanceof L2Character) && !(target instanceof L2DoorInstance))
			{
				if (((L2Character) target).isMoving())
				{
					offset -= 100;
				}
				if (offset < 5)
				{
					offset = 5;
				}
				
				startFollow((L2Character) target, offset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}
		
		if (getFollowTarget() != null)
		{
			stopFollow();
		}
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		// clientStopMoving(null);
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost or dead.<br>
	 * <B><U> Actions</U> : <I>If the target is lost or dead</I></B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li>
	 * </ul>
	 * <B><U> Example of use </U> :</B>
	 * <ul>
	 * <li>L2PLayerAI, L2SummonAI</li>
	 * </ul>
	 * @param target The targeted L2Object
	 * @return True if the target is lost or dead (false if fakedeath)
	 */
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if ((target == null) || target.isAlikeDead())
		{
			// check if player is fakedeath
			if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
				return false;
			}
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost.<br>
	 * <B><U> Actions</U> : <I>If the target is lost</I></B>
	 * <ul>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li>
	 * </ul>
	 * <B><U> Example of use </U> :</B>
	 * <ul>
	 * <li>L2PLayerAI, L2SummonAI</li>
	 * </ul>
	 * @param target The targeted L2Object
	 * @return True if the target is lost
	 */
	protected boolean checkTargetLost(L2Object target)
	{
		// check if player is fakedeath
		if (target instanceof L2PcInstance)
		{
			L2PcInstance target2 = (L2PcInstance) target; // convert object to chara
			
			if (target2.isFakeDeath())
			{
				target2.stopFakeDeath(true);
				return false;
			}
		}
		if (target == null)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		if ((_actor != null) && (_skill != null) && _skill.isOffensive() && (_skill.getAffectRange() > 0) && (Config.GEODATA > 0) && !GeoData.getInstance().canSeeTarget(_actor, target))
		{
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	protected class SelfAnalysis
	{
		public boolean isMage = false;
		public boolean isBalanced;
		public boolean isArcher = false;
		public boolean isHealer = false;
		public boolean isFighter = false;
		public boolean cannotMoveOnLand = false;
		public List<L2Skill> generalSkills = new FastList<>();
		public List<L2Skill> buffSkills = new FastList<>();
		public int lastBuffTick = 0;
		public List<L2Skill> debuffSkills = new FastList<>();
		public int lastDebuffTick = 0;
		public List<L2Skill> cancelSkills = new FastList<>();
		public List<L2Skill> healSkills = new FastList<>();
		// public List<L2Skill> trickSkills = new FastList<>();
		public List<L2Skill> generalDisablers = new FastList<>();
		public List<L2Skill> sleepSkills = new FastList<>();
		public List<L2Skill> rootSkills = new FastList<>();
		public List<L2Skill> muteSkills = new FastList<>();
		public List<L2Skill> resurrectSkills = new FastList<>();
		public boolean hasHealOrResurrect = false;
		public boolean hasLongRangeSkills = false;
		public boolean hasLongRangeDamageSkills = false;
		public int maxCastRange = 0;
		
		public SelfAnalysis()
		{
		}
		
		public void init()
		{
			switch (((L2NpcTemplate) _actor.getTemplate()).getAIDataStatic().getAiType())
			{
				case FIGHTER:
					isFighter = true;
					break;
				case MAGE:
					isMage = true;
					break;
				case CORPSE:
				case BALANCED:
					isBalanced = true;
					break;
				case ARCHER:
					isArcher = true;
					break;
				case HEALER:
					isHealer = true;
					break;
				default:
					isFighter = true;
					break;
			}
			// water movement analysis
			if (_actor instanceof L2Npc)
			{
				int npcId = ((L2Npc) _actor).getId();
				
				switch (npcId)
				{
					case 20314: // great white shark
					case 20849: // Light Worm
						cannotMoveOnLand = true;
						break;
					default:
						cannotMoveOnLand = false;
						break;
				}
			}
			// skill analysis
			for (L2Skill sk : _actor.getAllSkills())
			{
				if (sk.isPassive())
				{
					continue;
				}
				int castRange = sk.getCastRange();
				boolean hasLongRangeDamageSkill = false;
				switch (sk.getSkillType())
				{
					case BUFF:
						buffSkills.add(sk);
						continue; // won't be considered something for fighting
					case PARALYZE:
					case STUN:
						// hardcoding petrification until improvements are made to
						// EffectTemplate... petrification is totally different for
						// AI than paralyze
						switch (sk.getId())
						{
							case 367:
							case 4111:
							case 4383:
							case 4616:
							case 4578:
								sleepSkills.add(sk);
								break;
							default:
								generalDisablers.add(sk);
								break;
						}
						break;
					case MUTE:
						muteSkills.add(sk);
						break;
					case SLEEP:
						sleepSkills.add(sk);
						break;
					case ROOT:
						rootSkills.add(sk);
						break;
					case FEAR: // could be used as an alternative for healing?
					case CONFUSION:
						// trickSkills.add(sk);
					case DEBUFF:
						debuffSkills.add(sk);
						break;
					case RESURRECT:
						resurrectSkills.add(sk);
						hasHealOrResurrect = true;
						break;
					case NOTDONE:
					case COREDONE:
						continue; // won't be considered something for fighting
					default:
						if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
						{
							cancelSkills.add(sk);
						}
						else if (sk.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
						{
							healSkills.add(sk);
							hasHealOrResurrect = true;
						}
						else
						{
							generalSkills.add(sk);
							hasLongRangeDamageSkill = true;
						}
						break;
				}
				if (castRange > 70)
				{
					hasLongRangeSkills = true;
					if (hasLongRangeDamageSkill)
					{
						hasLongRangeDamageSkills = true;
					}
				}
				if (castRange > maxCastRange)
				{
					maxCastRange = castRange;
				}
				
			}
			// Because of missing skills, some mages/balanced cannot play like mages
			if (!hasLongRangeDamageSkills && isMage)
			{
				isBalanced = true;
				isMage = false;
				isFighter = false;
			}
			if (!hasLongRangeSkills && (isMage || isBalanced))
			{
				isBalanced = false;
				isMage = false;
				isFighter = true;
			}
			if (generalSkills.isEmpty() && isMage)
			{
				isBalanced = true;
				isMage = false;
			}
		}
	}
	
	protected class TargetAnalysis
	{
		public L2Character character;
		public boolean isMage;
		public boolean isBalanced;
		public boolean isArcher;
		public boolean isFighter;
		public boolean isCanceled;
		public boolean isSlower;
		public boolean isMagicResistant;
		
		public TargetAnalysis()
		{
		}
		
		public void update(L2Character target)
		{
			// update status once in 4 seconds
			if ((target == character) && (Rnd.nextInt(100) > 25))
			{
				return;
			}
			character = target;
			if (target == null)
			{
				return;
			}
			isMage = false;
			isBalanced = false;
			isArcher = false;
			isFighter = false;
			isCanceled = false;
			
			if (target.getMAtk(null, null) > (1.5 * target.getPAtk(null)))
			{
				isMage = true;
			}
			else if (((target.getPAtk(null) * 0.8) < target.getMAtk(null, null)) || ((target.getMAtk(null, null) * 0.8) > target.getPAtk(null)))
			{
				isBalanced = true;
			}
			else
			{
				L2Weapon weapon = target.getActiveWeaponItem();
				if ((weapon != null) && ((weapon.getItemType() == WeaponType.BOW) || (weapon.getItemType() == WeaponType.CROSSBOW)))
				{
					isArcher = true;
				}
				else
				{
					isFighter = true;
				}
			}
			if (target.getRunSpeed() < (_actor.getRunSpeed() - 3))
			{
				isSlower = true;
			}
			else
			{
				isSlower = false;
			}
			if ((target.getMDef(null, null) * 1.2) > _actor.getMAtk(null, null))
			{
				isMagicResistant = true;
			}
			else
			{
				isMagicResistant = false;
			}
			if (target.getBuffCount() < 4)
			{
				isCanceled = true;
			}
		}
	}
	
	public boolean canAura(L2Skill sk)
	{
		if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
		{
			for (L2Object target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
			{
				if (target == getAttackTarget())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canAOE(L2Skill sk)
	{
		if (sk.hasEffectType(L2EffectType.CANCEL, L2EffectType.CANCEL_ALL, L2EffectType.NEGATE))
		{
			if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
			{
				boolean cancast = true;
				for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoData.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
				}
				if (cancast)
				{
					return true;
				}
			}
			else if ((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA))
			{
				boolean cancast = true;
				for (L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoData.getInstance().canSeeTarget(_actor, target) || (target == null))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
					{
						cancast = true;
					}
				}
				if (cancast)
				{
					return true;
				}
			}
		}
		else
		{
			if ((sk.getTargetType() == L2TargetType.AURA) || (sk.getTargetType() == L2TargetType.BEHIND_AURA) || (sk.getTargetType() == L2TargetType.FRONT_AURA) || (sk.getTargetType() == L2TargetType.AURA_CORPSE_MOB))
			{
				boolean cancast = false;
				for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoData.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					if (effects.length > 0)
					{
						cancast = true;
					}
				}
				if (cancast)
				{
					return true;
				}
			}
			else if ((sk.getTargetType() == L2TargetType.AREA) || (sk.getTargetType() == L2TargetType.BEHIND_AREA) || (sk.getTargetType() == L2TargetType.FRONT_AREA))
			{
				boolean cancast = true;
				for (L2Character target : getAttackTarget().getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
				{
					if (!GeoData.getInstance().canSeeTarget(_actor, target))
					{
						continue;
					}
					if (target instanceof L2Attackable)
					{
						L2Npc targets = ((L2Npc) target);
						L2Npc actors = ((L2Npc) _actor);
						if ((targets.getEnemyClan() == null) || (actors.getClan() == null) || !targets.getEnemyClan().equals(actors.getClan()) || ((actors.getClan() == null) && (actors.getIsChaos() == 0)))
						{
							continue;
						}
					}
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							cancast = false;
							break;
						}
					}
				}
				if (cancast)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canParty(L2Skill sk)
	{
		if (sk.getTargetType() == L2TargetType.PARTY)
		{
			int count = 0;
			int ccount = 0;
			for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(sk.getAffectRange()))
			{
				if (!(target instanceof L2Attackable) || !GeoData.getInstance().canSeeTarget(_actor, target))
				{
					continue;
				}
				L2Npc targets = ((L2Npc) target);
				L2Npc actors = ((L2Npc) _actor);
				if ((actors.getFactionId() != null) && targets.getFactionId().equals(actors.getFactionId()))
				{
					count++;
					L2Effect[] effects = target.getAllEffects();
					for (int i = 0; (effects != null) && (i < effects.length); i++)
					{
						
						L2Effect effect = effects[i];
						if (effect.getSkill() == sk)
						{
							ccount++;
							break;
						}
					}
				}
			}
			if (ccount < count)
			{
				return true;
			}
			
		}
		return false;
	}
	
	public boolean isParty(L2Skill sk)
	{
		return (sk.getTargetType() == L2TargetType.PARTY);
	}
}
