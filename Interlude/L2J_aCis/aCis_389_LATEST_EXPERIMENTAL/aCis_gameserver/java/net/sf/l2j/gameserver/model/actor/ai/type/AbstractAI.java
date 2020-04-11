package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.Intention;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

abstract class AbstractAI
{
	protected final Creature _actor;
	
	protected final Intention _previousIntention = new Intention();
	protected final Intention _currentIntention = new Intention();
	protected final Intention _nextIntention = new Intention();
	
	protected boolean _thinking = false;
	
	protected AbstractAI(Creature actor)
	{
		_actor = actor;
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionSit(WorldObject target);
	
	protected abstract void onIntentionStand();
	
	protected abstract void onIntentionAttack(Creature target, Boolean isShiftPressed);
	
	protected abstract void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance);
	
	protected abstract void onIntentionMoveTo(Location loc, Boat boat);
	
	protected abstract void onIntentionFollow(Creature target, Boolean isShiftPressed);
	
	protected abstract void onIntentionPickUp(WorldObject object, Boolean isShiftPressed);
	
	protected abstract void onIntentionInteract(WorldObject object, Boolean isShiftPressed);
	
	protected abstract void onIntentionUseItem(Integer objectId);
	
	protected abstract void onIntentionFakeDeath();
	
	protected abstract void onEvtAttacked(Creature attacker);
	
	protected abstract void onEvtAggression(Creature target, int aggro);
	
	protected abstract void onEvtEvaded(Creature attacker);
	
	protected abstract void onEvtArrived(Boolean forceStopped);
	
	protected abstract void onEvtArrivedBlocked(SpawnLocation loc);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFinishedAttack();
	
	protected abstract void onEvtFinishedCasting(Boolean success);
	
	protected abstract void onEvtSatDown(WorldObject target);
	
	protected abstract void onEvtStoodUp();
	
	protected abstract void onEvtFinishedAttackBow();
	
	protected abstract void onEvtBowAttackReused();
	
	protected abstract void onOwnerAttacked(Creature attacker);
	
	protected abstract void thinkAttack();
	
	protected abstract void thinkCast();
	
	protected abstract void thinkPickUp();
	
	protected abstract void thinkInteract();
	
	protected abstract void thinkSit();
	
	protected abstract void thinkStand();
	
	@Override
	public String toString()
	{
		return "Actor: " + _actor;
	}
	
	public Creature getActor()
	{
		return _actor;
	}
	
	public final Intention getPreviousIntention()
	{
		return _previousIntention;
	}
	
	protected final void setPreviousIntention(Intention intention)
	{
		_previousIntention.update(intention);
	}
	
	public final Intention getCurrentIntention()
	{
		return _currentIntention;
	}
	
	protected final void setCurrentIntention(IntentionType type, Object firstParameter, Object secondParameter)
	{
		_currentIntention.update(type, firstParameter, secondParameter);
	}
	
	protected final void setCurrentIntention(Intention intention)
	{
		_currentIntention.update(intention);
	}
	
	public final Intention getNextIntention()
	{
		return _nextIntention;
	}
	
	protected final void setNextIntention(IntentionType type, Object firstParameter, Object secondParameter)
	{
		_nextIntention.update(type, firstParameter, secondParameter);
	}
	
	protected final void setNextIntention(Intention intention)
	{
		_nextIntention.update(intention);
	}
	
	/**
	 * Attempt to change the current intention to the provided {@link IntentionType} type parameter by calling changeCurrentIntention.<br>
	 * <br>
	 * If the {@link Creature} is currently not able to act, it schedules the {@link Intention} by calling setNextIntention.
	 * @param type : The new {@link IntentionType} to set.
	 * @param firstParameter : The first {@link Object} parameter of the {@link Intention}.
	 * @param secondParameter : The second {@link Object} parameter of the {@link Intention}.
	 */
	public void tryTo(IntentionType type, Object firstParameter, Object secondParameter)
	{
		if (_currentIntention.equals(type, firstParameter, secondParameter))
			return;
		
		if (_actor.getAttack().isAttackingNow() || _actor.getCast().isCastingNow() || _actor.getCast().isCastingSimultaneouslyNow() || _currentIntention.getType().canScheduleAfter(type))
			setNextIntention(type, firstParameter, secondParameter);
		else
			changeCurrentIntention(type, firstParameter, secondParameter);
	}
	
	/**
	 * Launch the onIntention method corresponding to the {@link IntentionType} set as parameter.<br>
	 * <br>
	 * Also do following actions :
	 * <ul>
	 * <li>Stop follow movement if necessary.</li>
	 * <li>Refresh previous intention with current {@link Intention}.</li>
	 * <li>Reset next {@link Intention}.</li>
	 * </ul>
	 * @param type : The new {@link IntentionType} to set.
	 * @param firstParameter : The first {@link Object} parameter of the {@link Intention}.
	 * @param secondParameter : The second {@link Object} parameter of the {@link Intention}.
	 */
	protected void changeCurrentIntention(IntentionType type, Object firstParameter, Object secondParameter)
	{
		_actor.getMove().stopFollow();
		
		// Refresh previous intention with current intention.
		setPreviousIntention(_currentIntention);
		
		// Reset current intention.
		_currentIntention.reset();
		
		// Reset next intention.
		_nextIntention.reset();
		
		// Launch the onIntention method corresponding to the IntentionType.
		switch (type)
		{
			case IDLE:
				onIntentionIdle();
				break;
			
			case ACTIVE:
				onIntentionActive();
				break;
			
			case SIT:
				onIntentionSit((WorldObject) firstParameter);
				break;
			
			case STAND:
				onIntentionStand();
				break;
			
			case ATTACK:
				onIntentionAttack((Creature) firstParameter, (Boolean) secondParameter);
				break;
			
			case CAST:
				onIntentionCast((SkillUseHolder) firstParameter, (ItemInstance) secondParameter);
				break;
			
			case MOVE_TO:
				onIntentionMoveTo((Location) firstParameter, (Boat) secondParameter);
				break;
			
			case FOLLOW:
				onIntentionFollow((Creature) firstParameter, (Boolean) secondParameter);
				break;
			
			case PICK_UP:
				onIntentionPickUp((WorldObject) firstParameter, (Boolean) secondParameter);
				break;
			
			case INTERACT:
				onIntentionInteract((WorldObject) firstParameter, (Boolean) secondParameter);
				break;
			
			case USE_ITEM:
				onIntentionUseItem((Integer) firstParameter);
				break;
			
			case FAKE_DEATH:
				onIntentionFakeDeath();
				break;
		}
	}
	
