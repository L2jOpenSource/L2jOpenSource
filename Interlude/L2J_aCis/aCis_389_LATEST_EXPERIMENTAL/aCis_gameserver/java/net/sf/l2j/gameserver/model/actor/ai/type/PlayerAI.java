package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.LootRule;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.StaticObject;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.BoatEntrance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.network.serverpackets.ChairSit;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocationInVehicle;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;

public class PlayerAI extends PlayableAI
{
	public PlayerAI(Player player)
	{
		super(player);
	}
	
	@Override
	protected void onEvtArrived(Boolean forceStopped)
	{
		if (_currentIntention.getType() == IntentionType.MOVE_TO && _currentIntention.getSecondParameter() != null)
		{
			final Boat boat = (Boat) _currentIntention.getSecondParameter();
			final BoatEntrance closestEntrance = boat.getClosestEntrance(getActor().getPosition());
			
			getActor().getBoatPosition().set(closestEntrance.getInnerLocation());
			
			// Since we're close enough to the boat we just send client onboarding packet without any movement on the server.
			getActor().broadcastPacket(new MoveToLocationInVehicle(getActor(), boat, closestEntrance.getInnerLocation(), getActor().getPosition()));
		}
		
		super.onEvtArrived(forceStopped);
	}
	
	@Override
	protected void onEvtSatDown(WorldObject target)
	{
		if (_nextIntention.isBlank())
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtStoodUp()
	{
		// Free the throne
		if (getActor().getThroneId() != 0)
		{
			final WorldObject object = World.getInstance().getObject(getActor().getThroneId());
			if (object instanceof StaticObject)
				((StaticObject) object).setBusy(false);
			
			getActor().setThroneId(0);
		}
		
		if (_nextIntention.isBlank())
			changeCurrentIntention(IntentionType.ACTIVE, null, null);
		else
			changeCurrentIntention(_nextIntention);
	}
	
	@Override
	protected void onEvtBowAttackReused()
	{
		if (_currentIntention.getType() == IntentionType.ATTACK && getActor().getAttackType() == WeaponType.BOW)
		{
			final Creature target = (Creature) _currentIntention.getFirstParameter();
			
			if (getActor().continueAttackingPlayable() || !(target instanceof Playable))
				notifyEvent(AiEventType.THINK, null, null);
			else
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		if (getActor().getTamedBeast() != null)
			getActor().getTamedBeast().getAI().notifyEvent(AiEventType.OWNER_ATTACKED, attacker, null);
		
		super.onEvtAttacked(attacker);
	}
	
	@Override
	protected void onIntentionSit(WorldObject target)
	{
		if (getActor().denyAiAction() || getActor().isSitting() || getActor().getMountType() != 0)
		{
			clientActionFailed();
			return;
		}
		
		setCurrentIntention(IntentionType.SIT, target, null);
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionStand()
	{
		if (getActor().denyAiAction() || !getActor().isSitting() || getActor().getMountType() != 0)
		{
			clientActionFailed();
			return;
		}
		
		setCurrentIntention(IntentionType.STAND, null, null);
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionActive()
	{
		setCurrentIntention(IntentionType.IDLE, null, null);
	}
	
	@Override
	protected void onIntentionInteract(WorldObject object, Boolean isShiftPressed)
	{
		if (getActor().denyAiAction() || getActor().isSitting() || getActor().isFlying())
		{
			clientActionFailed();
			return;
		}
		
		getActor().getMove().stop();
		
		setCurrentIntention(IntentionType.INTERACT, object, isShiftPressed);
		
		getActor().getMove().maybeMoveToPawn(object, 60, isShiftPressed);
	}
	
	@Override
	protected void onIntentionUseItem(Integer objectId)
	{
		final ItemInstance itemToTest = getActor().getInventory().getItemByObjectId(objectId);
		if (itemToTest == null)
			return;
		
		getActor().useEquippableItem(itemToTest, false);
		
		setCurrentIntention(getPreviousIntention());
		notifyEvent(AiEventType.THINK, null, null);
	}
	
	@Override
	protected void onIntentionFakeDeath()
	{
		if (getActor().denyAiAction() || getActor().isSitting() || getActor().getMountType() != 0)
		{
			clientActionFailed();
			return;
		}
		
		getActor().getMove().stop();
		
		getActor().startFakeDeath();
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
		final L2Skill skill = skillUseHolder.getSkill();
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			getActor().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players wearing Formal Wear cannot use skills.
		if (getActor().isWearingFormalWear())
		{
			getActor().sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
			getActor().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (getActor().isSkillDisabled(skill))
		{
			getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
			return;
		}
		
		final WorldObject target = skillUseHolder.getTarget();
		final boolean forceUse = skillUseHolder.isCtrlPressed();
		final boolean dontMove = skillUseHolder.isShiftPressed();
		
		// Set the player _currentSkill.
		getActor().setCurrentSkill(skill, target, forceUse, dontMove);
		
		if (!getActor().checkUseMagicConditions(skill, forceUse, dontMove))
			return;
		
		super.onIntentionCast(skillUseHolder, itemInstance);
	}
	
	@Override
	protected void thinkCast()
	{
		final SkillUseHolder skillUseHolder = (SkillUseHolder) _currentIntention.getFirstParameter();
		final L2Skill skill = skillUseHolder.getSkill();
		
		if (skill.getTargetType() == SkillTargetType.GROUND)
		{
			if (getActor().getMove().maybeMoveToPosition(getActor().getCurrentSkillWorldPosition(), skill.getCastRange()))
			{
				getActor().getCast().setIsCastingNow(false);
				return;
			}
		}
		else
		{
			final WorldObject target = skillUseHolder.getTarget();
			
			if (checkTargetLost(target))
			{
				getActor().getCast().setIsCastingNow(false);
				return;
			}
			
			final Boolean isShiftPressed = skillUseHolder.isShiftPressed();
			
			if (getActor().getMove().maybeMoveToPawn(target, skill.getCastRange(), isShiftPressed))
			{
				if (isShiftPressed)
					changeCurrentIntention(IntentionType.ACTIVE, null, null);
				
				getActor().getCast().setIsCastingNow(false);
				return;
			}
		}
		
		getActor().getCast().doCast(skill);
	}
	
	@Override
	protected void thinkPickUp()
	{
		final WorldObject target = (WorldObject) _currentIntention.getFirstParameter();
		if (checkTargetLost(target))
			return;
		
		Boolean isShiftPressed = (Boolean) _currentIntention.getSecondParameter();
		if (isShiftPressed == null)
			isShiftPressed = false;
		
		if (getActor().getMove().maybeMoveToPawn(target, 36, isShiftPressed))
		{
			if (isShiftPressed)
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			
			return;
		}
		
		if (!(target instanceof ItemInstance))
			return;
		
		final ItemInstance item = (ItemInstance) target;
		
		getActor().sendPacket(ActionFailed.STATIC_PACKET);
		
		synchronized (item)
		{
			if (!item.isVisible())
				return;
			
			if (!getActor().getInventory().validateWeight(item.getCount() * item.getItem().getWeight()))
			{
				getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
				return;
			}
			
			if (((getActor().isInParty() && getActor().getParty().getLootRule() == LootRule.ITEM_LOOTER) || !getActor().isInParty()) && !getActor().getInventory().validateCapacity(item))
			{
				getActor().sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (getActor().getActiveTradeList() != null)
			{
				getActor().sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				return;
			}
			
			if (item.getOwnerId() != 0 && !getActor().isLooterOrInLooterParty(item.getOwnerId()))
			{
				if (item.getItemId() == 57)
					getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
				else if (item.getCount() > 1)
					getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item).addNumber(item.getCount()));
				else
					getActor().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item));
				
				return;
			}
			
			if (item.hasDropProtection())
				item.removeDropProtection();
			
			item.pickupMe(getActor());
			
			ItemsOnGroundTaskManager.getInstance().remove(item);
		}
		
		if (item.getItemType() == EtcItemType.HERB)
		{
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(getActor(), item, false);
			
			item.destroyMe("Consume", getActor(), null);
		}
		else if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
		{
			getActor().addItem("Pickup", item, null, true);
		}
		else
		{
			if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType)
			{
				SystemMessage msg;
				if (item.getEnchantLevel() > 0)
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(getActor().getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
				else
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(getActor().getName()).addItemName(item.getItemId());
				
				getActor().broadcastPacketInRadius(msg, 1400);
			}
			
			if (getActor().isInParty())
				getActor().getParty().distributeItem(getActor(), item);
			else if (item.getItemId() == 57 && getActor().getInventory().getAdenaInstance() != null)
			{
				getActor().addAdena("Pickup", item.getCount(), null, true);
				item.destroyMe("Pickup", getActor(), null);
			}
			else
				getActor().addItem("Pickup", item, null, true);
		}
		
		ThreadPool.schedule(() -> getActor().setIsParalyzed(false), (int) (700 / getActor().getStat().getMovementSpeedMultiplier()));
		getActor().setIsParalyzed(true);
		
		changeCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void thinkInteract()
	{
		final WorldObject target = (WorldObject) _currentIntention.getFirstParameter();
		if (checkTargetLost(target))
			return;
		
		Boolean isShiftPressed = (Boolean) _currentIntention.getSecondParameter();
		if (isShiftPressed == null)
			isShiftPressed = false;
		
		if (getActor().getMove().maybeMoveToPawn(target, 60, isShiftPressed))
		{
			if (isShiftPressed)
				changeCurrentIntention(IntentionType.ACTIVE, null, null);
			
			return;
		}
		
		if (!(target instanceof StaticObject))
			getActor().getActingPlayer().doInteract((Creature) target);
		
		changeCurrentIntention(IntentionType.ACTIVE, null, null);
	}
	
	@Override
	protected void thinkSit()
	{
		final WorldObject target = (WorldObject) _currentIntention.getFirstParameter();
		
		getActor().getMove().stop();
		
		// sitDown sends the ChangeWaitType packet, which MUST precede the ChairSit packet (sent in this function) in order to properly sit on the throne.
		getActor().sitDown();
		
		final boolean isThrone = target instanceof StaticObject && ((StaticObject) target).getType() == 1;
		
		// Occupy the throne
		if (isThrone && !((StaticObject) target).isBusy() && getActor().isIn3DRadius(target, Npc.INTERACTION_DISTANCE))
		{
			getActor().setThroneId(target.getObjectId());
			
			((StaticObject) target).setBusy(true);
			getActor().broadcastPacket(new ChairSit(getActor().getObjectId(), ((StaticObject) target).getStaticObjectId()));
		}
	}
	
	@Override
	protected void thinkStand()
	{
		if (getActor().isFakeDeath())
			getActor().stopFakeDeath(true);
		else
			getActor().standUp();
	}
	
	@Override
	public void startAttackStance()
	{
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(getActor()))
		{
			final Summon summon = getActor().getSummon();
			if (summon != null)
				summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
			
			getActor().broadcastPacket(new AutoAttackStart(getActor().getObjectId()));
		}
		
		AttackStanceTaskManager.getInstance().add(getActor());
	}
	
	@Override
	protected void clientActionFailed()
	{
		getActor().sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public Player getActor()
	{
		return (Player) _actor;
	}
	
}