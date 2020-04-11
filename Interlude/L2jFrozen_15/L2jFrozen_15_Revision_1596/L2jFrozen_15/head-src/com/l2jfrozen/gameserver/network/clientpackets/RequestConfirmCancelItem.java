package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExConfirmCancelItem;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Format(ch) d
 * @author -Wooden-
 */
public final class RequestConfirmCancelItem extends L2GameClientPacket
{
	private int itemId;
	
	@Override
	protected void readImpl()
	{
		itemId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(itemId);
		
		if (activeChar == null || item == null)
		{
			return;
		}
		
		if (!item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM));
			return;
		}
		
		int price = 0;
		switch (item.getItem().getItemGrade())
		{
			case L2Item.CRYSTAL_C:
				if (item.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (item.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (item.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (item.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (item.getCrystalCount() < 2824)
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
				return;
		}
		
		activeChar.sendPacket(new ExConfirmCancelItem(itemId, price));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:2D RequestConfirmCancelItem";
	}
}
