package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExVariationCancelResult;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Format(ch) d
 * @author -Wooden-
 */
public final class RequestRefineCancel extends L2GameClientPacket
{
	private int targetItemObjId;
	
	@Override
	protected void readImpl()
	{
		targetItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = (L2ItemInstance) L2World.getInstance().findObject(targetItemObjId);
		
		if (activeChar == null)
		{
			return;
		}
		
		if (targetItem == null)
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}
		
		// cannot remove augmentation from a not augmented item
		if (!targetItem.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM));
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}
		
		// get the price
		int price = 0;
		switch (targetItem.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if (targetItem.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (targetItem.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (targetItem.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (targetItem.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (targetItem.getCrystalCount() < 2824)
				{
					price = 390000;
				}
				else
				{
					price = 420000;
				}
				break;
			case L2Item.CRYSTAL_S:
				price = 480000;
				break;
			// any other item type is not augmentable
			default:
				activeChar.sendPacket(new ExVariationCancelResult(0));
				return;
		}
		
		// try to reduce the players adena
		if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true))
		{
			return;
		}
		
		// unequip item
		if (targetItem.isEquipped())
		{
			activeChar.disarmWeapons();
		}
		
		// remove the augmentation
		targetItem.removeAugmentation();
		
		// send ExVariationCancelResult
		activeChar.sendPacket(new ExVariationCancelResult(1));
		
		// send inventory update
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		
		// send system message
		final SystemMessage sm = new SystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
		sm.addString(targetItem.getItemName());
		activeChar.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:2E RequestRefineCancel";
	}
	
}
