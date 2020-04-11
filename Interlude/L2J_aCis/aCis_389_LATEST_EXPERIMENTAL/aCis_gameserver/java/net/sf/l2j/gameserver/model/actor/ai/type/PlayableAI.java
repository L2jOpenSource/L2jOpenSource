package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.L2Skill;

public abstract class PlayableAI extends CreatureAI
{
	public PlayableAI(Playable playable)
	{
		super(playable);
	}
	
	@Override
	protected void onEvtFinishedCasting(Boolean success)
	{
		if (success)
		{
			if (_nextIntention.isBlank())
			{
				if (_currentIntention.getType() == IntentionType.CAST)
				{
					final SkillUseHolder skillUseHolder = (SkillUseHolder) _currentIntention.getFirstParameter();
					final L2Skill skill = skillUseHolder.getSkill();
					final WorldObject target = skillUseHolder.getTarget();
					
					if (skill.nextActionIsAttack() && target.isAutoAttackable(getActor()))
						changeCurrentIntention(IntentionType.ATTACK, target, null);
					else
						changeCurrentIntention(IntentionType.ACTIVE, null, null);
				}
				else
					changeCurrentIntention(IntentionType.ACTIVE, null, null);
			}
			else
				changeCurrentIntention(_nextIntention);
		}
		else
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void onEvtFinishedAttack()
	{
		if (_nextIntention.isBlank())
		{
			final Creature target = (Creature) _currentIntention.getFirstParameter();
			
			if (getActor().continueAttackingPlayable() || !(target instanceof Playable))
				notifyEvent(AiEventType.THINK, null, null);
			else
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
		}
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onIntentionPickUp(WorldObject object, Boolean isShiftPressed)
	{
		if (getActor().denyAiAction() || getActor().isSitting() || isShiftPressed)
		{
			clientActionFailed();
			return;
		}
		
		if (object instanceof ItemInstance && ((ItemInstance) object).getLocation() != ItemLocation.VOID)
			return;
		
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.PICK_UP, object, isShiftPressed);
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionFollow(Creature target, Boolean isShiftPressed)
	{
		if (isShiftPressed)
		{
			clientActionFailed();
			return;
		}
		
		super.onIntentionFollow(target, isShiftPressed);
	}
	
	@Override
	protected void onIntentionAttack(Creature target, Boolean isShiftPressed)
	{
		if (target instanceof Playable)
		{
			final Player targetPlayer = target.getActingPlayer();
			final Player actorPlayer = getActor().getActingPlayer();
			
			if (!target.isInsideZone(ZoneId.PVP))
			{
				if (targetPlayer.getProtectionBlessing() && (actorPlayer.getLevel() - targetPlayer.getLevel()) >= 10 && actorPlayer.getKarma() > 0)
				{
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					return;
				}
				
				if (actorPlayer.getProtectionBlessing() && (targetPlayer.getLevel() - actorPlayer.getLevel()) >= 10 && targetPlayer.getKarma() > 0)
				{
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					return;
				}
			}
			
			if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
			
			if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				return;
			}
		}
		super.onIntentionAttack(target, isShiftPressed);
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
		if (itemInstance != null)
		{
			final L2Skill itemSkill = skillUseHolder.getSkill();
			
			// Normal item consumption is 1, if more, it must be given in DP with getItemConsume().
			if (!getActor().destroyItem("Consume", itemInstance.getObjectId(), (itemSkill.getItemConsumeId() == 0 && itemSkill.getItemConsume() > 0) ? itemSkill.getItemConsume() : 1, null, false))
			{
				getActor().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}
		
		if (skillUseHolder.getTarget() instanceof Playable && skillUseHolder.getSkill().isOffensive())
		{
			final Player targetPlayer = skillUseHolder.getTarget().getActingPlayer();
			final Player actorPlayer = getActor().getActingPlayer();
			
			if (!skillUseHolder.getTarget().isInsideZone(ZoneId.PVP))
			{
				if (targetPlayer.getProtectionBlessing() && (actorPlayer.getLevel() - targetPlayer.getLevel()) >= 10 && actorPlayer.getKarma() > 0)
				{
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					getActor().getCast().setIsCastingNow(false);
					return;
				}
				
				if (actorPlayer.getProtectionBlessing() && (targetPlayer.getLevel() - actorPlayer.getLevel()) >= 10 && targetPlayer.getKarma() > 0)
				{
					actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					clientActionFailed();
					getActor().getCast().setIsCastingNow(false);
					return;
				}
			}
			
			if (targetPlayer.isCursedWeaponEquipped() && actorPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				getActor().getCast().setIsCastingNow(false);
				return;
			}
			
			if (actorPlayer.isCursedWeaponEquipped() && targetPlayer.getLevel() <= 20)
			{
				actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				clientActionFailed();
				getActor().getCast().setIsCastingNow(false);
				return;
			}
		}
		
		super.onIntentionCast(skillUseHolder, itemInstance);
	}
	
	@Override
	public Playable getActor()
	{
		return (Playable) _actor;
	}
	
}