	/**
	 * Launch the onIntention method corresponding to an existing {@link Intention}.
	 * @see #changeCurrentIntention(IntentionType, Object, Object)
	 * @param intention : The new {@link Intention} to set.
	 */
	protected final void changeCurrentIntention(Intention intention)
	{
		changeCurrentIntention(intention.getType(), intention.getFirstParameter(), intention.getSecondParameter());
	}
	
	/**
	 * Launch the onEvt method corresponding to the {@link AiEventType} set as parameter.<br>
	 * <br>
	 * <font color=#FF0000><b><u>Caution</u>: The current general intention won't be change (ex: if the character attack and is stunned, he will attack again after the stunned period).</b></font>
	 * @param evt : The {@link AiEventType} whose the AI must be notified.
	 * @param firstParameter : The first {@link Object} parameter of the event.
	 * @param secondParameter : The second {@link Object} parameter of the event.
	 */
	public final void notifyEvent(AiEventType evt, Object firstParameter, Object secondParameter)
	{
		if ((!_actor.isVisible() && !_actor.isTeleporting()) || !_actor.hasAI())
			return;
		
		switch (evt)
		{
			case THINK:
				onEvtThink();
				break;
			
			case ATTACKED:
				onEvtAttacked((Creature) firstParameter);
				break;
			
			case AGGRESSION:
				onEvtAggression((Creature) firstParameter, ((Number) secondParameter).intValue());
				break;
			
			case EVADED:
				onEvtEvaded((Creature) firstParameter);
				break;
			
			case FINISHED_ATTACK:
				onEvtFinishedAttack();
				break;
			
			case FINISHED_ATTACK_BOW:
				onEvtFinishedAttackBow();
				break;
			
			case BOW_ATTACK_REUSED:
				onEvtBowAttackReused();
				break;
			
			case ARRIVED:
				onEvtArrived((Boolean) firstParameter);
				break;
			
			case ARRIVED_BLOCKED:
				onEvtArrivedBlocked((SpawnLocation) firstParameter);
				break;
			
			case CANCEL:
				onEvtCancel();
				break;
			
			case DEAD:
				onEvtDead();
				break;
			
			case FINISHED_CASTING:
				onEvtFinishedCasting((Boolean) firstParameter);
				break;
			
			case SAT_DOWN:
				onEvtSatDown((WorldObject) firstParameter);
				break;
			
			case STOOD_UP:
				onEvtStoodUp();
				break;
			
			case OWNER_ATTACKED:
				onOwnerAttacked((Creature) firstParameter);
		}
	}
	
	protected final void onEvtThink()
	{
		if (_thinking)
		{
			setNextIntention(_currentIntention);
			return;
		}
		
		_thinking = true;
		
		try
		{
			switch (_currentIntention.getType())
			{
				case ATTACK:
					thinkAttack();
					break;
				
				case CAST:
					thinkCast();
					break;
				
				case PICK_UP:
					thinkPickUp();
					break;
				
				case INTERACT:
					thinkInteract();
					break;
				
				case SIT:
					thinkSit();
					break;
				
				case STAND:
					thinkStand();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
		
		if (!_nextIntention.isBlank())
			changeCurrentIntention(_nextIntention);
	}
	
	/**
	 * Cancel action client side by sending ActionFailed packet to the Player actor.
	 */
	protected void clientActionFailed()
	{
	}
	
	/**
	 * Activate the attack stance, broadcasting {@link AutoAttackStart} packet. Refresh the timer if already registered on {@link AttackStanceTaskManager}.
	 */
	public void startAttackStance()
	{
		// Initial check ; if the actor wasn't yet registered into AttackStanceTaskManager, broadcast AutoAttackStart packet.
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
		
		// Set out of the initial if check to be able to refresh the time.
		AttackStanceTaskManager.getInstance().add(_actor);
	}
	
	/**
	 * Deactivate the attack stance, broadcasting {@link AutoAttackStop} packet if the actor was registered on {@link AttackStanceTaskManager}.
	 */
	public void stopAttackStance()
	{
		// If we successfully remove the actor from AttackStanceTaskManager, we also broadcast AutoAttackStop packet.
		if (AttackStanceTaskManager.getInstance().remove(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
	}
	
	/**
	 * Send the state of this actor to a {@link Player}.
	 * @param player : The {@link Player} to notify with the state of this actor.
	 */
	public void describeStateToPlayer(Player player)
	{
		if (_currentIntention.getType() == IntentionType.MOVE_TO)
			_actor.getMove().describeMovementTo(player);
		// else if (getIntention() == CtrlIntention.CAST) TODO
	}
	
	/**
	 * Stop all tasks related to AI.
	 */
	public void stopAITask()
	{
		_actor.getMove().stopFollow();
	}
}