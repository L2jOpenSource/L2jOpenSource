package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.LootRule;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;

public class SummonAI extends PlayableAI
{
	private static final int AVOID_RADIUS = 70;
	
	private volatile boolean _startFollow = ((Summon) _actor).getFollowStatus();
	
	public SummonAI(Summon summon)
	{
		super(summon);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		_startFollow = false;
		
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		if (_nextIntention.isBlank() && _startFollow)
			changeCurrentIntention(IntentionType.FOLLOW, getOwner(), false);
		else
			super.onIntentionActive();
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
		final L2Skill skill = skillUseHolder.getSkill();
		
		// Check if the skill is active and ignore the passive skill request
		if (skill.isPassive())
			return;
		
		if (getActor().isSkillDisabled(skill))
		{
			getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addString(skill.getName()));
			return;
		}
		
		final WorldObject target = skillUseHolder.getTarget();
		final boolean forceUse = skillUseHolder.isCtrlPressed();
		final boolean dontMove = skillUseHolder.isShiftPressed();
		
		// Set current pet skill
		getOwner().setCurrentPetSkill(skill, target, forceUse, dontMove);
		
		if (!getActor().checkUseMagicConditions(skill, forceUse, dontMove))
			return;
		
		super.onIntentionCast(skillUseHolder, itemInstance);
	}
	
	@Override
	protected void onEvtFinishedCasting(Boolean success)
	{
		if (success)
		{
			if (_nextIntention.isBlank())
			{
				if (_previousIntention.getType() == IntentionType.ATTACK)
					changeCurrentIntention(_previousIntention);
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
	public void onEvtAttacked(Creature attacker)
	{
		super.onEvtAttacked(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		super.onEvtEvaded(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	protected void thinkCast()
	{
		final SkillUseHolder skillUseHolder = (SkillUseHolder) _currentIntention.getFirstParameter();
		
		final WorldObject target = skillUseHolder.getTarget();
		if (checkTargetLost(target))
		{
			getActor().getCast().setIsCastingNow(false);
			return;
		}
		
		final L2Skill skill = skillUseHolder.getSkill();
		final Boolean isShiftPressed = skillUseHolder.isShiftPressed();
		if (_actor.getMove().maybeMoveToPawn(target, skill.getCastRange(), isShiftPressed))
		{
			if (isShiftPressed)
				setCurrentIntention(IntentionType.ACTIVE, null, null);
			
			getActor().getCast().setIsCastingNow(false);
			return;
		}
		
		_actor.getCast().doCast(skill);
	}
	
	@Override
	protected void thinkPickUp()
	{
		final WorldObject target = (WorldObject) _currentIntention.getFirstParameter();
		if (checkTargetLost(target))
		{
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
			return;
		}
		
		Boolean isShiftPressed = (Boolean) _currentIntention.getSecondParameter();
		if (isShiftPressed == null)
			isShiftPressed = false;
		
		if (_actor.getMove().maybeMoveToPawn(target, 36, isShiftPressed))
		{
			if (isShiftPressed)
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			
			return;
		}
		
		if (_actor.isDead())
			return;
		
		changeCurrentIntention(IntentionType.IDLE, null, null);
		
		if (!(target instanceof ItemInstance))
			return;
		
		final ItemInstance item = (ItemInstance) target;
		
		if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
		{
			getActor().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item.getItemId()));
			return;
		}
		
		if (item.getItem().getItemType() == EtcItemType.ARROW || item.getItem().getItemType() == EtcItemType.SHOT)
		{
			getActor().getOwner().sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		synchronized (target)
		{
			if (!target.isVisible())
				return;
			
			if (!getActor().getInventory().validateCapacity(item))
			{
				getActor().getOwner().sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!getActor().getInventory().validateWeight(item, item.getCount()))
			{
				getActor().getOwner().sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			if (item.getOwnerId() != 0 && !getActor().getOwner().isLooterOrInLooterParty(item.getOwnerId()))
			{
				if (item.getItemId() == 57)
					getActor().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
				else if (item.getCount() > 1)
					getActor().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item.getItemId()).addNumber(item.getCount()));
				else
					getActor().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item.getItemId()));
				
				return;
			}
			
			if (item.hasDropProtection())
				item.removeDropProtection();
			
			final Party party = getActor().getOwner().getParty();
			if (party != null && party.getLootRule() != LootRule.ITEM_LOOTER)
				party.distributeItem(getActor().getOwner(), item);
			else
				item.pickupMe(_actor);
			
			ItemsOnGroundTaskManager.getInstance().remove(item);
		}
		
		if (item.getItemType() == EtcItemType.HERB)
		{
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(getActor(), item, false);
			
			item.destroyMe("Consume", getActor().getOwner(), null);
			getActor().broadcastStatusUpdate();
		}
		else
		{
			if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType)
			{
				SystemMessage msg;
				if (item.getEnchantLevel() > 0)
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2_S3).addCharName(getActor().getOwner()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
				else
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2).addCharName(getActor().getOwner()).addItemName(item.getItemId());
				
				getActor().getOwner().broadcastPacketInRadius(msg, 1400);
			}
			
			SystemMessage sm2;
			if (item.getItemId() == 57)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addItemNumber(item.getCount());
			else if (item.getEnchantLevel() > 0)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			else if (item.getCount() > 1)
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addItemName(item.getItemId()).addItemNumber(item.getCount());
			else
				sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addItemName(item.getItemId());
			
			getActor().getOwner().sendPacket(sm2);
			getActor().getInventory().addItem("Pickup", item, getActor().getOwner(), getActor());
			getActor().getOwner().sendPacket(new PetItemList(getActor()));
		}
		
		if (getActor().getFollowStatus())
			getActor().followOwner();
	}
	
	@Override
	public Summon getActor()
	{
		return (Summon) _actor;
	}
	
	private Player getOwner()
	{
		return getActor().getOwner();
	}
	
	@Override
	public void startAttackStance()
	{
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(getOwner()))
		{
			getActor().broadcastPacket(new AutoAttackStart(getActor().getObjectId()));
			getOwner().broadcastPacket(new AutoAttackStart(getOwner().getObjectId()));
		}
		
		AttackStanceTaskManager.getInstance().add(getOwner());
	}
	
	@Override
	public void stopAttackStance()
	{
		getActor().broadcastPacket(new AutoAttackStop(getActor().getObjectId()));
	}
	
	private void avoidAttack(Creature attacker)
	{
		final Player owner = getOwner();
		
		if (owner == null || owner == attacker || !owner.isIn3DRadius(_actor, 2 * AVOID_RADIUS) || !AttackStanceTaskManager.getInstance().isInAttackStance(owner))
			return;
		
		if (_currentIntention.getType() != IntentionType.ACTIVE && _currentIntention.getType() != IntentionType.FOLLOW)
			return;
		
		if (_actor.isMoving() || _actor.isDead() || _actor.isMovementDisabled())
			return;
		
		final int ownerX = owner.getX();
		final int ownerY = owner.getY();
		final double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());
		
		final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
		final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
		
		_actor.getMove().moveToLocation(targetX, targetY, _actor.getZ());
	}
	
	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (_currentIntention.getType())
		{
			case ACTIVE:
			case FOLLOW:
			case IDLE:
			case MOVE_TO:
			case PICK_UP:
				((Summon) _actor).setFollowStatus(_startFollow);
		}
	}
	
	public void setStartFollowController(boolean val)
	{
		_startFollow = val;
	}
}