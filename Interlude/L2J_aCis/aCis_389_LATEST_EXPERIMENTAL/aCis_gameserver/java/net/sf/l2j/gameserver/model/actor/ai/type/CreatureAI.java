package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class CreatureAI extends AbstractAI
{
	public CreatureAI(Creature actor)
	{
		super(actor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.IDLE, null, null);
	}
	
	@Override
	protected void onIntentionActive()
	{
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void onIntentionAttack(Creature target, Boolean isShiftPressed)
	{
		if (getActor().denyAiAction() || getActor().isSitting())
		{
			clientActionFailed();
			return;
		}
		
		// TODO advExt doesn't send a StopMove packet here. We do it here so we trigger the ARRIVED event now, rahter than later
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.ATTACK, target, isShiftPressed);
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
		// No check for intention already set to CAST, because the target can be different
		if (getActor().denyAiAction() || getActor().isSitting() || getActor().getAllSkillsDisabled())
		{
			clientActionFailed();
			return;
		}
		
		// TODO advExt doesn't send a StopMove packet here. If the cast should stopMove(), this is where it should do it, not in doCast. Temporary workaroud is in place with firstParam of onEvtArrived
		
		setCurrentIntention(IntentionType.CAST, skillUseHolder, itemInstance);
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc, Boat boat)
	{
		if (getActor().isMovementDisabled())
		{
			clientActionFailed();
			return;
		}
		
		setCurrentIntention(IntentionType.MOVE_TO, loc, boat);
		
		getActor().getMove().moveToLocation(loc);
	}
	
	@Override
	protected void onIntentionFollow(Creature target, Boolean isShiftPressed)
	{
		if (getActor().denyAiAction() || getActor().isSitting() || getActor().isMovementDisabled())
		{
			clientActionFailed();
			return;
		}
		
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.FOLLOW, target, isShiftPressed);
		
		getActor().getMove().startFollow(target);
	}
	
	@Override
	protected void onEvtFinishedAttack()
	{
		if (_nextIntention.isBlank())
			notifyEvent(AiEventType.THINK, null, null);
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtFinishedAttackBow()
	{
		if (!_nextIntention.isBlank())
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtBowAttackReused()
	{
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onEvtFinishedCasting(Boolean success)
	{
		if (success)
		{
			if (_nextIntention.isBlank())
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			else
				changeCurrentIntention(_nextIntention);
		}
		else
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void onEvtArrived(Boolean forceStopped)
	{
		getActor().revalidateZone(true);
		getActor().getMove().cancelMoveTask();
		
		if (_nextIntention.isBlank())
		{
			if (_currentIntention.getType() == IntentionType.MOVE_TO)
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			else if (!forceStopped)
				notifyEvent(AiEventType.THINK, null, null);
		}
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtArrivedBlocked(SpawnLocation loc)
	{
		getActor().getMove().stop();
		
		if (_nextIntention.isBlank())
		{
			if (_currentIntention.getType() == IntentionType.MOVE_TO)
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			else
				notifyEvent(AiEventType.THINK, null, null);
		}
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtCancel()
	{
		getActor().getCast().stop();
		getActor().getMove().stopFollow();
		
		changeCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		
		getActor().broadcastPacket(new Die(getActor()));
		
		stopAttackStance();
		
		if (!(getActor() instanceof Playable))
			getActor().forceWalkStance();
		
		changeCurrentIntention(IntentionType.IDLE, null, null);
	}
	
	@Override
	protected void thinkAttack()
	{
		final Creature target = (Creature) _currentIntention.getFirstParameter();
		if (target == null)
			return;
		
		Boolean isShiftPressed = (Boolean) _currentIntention.getSecondParameter();
		if (isShiftPressed == null)
			isShiftPressed = false;
		
		if (getActor().getMove().maybeMoveToPawn(target, getActor().getPhysicalAttackRange(), isShiftPressed))
		{
			if (isShiftPressed)
				setCurrentIntention(IntentionType.ACTIVE, null, null);
			
			return;
		}
		
		getActor().getAttack().doAttack(target);
	}
	
	@Override
	protected void thinkCast()
	{
		final SkillUseHolder skillUseHolder = (SkillUseHolder) _currentIntention.getFirstParameter();
		
		final WorldObject target = skillUseHolder.getTarget();
		if (checkTargetLost(target))
		{
			setCurrentIntention(IntentionType.ACTIVE, null, null);
			return;
		}
		
		final L2Skill skill = skillUseHolder.getSkill();
		final Boolean isShiftPressed = skillUseHolder.isShiftPressed();
		if (getActor().getMove().maybeMoveToPawn(target, skill.getCastRange(), isShiftPressed))
		{
			if (isShiftPressed)
				setCurrentIntention(IntentionType.ACTIVE, null, null);
			
			getActor().getCast().setIsCastingNow(false);
			return;
		}
		
		getActor().getCast().doCast(skill);
	}
	
	/**
	 * @param target The targeted WorldObject
	 * @return true if the target is lost or dead (fake death isn't considered), and set intention to ACTIVE.
	 */
	protected boolean checkTargetLostOrDead(Creature target)
	{
		if (target == null || target.isDead())
		{
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
			return true;
		}
		return false;
	}
	
	/**
	 * @param target : The targeted WorldObject
	 * @return true if the target is lost, and set intention to ACTIVE.
	 */
	protected boolean checkTargetLost(WorldObject target)
	{
		if (target == null)
		{
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
			return true;
		}
		return false;
	}
	
	public boolean canAura(L2Skill sk, Creature originalTarget)
	{
		if (sk.getTargetType() == SkillTargetType.AURA || sk.getTargetType() == SkillTargetType.BEHIND_AURA || sk.getTargetType() == SkillTargetType.FRONT_AURA)
		{
			for (final WorldObject target : getActor().getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
			{
				if (target == originalTarget)
					return true;
			}
		}
		return false;
	}
	
	public boolean canAOE(L2Skill sk, Creature originalTarget)
	{
		if (sk.getSkillType() != SkillType.NEGATE || sk.getSkillType() != SkillType.CANCEL)
		{
			if (sk.getTargetType() == SkillTargetType.AURA || sk.getTargetType() == SkillTargetType.BEHIND_AURA || sk.getTargetType() == SkillTargetType.FRONT_AURA)
			{
				boolean cancast = true;
				for (final Creature target : getActor().getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(getActor(), target))
						continue;
					
					if (target instanceof Attackable && !getActor().isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == SkillTargetType.AREA || sk.getTargetType() == SkillTargetType.BEHIND_AREA || sk.getTargetType() == SkillTargetType.FRONT_AREA)
			{
				boolean cancast = true;
				for (final Creature target : originalTarget.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(getActor(), target))
						continue;
					
					if (target instanceof Attackable && !getActor().isConfused())
						continue;
					
					final AbstractEffect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
		}
		else
		{
			if (sk.getTargetType() == SkillTargetType.AURA || sk.getTargetType() == SkillTargetType.BEHIND_AURA || sk.getTargetType() == SkillTargetType.FRONT_AURA)
			{
				boolean cancast = false;
				for (final Creature target : getActor().getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(getActor(), target))
						continue;
					
					if (target instanceof Attackable && !getActor().isConfused())
						continue;
					
					final AbstractEffect[] effects = target.getAllEffects();
					if (effects.length > 0)
						cancast = true;
				}
				if (cancast)
					return true;
			}
			else if (sk.getTargetType() == SkillTargetType.AREA || sk.getTargetType() == SkillTargetType.BEHIND_AREA || sk.getTargetType() == SkillTargetType.FRONT_AREA)
			{
				boolean cancast = true;
				for (final Creature target : originalTarget.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()))
				{
					if (!GeoEngine.getInstance().canSeeTarget(getActor(), target))
						continue;
					
					if (target instanceof Attackable && !getActor().isConfused())
						continue;
					
					if (target.getFirstEffect(sk) != null)
						cancast = false;
				}
				
				if (cancast)
					return true;
			}
		}
		return false;
	}
	
	public boolean canParty(L2Skill sk)
	{
		// Only TARGET_PARTY skills are allowed to be tested.
		if (sk.getTargetType() != SkillTargetType.PARTY)
			return false;
		
		// Retrieve actor factions.
		final String[] actorClans = ((Npc) getActor()).getTemplate().getClans();
		
		// Test all Attackable around skill radius.
		for (final Attackable target : getActor().getKnownTypeInRadius(Attackable.class, sk.getSkillRadius()))
		{
			// Can't see the target, continue.
			if (!GeoEngine.getInstance().canSeeTarget(getActor(), target))
				continue;
			
			// Faction doesn't match, continue.
			if (!ArraysUtil.contains(actorClans, target.getTemplate().getClans()))
				continue;
			
			// Return true if at least one target is missing the buff.
			if (target.getFirstEffect(sk) == null)
				return true;
		}
		return false;
	}
	
	@Override
	protected void thinkPickUp()
	{
	}
	
	@Override
	protected void thinkInteract()
	{
	}
	
	@Override
	protected void thinkSit()
	{
	}
	
	@Override
	protected void thinkStand()
	{
	}
	
	@Override
	protected void onEvtSatDown(WorldObject target)
	{
	}
	
	@Override
	protected void onEvtStoodUp()
	{
	}
	
	@Override
	protected void onIntentionSit(WorldObject target)
	{
	}
	
	@Override
	protected void onIntentionStand()
	{
	}
	
	@Override
	protected void onIntentionPickUp(WorldObject object, Boolean isShiftPressed)
	{
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object, Boolean isShiftPressed)
	{
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		startAttackStance();
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
	}
	
	@Override
	protected void onIntentionUseItem(Integer objectId)
	{
	}
	
	@Override
	protected void onIntentionFakeDeath()
	{
	}
	
	@Override
	protected void onOwnerAttacked(Creature attacker)
	{
	}
}