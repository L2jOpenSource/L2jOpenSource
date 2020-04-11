package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExConfirmVariationGemstone;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public final class RequestConfirmGemStone extends L2GameClientPacket
{
	private int targetItemObjId;
	private int refinerItemObjId;
	private int gemstoneItemObjId;
	private int gemstoneCount;
	
	@Override
	protected void readImpl()
	{
		targetItemObjId = readD();
		refinerItemObjId = readD();
		gemstoneItemObjId = readD();
		gemstoneCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = (L2ItemInstance) L2World.getInstance().findObject(targetItemObjId);
		final L2ItemInstance refinerItem = (L2ItemInstance) L2World.getInstance().findObject(refinerItemObjId);
		final L2ItemInstance gemstoneItem = (L2ItemInstance) L2World.getInstance().findObject(gemstoneItemObjId);
		
		if (targetItem == null || refinerItem == null || gemstoneItem == null)
		{
			return;
		}
		
		// Make sure the item is a gemstone
		final int gemstoneItemId = gemstoneItem.getItem().getItemId();
		
		if (gemstoneItemId != 2130 && gemstoneItemId != 2131)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}
		
		// Check if the gemstoneCount is sufficant
		final int itemGrade = targetItem.getItem().getItemGrade();
		
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
				if (gemstoneCount != 20 || gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (gemstoneCount != 30 || gemstoneItemId != 2130)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				break;
			case L2Item.CRYSTAL_A:
				if (gemstoneCount != 20 || gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				break;
			case L2Item.CRYSTAL_S:
				if (gemstoneCount != 25 || gemstoneItemId != 2131)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT));
					return;
				}
				break;
		}
		
		activeChar.sendPacket(new ExConfirmVariationGemstone(gemstoneItemObjId, gemstoneCount));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:2B RequestConfirmGemStone";
	}
}
