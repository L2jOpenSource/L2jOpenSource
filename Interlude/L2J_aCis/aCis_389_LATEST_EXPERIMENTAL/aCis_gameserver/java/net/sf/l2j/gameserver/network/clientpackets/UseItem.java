package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.ActionType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.L2Skill;

public final class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (player.isOperating())
		{
			player.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		
		if (player.isItemDisabled(item))
			return;
		
		if (item.getItem().getType2() == Item.TYPE2_QUEST)
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		if (player.isAlikeDead() || player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAfraid())
			return;
		
		if (!Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
		{
			final IntIntHolder[] sHolders = item.getItem().getSkills();
			if (sHolders != null)
			{
				for (final IntIntHolder sHolder : sHolders)
				{
					final L2Skill skill = sHolder.getSkill();
					if (skill != null && (skill.getSkillType() == SkillType.TELEPORT || skill.getSkillType() == SkillType.RECALL))
						return;
				}
			}
		}
		
		if (player.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot)
		{
			player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		/*
		 * The player can't use pet items if no pet is currently summoned. If a pet is summoned and player uses the item directly, it will be used by the pet.
		 */
		if (item.isPetItem())
		{
			// If no pet, cancels the use
			if (!player.hasPet())
			{
				player.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}
			
			final Pet pet = ((Pet) player.getSummon());
			
			if (!pet.canWear(item.getItem()))
			{
				player.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			if (pet.isDead())
			{
				player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}
			
			if (!pet.getInventory().validateCapacity(item))
			{
				player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!pet.getInventory().validateWeight(item, 1))
			{
				player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			player.transferItem("Transfer", _objectId, 1, pet.getInventory(), pet);
			
			// Equip it, removing first the previous item.
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
			}
			else
			{
				pet.getInventory().equipPetItem(item);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
			}
			
			player.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}
		
		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(player, player, true))
				return;
		}
		
		if (item.isEquipable())
		{
			if (player.getCast().isCastingNow() || player.getCast().isCastingSimultaneouslyNow())
			{
				player.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case Item.SLOT_LR_HAND:
				case Item.SLOT_L_HAND:
				case Item.SLOT_R_HAND:
				{
					if (player.isMounted())
					{
						player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					// Don't allow weapon/shield equipment if a cursed weapon is equipped
					if (player.isCursedWeaponEquipped())
						return;
					
					break;
				}
			}
			
			if (player.isCursedWeaponEquipped() && item.getItemId() == 6408) // Don't allow to put formal wear
				return;
			
			player.getAI().tryTo(IntentionType.USE_ITEM, _objectId, null);
		}
		else
		{
			if (player.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE)
			{
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				player.broadcastUserInfo();
				
				sendPacket(new ItemList(player, false));
				return;
			}
			
			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
			{
				handler.useItem(player, item, _ctrlPressed);
				
				if (item.isEtcItem())
					player.disableItem(item, item.getEtcItem().getReuseDelay());
			}
			
			for (final Quest quest : item.getQuestEvents())
			{
				final QuestState state = player.getQuestState(quest.getName());
				if (state == null || !state.isStarted())
					continue;
				
				quest.notifyItemUse(item, player, player.getTarget());
			}
		}
	}
}