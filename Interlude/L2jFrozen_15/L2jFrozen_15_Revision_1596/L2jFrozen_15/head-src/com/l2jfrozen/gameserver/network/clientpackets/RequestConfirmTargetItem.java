package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExConfirmVariationItem;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Format:(ch) d
 * @author -Wooden-
 */
public final class RequestConfirmTargetItem extends L2GameClientPacket
{
	private int itemObjId;
	
	@Override
	protected void readImpl()
	{
		itemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(itemObjId);
		
		if (item == null)
		{
			return;
		}
		
		if (activeChar.getLevel() < 46)
		{
			activeChar.sendMessage("You have to be level 46 in order to augment an item");
			return;
		}
		
		// check if the item is augmentable
		final int itemGrade = item.getItem().getItemGrade();
		final int itemType = item.getItem().getType2();
		
		if (item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN));
			return;
		}
		// TODO: can do better? : currently: using isdestroyable() as a check for hero / cursed weapons
		else if (itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE2_WEAPON || !item.isDestroyable() || item.isShadowItem())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}
		
		// check if the player can augment
		if (activeChar.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
			return;
		}
		
		if (activeChar.isDead())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
			return;
		}
		
		if (activeChar.isFishing())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
			return;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
			return;
		}
		
		activeChar.sendPacket(new ExConfirmVariationItem(itemObjId));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:29 RequestConfirmTargetItem";
	}
}
